/*
 * @(#)VorbelegungsImportJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.vorbelegungsimport;

import de.destatis.regdb.DesEncrypter;
import de.destatis.regdb.Email;
import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.AbstractImportJob;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.*;
import de.destatis.regdb.servlets.RegDBGeneralHttpServlet;
import de.werum.sis.idev.intern.actions.util.DownloadDienst;
import de.werum.sis.idev.intern.actions.util.DownloadStatus;
import de.werum.sis.idev.res.job.JobException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Vorbelegungs import job.
 */
public class VorbelegungsImportJob extends AbstractImportJob
{

  /**
   * The constant SQL_SELECT_VORBELEGUNG_BESTAND.
   */
  public static final String SQL_SELECT_VORBELEGUNG_BESTAND = "SELECT VORBELEGUNG_ID, VB_WERTE_INDX FROM vorbelegung_verwaltung WHERE AMT=? AND STATISTIK_ID=? AND BZR=? AND QUELL_REFERENZ_OF=? AND QUELL_REFERENZ_INT=? AND MELDER_ID=? AND FIRMEN_ID=? AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_FIRMA = "SELECT adressen.QUELL_REFERENZ_OF, firmen_adressen.FIRMEN_ID FROM adressen INNER JOIN firmen_adressen ON(firmen_adressen.ADRESSEN_ID = adressen.ADRESSEN_ID) WHERE adressen.QUELL_REFERENZ_OF IN({0}) AND adressen.QUELL_REFERENZ_ID = {1} AND adressen.STATUS != \"LOESCH\" AND firmen_adressen.STATUS != \"LOESCH\"";
  private static final String SQL_INSERT_VERWALTUNG = "INSERT INTO vorbelegung_verwaltung (AMT, FIRMEN_ID, MELDER_ID, STATISTIK_ID, QUELL_REFERENZ_OF, QUELL_REFERENZ_INT, BZR, FORMULARNAME, VB_WERTE_INDX, STATUS, ZEITPUNKT_EINTRAG) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
  private static final String SQL_INSERT_WERT = "REPLACE INTO vorbelegung_wert (VB_WERTE_INDX, FELD_NAME, FELD_INHALT) VALUES ";
  private static final String SQL_SELECT_MAX_VB_WERTE = "SELECT MAX(possible) AS VB_WERTE_INDX FROM ( SELECT IFNULL(MAX(VB_WERTE_INDX),0) AS possible FROM vorbelegung_verwaltung UNION ALL SELECT IFNULL(MAX(VB_WERTE_INDX),0) as possible FROM vorbelegung_wert ) AS SUBQUERY";
  private static final String SQL_UPDATE_VERWALTUNG = "UPDATE vorbelegung_verwaltung SET FORMULARNAME=?, STATUS=\"AEND\", ZEITPUNKT_AENDERUNG=? WHERE VORBELEGUNG_ID = ?";
  private static final String SQL_UPDATE_INFOSCHREIBEN = "UPDATE melder_statistiken SET IS_GRUND=\"VORBELEGUNG\", IS_ZUSATZ_INFO=?, ZEITPUNKT_AENDERUNG=? WHERE AMT=? AND STATISTIK_ID=? AND FIRMEN_ID=? AND MELDER_ID=? AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_LOESCHKANDIDATEN = "SELECT VORBELEGUNG_ID FROM vorbelegung_verwaltung  WHERE AMT=\"{0}\" AND STATISTIK_ID={1} AND BZR=\"{2}\" AND STATUS != \"LOESCH\" AND NOT ((STATUS = \"NEU\" AND ZEITPUNKT_EINTRAG = \"{3}\") OR (STATUS=\"AEND\" AND ZEITPUNKT_AENDERUNG = \"{3}\"))";
  private static final String SQL_UPDATE_LOESCHKANDIDATEN = "UPDATE vorbelegung_verwaltung SET STATUS=\"LOESCH\", ZEITPUNKT_AENDERUNG=\"{3}\" WHERE AMT=\"{0}\" AND STATISTIK_ID={1} AND BZR=\"{2}\" AND STATUS != \"LOESCH\" AND NOT ((STATUS = \"NEU\" AND ZEITPUNKT_EINTRAG = \"{3}\") OR (STATUS=\"AEND\" AND ZEITPUNKT_AENDERUNG = \"{3}\"))";
  private static final String SQL_DELETE_WERTE = "DELETE FROM vorbelegung_wert WHERE VB_WERTE_INDX IN(SELECT VB_WERTE_INDX from vorbelegung_verwaltung WHERE AMT=? AND STATISTIK_ID=? AND BZR=? AND STATUS != \"LOESCH\")";
  private static final String SQL_DELETE_WERTE_MIT_VBIDS = "DELETE FROM vorbelegung_wert WHERE VB_WERTE_INDX IN(SELECT VB_WERTE_INDX from vorbelegung_verwaltung WHERE VORBELEGUNG_ID IN({0}))";
  private static final String SQL_DELETE_VERWALTUNG_MIT_VBIDS = "UPDATE vorbelegung_verwaltung SET STATUS=\"LOESCH\", ZEITPUNKT_AENDERUNG=\"{1}\" WHERE VORBELEGUNG_ID IN({0})";
  private static final String SQL_SELECT_TRANSFERZIEL = "SELECT transferziel.TRANSFER_MAIL_ABSENDER, transferziel.TRANSFER_MAIL_BETREFF, transferziel.TRANSFER_MAIL_TEXT FROM transferziel INNER JOIN transfer USING(TRANSFERZIEL_ID) WHERE transfer.AMT=\"{0}\" AND transfer.STATISTIK_ID={1} AND transfer.AKTION=\"MAIL_DOWNLOAD\"";
  private static final String SQL_SELECT_MAIL_ADRESSEN = "SELECT DISTINCT(EMAIL) FROM ansprechpartner INNER JOIN firmen USING(ANSPRECHPARTNER_ID) WHERE LENGTH(EMAIL) > 0 AND firmen.status != \"LOESCH\" AND firmen.FIRMEN_ID IN({0})";
  private List<VorbelegungsImportBean> vorbelegungsImportBeans;

  /**
   * Instantiates a new Vorbelegungs import job.
   *
   * @param jobBean the job bean
   */
  public VorbelegungsImportJob(JobBean jobBean)
  {
    super("ImportVorbelegungen", jobBean);
  }

  /**
   * Instantiates a new Vorbelegungs import job.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public VorbelegungsImportJob(JobBean jobBean, SqlUtil sqlUtil)
  {
    super("ImportVorbelegungen", jobBean, sqlUtil);
  }

  @Override
  protected void doBeforeFirstImport() throws JobException
  {
    super.doBeforeFirstImport();
    if (this.jobBean.loescheDaten)
    {
      loescheVorbelegungsWerte();
    }
  }

  @Override
  protected void doNormalImport() throws JobException
  {
    Path path = this.jobBean.getImportdatei().getPath();
    this.leseTeilbereich(path);
    if (this.vorbelegungsImportBeans.isEmpty())
    {
      return;
    }
    this.verdichteDaten();
    this.ermittleIndizes();
    this.startTransaction();
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Vorbelegungen importiert");
  }

  private void transferiereDateien() throws JobException
  {
    File uploadFile = Paths.get(this.jobBean.getImportdatei().importVerzeichnis, "dateiupload.zip").toFile();
    this.log.debug("Transferiere Datei " + uploadFile);
    if (uploadFile.exists())
    {
      String kennung = this.jobBean.sachbearbeiterKennung;
      DesEncrypter encrypter = new DesEncrypter("MStatRegDB key");
      String passwort = encrypter.decrypt(this.jobBean.sachbearbeiterPasswort);
      DownloadDienst downloadDienst = new DownloadDienst(RegDBGeneralHttpServlet.interneAblaeufeHost, String.valueOf(RegDBGeneralHttpServlet.interneAblaeufePort), kennung, passwort);
      DownloadStatus ergebnis = downloadDienst.exportiereDownloadDatei(String.valueOf(this.jobBean.statistikId), this.jobBean.amt, this.jobBean.berichtszeitraum, uploadFile);
      if (ergebnis.getStatus() != DownloadStatus.STATUS_OK)
      {
        this.log.error(ergebnis.getMeldung());
        throw new JobException("Die Vorbelegungsdateien konnten nicht verarbeitet werden:" + ergebnis.getMeldung());
      }
      this.log.debug("Transfer erfolgreich!");
    }
  }

  @Override
  protected AbstractJob nextImportJob()
  {
    return new VorbelegungsImportJob(this.jobBean);
  }

  @Override
  protected AbstractJob doAfterLastImport() throws JobException
  {
    if (this.jobBean.loescheDaten)
    {
      this.loescheVerwaltungseintraege();
    }
    if (ImportFormat.VORBELEGUNGDOWNLOADIMPORT.equals(this.jobBean.getImportdatei().importFormat))
    {
      transferiereDateien();
      if (this.jobBean.versendeMails)
      {
        versendeMails();
      }
    }
    this.jobBean.setStatusAndInfo(JobStatus.BEENDET, "Import wurde durchgeführt");

    // Job beendet, Dateien loeschen
    AufraeumUtil util = new AufraeumUtil();
    util.entferneDateien(this.jobBean.jobId);
    return null;
  }

  @Override
  protected void doInTransaction() throws JobException
  {
    this.erzeugeNeueVerwaltungsEintraege();
    this.aktualisiereVerwaltungEintraege();
    this.erzeugeOderAktualisiereWertEintraege();
    this.aktualisiereInfoschreiben();
  }

  private void loescheVerwaltungseintraege() throws JobException
  {
    this.log.info("Lösche VorbelegungVerwaltung Eintraege...");
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_SELECT_LOESCHKANDIDATEN, this.jobBean.amt, String.valueOf(this.jobBean.statistikId), this.jobBean.berichtszeitraum, this.jobBean.zeitpunktEintrag);
    int anzahl = 0;
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      int vorbelId = row.getInt("VORBELEGUNG_ID");
      this.jobBean.getVorbelegungen().getIdentifikatoren().getLoeschung().getValues().add(vorbelId);
      anzahl++;
    }
    // Daten ermittelt, nun endgültig löschen
    sql = MessageFormat.format(SQL_UPDATE_LOESCHKANDIDATEN, this.jobBean.amt, String.valueOf(this.jobBean.statistikId), this.jobBean.berichtszeitraum, this.jobBean.zeitpunktEintrag);
    this.sqlUtil.update(sql);
    this.log.info(MessageFormat.format("{0} VorbelegungVerwaltung geloescht in {1} ", anzahl, this.getElapsedTime()));
  }

  /**
   * Verdichte daten.
   */
  public void verdichteDaten()
  {
    // Verdichte nach QUELL_REFERENZ_OF, QUELL_REFERENZ_INT und MELDER_ID
    Map<String, VorbelegungsImportBean> map = new HashMap<>();
    for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
    {
      VorbelegungsImportBean test = map.get(bean.getIdentifier());
      if (test == null)
      {
        map.put(bean.getIdentifier(), bean);
      }
      else
      {
        test.getWerte().putAll(bean.getWerte());
      }
    }
    this.vorbelegungsImportBeans = new ArrayList<>(map.values());
    Collections.sort(this.vorbelegungsImportBeans);
  }

  private void aktualisiereInfoschreiben() throws JobException
  {
    if (!this.jobBean.getVorbelegungen().eintragInfoschreiben)
    {
      return;
    }
    this.log.info("Aktualisiere Eintragungen fuer Infoschreiben...");
    this.beginStopWatch();
    int anzahl = 0;
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(SQL_UPDATE_INFOSCHREIBEN))
    {
      for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
      {
        this.log.debug(MessageFormat.format("update Infoschreiben, bzr={0},amt={1},statistikid={2},firmenid={3},melderid={4}", bean.getBzr(), bean.getAmt(), bean.getStatistikId(), bean.getFirmenId(), bean.getMelderId()));
        ps.addValue(bean.getBzr());
        ps.addValue(this.jobBean.zeitpunktEintrag);
        ps.addValue(bean.getAmt());
        ps.addValue(bean.getStatistikId());
        ps.addValue(bean.getFirmenId());
        ps.addValue(bean.getMelderId());
        anzahl += ps.update();
      }
    }
    this.log.info(MessageFormat.format("{0} Infoschreiben Eintragungen erzeugt in {1} ", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge oder aktualisiere wert eintraege.
   *
   * @throws JobException the job exception
   */
  public void erzeugeOderAktualisiereWertEintraege() throws JobException
  {
    this.log.info("Erzeuge neue Vorbelegungswerte...");
    this.beginStopWatch();
    int anzahl = 0;
    StringBuilder buf = new StringBuilder(1000);
    // VB_WERTE_INDX, FELD_NAME, FELD_INHALT
    for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
    {
      // Sicherheitshalber VB_WERTE_INDX prüfen!
      if (bean.getVbWerteIndex() > 0)
      {
        buf.setLength(0);
        for (Map.Entry<String, String> entry : bean.getWerte().entrySet())
        {
          if (buf.length() > 0)
          {
            buf.append(',');
          }
          String feldName = entry.getKey();
          if (feldName.endsWith("_datei"))
          {
            this.jobBean.getVorbelegungen().getFirmenIdsFuerMailVersand().add(bean.getFirmenId());
          }
          String value = "(" + bean.getVbWerteIndex() + ",\"" + StringUtil.escapeSqlString(feldName) + "\",\"" + StringUtil.escapeSqlString(entry.getValue()) + "\")";
          buf.append(value);
          anzahl++;
        }
        if (buf.length() > 0)
        {
          String sql = SQL_INSERT_WERT + buf.toString();
          this.sqlUtil.execute(sql);
        }
      }
      else
      {
        this.log.info("VB_WERTE_INDX ist 0");
      }
    }
    this.log.info(MessageFormat.format("{0} Vorbelegungswerte erzeugt in {1} ", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere verwaltung eintraege.
   *
   * @throws JobException the job exception
   */
  public void aktualisiereVerwaltungEintraege() throws JobException
  {
    this.log.info("Aktualisiere Verwaltungs-Eintraege...");
    this.beginStopWatch();
    int anzahl = 0;
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(SQL_UPDATE_VERWALTUNG))
    {
      // FORMULARNAME=?, ZEITPUNKT_AENDERUNG=? WHERE VORBELEGUNG_ID = ?
      for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
      {
        if (!bean.isNeueVorbelegung())
        {
          anzahl++;
          ps.addValue(bean.getFormularname());
          ps.addValue(this.jobBean.zeitpunktEintrag);
          ps.addValue(bean.getVorbelegungId());
          ps.update();
          this.jobBean.getVorbelegungen().getIdentifikatoren().getAenderung().getValues().add(bean.getVorbelegungId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Verwaltungs-Eintraege aktualisiert in {1} ", anzahl, this.getElapsedTime()));
  }

  private int ermittleHoechstenVbwerteIndx() throws JobException
  {
    this.log.info("Ermittle hoechsten VB_WERTE_INDX...");
    this.beginStopWatch();
    ResultRow row = this.sqlUtil.fetchOne(SQL_SELECT_MAX_VB_WERTE);
    if (row != null)
    {
      this.log.info("VB_WERTE_INDX betraegt " + row.getInt(1) + ", " + MSG_AUSGEFUEHRT_IN + this.getElapsedTime());
      this.jobBean.getVorbelegungen().vbWerteIndex = row.getInt(1);
      return this.jobBean.getVorbelegungen().vbWerteIndex;
    }

    this.log.error("VB Werte Index konnte nicht ermittelt werden!");
    throw new JobException("VB Werte Index konnte nicht ermittelt werden!");

  }

  /**
   * Erzeuge neue verwaltungs eintraege.
   *
   * @throws JobException the job exception
   */
  public void erzeugeNeueVerwaltungsEintraege() throws JobException
  {
    this.log.info("Erzeuge neue VorbelegungVerwaltung Eintraege...");
    this.beginStopWatch();
    int anzahl = 0;
    int indx = this.ermittleHoechstenVbwerteIndx();
    // Ermittle den hoechsten VB_WERTE_INDX der Tabellen

    try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(SQL_INSERT_VERWALTUNG))
    {
      for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
      {
        if (bean.isNeueVorbelegung())
        {
          anzahl++;
          ps.addValue(bean.getAmt());
          ps.addValue((bean.getFirmenId() == null ? 0 : bean.getFirmenId()));
          ps.addValue(bean.getMelderId());
          ps.addValue(bean.getStatistikId());
          ps.addValue(bean.getQuellReferenzOf());
          ps.addValue(bean.getQuellReferenzInt());
          ps.addValue(bean.getBzr());
          ps.addValue(bean.getFormularname());
          bean.setVbWerteIndex(++indx);
          ps.addValue(bean.getVbWerteIndex());
          ps.addValue("NEU");
          ps.addValue(this.jobBean.zeitpunktEintrag);
          ps.insert();
          ResultRow keys = ps.getGeneratedKeys();
          if (keys != null)
          {
            bean.setVorbelegungId(keys.getInt(1));
            this.jobBean.getVorbelegungen().getIdentifikatoren().getNeu().getValues().add(bean.getVorbelegungId());
          }
        }

      }
    }
    this.log.info(MessageFormat.format("{0} VorbelegungVerwaltung erzeugt in {1} ", anzahl, this.getElapsedTime()));

  }

  /**
   * Ermittle indizes.
   *
   * @throws JobException the job exception
   */
  public void ermittleIndizes() throws JobException
  {
    this.log.info("Ermittle Vorbelegungs-Indizes...");
    this.beginStopWatch();
    int anzahlNeueintraege = 0;
    int anzahlBestehendeEintraege = 0;
    ermittleFirmenIds();
    try (PreparedSelect psBestand = this.sqlUtil.createPreparedSelect(SQL_SELECT_VORBELEGUNG_BESTAND))
    {
      for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
      {
        psBestand.addValue(bean.getAmt());
        psBestand.addValue(bean.getStatistikId());
        psBestand.addValue(bean.getBzr());
        psBestand.addValue(bean.getQuellReferenzOf());
        psBestand.addValue(bean.getQuellReferenzInt());
        psBestand.addValue(bean.getMelderId());
        psBestand.addValue(bean.getFirmenId());
        ResultRow row = psBestand.fetchOne();
        if (row != null)
        {
          bean.setVorbelegungId(row.getInt("VORBELEGUNG_ID"));
          bean.setVbWerteIndex(row.getInt("VB_WERTE_INDX"));
          bean.setNeueVorbelegung(false);
          anzahlBestehendeEintraege++;
        }
        else
        {
          anzahlNeueintraege++;
        }
      }

    }
    this.log.info(MessageFormat.format("Indizes Ermittlung ({0} neue und {1} bestehende Eintraege) beendet in {2}", anzahlNeueintraege, anzahlBestehendeEintraege, this.getElapsedTime()));
  }

  private void ermittleFirmenIds() throws JobException
  {
    String ofs = this.sqlUtil.convertStringList(this.vorbelegungsImportBeans.stream().filter(b -> b.getFirmenId() == 0).map(VorbelegungsImportBean::getQuellReferenzOf).collect(Collectors.toSet()));
    String sql = MessageFormat.format(SQL_SELECT_FIRMA, ofs, String.valueOf(this.jobBean.quellReferenzId));
    HashMap<String, Integer> ofFirmen = new HashMap<>(this.vorbelegungsImportBeans.size());
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      ofFirmen.put(row.getString(1), row.getInt(2)); // 1 = OF , 2 = FirmenId
    }

    // iteriere über allle Ordnungsfelder und weise Firmen Id zu
    for (VorbelegungsImportBean bean : this.vorbelegungsImportBeans)
    {
      if (bean.getFirmenId() == 0)
      {
        String of = bean.getQuellReferenzOf();
        Integer firmenId = ofFirmen.get(of);
        if (firmenId == null)
        {
          throw new JobException("Kein Unternehmen zu Ordnungsfeld " + of + " gefunden...");
        }
        bean.setFirmenId(firmenId);
      }
    }
  }

  private void leseTeilbereich(Path path) throws JobException
  {
    SegmentedCsvFileReader reader = new SegmentedCsvFileReader();
    List<String[]> rows = reader.readSegment(path, this.jobBean.getImportdatei().getCharset(), this.jobBean.getImportdatei().datensatzOffset, this.jobBean.importBlockGroesse);
    this.vorbelegungsImportBeans = new ArrayList<>(rows.size());
    for (String[] cols : rows)
    {
      VorbelegungsImportBean bean = new VorbelegungsImportBean(cols);
      bean.setStatistikId(this.jobBean.statistikId);
      bean.setBzr(this.jobBean.berichtszeitraum);
      bean.setAmt(this.jobBean.amt);
      this.vorbelegungsImportBeans.add(bean);
    }
  }

  /**
   * Gets vorbelegungs import beans.
   *
   * @return the vorbelegungs import beans
   */
  public List<VorbelegungsImportBean> getVorbelegungsImportBeans()
  {
    return this.vorbelegungsImportBeans;
  }

  /**
   * Sets vorbelegungs import beans.
   *
   * @param vorbelegungsImportBeans the vorbelegungs import beans
   */
  public void setVorbelegungsImportBeans(List<VorbelegungsImportBean> vorbelegungsImportBeans)
  {
    this.vorbelegungsImportBeans = vorbelegungsImportBeans;
  }

  private void loescheVorbelegungsWerte() throws JobException
  {
    this.log.info("Loesche Vorbelegungswerte...");
    // Zuerst Werte loeschen
    // Relevanter Schluessel ist AMT, STATISTIK_ID,BZR
    int anzWerte;
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(SQL_DELETE_WERTE))
    {
      ps.addValue(this.jobBean.amt);
      ps.addValue(this.jobBean.statistikId);
      ps.addValue(this.jobBean.berichtszeitraum);
      anzWerte = ps.update();
    }
    this.log.info(anzWerte + " Vorbelegungswerte geloescht!");

  }

  /**
   * Loesche vorbelegungen mit ids.
   *
   * @param ids the ids
   * @throws JobException the job exception
   */
  public void loescheVorbelegungenMitIds(Set<Integer> ids) throws JobException
  {
    if (ids == null || ids.isEmpty())
    {
      return;
    }
    this.log.debug("Loesche Vorbelegungen mit IDs...");
    String in = this.sqlUtil.convertNumberList(ids);
    String sqlWerte = MessageFormat.format(SQL_DELETE_WERTE_MIT_VBIDS, in);
    String sqlVerwaltung = MessageFormat.format(SQL_DELETE_VERWALTUNG_MIT_VBIDS, in, this.jobBean.zeitpunktEintrag);
    this.sqlUtil.execute(sqlWerte);
    this.sqlUtil.execute(sqlVerwaltung);
    this.jobBean.getVorbelegungen().getIdentifikatoren().getLoeschung().getValues().addAll(ids);
    this.log.debug("Vorbelegungen gelöscht!");
  }

  /**
   * Versende mails.
   *
   * @throws JobException the job exception
   */
  public void versendeMails() throws JobException
  {
    // Versende Mails, falls Dateien da waren und in TRANSFERZIEL gewünscht ( Eintrag MAIL_DOWNLOAD )
    if (this.jobBean.getVorbelegungen().getFirmenIdsFuerMailVersand().isEmpty())
    {
      this.log.info("Keine Firmen fuer Mailbenachrichtigung");
      return;
    }

    // Ermittle alle Emails zum Versenden
    Email mail = createMailObjekt();
    if (mail == null)
    {
      this.log.info("Kein Transferziel mit der Aktion 'MAIL_DOWNLOAD' vorhanden! Es werden keine Mails versendet!");
      return;
    }

    // Sind Mail-Adressen da ?
    String ids = this.sqlUtil.convertNumberList(this.jobBean.getVorbelegungen().getFirmenIdsFuerMailVersand());
    String sql = MessageFormat.format(SQL_SELECT_MAIL_ADRESSEN, ids);
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      mail.addEmpfaenger(row.getString("EMAIL"));
    }
    MailVersandDaemon.getInstance().sendMail(mail);
  }

  private Email createMailObjekt() throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_TRANSFERZIEL, this.jobBean.amt, "" + this.jobBean.statistikId);
    ResultRow row = this.sqlUtil.fetchOne(sql);
    if (row != null)
    {
      String absender = row.getString(1);
      String betreff = row.getString(2);
      String text = row.getString(3);
      return new Email(absender, betreff, text);
    }
    return null;
  }

}
