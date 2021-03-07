/*
 * @(#)RegisterImportJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.registerimport;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.LoeschenJob;
import de.destatis.regdb.dateiimport.job.adressimport.*;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportBean;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedStringFileReader;
import de.destatis.regdb.db.*;
import de.werum.sis.idev.res.job.JobException;

import java.nio.file.Path;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RegisterImportJob extends AdressImportJob
{

  public static final String SQL_SELECT_STATONLINEKEY = "SELECT STATISTIK_ID,QUELL_REFERENZ_ID FROM statistiken_amt WHERE amt = \"{0}\" AND STAT_ONLINE_KEY= \"{1}\" AND STATUS != \"LOESCH\"";
  public static final String SQL_SELECT_BZR = "SELECT BZR FROM erhebung WHERE amt=\"{0}\" AND STATISTIK_ID={1} AND BZR=\"{2}\" AND STATUS != \"LOESCH\"";
  public static final String SQL_INSERT_MELDER_STATISTIKEN = "INSERT IGNORE INTO melder_statistiken (MELDER_ID, STATISTIK_ID, AMT, FIRMEN_ID, ROLLE, MELDERECHT_BZR, STATUS, ERINNERUNGSSERVICE, IS_ADRESSEN_ID,SACHBEARBEITER_ID, ZEITPUNKT_EINTRAG,IS_STATUS) VALUES(?,?,?,?,?,?,?,?,?,?,?,'NEU')";
  private final VorbelegungsImportJob vorbelegungsJob;
  /**
   * The neue passwoerter generieren.
   */

  private HashMap<String, Integer> amtStatOnlineKeys;

  /**
   * Instantiates a new register import job.
   *
   * @param jobBean the job Bean
   */
  public RegisterImportJob(JobBean jobBean)
  {
    super("RegisterImport", jobBean);
    this.vorbelegungsJob = new VorbelegungsImportJob(jobBean, this.sqlUtil);
  }

  /**
   * Lese teilbereich.
   *
   * @param path the path
   * @throws JobException the job exception
   * @see de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob#leseTeilbereich(java.nio.file.Path)
   */
  @Override
  protected void leseTeilbereich(Path path) throws JobException
  {
    SegmentedStringFileReader fileReader = new SegmentedStringFileReader();
    List<String> rows = fileReader.readSegment(path, this.jobBean.getImportdatei().getCharset(), this.jobBean.getImportdatei().datensatzOffset, this.jobBean.importBlockGroesse);
    this.adressImportBeans = new HashMap<>(rows.size());
    this.vorbelegungsJob.setVorbelegungsImportBeans(new ArrayList<>(rows.size()));
    this.amtStatOnlineKeys = new HashMap<>();
    for (String row : rows)
    {
      AdressImportBean bean = this.doParseZeile(row);
      if (bean != null && !this.adressImportBeans.containsKey(bean.getAdresse().getQuellReferenzOf()))
      {
        this.adressImportBeans.put(bean.getAdresse().getQuellReferenzOf(), bean);
        // Ermittle Statistik-Id
        String key = bean.getAdresse().getAmt() + "|" + bean.getStatOnlineKey();
        Integer statId = this.amtStatOnlineKeys.get(key);
        if (statId == null)
        {
          this.jobBean.statistikId = this.getStatistikIdFromAmtStatOnlineKey(bean.getAdresse().getAmt(), bean.getStatOnlineKey());
          this.amtStatOnlineKeys.put(key, this.jobBean.statistikId);
        }
        if (this.jobBean.getMelder().erzeugeVorbelegungenStattAnsprechpartner)
        {
          this.vorbelegungsJob.getVorbelegungsImportBeans().add(this.erzeugeVorbelegungsBean(bean, this.jobBean.statistikId));
        }
      }
    }
    this.sortedAdressImportBeans = new ArrayList<>(this.adressImportBeans.values());
    Collections.sort(this.sortedAdressImportBeans);
  }

  /**
   * Aktualisiere bestehende melder.
   *
   * @throws JobException the SQL exception
   */
  @Override
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
    String sql = MelderBean.SQL_UPDATE_MELDER_REGISTER;
    this.log.debug(sql);
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(sql))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        MelderBean mb = bean.getMelder();
        if (bean.getAdresse().isNotManuelleAdresse() && !mb.isNeu())
        {
          anzahl++;
          mb.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          mb.updateRegisterimport(ps);
          this.jobBean.getMelder().getIdentifikatoren().getAenderung().getValues().add(mb.getMelderId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge vorbelegungs bean.
   *
   * @param adressImportBean the adress import bean
   * @param statistikId      the statistik id
   * @return the vorbelegungs import bean
   */
  private VorbelegungsImportBean erzeugeVorbelegungsBean(AdressImportBean adressImportBean, Integer statistikId)
  {
    VorbelegungsImportBean vbBean = new VorbelegungsImportBean();
    vbBean.setStatistikId(statistikId);
    vbBean.setAmt(adressImportBean.getAdresse().getAmt());
    vbBean.setBzr(adressImportBean.getBzr());
    vbBean.setFormularname("");
    vbBean.setMelderId(0);
    vbBean.setQuellReferenzInt(adressImportBean.getQuellReferenzInt());
    vbBean.setQuellReferenzOf(adressImportBean.getAdresse().getQuellReferenzOf());
    vbBean.getWerte().put("Ansprechpartner_Name", adressImportBean.getAnsprechpartnerFachabteilungName());
    vbBean.getWerte().put("Ansprechpartner_Telefon", adressImportBean.getAnsprechpartnerFachabteilungTelefon());
    vbBean.getWerte().put("Ansprechpartner_Mail", adressImportBean.getAnsprechpartnerFachabteilungEmail());
    vbBean.getWerte().put("Datenempfaenger", adressImportBean.getDddkislId());
    return vbBean;
  }

  /**
   * Do in transaction.
   *
   * @throws SQLException the SQL exception
   * @throws JobException the job exception
   * @see de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob#doInTransaction()
   */
  @Override
  protected void doInTransaction() throws SQLException, JobException
  {
    super.doInTransaction();
    this.erzeugeMelderStatistiken();
    this.uebertrageFirmenIds();
    // Ermittle Indizes der Vorbelegungen
    if (this.jobBean.getMelder().erzeugeVorbelegungenStattAnsprechpartner)
    {
      this.vorbelegungsJob.verdichteDaten();
      this.vorbelegungsJob.ermittleIndizes();
      this.vorbelegungsJob.erzeugeNeueVerwaltungsEintraege();
      this.vorbelegungsJob.aktualisiereVerwaltungEintraege();
      this.vorbelegungsJob.erzeugeOderAktualisiereWertEintraege();
    }
  }

  /**
   * Uebertrage firmen ids.
   */
  private void uebertrageFirmenIds()
  {
    HashMap<String, VorbelegungsImportBean> tmpVorbelegungen = new HashMap<>(this.vorbelegungsJob.getVorbelegungsImportBeans().size());
    for (VorbelegungsImportBean vorbelBean : this.vorbelegungsJob.getVorbelegungsImportBeans())
    {
      String key2 = vorbelBean.getAmt() + "|" + vorbelBean.getQuellReferenzOf() + "|" + vorbelBean.getQuellReferenzInt();
      tmpVorbelegungen.put(key2, vorbelBean);
    }
    for (AdressImportBean adressBean : this.sortedAdressImportBeans)
    {
      String key1 = adressBean.getAdresse().getAmt() + "|" + adressBean.getAdresse().getQuellReferenzOf() + "|" + adressBean.getQuellReferenzInt();
      VorbelegungsImportBean vorbelBean = tmpVorbelegungen.get(key1);
      if (vorbelBean != null)
      {
        vorbelBean.setFirmenId(adressBean.getFirma().getFirmenId());
      }
    }
  }

  /**
   * Next import job.
   *
   * @return the abstract job
   * @see de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob#nextImportJob()
   */
  @Override
  protected AbstractJob nextImportJob()
  {
    return new RegisterImportJob(this.jobBean);
  }

  /**
   * Do after last import.
   *
   * @return the abstract job
   * @see de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob#doAfterLastImport()
   */
  @Override
  protected AbstractJob doAfterLastImport()
  {
    if (this.jobBean.loescheDaten)
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
    return null;
  }

  /**
   * Liefert statistik id from amt stat online key.
   *
   * @param amt           the amt
   * @param statOnlineKey the stat online key
   * @return statistik id from amt stat online key
   * @throws JobException the SQL exception
   */
  private Integer getStatistikIdFromAmtStatOnlineKey(String amt, String statOnlineKey) throws JobException
  {
    String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_STATONLINEKEY, StringUtil.escapeSqlString(amt), StringUtil.escapeSqlString(statOnlineKey));
    ResultRow rs = this.sqlUtil.fetchOne(sql);
    return (rs != null ? rs.getInt(1) : 0);

  }

  /**
   * Do parse zeile.
   *
   * @param row the row
   * @return the adress import bean
   */
  private AdressImportBean doParseZeile(String row)
  {
    if (row.length() < 50 || row.length() > 440)
    {
      return null;
    }
    // Verwende safe StringUtils.substring, da Zeilenlänge kleiner sein kann
    AdressImportBean bean = new AdressImportBean();
    AdresseBean ab = bean.getAdresse();
    ab.setQuellReferenzId(this.jobBean.quellReferenzId);
    ab.setAmt(StringUtil.substring(row, 20, 22).trim());
    bean.setStatOnlineKey(StringUtil.substring(row, 0, 10).trim());
    ab.setQuellReferenzOf(StringUtil.substring(row, 10, 20).trim());
    bean.setBzr(StringUtil.substring(row, 22, 28).trim());
    bean.setQuellReferenzInt(StringUtil.substring(row, 28, 48).trim());
    bean.setDddkislId(StringUtil.substring(row, 48, 50).trim());
    ab.setUrs(0, StringUtil.substring(row, 50, 90).trim());
    ab.setUrs(1, StringUtil.substring(row, 90, 130).trim());
    ab.setUrs(2, StringUtil.substring(row, 130, 170).trim());
    ab.setUrs(3, StringUtil.substring(row, 170, 210).trim());
    bean.setAuswahlkriterium(ab.getUrs(3));
    ab.setUrs(4, StringUtil.substring(row, 210, 250).trim());
    ab.setUrs(5, StringUtil.substring(row, 250, 260).trim());
    ab.setUrs(6, StringUtil.substring(row, 260, 300).trim());
    ab.setName((ab.getUrs(0) + " " + ab.getUrs(1)).trim());
    ab.setNameErgaenzung((ab.getUrs(2) + " " + ab.getUrs(3)).trim());
    ab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    ab.setMelderAenderbar(!this.jobBean.getAdressen().nichtAenderbar);
    // Wenn NAME leer ist, dann tausche NAME mit NAME_ERGAENZUNG
    if (ab.getName().isEmpty())
    {
      ab.setName(ab.getNameErgaenzung());
      ab.setNameErgaenzung("");
    }
    ab.setStrasse(ab.getUrs(4));
    ab.setPostleitzahl(ab.getUrs(5));
    ab.setOrt(ab.getUrs(6));

    bean.setAuswahlkriterium(StringUtil.substring(row, 300, 320).trim()); // AUSWAHLKRITERIUM
    bean.setAnsprechpartnerFachabteilungName(StringUtil.substring(row, 320, 360).trim()); // VORBEL_AN_NAME
    bean.setAnsprechpartnerFachabteilungTelefon(StringUtil.substring(row, 360, 380).trim()); // VORBEL_AN_TELEFON
    bean.setAnsprechpartnerFachabteilungEmail(StringUtil.substring(row, 380, 430).trim()); // VORBEL_AN_EMAIL
    for (int x = 0; x < 10; x++)
    {
      ab.setZusatz(x, "");
    }
    // Zusatzfelder statt Vorbelegungen ?
    if (!this.jobBean.getMelder().erzeugeVorbelegungenStattAnsprechpartner)
    {
      ab.setZusatz(0, bean.getAnsprechpartnerFachabteilungName());
      ab.setZusatz(1, bean.getAnsprechpartnerFachabteilungTelefon());
      ab.setZusatz(2, bean.getAnsprechpartnerFachabteilungEmail());
      ab.setZusatz(3, bean.getDddkislId());
      ab.setZusatz(4, bean.getAuswahlkriterium());
    }
    AnsprechpartnerBean firmenAnsprechpartnerBean = bean.getFirma().getAnsprechpartner();
    firmenAnsprechpartnerBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    // Firma befuellen
    FirmenBean firmenBean = bean.getFirma();
    firmenBean.setName(ab.getName());
    firmenBean.setNameErgaenzung(ab.getNameErgaenzung());
    firmenBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    // Ansprechpartner Melder befuellen
    AnsprechpartnerBean melderAnsprechpartnerBean = bean.getMelder().getAnsprechpartner();
    melderAnsprechpartnerBean.setName(ab.getName());
    melderAnsprechpartnerBean.setVorname(ab.getNameErgaenzung());
    melderAnsprechpartnerBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    // Melder befuellen
    MelderBean melderBean = bean.getMelder();
    melderBean.setPasswortAenderbar(!this.jobBean.getMelder().passwortUnveraenderbar);
    melderBean.setZusammenfuehrbar(this.jobBean.getMelder().zusammenfuehrbar);
    melderBean.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    return bean;
  }

  /**
   * Erzeuge melder statistiken.
   *
   * @throws JobException the SQL exception
   */
  private void erzeugeMelderStatistiken() throws JobException
  {
    this.log.info("Erzeuge Neue Melder-Statistik-Eintraege...");
    int anzahl = 0;
    this.beginStopWatch();
    try (PreparedInsert pi = this.sqlUtil.createPreparedInsert(SQL_INSERT_MELDER_STATISTIKEN))
    {
      for (AdressImportBean bean : this.sortedAdressImportBeans)
      {
        String key = bean.getAdresse().getAmt() + "|" + bean.getStatOnlineKey();
        this.jobBean.statistikId = this.amtStatOnlineKeys.get(key);
        pi.addValue(bean.getMelder().getMelderId()); //
        pi.addValue(this.jobBean.statistikId); // STATISTIK_ID
        pi.addValue(bean.getAdresse().getAmt()); //
        pi.addValue(bean.getFirma().getFirmenId()); //
        pi.addValue("MELDER"); //
        pi.addValue(bean.getBzr()); //
        pi.addValue((this.jobBean.getMelder().gesperrt ? "SPERRE" : "NEU")); // STATUS
        pi.addValue("J"); // ERINNERUNGSSERVICE
        pi.addValue(bean.getAdresse().getAdressenId()); // IS_ADRESSEN_ID
        pi.addValue(this.jobBean.sachbearbeiterId); //
        pi.addValue(this.jobBean.zeitpunktEintrag);
        anzahl += pi.insert();
      }
    }
    this.log.info(MessageFormat.format("{0} Melder-Statistik-Eintraege erzeugt in {1}", anzahl, this.getElapsedTime()));
  }
}
