/*
 * @(#)AdressImportJob.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.dateiimport.job.AbstractImportJob;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.LoeschenJob;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.*;
import de.werum.sis.idev.intern.actions.util.MelderDaten;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.secure.PasswordAlgorithm;
import de.werum.sis.idev.res.secure.UserKeyEncryptionAlgorithm;
import de.werum.sis.idev.res.util.PasswordGenerator;

import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class AdressImportJob.
 */
public class AdressImportJob extends AbstractImportJob
{

  /**
   * The Constant SQL_SELECT_BESTAND.
   */
  public static final String SQL_SELECT_BESTAND = "SELECT adressen.QUELL_REFERENZ_OF, IF(adressen.QUELL_REFERENZ_TYP=\"MANUELL\",1,0) AS MANUELLE_ADRESSE,"
    + "adressen.ADRESSEN_ID, firmen.FIRMEN_ID, firmen.ANSPRECHPARTNER_ID AS FIRMA_PARTNER_ID, melder.MELDER_ID, melder.ANSPRECHPARTNER_ID AS MELDER_PARTNER_ID, melder.KENNUNG,"
    + "IF(firmen_adressen.STATUS IS NULL,1,0) AS FIRMEN_ADRESSEN_STATUS FROM adressen"
    + " LEFT JOIN firmen_adressen ON(firmen_adressen.ADRESSEN_ID = adressen.ADRESSEN_ID AND firmen_adressen.firmen_id > 0 AND firmen_adressen.STATUS = \"AKTIV\")"
    + " LEFT JOIN firmen ON(firmen_adressen.FIRMEN_ID = firmen.FIRMEN_ID AND firmen_adressen.STATUS = \"AKTIV\" AND firmen.STATUS != \"LOESCH\")"
    + " LEFT JOIN melder ON(melder.ADRESSEN_ID = adressen.ADRESSEN_ID AND firmen_adressen.FIRMEN_ID = melder.FIRMEN_ID AND melder.STATUS != \"LOESCH\")"
    + " WHERE adressen.STATUS != \"LOESCH\" AND adressen.QUELL_REFERENZ_ID = {0} AND adressen.QUELL_REFERENZ_OF IN({1})";

  /**
   * The adress import beans.
   */
  protected HashMap<String, AdressImportBean> adressImportBeans;

  /**
   * The sorted adress import beans.
   */
  protected ArrayList<AdressImportBean> sortedAdressImportBeans;

  /**
   * The Neue kennungen.
   */
  protected ArrayList<String> neueKennungen;

  /**
   * The Anzahl bestandsadressen.
   */
  protected int anzahlBestandsadressen;

  /**
   * The Anzahl neuadressen.
   */
  protected int anzahlNeuadressen;

  /**
   * Instantiates a new adress import job.
   *
   * @param jobBean the job bean
   */
  public AdressImportJob(JobBean jobBean)
  {
    super("ImportAdressen", jobBean);
  }

  /**
   * Instantiates a new Adress import job.
   *
   * @param jobName the job name
   * @param jobBean the job bean
   */
  public AdressImportJob(String jobName, JobBean jobBean)
  {
    super(jobName, jobBean);
  }

  /**
   * Do normal import.
   *
   * @throws JobException the job exception
   */
  @Override
  protected void doNormalImport() throws JobException
  {
    Path path = this.jobBean.getImportdatei()
      .getPath();
    this.leseTeilbereich(path);
    if (this.sortedAdressImportBeans.isEmpty())
    {
      return;
    }
    this.ermittleIndizes();
    this.neueKennungen = this.ermittleKennungen();
    this.startTransaction();
  }

  /**
   * Next import job.
   *
   * @return the abstract job
   */
  @Override
  protected AbstractJob nextImportJob()
  {
    jobBean.setStatusAndInfo(JobStatus.AKTIV, "Import aktiv");
    return new AdressImportJob(this.jobBean);
  }

  /**
   * Do after last import.
   *
   * @return the abstract job
   */
  @Override
  protected AbstractJob doAfterLastImport()
  {
    if (this.jobBean.loescheDaten && !this.isCancelled())
    {
      return new LoeschenJob(this.jobBean);
    }
    boolean nurPruefen = this.jobBean.getSimulation().importSimulieren;
    if (!nurPruefen)
    {

      // Job beendet, Dateien loeschen
      AufraeumUtil util = new AufraeumUtil();
      util.entferneDateien(this.jobBean.jobId);
    }
    this.jobBean.setStatusAndInfo(JobStatus.BEENDET, "Adress-Import wurde durchgeführt");
    return null;
  }

  /**
   * Do in transaction.
   *
   * @throws SQLException the SQL exception
   * @throws JobException the job exception
   */
  @Override
  protected void doInTransaction() throws SQLException, JobException
  {
    this.erzeugeNeueAdressen();
    this.erzeugeNeueMelderAnsprechpartner();
    this.erzeugeNeueFirmenAnsprechpartner();
    this.erzeugeNeueFirmen();
    this.erzeugeNeueFirmenAdressen();
    this.erzeugeNeueMelder();
    this.aktualisiereBestehendeAdressen();
    this.aktualisiereBestehendeFirmen();
    this.aktualisiereBestehendeMelder();
    this.generiereNeuePasswoerter();
  }

  /**
   * Ermittle indizes.
   *
   * @throws JobException the job exception
   */
  private void ermittleIndizes() throws JobException
  {
    this.log.info("Ermittle Adress-Indizes...");
    this.beginStopWatch();
    this.anzahlBestandsadressen = 0;
    this.anzahlNeuadressen = 0;

    String ofs = sqlUtil.convertStringList(sortedAdressImportBeans.stream()
      .map(b -> b.getAdresse()
        .getQuellReferenzOf())
      .collect(Collectors.toSet()));
    String sql = MessageFormat.format(SQL_SELECT_BESTAND, "" + this.jobBean.quellReferenzId, ofs);
    List<ResultRow> rows = sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      AdressImportBean bean = this.adressImportBeans.get(row.getString("QUELL_REFERENZ_OF"));
      int adressenId = row.getInt("ADRESSEN_ID");
      int firmenId = row.getInt("FIRMEN_ID");
      int melderId = row.getInt("MELDER_ID");
      int firmaPartnerId = row.getInt("FIRMA_PARTNER_ID");
      int melderPartnerId = row.getInt("MELDER_PARTNER_ID");

      MelderBean mb = bean.getMelder();
      AdresseBean ab = bean.getAdresse();
      FirmenBean fb = bean.getFirma();
      // Adressdaten
      ab.setAdressenId(adressenId);
      ab.setManuelleAdresse(row.getBoolean("MANUELLE_ADRESSE"));
      ab.setNeu(adressenId == 0);
      // Melderdaten
      mb.setAdressenId(adressenId);
      mb.setFirmenId(firmenId);
      mb.setMelderId(melderId);
      mb.setNeu(melderId == 0);
      mb.getAnsprechpartner()
        .setAnsprechpartnerId(melderPartnerId);
      mb.getAnsprechpartner()
        .setNeu(melderPartnerId == 0);
      mb.setKennung(row.getString("KENNUNG"));
      // Firmendaten
      fb.setFirmenId(firmenId);
      fb.setNeu(firmenId == 0);
      fb.getAnsprechpartner()
        .setAnsprechpartnerId(firmaPartnerId);
      fb.getAnsprechpartner()
        .setNeu(firmaPartnerId == 0);
      bean.setFirmenAdressenNeu(row.getInt("FIRMEN_ADRESSEN_STATUS") == 1);
    }
    this.anzahlNeuadressen = this.sortedAdressImportBeans.size() - this.anzahlBestandsadressen;
    this.log.info(MessageFormat.format("Indizes Ermittlung ({0} neue und {1} bestehende Eintraege) beendet in {2}", this.anzahlNeuadressen, this.anzahlBestandsadressen, this.getElapsedTime()));

  }

  /**
   * Lese teilbereich.
   *
   * @param path the path
   * @throws JobException the job exception
   */
  protected void leseTeilbereich(Path path) throws JobException
  {
    SegmentedCsvFileReader reader = new SegmentedCsvFileReader();
    List<String[]> rows = reader.readSegment(path, this.jobBean.getImportdatei().getCharset(), this.jobBean.getImportdatei().datensatzOffset, this.jobBean.importBlockGroesse);
    this.adressImportBeans = new HashMap<>(rows.size());
    for (String[] cols : rows)
    {
      AdressImportBean bean;
      if (ImportFormat.IMPORTMITANSPRECHPARTNER.equals(this.jobBean.getImportdatei().importFormat))
      {
        bean = this.doParseZeileW3StatFormat(cols);
      } else
      {
        bean = this.doParseZeileIdevFormat(cols);
      }
      if (bean != null)
      {
        this.adressImportBeans.put(bean.getAdresse()
          .getQuellReferenzOf(), bean);
      }
    }
    this.sortedAdressImportBeans = new ArrayList<>(this.adressImportBeans.values());
    Collections.sort(this.sortedAdressImportBeans);
  }

  /**
   * Do parse zeile idev format.
   *
   * @param cols the cols
   * @return the adress import bean
   */
  private AdressImportBean doParseZeileIdevFormat(String[] cols)
  {
    // 18:
    // OF,ROLLE,ANREDE,NAME,NAME_ERGAENZUNG,KURTEXT,ABTEILUNG,STRASSE,HNR,PLZ,ORT,POSTFACH,POSTFACH_PLZ,POSTFACH_ORT,LAND,TELEFON,FAX,EMAIL
    // 10: Wahlweise ZUSATZ1 bis ZUSATZ10
    // Letzte Spalte nicht beruecksichtigen, da oft ein Semikolon als Abschluss dran haengt
    if (cols.length != 18 && cols.length != 19 && cols.length != 28 && cols.length != 29)
    {
      return null;
    }

    AdressImportBean bean = new AdressImportBean();
    // Adresse befuellen
    AdresseBean ab = bean.getAdresse();
    ab.setAmt(this.jobBean.amt);
    ab.setQuellReferenzOf(cols[0]);
    ab.setQuellReferenzId(this.jobBean.quellReferenzId);
    ab.setRolle(cols[1]);
    ab.setAnrede(cols[2]);
    ab.setName(cols[3]);
    ab.setNameErgaenzung(cols[4]);
    ab.setKurztext(cols[5]);
    ab.setAbteilung(cols[6]);
    ab.setStrasse(cols[7]);
    ab.setHausnummer(cols[8]);
    ab.setPostleitzahl(cols[9]);
    ab.setOrt(cols[10]);
    ab.setPostfach(cols[11]);
    ab.setPostfachPlz(cols[12]);
    ab.setPostfachOrt(cols[13]);
    ab.setLand(cols[14]);
    ab.setTelefon(cols[15]);
    ab.setFax(cols[16]);
    ab.setEmail(cols[17]);
    if (cols.length >= 28)
    {
      for (int x = 0; x < 10; x++)
      {
        ab.setZusatz(x, cols[18 + x]);
      }
    }
    ab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    ab.setMelderAenderbar(!this.jobBean.getAdressen().nichtAenderbar);
    // Ansprechpartner Firma befuellen
    AnsprechpartnerBean firmenAnsprechpartnerBean = bean.getFirma()
      .getAnsprechpartner();
    firmenAnsprechpartnerBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    if (!this.jobBean.getMelder().erzeugeLeereAnsprechpartner)
    {
      firmenAnsprechpartnerBean.setAnrede("");
      firmenAnsprechpartnerBean.setName(cols[3]);
      firmenAnsprechpartnerBean.setVorname(cols[4]);
    }
    // Firma befuellen
    FirmenBean firmenBean = bean.getFirma();
    firmenBean.setName(cols[3]);
    firmenBean.setNameErgaenzung(cols[4]);
    firmenBean.setKurztext(cols[5]);
    firmenBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    MelderBean melderBean = bean.getMelder();
    // Ansprechpartner Melder befuellen
    AnsprechpartnerBean melderAnsprechpartnerBean = melderBean.getAnsprechpartner();
    melderAnsprechpartnerBean.setAnrede(cols[2]);
    melderAnsprechpartnerBean.setName(cols[3]);
    melderAnsprechpartnerBean.setVorname(cols[4]);
    melderAnsprechpartnerBean.setAbteilung(cols[6]);
    melderAnsprechpartnerBean.setTelefon(cols[15]);
    melderAnsprechpartnerBean.setFax(cols[16]);
    melderAnsprechpartnerBean.setEmail(cols[17]);
    melderAnsprechpartnerBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    // Melder befuellen
    melderBean.setPasswortAenderbar(!this.jobBean.getMelder().passwortUnveraenderbar);
    melderBean.setZusammenfuehrbar(this.jobBean.getMelder().zusammenfuehrbar);
    melderBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);

    return bean;
  }

  /**
   * Do parse zeile W 3 stat format.
   *
   * @param cols the cols
   * @return the adress import bean
   */
  private AdressImportBean doParseZeileW3StatFormat(String[] cols)
  {
    // 16:
    // OF,ANREDE,NAME,NAME_ERGAENZUNG,STRASSE,HNR,PLZ,ORT,POSTFACH,POSTFACH_PLZ,POSTFACH_ORT,AN_NAME,AN_VORNAME,AN_TELEFON,AN_FAX,AN_EMAIL
    // 10: Wahlweise ZUSATZ1 bis ZUSATZ10
    // Letzte Spalte nicht beruecksichtigen, da oft ein Semikolon als Abschluss dran haengt
    if (cols.length != 16 && cols.length != 17)
    {
      return null;
    }
    AdressImportBean bean = new AdressImportBean();
    // Adresse befuellen
    AdresseBean ab = bean.getAdresse();
    ab.setAmt(this.jobBean.amt);
    ab.setQuellReferenzOf(cols[0]);
    ab.setQuellReferenzId(this.jobBean.quellReferenzId);
    ab.setAnrede(cols[1]);
    ab.setName(cols[2]);
    ab.setNameErgaenzung(cols[3]);
    ab.setStrasse(cols[4]);
    ab.setHausnummer(cols[5]);
    ab.setPostleitzahl(cols[6]);
    ab.setOrt(cols[7]);
    ab.setPostfach(cols[8]);
    ab.setPostfachPlz(cols[9]);
    ab.setPostfachOrt(cols[10]);
    ab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    ab.setMelderAenderbar(!this.jobBean.getAdressen().nichtAenderbar);
    // Ansprechpartner Firma befuellen
    AnsprechpartnerBean firmenAnsprechpartnerBean = bean.getFirma()
      .getAnsprechpartner();
    firmenAnsprechpartnerBean.setName(cols[11]);
    firmenAnsprechpartnerBean.setVorname(cols[12]);
    firmenAnsprechpartnerBean.setTelefon(cols[13]);
    firmenAnsprechpartnerBean.setFax(cols[14]);
    firmenAnsprechpartnerBean.setEmail(cols[15]);
    firmenAnsprechpartnerBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    // Firma befuellen
    FirmenBean firmenBean = bean.getFirma();
    firmenBean.setName(cols[2]);
    firmenBean.setNameErgaenzung(cols[3]);
    firmenBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    MelderBean melderBean = bean.getMelder();
    // Ansprechpartner Melder befuellen
    AnsprechpartnerBean melderAnsprechpartnerBean = melderBean.getAnsprechpartner();
    melderAnsprechpartnerBean.setName(cols[11]);
    melderAnsprechpartnerBean.setVorname(cols[12]);
    melderAnsprechpartnerBean.setTelefon(cols[13]);
    melderAnsprechpartnerBean.setFax(cols[14]);
    melderAnsprechpartnerBean.setEmail(cols[15]);
    melderAnsprechpartnerBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    // Melder befuellen
    melderBean.setPasswortAenderbar(!this.jobBean.getMelder().passwortUnveraenderbar);
    melderBean.setZusammenfuehrbar(this.jobBean.getMelder().zusammenfuehrbar);
    melderBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    return bean;
  }

  /**
   * Erzeuge neue adressen.
   *
   * @throws JobException the SQL exception
   */
  protected void erzeugeNeueAdressen() throws JobException
  {
    if (this.anzahlNeuadressen == 0)
    {
      this.log.info("Keine neuen Adresseintraege vorhanden!");
      return;
    }
    this.log.info("Erzeuge Neue Adresse...");
    this.beginStopWatch();
    int anzahl = 0;
    try (PreparedInsert pi = sqlUtil.createPreparedInsert(AdresseBean.SQL_INSERT_ADRESSEN))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        AdresseBean ab = bean.getAdresse();
        if (ab.isNotManuelleAdresse() && ab.isNeu())
        {
          anzahl++;
          ab.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          ab.insert(pi);
          bean.getMelder()
            .setAdressenId(ab.getAdressenId());
          this.jobBean.getAdressen()
            .getIdentifikatoren()
            .getNeu()
            .getValues()
            .add(ab.getAdressenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Adressen erzeugt in {1} ", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue firmen.
   *
   * @throws JobException the SQL exception
   */
  protected void erzeugeNeueFirmen() throws JobException
  {
    if (!this.jobBean.getFirmen().neuanlageErlaubt)
    {
      this.log.info("Überspringe Firmenerzeugung, da nicht aktiviert");
      return;
    }
    this.log.info("Erzeuge Neue Firmen...");
    this.beginStopWatch();
    int anzahl = 0;
    try (PreparedInsert pi = sqlUtil.createPreparedInsert(FirmenBean.SQL_INSERT_FIRMEN))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        FirmenBean fb = bean.getFirma();
        if (bean.getAdresse()
          .isNotManuelleAdresse() && fb.isNeu())
        {
          anzahl++;
          fb.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          fb.insert(pi);
          bean.getMelder()
            .setFirmenId(fb.getFirmenId());
          this.jobBean.getFirmen()
            .getIdentifikatoren()
            .getNeu()
            .getValues()
            .add(fb.getFirmenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue melder ansprechpartner.
   *
   * @throws JobException the SQL exception
   */
  protected void erzeugeNeueMelderAnsprechpartner() throws JobException
  {
    if (!this.jobBean.getMelder().neuanlageErlaubt)
    {
      this.log.info("Überspringe Melder-Ansprechpartner-Erzeugung, da nicht aktiviert");
      return;
    }
    this.beginStopWatch();
    this.log.info("Erzeuge Neue Melder-Ansprechpartner...");
    int anzahl = 0;
    try (PreparedInsert pi = sqlUtil.createPreparedInsert(AnsprechpartnerBean.SQL_INSERT_ANSPRECHPARTNER))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        AnsprechpartnerBean asp = bean.getMelder()
          .getAnsprechpartner();
        if (bean.getAdresse()
          .isNotManuelleAdresse() && asp.isNeu())
        {
          anzahl++;
          asp.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          asp.insert(pi);
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder-Ansprechpartner erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue firmen ansprechpartner.
   *
   * @throws JobException the SQL exception
   */
  protected void erzeugeNeueFirmenAnsprechpartner() throws JobException
  {
    if (!this.jobBean.getFirmen().neuanlageErlaubt)
    {
      this.log.info("Überspringe Firmen-Ansprechpartner-Erzeugung, da nicht aktiviert");
      return;
    }
    this.beginStopWatch();
    this.log.info("Erzeuge Neue Firmen-Ansprechpartner...");
    int anzahl = 0;
    try (PreparedInsert ps = sqlUtil.createPreparedInsert(AnsprechpartnerBean.SQL_INSERT_ANSPRECHPARTNER))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        AnsprechpartnerBean asp = bean.getFirma()
          .getAnsprechpartner();
        if (bean.getAdresse()
          .isNotManuelleAdresse() && asp.isNeu())
        {
          anzahl++;
          asp.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          asp.insert(ps);
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen-Ansprechpartner erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue firmen adressen.
   *
   * @throws JobException the SQL exception
   */
  protected void erzeugeNeueFirmenAdressen() throws JobException
  {
    if (!this.jobBean.getFirmen().neuanlageErlaubt)
    {
      this.log.info("Überspringe Firmen-Adressen-Erzeugung, da nicht aktiviert");
      return;
    }
    this.log.info("Erzeuge Neue Firmen-Adressen...");
    int anzahl = 0;
    this.beginStopWatch();
    try (PreparedUpdate ps = sqlUtil.createPreparedUpdate(FirmenAdressenBean.SQL_INSERT_FIRMEN_ADRESSEN))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        if (bean.getAdresse()
          .isNotManuelleAdresse() && bean.isFirmenAdressenNeu())
        {

          FirmenAdressenBean fab = new FirmenAdressenBean();
          fab.setAdressenId(bean.getAdresse()
            .getAdressenId());
          fab.setFirmenId(bean.getFirma()
            .getFirmenId());
          fab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
          fab.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          if (fab.getAdressenId() != null && fab.getFirmenId() != null && fab.getAdressenId() > 0 && fab.getFirmenId() > 0)
          {
            anzahl++;
            fab.insert(ps);
            bean.setFirmenAdressenNeu(false);
          }
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen-Adressen erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Ermittle kennungen array list.
   *
   * @return the array list
   * @throws JobException the job exception
   */
  protected ArrayList<String> ermittleKennungen() throws JobException
  {
    ArrayList<String> result = new ArrayList<>();
    if (this.jobBean.getMelder().neuanlageErlaubt)
    {
      this.beginStopWatch();
      // Ermittle zuerst die Anzahl der benoetigten Kennungen!
      int anzahl = 0;
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        if (bean.getAdresse()
          .isNotManuelleAdresse() && bean.getMelder()
          .isNeu())
        {
          anzahl++;
        }
      }
      if (anzahl > 0)
      {
        this.log.info(MessageFormat.format("Erzeuge {0} eindeutige Melderkennungen...", anzahl));
        KennungTool kennungTool = new KennungTool(sqlUtil);
        result = kennungTool.erzeugeEindeutigeKennungen(anzahl, this.jobBean.sachbearbeiterLand);
        this.log.info(MessageFormat.format("{0} Kennungen erzeugt in {1}", anzahl, this.getElapsedTime()));
      }
    }
    return result;
  }

  /**
   * Erzeuge neue melder.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueMelder() throws JobException
  {
    if (!this.jobBean.getMelder().neuanlageErlaubt)
    {
      this.log.info("Überspringe Meldererzeugung, da nicht aktiviert");
      return;
    }
    this.beginStopWatch();
    this.log.info("Erzeuge Neue Melder...");
    int anzahl = 0;
    // Ermittle zuerst die Anzahl der benoetigten Kennungen!
    ArrayList<AdressImportBean> neueMelder = new ArrayList<>();
    for (AdressImportBean bean : this.sortedAdressImportBeans)
    {
      if (bean.getAdresse()
        .isNotManuelleAdresse() && bean.getMelder()
        .isNeu())
      {
        neueMelder.add(bean);
      }
    }
    if (!neueMelder.isEmpty())
    {
      if (this.neueKennungen == null || this.neueKennungen.size() < neueMelder.size())
      {
        throw new JobException("Es sind nicht genügend freie Kennungen vorhanden!");
      }
      try (PreparedInsert ps = sqlUtil.createPreparedInsert(MelderBean.SQL_INSERT_MELDER))
      {
        for (AdressImportBean bean : neueMelder)
        {
          MelderBean mb = bean.getMelder();
          mb.setKennung(this.neueKennungen.get(anzahl));
          anzahl++;
          MelderDaten melderDaten = MelderDatenService.getInstance()
            .getMelderDaten();
          mb.setMelderDaten(melderDaten);
          mb.setZeitpunktRegistrierung(this.jobBean.zeitpunktEintrag);
          mb.insert(ps);
          this.jobBean.getMelder()
            .getIdentifikatoren()
            .getNeu()
            .getValues()
            .add(mb.getMelderId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende adressen.
   *
   * @throws JobException the SQL exception
   */
  protected void aktualisiereBestehendeAdressen() throws JobException
  {
    this.beginStopWatch();
    this.log.info("Aktualisierung Adressen...");
    int anzahl = 0;
    try (PreparedUpdate pu = sqlUtil.createPreparedUpdate(AdresseBean.SQL_UPDATE_ADRESSEN))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        AdresseBean ab = bean.getAdresse();
        if (ab.isNotManuelleAdresse() && !ab.isNeu())
        {
          anzahl++;
          ab.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          ab.update(pu);
          this.jobBean.getAdressen()
            .getIdentifikatoren()
            .getAenderung()
            .getValues()
            .add(ab.getAdressenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Adressen aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende firmen.
   *
   * @throws JobException the SQL exception
   */
  protected void aktualisiereBestehendeFirmen() throws JobException
  {
    if (!this.jobBean.getFirmen().aktualisierungErlaubt)
    {
      this.log.info("Überspringe Firmen-Aktualisierung, da nicht aktiviert");
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung Firmen...");
    int anzahl = 0;
    try (PreparedUpdate pu = sqlUtil.createPreparedUpdate(FirmenBean.SQL_UPDATE_FIRMEN))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        FirmenBean fb = bean.getFirma();
        if (bean.getAdresse()
          .isNotManuelleAdresse() && !fb.isNeu())
        {
          anzahl++;
          fb.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          fb.update(pu);
          this.jobBean.getFirmen()
            .getIdentifikatoren()
            .getAenderung()
            .getValues()
            .add(fb.getFirmenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende melder.
   *
   * @throws JobException the job exception
   */
  protected void aktualisiereBestehendeMelder() throws JobException
  {
    if (!this.jobBean.getMelder().aktualisierungErlaubt)
    {
      this.log.info("Überspringe Melder-Aktualisierung, da nicht aktiviert");
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung Melder...");
    int anzahl = 0;
    String sql = MelderBean.SQL_UPDATE_MELDER;
    try (PreparedUpdate pu = sqlUtil.createPreparedUpdate(sql))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        MelderBean mb = bean.getMelder();
        if (bean.getAdresse()
          .isNotManuelleAdresse() && !mb.isNeu())
        {
          anzahl++;
          mb.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          mb.update(pu);
          this.jobBean.getMelder()
            .getIdentifikatoren()
            .getAenderung()
            .getValues()
            .add(mb.getMelderId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Generiere neue passwoerter.
   *
   * @throws SQLException the sql exception
   */
  protected void generiereNeuePasswoerter() throws SQLException
  {
    if (!this.jobBean.getMelder().neuePasswoerterGenerieren)
    {
      this.log.debug("Überspringe Passwort-Aktualisierung, da nicht aktiviert");
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung Melder-Passwoerter...");
    int anzahl = 0;
    Set<Integer> mids = this.sortedAdressImportBeans.stream().filter(b -> b.getMelder() != null).filter(b -> b.getMelder().isNeu() && b.getMelder().getMelderId() > 0).map(b -> b.getMelder().getMelderId()).collect(Collectors.toSet());
    if (!mids.isEmpty())
    {
      String ids = sqlUtil.convertNumberList(mids);
      String sql = MessageFormat.format(MelderBean.SQL_SELECT_FUER_PASSWOERTER, ids);
      try (ResultSet rs = this.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)
        .executeQuery(sql))
      {
        while (rs.next())
        {
          final byte[] privaterSchluessel = rs.getBytes("PRIVATER_SCHLUESSEL");
          final byte[] oeffentlicherSchluessel = rs.getBytes("OEFFENTLICHER_SCHLUESSEL");
          String systemPasswort = PasswordGenerator.generatePassword();
          MelderDaten melderDaten = datenVerschluesseln(systemPasswort, privaterSchluessel, oeffentlicherSchluessel);
          if (melderDaten != null)
          {
            rs.updateString("SYSTEM_PASSWORT", melderDaten.getSystemPasswort());
            rs.updateString("PASSWORT", melderDaten.getPasswort());
            rs.updateBytes("PRIVATER_SCHLUESSEL_GESCHUETZT", melderDaten.getPrivaterSchluesselGeschuetzt());
            rs.updateBytes("OEFFENTLICHER_SCHLUESSEL_GESCHUETZT", melderDaten.getOeffentlicherSchluesselGeschuetzt());
            rs.updateString("PASSWORT_AENDERUNG", "J");
            rs.updateString("ZEITPUNKT_AENDERUNG", this.jobBean.zeitpunktEintrag);
            rs.updateString("STATUS", "AEND");
            rs.updateRow();
            anzahl++;
          } else
          {
            this.log.error("Konnte keine MelderDaten erzeugen");
          }
        }
      }
    }

    this.log.info(MessageFormat.format("{0} Melder-Passwoerter aktualisiert in {1}", anzahl, this.getElapsedTime()));

  }

  /**
   * Daten verschluesseln.
   *
   * @param systemPasswort          the system passwort
   * @param privaterSchluessel      the privater schluessel
   * @param oeffentlicherSchluessel the oeffentlicher schluessel
   * @return the melder daten
   */
  private MelderDaten datenVerschluesseln(String systemPasswort, byte[] privaterSchluessel, byte[] oeffentlicherSchluessel)
  {
    try
    {
      String passwort = PasswordAlgorithm.createExternalPassword(systemPasswort);
      byte[] privaterSchluesselGeschuetzt = UserKeyEncryptionAlgorithm.encryptUserKey(privaterSchluessel, systemPasswort);
      byte[] oeffentlicherSchluesselGeschuetzt = UserKeyEncryptionAlgorithm.encryptUserKey(oeffentlicherSchluessel, systemPasswort);
      return new MelderDaten(systemPasswort, passwort, privaterSchluessel, oeffentlicherSchluessel, privaterSchluesselGeschuetzt, oeffentlicherSchluesselGeschuetzt);
    } catch (GeneralSecurityException e)
    {
      this.log.error("datenVerschluesseln schlug fehl:" + e.getMessage());
    }
    return null;
  }
}
