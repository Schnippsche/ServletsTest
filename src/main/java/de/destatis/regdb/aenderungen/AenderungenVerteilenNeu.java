package de.destatis.regdb.aenderungen;

import au.com.bytecode.opencsv.CSVWriter;
import de.destatis.regdb.FTP;
import de.destatis.regdb.SFTP;
import de.destatis.regdb.TransferException;
import de.destatis.regdb.db.PreparedSelect;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AenderungenVerteilenNeu
{
  private static final String DIREKTEINTRAG_FEHLERTEXT = "Fehler beim Direkteintrag der {0} (ID={1}):";
  private static final String SQL_UPDATE_STATUS_DIREKTEINTRAG = "UPDATE aenderung SET STATUS=\"BEARBEITET\", ZEITPUNKT_EXPORT = NOW(), STATUS_DIREKTEINTRAG = (STATUS_DIREKTEINTRAG | {0}) WHERE AENDERUNG_ID IN({1})";
  private static final String SQL_UPDATE_STATUS_DATEIEXPORT = "UPDATE aenderung SET STATUS=\"BEARBEITET\", ZEITPUNKT_EXPORT = NOW(), STATUS_EXPORT_AENDERUNG = (STATUS_EXPORT_AENDERUNG | {0}) WHERE AENDERUNG_ID IN({1})";
  private static final String SQL_DIREKTEINTRAG = "UPDATE {0} SET {1}, SACHBEARBEITER_ID={2}, STATUS=\"AEND\", ZEITPUNKT_AENDERUNG = NOW() WHERE {3} = {4}";
  private static final String[] adressenSpalten = {"ANREDE", "NAME", "NAME_ERGAENZUNG", "KURZTEXT", "ABTEILUNG", "STRASSE", "HAUSNUMMER", "POSTLEITZAHL", "ORT", "POSTFACH", "POSTFACH_PLZ", "POSTFACH_ORT", "LAND", "TELEFON", "FAX", "EMAIL", "ZUSATZ1", "ZUSATZ2", "ZUSATZ3", "ZUSATZ4", "ZUSATZ5", "ZUSATZ6", "ZUSATZ7", "ZUSATZ8", "ZUSATZ9", "ZUSATZ10", "URS1", "URS2", "URS3", "URS4", "URS5", "URS6", "URS7"};
  private static final String[] ansprechpartnerSpalten = {"AN_ANREDE", "AN_NAME", "AN_VORNAME", "AN_ABTEILUNG", "AN_TELEFON", "AN_MOBIL", "AN_FAX", "AN_EMAIL"};
  private static final String[] firmenSpalten = {"FA_NAME", "FA_NAME_ERGAENZUNG", "FA_KURZTEXT"};
  private static final String SQL_SELECT_STANDARD_KONFIGURATION = "SELECT UCASE(KONFIGURATION_ID) AS ID, WERT_STRING FROM konfiguration WHERE STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_TRANSFERZIEL = "SELECT transfer.AMT, transfer.STATISTIK_ID, transfer.AKTION, CAST(transfer.AENDERUNGSART AS UNSIGNED) AS AENDERUNGSARTVALUE, transferziel.KONVERTER, transferziel.KONVERTER_OPTIONEN, transferziel.TRANSFER_FORM, transferziel.TRANSFER_PLATTFORM, transferziel.TRANSFER_HOST, transferziel.TRANSFER_USER, transferziel.TRANSFER_PASSWORT, transferziel.TRANSFER_ACCOUNT, transferziel.TRANSFER_ZIEL_VERZEICHNIS, transferziel.TRANSFER_MODUS, transferziel.TRANSFER_MAIL_EMPFAENGER, transferziel.TRANSFER_MAIL_ABSENDER, transferziel.TRANSFER_MAIL_BETREFF, transferziel.TRANSFER_MAIL_TEXT, transferziel.AENDERUNGS_EXPORT_SPALTEN FROM transfer INNER JOIN transferziel USING(TRANSFERZIEL_ID) WHERE transfer.amt=\"{0}\"  AND LEFT(transfer.AKTION,5) = \"AEND_\" AND transfer.STATUS != \"LOESCH\" AND transferziel.STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_SACHBEARBEITER = "SELECT SACHBEARBEITER_ID FROM sachbearbeiter WHERE kennung=\"{0}\" AND status != \"LOESCH\"";
  private static final String SQL_SELECT_AENDERUNGEN_DIREKT = "SELECT CAST(AENDERUNGSART AS UNSIGNED) AS AENDERUNGSARTVALUE, aenderung.* FROM aenderung WHERE AMT =? AND STATISTIK_ID = ? AND TYP = ? AND STATUS IN(\"NEU\",\"ERLEDIGT\",\"BEARBEITET\") AND (AENDERUNGSART & ?) AND (STATUS_EXPORT_AENDERUNG & ? = 0)  ORDER BY AENDERUNG_ID LIMIT ?";
  private static final String SQL_SELECT_AENDERUNGEN_OHNE_AMT = "SELECT CAST(AENDERUNGSART AS UNSIGNED) AS AENDERUNGSARTVALUE, ae.* FROM aenderung AS ae INNER JOIN melder AS m ON (m.melder_id = ae.melder_id) INNER JOIN adressen as a ON (m.adressen_id = a.adressen_id) INNER JOIN quell_referenz_verwaltung as q ON (a.quell_referenz_id = q.quell_referenz_id) INNER JOIN melder_statistiken as ms ON (m.melder_id = ms.melder_id) WHERE q.amt IN(\"\", ?) AND q.statistik_id IN(0, ?) AND ae.amt=\"\" AND ae.statistik_id=0 AND ms.amt=? AND ms.statistik_id=? AND ms.STATUS != \"LOESCH\" AND ae.STATUS IN(\"NEU\",\"ERLEDIGT\",\"BEARBEITET\")  AND typ=? AND q.STATUS != \"LOESCH\" AND (ae.aenderungsart & ?) AND (ae.STATUS_EXPORT_AENDERUNG & ? = 0) ORDER BY ae.aenderung_id LIMIT ?";
  private static final String MSG_UEBERTRAGE_DATEI = "uebertrage Datendatei {0} nach {1} {2} per {3}, Zielname = {4}";
  private static final String MSG_UEBERTRAGUNG_KORREKT = "Datei: -{0}- wurde korrekt mittels {1} in das Verzeichnis -{2}- des Servers -{3}- uebertragen.";
  private static final String MSG_UEBERTRAGUNG_FEHLER = "Datei: -{0}- konnte nicht mittels {1} in das Verzeichnis -{2}- des Servers -{3}- uebertragen werden: {4}";

  private static final LoggerIfc log = Logger.getInstance().getLogger(AenderungenVerteilenNeu.class);
  private final ArrayList<TransferDaten> transferDatenList;
  private final String amt;
  private final String sbKennung;
  private final DateTimeFormatter sdf;
  private final SqlUtil sqlUtil;
  private final List<TransferDaten> direktEintraege;
  private final List<TransferDaten> dateiExporte;
  private String zielZeichensatz;
  private boolean disableHostKeyCheck;
  private String knownHostDatei;
  private boolean exportMitUeberschrift;
  private int sbId;
  private HashMap<String, String> defaultKonfigurationMap;
  private String tempVerzeichnis;
  private int anzahlAenderungenErlaubt;

  public AenderungenVerteilenNeu(Connection connection, String amt, String kennung)
  {
    this.sqlUtil = new SqlUtil(connection);
    this.transferDatenList = new ArrayList<>();
    this.amt = amt;
    this.sbKennung = kennung;
    this.direktEintraege = new ArrayList<>();
    this.dateiExporte = new ArrayList<>();
    setZielZeichensatz(StandardCharsets.ISO_8859_1.name());
    disableHostKeyCheck(false);
    setKnownHostDatei(null);
    setExportMitUeberschrift(false);
    this.sdf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSSS");
    this.tempVerzeichnis = ".";
    setAnzahlAenderungenErlaubt(1000);
  }

  public void starteVerarbeitung() throws JobException
  {
    if (ermittleTransferziele())
    {
      verarbeiteDirektEintraege();
      verarbeiteDateiexporte();
    }
    else
    {
      log.info("keine neue Änderungen mit Transferziel vorhanden");
    }
  }

  private HashMap<String, String> ermittleStandardKonfiguration() throws JobException
  {
    HashMap<String, String> result = new HashMap<>();
    List<ResultRow> rows = this.sqlUtil.fetchMany(SQL_SELECT_STANDARD_KONFIGURATION);
    for (ResultRow row : rows)
    {
      result.put(row.getString(1), row.getString(2));
    }
    return result;
  }

  private void ermittleSachbearbeiter() throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_SACHBEARBEITER, this.sbKennung);
    ResultRow row = this.sqlUtil.fetchOne(sql);
    if (row != null)
    {
      this.sbId = row.getInt(1);
    }
  }

  /**
   * Sets known host datei.
   *
   * @param knownHostDatei the known host datei
   */
  public void setKnownHostDatei(String knownHostDatei)
  {
    this.knownHostDatei = knownHostDatei;
  }

  /**
   * Disable host key check.
   *
   * @param status the status
   */
  public void disableHostKeyCheck(boolean status)
  {
    this.disableHostKeyCheck = status;
  }

  public void setAnzahlAenderungenErlaubt(int anzahlAenderungenErlaubt)
  {
    this.anzahlAenderungenErlaubt = anzahlAenderungenErlaubt;
  }

  /**
   * Sets ziel zeichensatz.
   *
   * @param charsetName the charset name
   */
  public void setZielZeichensatz(String charsetName)
  {
    this.zielZeichensatz = charsetName;
  }

  public void setExportMitUeberschrift(boolean status)
  {
    this.exportMitUeberschrift = status;
  }

  /**
   * Ermittle transferziele boolean.
   *
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean ermittleTransferziele() throws JobException
  {
    this.transferDatenList.clear();
    this.defaultKonfigurationMap = ermittleStandardKonfiguration();
    ermittleSachbearbeiter();
    ermittleTempDirectory();
    String sql = MessageFormat.format(SQL_SELECT_TRANSFERZIEL, this.amt);
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      TransferDaten td = new TransferDaten(row);
      this.transferDatenList.add(td);
      if (td.isDirekteintrag())
      {
        this.direktEintraege.add(td);
      }
      if (td.isDateiexport())
      {
        this.dateiExporte.add(td);
      }
    }
    return !this.transferDatenList.isEmpty();
  }

  private void ermittleTempDirectory()
  {
    this.tempVerzeichnis = this.defaultKonfigurationMap.get("INT_TEMP_DIRECTORY");
    if (this.tempVerzeichnis == null)
    {
      this.tempVerzeichnis = System.getProperty("java.io.tmpdir");
    }
  }

  public void verarbeiteDirektEintraege() throws JobException
  {
    // Alle Transferziele mit Direkteintrag
    for (TransferDaten td : this.direktEintraege)
    {
      Set<Aenderung> aenderungen = ermittleAenderungen(td);
      td.setAenderungen(aenderungen);
      for (Aenderung aenderung : aenderungen)
      {
        doDirektEintrag(aenderung);
      }
      Set<Integer> idSet = aenderungen.stream().filter(Aenderung::isDirektEintragErfolgreich).map(Aenderung::getAenderungsId).collect(Collectors.toSet());
      doStatusOk(SQL_UPDATE_STATUS_DIREKTEINTRAG, td.getAenderungsart(), idSet);
      td.setDirektEintragErfolgreich(true);
    }
  }

  private void doDirektEintrag(Aenderung aenderung)
  {
    String sql;
    String fehlertext;
    boolean eintragOk = true;
    // Adressen ?
    String set = aenderung.getSqlUpdateWithColumns(adressenSpalten, "");
    if (StringUtil.notEmpty(set))
    {
      sql = MessageFormat.format(SQL_DIREKTEINTRAG, "adressen", set, "" + this.sbId, "adressen_id", "" + aenderung.getAdressenId());
      fehlertext = MessageFormat.format(DIREKTEINTRAG_FEHLERTEXT, "Adresse", "" + aenderung.getAdressenId());
      if (doSqlDirekteintrag(sql, fehlertext) == -1)
      {
        eintragOk = false;
      }
    }
    // Firmen ?
    set = aenderung.getSqlUpdateWithColumns(firmenSpalten, "FA_");
    if (StringUtil.notEmpty(set))
    {
      sql = MessageFormat.format(SQL_DIREKTEINTRAG, "firmen", set, "" + this.sbId, "firmen_id", "" + aenderung.getFirmenId());
      fehlertext = MessageFormat.format(DIREKTEINTRAG_FEHLERTEXT, "Firma", "" + aenderung.getFirmenId());
      if (doSqlDirekteintrag(sql, fehlertext) == -1)
      {
        eintragOk = false;
      }
    }
    // Ansprechpartner ?
    set = aenderung.getSqlUpdateWithColumns(ansprechpartnerSpalten, "AN_");
    if (StringUtil.notEmpty(set))
    {
      sql = MessageFormat.format(SQL_DIREKTEINTRAG, "ansprechpartner", set, "" + this.sbId, "ansprechpartner_id", "" + aenderung.getAnsprechpartnerId());
      fehlertext = MessageFormat.format(DIREKTEINTRAG_FEHLERTEXT, "Ansprechpartner", "" + aenderung.getAnsprechpartnerId());
      if (doSqlDirekteintrag(sql, fehlertext) == -1)
      {
        eintragOk = false;
      }
    }
    if (eintragOk)
    {
      aenderung.setDirektEintragErfolgreich(true);
    }
  }

  private int doSqlDirekteintrag(String sql, String fehlertext)
  {
    try
    {
      return this.sqlUtil.update(sql);

    }
    catch (JobException e)
    {
      log.error(fehlertext + ":" + e.getMessage());
    }
    return -1;
  }

  public void verarbeiteDateiexporte() throws JobException
  {
    // Alle Transferziele mit Direkteintrag
    for (TransferDaten td : this.dateiExporte)
    {
      Set<Aenderung> aenderungen = ermittleAenderungen(td);
      if (!aenderungen.isEmpty())
      {
        td.setAenderungen(aenderungen);
        if (td.isSammel())
        {
          erzeugeExportDatei(aenderungen, td);
        }
        else
        {
          Set<Aenderung> einzel = new HashSet<>(1);
          for (Aenderung aenderung : aenderungen)
          {
            einzel.clear();
            einzel.add(aenderung);
            erzeugeExportDatei(einzel, td);
          }
        }
        doDateiTransfer(td);
        /*
        if (td.isExportErfolgreich())
        {
          Set<Integer> idSet = aenderungen.stream().map(Aenderung::getAenderungsId).collect(Collectors.toSet());
          doStatusOk(SQL_UPDATE_STATUS_DATEIEXPORT, td.getAenderungsart(), idSet);
        } */
      }
    }
  }

  public void doDateiTransfer(TransferDaten td) throws JobException
  {
    // Verarbeite alle Aenderungen zu diesem Transferziel

    int plattForm = td.getPlattformInt();
    switch (plattForm)
    {
      case AenderungsTransferDaten.PLATTFORM_LOKAL:
        copyFile(td, td.getDateiTransferList());
        break;
      case AenderungsTransferDaten.PLATTFORM_HOST_FTP:
      case AenderungsTransferDaten.PLATTFORM_UNIX_FTP:
        sendFTP(td, td.getDateiTransferList());
        break;
      case AenderungsTransferDaten.PLATTFORM_HOST_SFTP:
      case AenderungsTransferDaten.PLATTFORM_UNIX_SFTP:
        sendSFTP(td, td.getDateiTransferList());
        break;
      default:
        log.info("Keine Aktion für Plattform " + plattForm);
    }
  }

  private void copyFile(TransferDaten td, List<DateiTransfer> dateien) throws JobException
  {
    for (DateiTransfer datei : dateien)
    {
      String zielDatei = datei.getZielVerzeichnis() + "/" + datei.getZielDateiname();
      log.info("kopiere Datendatei " + datei.getQuellPfad().getFileName() + " lokal nach " + zielDatei);
      try
      {
        Files.copy(datei.getQuellPfad(), Paths.get(zielDatei), StandardCopyOption.REPLACE_EXISTING);
        datei.setTransferErolgreich(true);
      }
      catch (IOException e)
      {
        datei.setTransferErolgreich(false);
        throw new JobException(e.getMessage(), e);
      }
    }
  }

  private void sendFTP(TransferDaten td, List<DateiTransfer> dateien)
  {
    FTP ftp = new FTP();
    String server = td.getHost();
    String user = td.getUser();
    String passwort = td.getPasswort();
    String account = td.getAccount();
    int port = 21;
    boolean isUnix = (td.getPlattformInt() == AenderungsTransferDaten.PLATTFORM_UNIX_FTP);
    try
    {
      if (isUnix)
      {
        ftp.connect(server, port, user, passwort);
      }
      else
      {
        ftp.connect(server, port, user, passwort, account);
      }
    }
    catch (TransferException e)
    {
      log.error(e.getMessage());
      return;
    }
    for (DateiTransfer datei : dateien)
    {
      String zielDatei = datei.getZielVerzeichnis() + "/" + datei.getZielDateiname();
      log.info(MessageFormat.format(MSG_UEBERTRAGE_DATEI, datei.getQuellPfad().getFileName(), "Host", td.getHost(), "FTP", zielDatei));
      try
      {
        boolean ok = ftp.storeFileSimple(datei.getQuellPfad().toFile(), datei.getZielDateiname(), datei.getZielVerzeichnis(), td.getModus());
        if (ok)
        {
          log.info(MessageFormat.format(MSG_UEBERTRAGUNG_KORREKT, datei.getZielDateiname(), "FTP", datei.getZielVerzeichnis(), server));
          datei.setTransferErolgreich(true);
        }
      }
      catch (TransferException e)
      {
        log.error(MessageFormat.format(MSG_UEBERTRAGUNG_FEHLER, datei.getZielDateiname(), "FTP", datei.getZielVerzeichnis(), server, e.getMessage()));
        datei.setTransferErolgreich(false);
      }
    }
    ftp.disconnect();
  }

  private void sendSFTP(TransferDaten td, List<DateiTransfer> dateien)
  {
    SFTP sftp;
    String server = td.getHost();
    String user = td.getUser();
    String passwort = td.getPasswort();
    boolean isUnix = (td.getPlattformInt() == AenderungsTransferDaten.PLATTFORM_UNIX_FTP || td.getPlattformInt() == AenderungsTransferDaten.PLATTFORM_UNIX_SFTP);
    if (!isUnix)
    {
      log.error("Uebertragung auf einen Host mittels SFTP nicht moeglich");
      return;
    }
    sftp = new SFTP();
    try
    {
      if (StringUtil.notEmpty(this.knownHostDatei))
      {
        sftp.setKnownHostFile(this.knownHostDatei);
      }
      if (this.disableHostKeyCheck)
      {
        sftp.disableHostKeyChecking();
      }
      sftp.connect(server, user, passwort);
    }
    catch (TransferException e)
    {
      log.error("Konnte nicht an Server " + server + " anmelden:" + e.getMessage());
      return;
    }
    for (DateiTransfer datei : dateien)
    {
      String zielDatei = datei.getZielVerzeichnis() + "/" + datei.getZielDateiname();
      log.info(MessageFormat.format(MSG_UEBERTRAGE_DATEI, datei.getQuellPfad().getFileName(), "Host", td.getHost(), "FTP", zielDatei));
      try
      {
        sftp.storeFileSimple(datei.getQuellPfad().toString(), datei.getZielVerzeichnis(), datei.getZielDateiname());
        datei.setTransferErolgreich(true);
        log.info(MessageFormat.format(MSG_UEBERTRAGUNG_KORREKT, datei.getZielDateiname(), "SFTP", datei.getZielVerzeichnis(), server));
      }
      catch (TransferException e)
      {
        log.error(MessageFormat.format(MSG_UEBERTRAGUNG_FEHLER, datei.getZielDateiname(), "SFTP", datei.getZielVerzeichnis(), server, e.getMessage()));
        datei.setTransferErolgreich(false);
      }
    }
    sftp.disconnect();
  }

  private void doStatusOk(String sql, int aenderungsArt, Set<Integer> ids)
  {
    if (ids == null || ids.isEmpty())
    {
      return;
    }
    try
    {
      this.sqlUtil.update(MessageFormat.format(sql, "" + aenderungsArt, this.sqlUtil.convertNumberList(ids)));
    }
    catch (JobException e)
    {
      log.error(e.getMessage(), e);
    }
  }

  private void erzeugeExportDatei(Set<Aenderung> aenderungen, TransferDaten transferDaten) throws JobException
  {
    String dateiname = "IDEV." + (transferDaten.isSammel() ? "SAMMEL" : "") + transferDaten.getAktion() + "." + this.amt + "." + transferDaten.getStatistikId() + "." + LocalDateTime.now().format(this.sdf) + ".csv";
    String prefix = "AEND." + this.amt + "." + transferDaten.getStatistikId() + ".";
    DateiTransfer dateiTransfer = new DateiTransfer();
    dateiTransfer.setZielDateiname(dateiname);
    dateiTransfer.setZielVerzeichnis(transferDaten.getZielverzeichnis());
    // Erzeuge Datei und speichere die Eintraege als CSV
    Path path;
    try
    {
      path = Files.createTempFile(Paths.get(this.tempVerzeichnis), prefix, ".csv");
      dateiTransfer.setQuellPfad(path);
      transferDaten.getDateiTransferList().add(dateiTransfer);
      log.debug("Erzeuge datei " + path + " mit " + aenderungen.size() + " Eintraegen");
    }
    catch (Exception e)
    {
      throw new JobException(e.getMessage(), e);
    }
    try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName(this.zielZeichensatz)); CSVWriter writer = new CSVWriter(bw, ';', '"'))
    {
      if (this.exportMitUeberschrift)
      {
        writer.writeNext(transferDaten.getExportSpaltenAsArray());
      }
      for (Aenderung ae : aenderungen)
      {
        writer.writeNext(ae.getValuesAsArray());
      }
      writer.flush();
    }
    catch (IOException e)
    {
      throw new JobException("Fehler beim Erstellen der Exportdatei:" + e.getMessage(), e);
    }
  }

  private Set<Aenderung> ermittleAenderungen(TransferDaten td) throws JobException
  {
    String[] spalten = td.getExportSpalten().split("[,;]");
    Set<Aenderung> aenderungSet = new HashSet<>();
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_AENDERUNGEN_DIREKT))
    {
      ermittleAenderungenAbfrage(td, spalten, aenderungSet, ps);
    }
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_AENDERUNGEN_OHNE_AMT))
    {
      ps.addValue(td.getAmt());
      ps.addValue(td.getStatistikId());
      ermittleAenderungenAbfrage(td, spalten, aenderungSet, ps);
    }
    return aenderungSet;
  }

  private void ermittleAenderungenAbfrage(TransferDaten td, String[] spalten, Set<Aenderung> aenderungSet, PreparedSelect ps) throws JobException
  {
    ps.addValue(td.getAmt());
    ps.addValue(td.getStatistikId());
    ps.addValue(td.isMelderAenderung() ? "AEND_MELDER" : "AEND_AUSKUNFTPFL");
    ps.addValue(td.getAenderungsart());
    ps.addValue(td.getAenderungsart());
    ps.addValue(this.anzahlAenderungenErlaubt);
    List<ResultRow> rows = ps.fetchMany();
    createAenderungen(spalten, aenderungSet, rows);
  }

  private void createAenderungen(String[] spalten, Set<Aenderung> aenderungsSet, List<ResultRow> rows)
  {
    for (ResultRow row : rows)
    {
      if (aenderungsSet.size() >= this.anzahlAenderungenErlaubt)
      {
        break;
      }
      Aenderung aenderung = new Aenderung(spalten);
      aenderung.convertResultset(row);
      aenderungsSet.add(aenderung);
    }
  }
}
