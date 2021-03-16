package de.destatis.regdb.aenderungen;

import de.destatis.regdb.Email;
import de.destatis.regdb.FTP;
import de.destatis.regdb.SFTP;
import de.destatis.regdb.db.*;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The type Aenderungen verteilen.
 */
public class AenderungenVerteilen
{
  private static final String SQL_UPDATE_STATUS_DIREKTEINTRAG = "UPDATE aenderung SET STATUS=\"BEARBEITET\", ZEITPUNKT_EXPORT = NOW(), STATUS_DIREKTEINTRAG = (STATUS_DIREKTEINTRAG | {0}) WHERE AENDERUNG_ID IN({1})";
  private static final String SQL_UPDATE_STATUS_EXPORT = "\"UPDATE aenderung SET STATUS='BEARBEITET', ZEITPUNKT_EXPORT = NOW(), STATUS_EXPORT_AENDERUNG = (STATUS_EXPORT_AENDERUNG | {0} WHERE AENDERUNG_ID IN({1})";
  private static final String TRENNLINIE = "------------------------------------------------------------";
  private static final String SQL_SELECT_STATISTIKEN_MIT_TRANSFERZIEL = "SELECT statistiken_amt.AMT,statistiken_amt.STATISTIK_ID FROM statistiken_amt INNER JOIN transfer USING(AMT,STATISTIK_ID) WHERE statistiken_amt.AMT = \"{0}\" AND transfer.AKTION != \"MELDUNGS_TRANSFER\" AND transfer.STATUS != \"LOESCH\" AND statistiken_amt.STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_TRANSFERZIEL = "SELECT transfer.AKTION, CAST(transfer.AENDERUNGSART AS UNSIGNED) AS AENDERUNGSARTVALUE, transferziel.* FROM transfer INNER JOIN transferziel USING(TRANSFERZIEL_ID) WHERE AMT=? AND STATISTIK_ID=? AND AKTION != \"MELDUNGS_TRANSFER\" AND transfer.STATUS != \"LOESCH\" AND transferziel.STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_STANDARD_KONFIGURATION = "SELECT UCASE(KONFIGURATION_ID) AS ID, WERT_STRING FROM konfiguration WHERE STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_STADNDARD_ERRORMAIL = "SELECT WERT from standardwerte where KONFIGURATION_ID=\"Aenderungstransfer_error_mail\" and KEY1=\"{0}\" and (KEY2 = \"0\" OR KEY2=\"{1}\")";
  private static final String SQL_SELECT_SACHBEARBEITER = "SELECT SACHBEARBEITER_ID FROM sachbearbeiter WHERE kennung=\"{0}\" AND status != \"LOESCH\"";
  private static final LoggerIfc log = Logger.getInstance().getLogger(AenderungenVerteilen.class);
  private static final String MSG_UEBERTRAGE_DATEI = "uebertrage Datendatei {0} nach {1} {2} per {3}, Zielname = {4}";
  private final SqlUtil sqlUtil;
  private final String amt;
  private final ArrayList<AenderungsTransferDaten> aenderungsTransferDatenList;
  private final String sbKennung;
  private String zielZeichensatz;
  private HashMap<String, String> defaultKonfigurationMap;
  private int sbId;
  private String knownHostDatei;
  private boolean disableHostKeyCheck;
  private boolean exportMitUeberschrift;
  /**
   * Instantiates a new Aenderungen verteilen.
   *
   * @param connection the connection
   * @param amt        the amt
   * @param kennung    the kennung
   */
  public AenderungenVerteilen(Connection connection, String amt, String kennung)
  {
    this.sqlUtil = new SqlUtil(connection);
    this.aenderungsTransferDatenList = new ArrayList<>();
    this.amt = amt;
    this.sbKennung = kennung;
    this.zielZeichensatz = StandardCharsets.ISO_8859_1.name();
    this.disableHostKeyCheck = true;
    this.knownHostDatei = null;
    this.exportMitUeberschrift = false;
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
    ResultRow row = this.sqlUtil.fetchOne(MessageFormat.format(SQL_SELECT_SACHBEARBEITER, StringUtil.escapeSqlString(this.sbKennung)));
    this.sbId = (row != null) ? row.getInt(1) : 0;
  }

  /**
   * Ermittle transferziele boolean.
   *
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean ermittleTransferziele() throws JobException
  {
    this.aenderungsTransferDatenList.clear();
    this.defaultKonfigurationMap = ermittleStandardKonfiguration();
    ermittleSachbearbeiter();
    List<ResultRow> rows = this.sqlUtil.fetchMany(MessageFormat.format(SQL_SELECT_STATISTIKEN_MIT_TRANSFERZIEL, this.amt));
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_TRANSFERZIEL))
    {
      for (ResultRow row : rows)
      {
        String tmpAmt = row.getString("AMT");
        int statistikId = row.getInt("STATISTIK_ID");
        ps.addValue(tmpAmt);
        ps.addValue(statistikId);
        List<ResultRow> ziele = ps.fetchMany();
        for (ResultRow rr : ziele)
        {
          ermittleTransferdaten(rr, tmpAmt, statistikId);
        }
      }
    }
    return !this.aenderungsTransferDatenList.isEmpty();
  }

  /**
   * Verteile aenderungen.
   */
  public void verteileAenderungen()
  {
    for (AenderungsTransferDaten atd : this.aenderungsTransferDatenList)
    {
      if (atd.isDateiExport())
      {
        holeAenderungsDatei(atd);
        if (atd.getZipContainerFile() != null)
        {
          verteileDatei(atd);
        }
      } else
      {
        atd.setHolenStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_OK));
      }
    }
  }

  private void holeAenderungsDatei(AenderungsTransferDaten atd)
  {
    AenderungenHolen aenderungenHolen = new AenderungenHolen("" + this.sbId, this.sbKennung, atd.getAmt(), "" + atd.getStatistikId(), atd.getAenderungsart(), atd.getTyp(), this.sqlUtil.getConnection());
    aenderungenHolen.setClient("localhost");
    try
    {
      atd.setHolenStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_FEHLER));
      aenderungenHolen.validate();
      File zipContainerFile = aenderungenHolen.starteVerarbeitung(atd.getExportSpalten(), exportMitUeberschrift);
      if (zipContainerFile != null)
      {
        atd.setZipContainerFile(zipContainerFile);
        atd.setHolenStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_OK));
      }
    } catch (AenderungenHolenException e)
    {
      log.error(e.getMessage());
    }
  }

  private void ermittleTransferdaten(ResultRow row, String amt, int statistikId) throws JobException
  {
    String typ = row.getString("AKTION");
    if (typ.endsWith("_DATEIEXPORT"))
    {
      typ = typ.substring(0, typ.length() - 12);
    } else if (typ.endsWith("_DIREKTEINTRAG"))
    {
      typ = typ.substring(0, typ.length() - 14);
    } else if (typ.endsWith("_BEIDES"))
    {
      typ = typ.substring(0, typ.length() - 7);
    }
    // Welche Spalten sollen ausgegeben werden ?
    // nimm die Spalten aus Spalte AENDERUNGS_EXPORT_SPALTEN aus Tabelle Transferziel
    // falls nein, nimm alle Spalten
    String spalten = row.getString("AENDERUNGS_EXPORT_SPALTEN");
    int aenderungsart = row.getInt("AENDERUNGSARTVALUE");
    if (aenderungsart == 0)
    {
      aenderungsart = 15;
    }
    AenderungsTransferDaten atd = new AenderungsTransferDaten(amt, statistikId, typ);
    atd.setStandardValues(this.defaultKonfigurationMap);
    atd.setTransferdatenFromResult(row);
    atd.setAenderungsart(aenderungsart);
    atd.setExportSpalten(spalten);
    checkStandardErrorMail(atd, amt, statistikId);
    this.aenderungsTransferDatenList.add(atd);
  }

  private void checkStandardErrorMail(AenderungsTransferDaten atd, String amt, int statistikId) throws JobException
  {
    // Prüfe ob eine Error-Mailadresse in Standardwerte eingetragen ist und setze sie evtl.
    ResultRow row = this.sqlUtil.fetchOne(MessageFormat.format(SQL_SELECT_STADNDARD_ERRORMAIL, amt, "" + statistikId));
    if (row != null)
    {
      atd.setErrorMail(row.getString(1).trim());
    }
  }

  private void verteileDatei(AenderungsTransferDaten atd)
  {
    File zipContainerFile = atd.getZipContainerFile();
    File datenFile = null;
    ErgebnisStatus erg;
    try (ZipFile sourceFile = new ZipFile(zipContainerFile))
    {
      // Validierungen
      if (atd.getForm() == null)
      {
        throw new JobException("fuer die Aenderung ist kein Transferziel hinterlegt!");
      }
      if (atd.getHost() == null)
      {
        throw new JobException("fuer die Aenderung ist kein Host hinterlegt!");
      }
      erg = atd.getHolenStatus();
      if (erg.getStatus() != ErgebnisStatus.STATUS_OK)
      {
        throw new JobException("keine Aenderungen zum Verteilen da");
      }
      // Entzippe Datei temporär
      Enumeration<? extends ZipEntry> en = sourceFile.entries();
      while (en.hasMoreElements())
      {
        ZipEntry entry = en.nextElement();
        InputStream in = sourceFile.getInputStream(entry);
        if ("daten".equalsIgnoreCase(entry.getName()))
        {
          datenFile = verarbeiteDatenDatei(atd, in);
        }
        if ("ids".equalsIgnoreCase(entry.getName()))
        {
          verarbeiteIdFile(atd, in);
        }
        if ("info".equalsIgnoreCase(entry.getName()))
        {
          verarbeiteMailFile(atd, in);
        }
      }
      verarbeitePlattform(atd, datenFile);
      erg = new ErgebnisStatus(ErgebnisStatus.STATUS_OK);
      atd.setVerteilenStatus(erg);
    } catch (Exception e)
    {
      String logTxt = "Verteilung ";
      if (datenFile != null)
      {
        logTxt += "von " + datenFile;
      }
      logTxt += " fehlgeschlagen: " + e.getMessage();
      erg = new ErgebnisStatus(ErgebnisStatus.STATUS_FEHLER, logTxt);
      atd.setVerteilenStatus(erg);
      log.error(logTxt);
      sendErrorMail(atd, logTxt);
    }
    // Lösche übertragene Datei
    FileUtil.delete(datenFile);
    FileUtil.delete(zipContainerFile);
  }

  private void sendErrorMail(AenderungsTransferDaten atd, String mailText)
  {
    String errorMailEmpfaenger = atd.getErrorMail();
    if (StringUtil.notEmpty(errorMailEmpfaenger))
    {
      String emailAbsender = atd.getErrorMail();
      String emailBetreff = "Fehler beim IDEV Aenderungstransfer";
      Email email = new Email(errorMailEmpfaenger, emailAbsender, emailBetreff, mailText);
      MailVersandDaemon.getInstance().sendMail(email);
    }
  }

  /**
   * Versende mails.
   */
  public void versendeMails()
  {
    for (AenderungsTransferDaten atd : this.aenderungsTransferDatenList)
    {
      ErgebnisStatus erg = atd.getVerteilenStatus();
      // Nur Mail verschicken wenn was transferiert wurde
      if (erg != null && erg.getStatus() == ErgebnisStatus.STATUS_OK)
      {
        sendeMail(atd);
      }
    }
  }

  private void sendeMail(AenderungsTransferDaten atd)
  {
    String emailBetreff = atd.getMailbetreff();
    if (StringUtil.isEmpty(emailBetreff))
      emailBetreff = "IDEV-Automatische Aenderungsverteilung - Meldungen via Internet";
    String mailText = StringUtil.isEmpty(atd.getMailtext()) ? "Hallo, fuer Sie ist eine Aenderung eingegangen !" : atd.getMailtext();
    mailText += "\n\nInhalt der Zieldatei " + atd.getZielDateiName() + ":\n" + TRENNLINIE + "\n" + atd.getMailFileText() + TRENNLINIE;
    if (StringUtil.notEmpty(atd.getWarnung()))
      mailText += "\n" + atd.getWarnung();
    Email email = new Email(atd.getMailempfaenger(), atd.getMailabsender(), emailBetreff, mailText);
    if (MailVersandDaemon.getInstance().sendMail(email))
    {
      atd.setMailStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_OK));
      log.info("Mail von Aenderung wurde an " + atd.getMailempfaenger() + " versendet");
    } else
    {
      atd.setMailStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_FEHLER));
      log.info("Mail von Aenderung konnte nicht an " + atd.getMailempfaenger() + " versendet werden!");
    }
  }

  private void verarbeitePlattform(AenderungsTransferDaten atd, File datenFile) throws IOException, JobException
  {
    String zielVerzeichnis = atd.getZielverzeichnis();
    if (!"".equals(zielVerzeichnis))
    {
      zielVerzeichnis = zielVerzeichnis.replace("\\", "/");
      if (zielVerzeichnis.endsWith("/"))
      {
        zielVerzeichnis = zielVerzeichnis.substring(0, zielVerzeichnis.length() - 1);
      }
    }
    String zielDatei = zielVerzeichnis + "/" + atd.getZielDateiName();
    int plattForm = atd.getPlattformInt();
    switch (plattForm)
    {
      case AenderungsTransferDaten.PLATTFORM_LOKAL:
        log.info("kopiere Datendatei " + datenFile.getName() + " lokal nach " + zielDatei);
        Files.copy(datenFile.toPath(), Paths.get(zielDatei), StandardCopyOption.REPLACE_EXISTING);
        break;
      case AenderungsTransferDaten.PLATTFORM_HOST_FTP:
        log.info(MessageFormat.format(MSG_UEBERTRAGE_DATEI, datenFile.getName(), "Host", atd.getHost(), "FTP", zielDatei));
        sendFTP(atd, zielVerzeichnis);
        break;
      case AenderungsTransferDaten.PLATTFORM_HOST_SFTP:
        log.info(MessageFormat.format(MSG_UEBERTRAGE_DATEI, datenFile.getName(), "Host", atd.getHost(), "SFTP", zielDatei));
        sendSFTP(atd, zielVerzeichnis);
        break;
      case AenderungsTransferDaten.PLATTFORM_UNIX_FTP:
        log.info(MessageFormat.format(MSG_UEBERTRAGE_DATEI, datenFile.getName(), "Unix", atd.getHost(), "FTP", zielDatei));
        sendFTP(atd, zielVerzeichnis);
        break;
      case AenderungsTransferDaten.PLATTFORM_UNIX_SFTP:
        log.info(MessageFormat.format(MSG_UEBERTRAGE_DATEI, datenFile.getName(), "Unix", atd.getHost(), "SFTP", zielDatei));
        sendSFTP(atd, zielVerzeichnis);
        break;
      default:
        log.info("Keine Aktion für Plattform " + plattForm);
    }
  }

  private File verarbeiteDatenDatei(AenderungsTransferDaten atd, InputStream in) throws IOException, JobException
  {
    File datenFile = File.createTempFile("daten", ".csv");
    ErgebnisStatus copyStatus = FileUtil.copyUTF8StreamToExportFile(in, datenFile, this.zielZeichensatz);
    if (copyStatus.isFehler())
    {
      FileUtil.delete(datenFile);
      throw new JobException(copyStatus.getMeldung());
    } else
    {
      atd.setCsvFile(datenFile);
      if (copyStatus.isWarnung())
      {
        log.debug("Exportdatei wurde mit Verlust in " + this.zielZeichensatz + " konvertiert, Amt=" + atd.getAmt() + ", Statistik-id=" + atd.getStatistikId());
        atd.setWarnung("Ausgabedatei wurde mit Verlust in den Zielzeichensatz " + this.zielZeichensatz + " konvertiert!");
      }
    }
    return datenFile;
  }

  private void verarbeiteIdFile(AenderungsTransferDaten atd, InputStream in) throws IOException, JobException
  {
    File idFile = File.createTempFile("ids", ".txt");
    FileOutputStream out = new FileOutputStream(idFile);
    FileUtil.copyFromStream(in, out);
    atd.setExportIdsFromFile(idFile);
    FileUtil.delete(idFile);
  }

  private void verarbeiteMailFile(AenderungsTransferDaten atd, InputStream in) throws IOException, JobException
  {
    File mailFile = File.createTempFile("info", ".txt");
    FileOutputStream out = new FileOutputStream(mailFile);
    FileUtil.copyFromStream(in, out);
    String mailText = new String(Files.readAllBytes(mailFile.toPath()), StandardCharsets.UTF_8);
    atd.setMailFileText(mailText);
    FileUtil.delete(mailFile);
  }

  /**
   * sendet per FTP an HOST
   *
   * @param atd                AenderungsTransferDaten
   * @param ftpZielverzeichnis das Zielverzeichnis
   */
  private void sendFTP(AenderungsTransferDaten atd, String ftpZielverzeichnis) throws JobException
  {
    FTP ftp = new FTP();
    String server = atd.getHost();
    String user = atd.getUser();
    String passwort = atd.getPasswort();
    String account = atd.getAccount();
    int port = 21;
    boolean isUnix = (atd.getPlattformInt() == AenderungsTransferDaten.PLATTFORM_UNIX_FTP);
    boolean ok;
    if (isUnix)
    {
      ok = ftp.connect(server, port, user, passwort);
    } else
    {
      ok = ftp.connect(server, port, user, passwort, account);
    }
    if (!ok)
    {
      throw new JobException("Anmeldung an Server " + server + " mit Kennung und Passwort nicht erfolgreich!");
    }
    ok = ftp.storeFileSimple(atd.getCsvFile(), atd.getZielDateiName(), ftpZielverzeichnis, atd.getModus());
    if (ok)
    {
      log.info("Datei: -" + atd.getZielDateiName() + "- wurde korrekt mittels FTP in das Verzeichnis -" + ftpZielverzeichnis + "- des Servers -" + server + "- uebertragen.");
    }
    ftp.disconnect();
  }

  private void sendSFTP(AenderungsTransferDaten mtd, String sftpZielverzeichnis) throws JobException
  {
    SFTP sftp;
    String server = mtd.getHost();
    String user = mtd.getUser();
    String passwort = mtd.getPasswort();
    boolean isUnix = (mtd.getPlattformInt() == AenderungsTransferDaten.PLATTFORM_UNIX_FTP || mtd.getPlattformInt() == AenderungsTransferDaten.PLATTFORM_UNIX_SFTP);
    if (!isUnix)
    {
      throw new JobException("Uebertragung auf einen Host mittels SFTP nicht moeglich");
    }
    sftp = new SFTP();
    if (StringUtil.notEmpty(this.knownHostDatei))
    {
      sftp.setKnownHostFile(this.knownHostDatei);
    }
    if (this.disableHostKeyCheck)
    {
      sftp.disableHostKeyChecking();
    }
    sftp.connect(server, user, passwort);
    sftp.storeFileSimple(mtd.getCsvFile().getAbsolutePath(), sftpZielverzeichnis, mtd.getZielDateiName());
    log.info("Datei: -" + mtd.getZielDateiName() + "- wurde korrekt mittels FTP in das Verzeichnis -" + sftpZielverzeichnis + "- des Servers -" + server + "- uebertragen.");
    sftp.disconnect();
  }

  /**
   * Mache direkteintraege.
   */
  public void macheDirekteintraege()
  {
    for (AenderungsTransferDaten atd : this.aenderungsTransferDatenList)
    {
      if (atd.isDirekteintrag())
        doDirekteintrag(atd);
    }
  }

  private void doDirekteintrag(AenderungsTransferDaten atd)
  {
    try
    {
      AenderungenHolen aenderungenHolen = new AenderungenHolen("" + this.sbId, this.sbKennung, atd.getAmt(), "" + atd.getStatistikId(), atd.getAenderungsart(), atd.getTyp(), this.sqlUtil.getConnection());
      aenderungenHolen.setClient("localhost");
      aenderungenHolen.validateDirekteintrag();
      File file = aenderungenHolen.starteDirektEintrag(atd.getExportSpalten());
      if (file != null)
      {
        log.info("direktEintrag fuer Amt '" + atd.getAmt() + "', Statistik-id " + atd.getStatistikId() + ", Typ '" + atd.getTyp() + "', Aenderungsart=" + atd.getAenderungsart() + " durchgefuehrt!");
        atd.setDirekteintragIdsFromFile(file);
        atd.setVerteilenStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_OK));
        FileUtil.delete(file);
      }
    } catch (Exception e)
    {
      log.error("Direkteintrag von Amt '" + atd.getAmt() + "' , StatistikId '" + atd.getStatistikId() + "', Typ '" + atd.getTyp() + "' fehlgeschlagen:" + e.getMessage());
      atd.setVerteilenStatus(new ErgebnisStatus(ErgebnisStatus.STATUS_FEHLER));
    }
  }

  /**
   * Sets export status aenderungen.
   */
  public void setzeExportStatusAenderungen()
  {
    log.info("setze Status der fehlerfrei verarbeiteten Aenderungen auf EXPORT_V");

    ErgebnisStatus verteilt;
    ErgebnisStatus geholt;
    String korrekteAenderungen;
    for (AenderungsTransferDaten atd : this.aenderungsTransferDatenList)
    {
      verteilt = atd.getVerteilenStatus();
      geholt = atd.getHolenStatus();
      if (atd.isDateiExport() && verteilt != null && verteilt.isOK() && geholt != null && geholt.isOK())
      {
        korrekteAenderungen = atd.getExportIds();
        if (korrekteAenderungen.length() > 0)
          setzeStatusExportAenderung(korrekteAenderungen, atd.getAenderungsart());
      }
      if (atd.isDirekteintrag() && geholt != null && geholt.isOK())
      {
        korrekteAenderungen = atd.getDirekteintragIds();
        if (korrekteAenderungen.length() > 0)
          setzeStatusErledigt(korrekteAenderungen, atd.getAenderungsart());
      }
    }
  }

  private void setzeStatusErledigt(String aenderungsIds, int aenderungsart)
  {
    if (StringUtil.notEmpty(aenderungsIds))
    {
      String sql = MessageFormat.format(SQL_UPDATE_STATUS_DIREKTEINTRAG, "" + aenderungsart, aenderungsIds);
      try
      {
        this.sqlUtil.execute(sql);
        log.info("Aenderungs-ids von Direkteintrag aktualisiert:" + aenderungsIds);
      } catch (JobException e)
      {
        log.error("Fehler beim Umsetzen des Erledigt-Status der Aenderungen " + aenderungsIds + ":" + e.getMessage());
      }
    }
  }

  private void setzeStatusExportAenderung(String aenderungsIds, int aenderungsart)
  {
    if (aenderungsIds != null && aenderungsIds.length() > 0)
    {
      String sql = MessageFormat.format(SQL_UPDATE_STATUS_EXPORT, "" + aenderungsart, aenderungsIds);
      try
      {
        this.sqlUtil.execute(sql);
        log.info("Aenderungs-ids von Export aktualisiert:" + aenderungsIds);
      } catch (JobException e)
      {
        log.error("Fehler beim Umsetzen des ExportAenderung-Status der Aenderungen " + aenderungsIds + ":" + e.getMessage());
      }
    }
  }
}
