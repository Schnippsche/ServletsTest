package de.destatis.regdb.db;

import au.com.bytecode.opencsv.CSVWriter;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Stefan Toengi
 */
public class LoeschUtil
{
  /**
   * The Constant LOESCH_PROTOKOLL_ADRESSEN.
   */
  public static final String LOESCH_PROTOKOLL_ADRESSEN = "geloeschteAdressen.csv";
  /**
   * The Constant LOESCH_PROTOKOLL_FIRMEN.
   */
  public static final String LOESCH_PROTOKOLL_FIRMEN = "geloeschteFirmen.csv";
  /**
   * The Constant LOESCH_PROTOKOLL_MELDER.
   */
  public static final String LOESCH_PROTOKOLL_MELDER = "geloeschteMelder.csv";
  /**
   * The Constant SQL_SELECT_FIRMEN_ADRESSEN.
   */
  public static final String SQL_SELECT_FIRMEN_ADRESSEN = "SELECT fa.FIRMEN_ID FROM firmen_adressen fa WHERE fa.adressen_id IN({0}) AND fa.FIRMEN_ID > 0 AND fa.STATUS != \"LOESCH\"";
  /**
   * The Constant SQL_SELECT_LOESCH_MELDER.
   */
  public static final String SQL_SELECT_LOESCH_MELDER = "SELECT MELDER_ID FROM melder WHERE STATUS != \"LOESCH\" AND ADRESSEN_ID IN({0})";
  /**
   * The Constant ADRESSEN_ID.
   */
  protected static final String ADRESSEN_ID = "ADRESSEN_ID";
  /**
   * The Constant FIRMEN_ID.
   */
  protected static final String FIRMEN_ID = "FIRMEN_ID";
  /**
   * The Constant MELDER_ID.
   */
  protected static final String MELDER_ID = "MELDER_ID";
  protected static final String SQL_ENTFERNE_SERVERIMPORT = "DELETE FROM standardwerte WHERE KONFIGURATION_ID=\"PRUEFLAUF\" AND KEY3=?";
  protected static final String SQL_ENTFERNE_SERVERIMPORT_AMT_STATISTIK = "DELETE FROM standardwerte WHERE KONFIGURATION_ID=\"PRUEFLAUF\" AND KEY1=? AND KEY2=? AND SB_ID=?";
  protected static final String SQL_INSERT_STANDARDWERTE = "REPLACE INTO standardwerte (KONFIGURATION_ID, WERT, KEY1, KEY2, KEY3, SB_ID, ZEITPUNKT_EINTRAG) VALUES(?,?,?,?,?,?,?)";
  /**
   * The Constant SQL_LOESCH_MELDERZUSAMMENFFUEHRUNG.
   */
  protected static final String SQL_LOESCH_MELDERZUSAMMENFFUEHRUNG = "UPDATE melder_zusammenfuehrung SET STATUS = \"INAKTIV\", ZEITPUNKT_AENDERUNG=\"{1}\" WHERE STATUS != \"INAKTIV\" AND {0} IN([IDS])";
  /**
   * The Constant SQL_LOESCH_UPDATE.
   */
  protected static final String SQL_LOESCH_UPDATE = "UPDATE {0} SET STATUS = \"LOESCH\", ZEITPUNKT_AENDERUNG=\"{2}\", SACHBEARBEITER_ID={3} WHERE STATUS != \"LOESCH\" AND {1} IN([IDS])";
  /**
   * The Constant SQL_LOESCH_UPDATE_OHNE_SB.
   */
  protected static final String SQL_LOESCH_UPDATE_OHNE_SB = "UPDATE {0} SET STATUS = \"LOESCH\", ZEITPUNKT_AENDERUNG=\"{2}\" WHERE STATUS != \"LOESCH\" AND {1} IN([IDS])";
  /**
   * The Constant SQL_SELECT_ANSPRECHPARTNER.
   */
  protected static final String SQL_SELECT_ANSPRECHPARTNER = "SELECT ANSPRECHPARTNER_ID FROM {1} WHERE {2} IN({0})";
  protected static final String SQL_SELECT_LOESCH_ADRESSEN_INFO = "SELECT ADRESSEN_ID,NAME,NAME_ERGAENZUNG,KURZTEXT,QUELL_REFERENZ_OF FROM adressen WHERE ADRESSEN_ID IN({0})";
  /**
   * The Constant SQL_SELECT_LOESCH_FIRMEN_INFO.
   */
  protected static final String SQL_SELECT_LOESCH_FIRMEN_INFO = "SELECT FIRMEN_ID,firmen.NAME AS FIRMA_NAME,firmen.NAME_ERGAENZUNG AS FIRMA_NAME_ERGAENZUNG,firmen.KURZTEXT AS FIRMA_KURZTEXT, ansprechpartner.ANSPRECHPARTNER_ID,ansprechpartner.ANREDE AS ANSPRECHPARTNER_ANREDE,ansprechpartner.NAME AS ANSPRECHPARTNER_NAME,ansprechpartner.VORNAME AS ANSPRECHPARTNER_VORNAME FROM firmen LEFT JOIN ansprechpartner USING(ANSPRECHPARTNER_ID) WHERE firmen_id IN({0})";
  /**
   * The Constant SQL_SELECT_LOESCH_MELDER_INFO.
   */
  protected static final String SQL_SELECT_LOESCH_MELDER_INFO = "SELECT ANREDE,VORNAME,NAME,KENNUNG,MELDER_ID FROM melder LEFT JOIN ansprechpartner USING(ANSPRECHPARTNER_ID) WHERE melder_id IN({0})";
  private static final String[] ADRESSEN_INFO_SPALTEN = {ADRESSEN_ID, "QUELL_REFERENZ_OF", "NAME", "NAME_ERGAENZUNG", "KURZTEXT"};
  private static final String[] FIRMEN_INFO_SPALTEN = {FIRMEN_ID, "FIRMA_NAME", "FIRMA_NAME_ERGAENZUNG", "FIRMA_KURZTEXT", "ANSPRECHPARTNER_ID", "ANSPRECHPARTNER_ANREDE", "ANSPRECHPARTNER_NAME", "ANSPRECHPARTNER_VORNAME"};
  private static final String[] MELDER_INFO_SPALTEN = {MELDER_ID, "KENNUNG", "ANREDE", "VORNAME", "NAME"};
  /**
   * The Constant SQL_DELETE_MAINJOB.
   */
  private static final String SQL_DELETE_MAINJOB = "DELETE FROM {0} WHERE IMPORT_VERWALTUNG_ID = {1}";
  protected final LoggerIfc log = Logger.getInstance().getLogger(LoeschUtil.class);
  /**
   * The loesch ansprechpartner.
   */
  private final Set<Integer> loeschAnsprechpartnerSet;
  private final SqlUtil sqlUtil;
  /**
   * The loesch adressen.
   */
  private Set<Integer> loeschAdressenSet;
  /**
   * The loesch firmen.
   */
  private Set<Integer> loeschFirmenSet;
  /**
   * The loesch melder.
   */
  private Set<Integer> loeschMelderSet;
  private String zeitpunkt;

  private Integer sachbearbeiterId;

  private long start;

  private String importVerzeichnis;

  /**
   * Instantiates a new loesch util.
   *
   * @param sqlUtil the sql util
   */
  public LoeschUtil(SqlUtil sqlUtil)
  {
    super();
    this.sqlUtil = sqlUtil;
    this.sachbearbeiterId = 0;
    this.zeitpunkt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    this.loeschFirmenSet = new HashSet<>();
    this.loeschMelderSet = new HashSet<>();
    this.loeschAnsprechpartnerSet = new HashSet<>();
  }

  /**
   * Instantiates a new loesch util.
   *
   * @param conn the conn
   */
  public LoeschUtil(Connection conn)
  {
    this(new SqlUtil(conn));
  }

  /**
   * Begin stop watch.
   */
  protected void beginStopWatch()
  {
    this.start = System.currentTimeMillis();
  }

  /**
   * Creates the infos.
   *
   * @param sql     the sql
   * @param spalten the spalten
   * @return the list
   * @throws JobException the job exception
   */
  public List<String[]> createInfos(String sql, String[] spalten) throws JobException
  {
    List<String[]> all = new ArrayList<>();
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow rs : rows)
    {
      String[] row = new String[spalten.length];
      for (int i = 0; i < spalten.length; i++)
      {
        row[i] = rs.getString(spalten[i]);
      }
      all.add(row);
    }

    return all;
  }

  /**
   * Delete.
   *
   * @param set the set
   * @param sql the sql
   * @return the int
   * @throws JobException the job exception
   */
  private int delete(Set<Integer> set, String sql) throws JobException
  {
    if (set == null || set.isEmpty())
    {
      return 0;
    }

    String ids = this.sqlUtil.convertNumberList(set);
    // Ersetze Platzhalter fuer IDs mit echten IDS
    String cmd = sql.replace("[IDS]", ids);
    return this.sqlUtil.update(cmd);
  }

  /**
   * Delete adressen.
   *
   * @throws JobException the SQL exception
   */
  private void deleteAdressen() throws JobException
  {
    beginStopWatch();
    int anzahl = delete(this.loeschAdressenSet, MessageFormat.format(SQL_LOESCH_UPDATE, "adressen", ADRESSEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId));
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} Adressen geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete aenderungen.
   *
   * @throws JobException the SQL exception
   */
  private void deleteAenderungen() throws JobException
  {
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE, "aenderung", ADRESSEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    int anzahl = delete(this.loeschAdressenSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE, "aenderung", "ANSPRECHPARTNER_ID", this.zeitpunkt, "" + this.sachbearbeiterId);
    anzahl += delete(this.loeschAnsprechpartnerSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE, "aenderung", MELDER_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    anzahl += delete(this.loeschMelderSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE, "aenderung", FIRMEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    anzahl += delete(this.loeschFirmenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} Aenderungen geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete ansprechpartner.
   *
   * @throws JobException the SQL exception
   */
  private void deleteAnsprechpartner() throws JobException
  {
    this.beginStopWatch();
    String ids;
    String sql;
    if (this.loeschMelderSet != null && !this.loeschMelderSet.isEmpty())
    {
      ids = this.sqlUtil.convertNumberList(this.loeschMelderSet);
      sql = MessageFormat.format(SQL_SELECT_ANSPRECHPARTNER, ids, "melder", MELDER_ID);
      this.sqlUtil.fetchMany(sql).forEach(e -> this.loeschAnsprechpartnerSet.add(e.getInt(1)));
    }
    if (this.loeschFirmenSet != null && !this.loeschFirmenSet.isEmpty())
    {
      ids = this.sqlUtil.convertNumberList(this.loeschFirmenSet);
      sql = MessageFormat.format(SQL_SELECT_ANSPRECHPARTNER, ids, "firmen", FIRMEN_ID);
      this.sqlUtil.fetchMany(sql).forEach(e -> this.loeschAnsprechpartnerSet.add(e.getInt(1)));
    }
    if (!this.loeschAnsprechpartnerSet.isEmpty())
    {
      sql = MessageFormat.format(SQL_LOESCH_UPDATE, "ansprechpartner", "ANSPRECHPARTNER_ID", this.zeitpunkt, "" + this.sachbearbeiterId);
      int anzahl = delete(this.loeschAnsprechpartnerSet, sql);
      this.log.info(MessageFormat.format("{0} Ansprechpartner geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete downloadinfo.
   *
   * @throws JobException the SQL exception
   */
  private void deleteDownloadinfo() throws JobException
  {
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE_OHNE_SB, "downloadinfo", MELDER_ID, this.zeitpunkt);
    int anzahl = delete(this.loeschMelderSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE_OHNE_SB, "downloadinfo", FIRMEN_ID, this.zeitpunkt);
    anzahl += delete(this.loeschFirmenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} DownloadInfo geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete firmen.
   *
   * @throws JobException the SQL exception
   */
  private void deleteFirmen() throws JobException
  {
    beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE, "firmen", FIRMEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    int anzahl = delete(this.loeschFirmenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} Firmen geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete firmen adressen.
   *
   * @throws JobException the SQL exception
   */
  private void deleteFirmenAdressen() throws JobException
  {
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE, "firmen_adressen", FIRMEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    int anzahl = delete(this.loeschFirmenSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE, "firmen_adressen", ADRESSEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    anzahl += delete(this.loeschAdressenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} FirmenAdressen geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete melder.
   *
   * @throws JobException the SQL exception
   */
  private void deleteMelder() throws JobException
  {
    beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE, "melder", MELDER_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    int anzahl = delete(this.loeschMelderSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} Melder geloescht in {1}", anzahl, this.getElapsedTime()));
    }

  }

  /**
   * Delete melder konto.
   *
   * @throws JobException the SQL exception
   */
  private void deleteMelderKonto() throws JobException
  {
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE, "melderkonto", MELDER_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    int anzahl = delete(this.loeschMelderSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE, "melderkonto", FIRMEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    anzahl += delete(this.loeschFirmenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} Melderkonto geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete melder statistiken.
   *
   * @throws JobException the SQL exception
   */
  private void deleteMelderStatistiken() throws JobException
  {
    beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE, "melder_statistiken", MELDER_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    int anzahl = delete(this.loeschMelderSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE, "melder_statistiken", FIRMEN_ID, this.zeitpunkt, "" + this.sachbearbeiterId);
    anzahl += delete(this.loeschFirmenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} MelderStatistiken geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete melder zusammenfuehrung.
   *
   * @throws JobException the SQL exception
   */
  private void deleteMelderZusammenfuehrung() throws JobException
  {
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_MELDERZUSAMMENFFUEHRUNG, MELDER_ID, this.zeitpunkt);
    int anzahl = delete(this.loeschMelderSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_MELDERZUSAMMENFFUEHRUNG, "IDENT_MELDER_ID", this.zeitpunkt);
    anzahl += delete(this.loeschMelderSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} MelderZusammenfuehrungen geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Delete meldung.
   *
   * @throws JobException the SQL exception
   */
  private void deleteMeldung() throws JobException
  {
    this.beginStopWatch();
    String sql = MessageFormat.format(SQL_LOESCH_UPDATE_OHNE_SB, "meldung", MELDER_ID, this.zeitpunkt);
    int anzahl = delete(this.loeschMelderSet, sql);
    sql = MessageFormat.format(SQL_LOESCH_UPDATE_OHNE_SB, "meldung", FIRMEN_ID, this.zeitpunkt);
    anzahl += delete(this.loeschFirmenSet, sql);
    if (anzahl > 0)
    {
      this.log.info(MessageFormat.format("{0} Meldungen geloescht in {1}", anzahl, this.getElapsedTime()));
    }
  }

  /**
   * Do loeschung.
   *
   * @throws JobException the job exception
   */
  public void doLoeschung() throws JobException
  {
    try
    {
      this.sqlUtil.dbBeginTransaction();
      this.deleteAdressen();
      this.deleteFirmen();
      this.deleteFirmenAdressen();
      this.deleteMelder();
      this.deleteAnsprechpartner();
      this.deleteMelderStatistiken();
      this.deleteMelderKonto();
      this.deleteMelderZusammenfuehrung();
      this.deleteAenderungen();
      this.deleteMeldung();
      this.deleteDownloadinfo();
      writeProtokoll();
      this.sqlUtil.dbCommit();
    }
    catch (Throwable throwable)
    {
      this.sqlUtil.dbRollback();
      throw new JobException(throwable.getMessage(), throwable);
    }
  }

  /**
   * Liefert elapsed time.
   *
   * @return elapsed time
   */
  protected String getElapsedTime()
  {
    return (System.currentTimeMillis() - this.start) / 1000 + " Sekunden";
  }

  /**
   * Sets the import verzeichnis.
   *
   * @param importVerzeichnis the new import verzeichnis
   */
  public void setImportVerzeichnis(String importVerzeichnis)
  {
    this.importVerzeichnis = importVerzeichnis;
  }

  /**
   * Sets the loesch adressen.
   *
   * @param loeschAdressen the new loesch adressen
   */
  public void setLoeschAdressen(Set<Integer> loeschAdressen)
  {
    this.loeschAdressenSet = loeschAdressen;
  }

  /**
   * Sets the loesch firmen.
   *
   * @param loeschFirmen setzt loeschFirmen
   */
  public void setLoeschFirmen(Set<Integer> loeschFirmen)
  {
    this.loeschFirmenSet = loeschFirmen;
  }

  /**
   * Sets the loesch melder.
   *
   * @param loeschMelder setzt loeschMelder
   */
  public void setLoeschMelder(Set<Integer> loeschMelder)
  {
    this.loeschMelderSet = loeschMelder;
  }

  /**
   * Sets the sachbearbeiter id.
   *
   * @param sachbearbeiterId the new sachbearbeiter id
   */
  public void setSachbearbeiterId(Integer sachbearbeiterId)
  {
    this.sachbearbeiterId = sachbearbeiterId;
  }

  /**
   * Sets the zeitpunkt.
   *
   * @param zeitpunkt the new zeitpunkt
   */
  public void setZeitpunkt(String zeitpunkt)
  {
    this.zeitpunkt = zeitpunkt;
  }

  /**
   * Loesche import.
   *
   * @param jobId the job id
   * @throws JobException the job exception
   */
  public void loescheImport(Integer jobId) throws JobException
  {
    this.log.debug("Loesche Importeintraege mit ID " + jobId);
    String sql;
    sql = MessageFormat.format(SQL_DELETE_MAINJOB, "import_teil", "" + jobId);
    this.sqlUtil.update(sql);
    sql = MessageFormat.format(SQL_DELETE_MAINJOB, "import_verwaltung", "" + jobId);
    this.sqlUtil.update(sql);
  }

  /**
   * Loesche standard werte.
   *
   * @param mainJobId the main job id
   * @throws JobException the job exception
   */
  public void loescheStandardWerte(Integer mainJobId) throws JobException
  {
    this.log.debug("loescheStandardWerte, mainJobId = " + mainJobId);
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(SQL_ENTFERNE_SERVERIMPORT))
    {
      ps.addValues(mainJobId);
      ps.update();
    }
  }

  /**
   * Loesche standard werte.
   *
   * @param amt              the amt
   * @param statistikId      the statistik id
   * @param sachbearbeiterId the sachbearbeiter id
   * @throws JobException the job exception
   */
  public void loescheStandardWerte(String amt, Integer statistikId, Integer sachbearbeiterId) throws JobException
  {
    this.log.debug(MessageFormat.format("loescheStandardWerte (Amt={0}, statistikId={1}, sbId={2}", amt, statistikId, sachbearbeiterId));
    try (PreparedUpdate pu = this.sqlUtil.createPreparedUpdate(SQL_ENTFERNE_SERVERIMPORT_AMT_STATISTIK))
    {
      pu.addValues(amt, statistikId, sachbearbeiterId);
      pu.update();
    }
  }

  /**
   * Pruefe adress verweise.
   *
   * @param adressenIds the adressen ids
   * @param defaultSql  the default sql
   * @return the sets the
   * @throws JobException the job exception
   */
  public Set<Integer> pruefeAdressVerweise(Set<Integer> adressenIds, String defaultSql) throws JobException
  {
    HashSet<Integer> loeschKandidaten = new HashSet<>();
    if (adressenIds == null || adressenIds.isEmpty())
    {
      return loeschKandidaten;
    }
    String tmpsql = MessageFormat.format(defaultSql, this.sqlUtil.convertNumberList(adressenIds));
    this.sqlUtil.fetchMany(tmpsql).forEach(e -> loeschKandidaten.add(e.getInt(1)));
    return loeschKandidaten;
  }

  /**
   * Schreibe adress infos.
   *
   * @param protokoll   the protokoll
   * @param adressenIds the adressen ids
   * @return true, if successful
   * @throws JobException the job exception
   */
  public boolean schreibeAdressInfos(Path protokoll, Collection<Integer> adressenIds) throws JobException
  {
    return schreibeInfos(protokoll, adressenIds, SQL_SELECT_LOESCH_ADRESSEN_INFO, ADRESSEN_INFO_SPALTEN);
  }

  /**
   * Schreibe firmen infos.
   *
   * @param protokoll the protokoll
   * @param firmenIds the firmen ids
   * @return true, if successful
   * @throws JobException the job exception
   */
  public boolean schreibeFirmenInfos(Path protokoll, Collection<Integer> firmenIds) throws JobException
  {
    return schreibeInfos(protokoll, firmenIds, SQL_SELECT_LOESCH_FIRMEN_INFO, FIRMEN_INFO_SPALTEN);
  }

  /**
   * Schreibe infos.
   *
   * @param protokoll   the protokoll
   * @param ids         the ids
   * @param sqlSelect   the sql select
   * @param infoSpalten the info spalten
   * @return true, if successful
   * @throws JobException the job exception
   */
  private boolean schreibeInfos(Path protokoll, Collection<Integer> ids, String sqlSelect, String[] infoSpalten) throws JobException
  {
    if (ids == null || ids.isEmpty())
    {
      return false;
    }
    String tmpSql = MessageFormat.format(sqlSelect, this.sqlUtil.convertNumberList(ids));
    List<String[]> all = createInfos(tmpSql, infoSpalten);
    return this.writeLogInfo(protokoll, infoSpalten, all);
  }

  /**
   * Schreibe melder infos.
   *
   * @param protokoll the protokoll
   * @param melderIds the melder ids
   * @return true, if successful
   * @throws JobException the job exception
   */
  public boolean schreibeMelderInfos(Path protokoll, Collection<Integer> melderIds) throws JobException
  {
    return schreibeInfos(protokoll, melderIds, SQL_SELECT_LOESCH_MELDER_INFO, MELDER_INFO_SPALTEN);
  }

  /**
   * Speichere standardwerte.
   *
   * @param originalDateiname the original dateiname
   * @param amt               the amt
   * @param statistikId       the statistik id
   * @param mainJobId         the main job id
   * @param sachbearbeiterId  the sachbearbeiter id
   * @param zeitpunkt         the zeitpunkt
   * @throws JobException the job exception
   */
  public void speichereStandardwerte(String originalDateiname, String amt, Integer statistikId, Integer mainJobId, Integer sachbearbeiterId, String zeitpunkt) throws JobException
  {
    this.log.debug(MessageFormat.format("speichereStandardWerte (Dateiname={0}, Amt={1}, statistikId={2}, mainJobId={3}, sbId={4}, zeitpunkt={5}", originalDateiname, amt, statistikId, mainJobId, sachbearbeiterId, zeitpunkt));
    try (PreparedInsert pi = this.sqlUtil.createPreparedInsert(SQL_INSERT_STANDARDWERTE))
    {
      pi.addValues("PRUEFLAUF", originalDateiname, amt, statistikId, mainJobId, sachbearbeiterId, zeitpunkt);
      pi.insert();
    }
  }

  /**
   * Write log info.
   *
   * @param ziel           the ziel
   * @param ueberschriften the ueberschriften
   * @param arr            the arr
   * @return true, if successful
   */
  protected boolean writeLogInfo(Path ziel, String[] ueberschriften, List<String[]> arr)
  {
    this.log.debug(MessageFormat.format("writeLogInfo to {0} mit {1} Einträgen", ziel, arr.size()));
    boolean writeHeader = !Files.exists(ziel);
    try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(ziel, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND), ';'))
    {
      if (writeHeader)
      {
        writer.writeNext(ueberschriften);
      }
      writer.writeAll(arr);
      writer.flush();
      return true;
    }
    catch (IOException e)
    {
      this.log.error("Fehler beim Schreiben der Protokolldatei " + ziel.getFileName() + ":" + e.getMessage());
      return false;
    }
  }

  /**
   * Write protokoll.
   *
   * @throws JobException the job exception
   */
  public void writeProtokoll() throws JobException
  {
    Path adressProtokoll = Paths.get(this.importVerzeichnis, LOESCH_PROTOKOLL_ADRESSEN);
    Path firmenProtokoll = Paths.get(this.importVerzeichnis, LOESCH_PROTOKOLL_FIRMEN);
    Path melderProtokoll = Paths.get(this.importVerzeichnis, LOESCH_PROTOKOLL_MELDER);
    schreibeAdressInfos(adressProtokoll, this.loeschAdressenSet);
    schreibeFirmenInfos(firmenProtokoll, this.loeschFirmenSet);
    schreibeMelderInfos(melderProtokoll, this.loeschMelderSet);
  }
}
