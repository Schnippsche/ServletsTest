package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AenderungsTransferDaten
{
  public static final int PLATTFORM_LOKAL = 1;
  public static final int PLATTFORM_UNIX_FTP = 2;
  public static final int PLATTFORM_UNIX_SFTP = 3;
  public static final int PLATTFORM_HOST_FTP = 4;
  public static final int PLATTFORM_HOST_SFTP = 5;
  public static final int FORM_UNKNOWN = 0;
  public static final int FORM_EINZEL = 1;
  public static final int FORM_SAMMEL = 2;
  private static final String AKTION = "AKTION";
  private static final String TRANSFER_HOST = "TRANSFER_HOST";
  private static final String TRANSFER_FORM = "TRANSFER_FORM";
  private static final String TRANSFER_PLATTFORM = "TRANSFER_PLATTFORM";
  private static final String TRANSFER_USER = "TRANSFER_USER";
  private static final String TRANSFER_PASSWORT = "TRANSFER_PASSWORT";
  private static final String TRANSFER_ACCOUNT = "TRANSFER_ACCOUNT";
  private static final String TRANSFER_ZIEL_VERZEICHNIS = "TRANSFER_ZIEL_VERZEICHNIS";
  private static final String TRANSFER_MODUS = "TRANSFER_MODUS";
  private static final String TRANSFER_MAIL_EMPFAENGER = "TRANSFER_MAIL_EMPFAENGER";
  private static final String TRANSFER_MAIL_ABSENDER = "TRANSFER_MAIL_ABSENDER";
  private static final String TRANSFER_MAIL_BETREFF = "TRANSFER_MAIL_BETREFF";
  private static final String TRANSFER_MAIL_TEXT = "TRANSFER_MAIL_TEXT";
  private static final String KONVERTER = "KONVERTER";
  private static final String KONVERTER_OPTIONEN = "KONVERTER_OPTIONEN";
  private static final int PLATTFORM_UNKNOWN = 0;
  public static LoggerIfc log;
  // Daten aus Tabelle Aenderung
  private final String amt;
  private final int statistikId;
  private final String typ;
  private String aktion;
  // Weitere Felder
  private String exportids;
  private String direkteintragids;
  private String filedatum;
  private String mailFileText;
  private String errormail;
  private File zipContainerFile;
  private File csvFile;
  private int aenderungsart;
  private String warnung;
  private ErgebnisStatus statusHolen;
  private ErgebnisStatus statusVerteilen;
  private ErgebnisStatus statusMailen;
  // Daten aus Tabelle Transfer
  private String host;
  private String form;
  private String plattform;
  private String user;
  private String passwort;
  private String account;
  private String zielverzeichnis;
  private String modus;
  private String mailempfaenger;
  private String mailabsender;
  private String mailbetreff;
  private String mailtext;
  private String konverter;
  private String konverteroptionen;
  private String exportSpalten;
  private HashMap<String, String> defaultValues;

  public AenderungsTransferDaten(String amt, int statistikId, String typ)
  {
    log = Logger.getInstance().getLogger(this.getClass());
    this.amt = amt;
    this.statistikId = statistikId;
    this.defaultValues = new HashMap<>();
    this.typ = typ;
    this.aktion = null;
    this.zipContainerFile = null;
    this.exportids = "0";
    this.direkteintragids = "0";
    this.aenderungsart = 15;
    this.exportSpalten = null;
    this.warnung = null;
  }

  public void setStandardValues(HashMap<String, String> config)
  {
    this.defaultValues = (config != null) ? config : new HashMap<>();
  }

  public void setTransferdatenFromResult(ResultRow rs)
  {
    if (rs != null)
    {
      // Transferdaten
      this.aktion = rs.getString(AKTION);
      this.host = rs.getString(TRANSFER_HOST);
      this.form = rs.getString(TRANSFER_FORM);
      this.plattform = rs.getString(TRANSFER_PLATTFORM);
      this.user = rs.getString(TRANSFER_USER);
      this.passwort = rs.getString(TRANSFER_PASSWORT);
      this.account = rs.getString(TRANSFER_ACCOUNT);
      this.zielverzeichnis = rs.getString(TRANSFER_ZIEL_VERZEICHNIS);
      this.modus = rs.getString(TRANSFER_MODUS);
      this.mailempfaenger = rs.getString(TRANSFER_MAIL_EMPFAENGER);
      this.mailabsender = rs.getString(TRANSFER_MAIL_ABSENDER);
      this.mailbetreff = rs.getString(TRANSFER_MAIL_BETREFF);
      this.mailtext = rs.getString(TRANSFER_MAIL_TEXT);
      this.konverter = rs.getString(KONVERTER);
      this.konverteroptionen = rs.getString(KONVERTER_OPTIONEN);
    }
  }

  public File getZipContainerFile()
  {
    return this.zipContainerFile;
  }

  public void setZipContainerFile(File f)
  {
    this.zipContainerFile = f;
    if (f != null && f.exists())
    {
      String tmp = f.getName();
      if (tmp.length() > 19)
      {
        this.filedatum = tmp.substring(tmp.length() - 24, tmp.length() - 9);
      }
    }
  }

  public String getAccount()
  {
    return getValue(this.account, TRANSFER_ACCOUNT);
  }

  public void setAccount(String account)
  {
    this.account = account;
  }

  public String getForm()
  {
    return getValue(this.form, TRANSFER_FORM);
  }

  public void setForm(String form)
  {
    this.form = form;
  }

  public String getHost()
  {
    return getValue(this.host, TRANSFER_HOST);
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  public String getKonverter()
  {
    return getValue(this.konverter, KONVERTER);
  }

  public void setKonverter(String konverter)
  {
    this.konverter = konverter;
  }

  public String getKonverteroptionen()
  {
    return getValue(this.konverteroptionen, KONVERTER_OPTIONEN);
  }

  public void setKonverteroptionen(String konverteroptionen)
  {
    this.konverteroptionen = konverteroptionen;
  }

  public String getMailabsender()
  {
    return getValue(this.mailabsender, TRANSFER_MAIL_ABSENDER);
  }

  public void setMailabsender(String mailabsender)
  {
    this.mailabsender = mailabsender;
  }

  public String getMailbetreff()
  {
    return getValue(this.mailbetreff, TRANSFER_MAIL_BETREFF);
  }

  public void setMailbetreff(String mailbetreff)
  {
    this.mailbetreff = mailbetreff;
  }

  public String getMailempfaenger()
  {
    return getValue(this.mailempfaenger, TRANSFER_MAIL_EMPFAENGER);
  }

  public void setMailempfaenger(String mailempfaenger)
  {
    this.mailempfaenger = mailempfaenger;
  }

  public String getMailtext()
  {
    return getValue(this.mailtext, TRANSFER_MAIL_TEXT);
  }

  public void setMailtext(String mailtext)
  {
    this.mailtext = mailtext;
  }

  public String getModus()
  {
    return getValue(this.modus, TRANSFER_MODUS);
  }

  public void setModus(String modus)
  {
    this.modus = modus;
  }

  public String getPasswort()
  {
    return getValue(this.passwort, TRANSFER_PASSWORT);
  }

  public void setPasswort(String passwort)
  {
    this.passwort = passwort;
  }

  public String getPlattform()
  {
    return getValue(this.plattform, TRANSFER_PLATTFORM);
  }

  public void setPlattform(String plattform)
  {
    this.plattform = plattform;
  }

  public int getPlattformInt()
  {
    String pf = getPlattform();
    if ("LOKAL".equals(pf))
    {
      return PLATTFORM_LOKAL;
    }
    if ("UNIXFTP".equals(pf))
    {
      return PLATTFORM_UNIX_FTP;
    }
    if ("UNIXSFTP".equals(pf))
    {
      return PLATTFORM_UNIX_SFTP;
    }
    if ("HOSTFTP".equals(pf))
    {
      return PLATTFORM_HOST_FTP;
    }
    if ("HOSTSFTP".equals(pf))
    {
      return PLATTFORM_HOST_SFTP;
    }
    return PLATTFORM_UNKNOWN;
  }

  public int getFormInt()
  {
    String f = getForm();
    if ("EINZEL".equals(f))
    {
      return FORM_EINZEL;
    }
    if ("SAMMEL".equals(f))
    {
      return FORM_SAMMEL;
    }
    return FORM_UNKNOWN;
  }

  public boolean isSammelMeldung()
  {
    return "SAMMEL".equals(getForm());
  }

  public String getUser()
  {
    return getValue(this.user, TRANSFER_USER);
  }

  public void setUser(String user)
  {
    this.user = user;
  }

  public String getExportSpalten()
  {
    return this.exportSpalten;
  }

  public void setExportSpalten(String exportSpalten)
  {
    this.exportSpalten = exportSpalten;
  }

  public String getZielverzeichnis()
  {
    return getValue(this.zielverzeichnis, TRANSFER_ZIEL_VERZEICHNIS);
  }

  public void setZielverzeichnis(String zielverzeichnis)
  {
    this.zielverzeichnis = zielverzeichnis;
  }

  public String getAmt()
  {
    return this.amt;
  }

  public int getStatistikId()
  {
    return this.statistikId;
  }

  public int getAenderungsart()
  {
    return this.aenderungsart;
  }

  public void setAenderungsart(int aenderungsart)
  {
    this.aenderungsart = aenderungsart;
  }

  /**
   * liefert bei leerem Wert den Default-Wert, ansonsten den Wert selbst
   *
   * @param value    der zu überprüfende Wert
   * @param standard der Standardwert
   * @return String Wert
   */
  private String getValue(String value, String standard)
  {
    if (value == null || "".equals(value))
    {
      return this.defaultValues.get(standard);
    }
    return value;
  }

  public String getErrorMail()
  {
    return getValue(this.errormail, "");
  }

  public void setErrorMail(String errorMail)
  {
    this.errormail = errorMail;
  }

  public String toString()
  {
    return " Amt:" + this.amt + " Statistik-ID:" + this.statistikId + " Aktion:" + this.aktion + " Aenderungsart:" + this.aenderungsart + " Typ:" + this.typ + " ZipContainer:" + this.zipContainerFile + " Host:" + getHost() + " Form:" + getForm() + " Plattform:" + getPlattform() + " User:" + getUser() + " Passwort:" + getPasswort() + " Account:" + getAccount() + " Ziel:" + getZielverzeichnis() + " Modus:" + getModus() + " MailEmpfaenger:" + getMailempfaenger() + " MailAbsender:" + getMailabsender() + " Betreff:" + getMailbetreff() + " Mailtext:" + getMailtext() + " Konverter:" + getKonverter() + " direktIDs:" + this.direkteintragids + " exportIDs:" + this.exportids;
  }

  public String getZielDateiName()
  {
    if (this.filedatum == null)
    {
      Date date = new Date();
      SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd.HHmmss");
      this.filedatum = fmt.format(date);
    }
    return "IDEV." + this.aktion + "." + this.amt + "." + this.statistikId + "." + this.filedatum + ".csv";
  }

  public ErgebnisStatus getHolenStatus()
  {
    return this.statusHolen;
  }

  public void setHolenStatus(ErgebnisStatus status)
  {
    this.statusHolen = status;
  }

  public ErgebnisStatus getVerteilenStatus()
  {
    return this.statusVerteilen;
  }

  public void setVerteilenStatus(ErgebnisStatus status)
  {
    this.statusVerteilen = status;
  }

  public ErgebnisStatus getMailStatus()
  {
    return this.statusMailen;
  }

  public void setMailStatus(ErgebnisStatus status)
  {
    this.statusMailen = status;
  }

  public String getMailFileText()
  {
    return this.mailFileText;
  }

  public void setMailFileText(String text)
  {
    this.mailFileText = text;
  }

  public File getCsvFile()
  {
    return this.csvFile;
  }

  public void setCsvFile(File file)
  {
    this.csvFile = file;
  }

  public String getTyp()
  {
    return this.typ;
  }

  public void setExportIdsFromFile(File file)
  {
    if (file != null && file.exists())
    {
      this.exportids = "0";
      try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8))
      {
        this.exportids = reader.readLine();
        if (this.exportids != null)
        {
          this.exportids = this.exportids.trim();
        }

      }
      catch (Exception e)
      {
        log.error("Fehler beim Lesen der ExportIds:" + e.getMessage());
      }
    }
  }

  public void setDirekteintragIdsFromFile(File file)
  {
    if (file != null && file.exists())
    {
      this.direkteintragids = "0";
      try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8))
      {
        this.direkteintragids = reader.readLine();
        if (this.direkteintragids != null)
        {
          this.direkteintragids = this.direkteintragids.trim();
        }
      }
      catch (Exception e)
      {
        log.error("Fehler beim Lesen der ExportIds:" + e.getMessage());
      }
    }
  }

  public String getExportIds()
  {
    return this.exportids;
  }

  public String getDirekteintragIds()
  {
    return this.direkteintragids;
  }

  public void setIds(String ids)
  {
    this.exportids = ids;
  }

  public boolean isDirekteintrag()
  {
    return (this.aktion != null && (this.aktion.endsWith("_DIREKTEINTRAG") || this.aktion.endsWith("_BEIDES")));
  }

  public boolean isDateiExport()
  {
    return (this.aktion != null && (this.aktion.endsWith("_DATEIEXPORT") || this.aktion.endsWith("_BEIDES")));
  }

  public String getWarnung()
  {
    return this.warnung;
  }

  public void setWarnung(String warnung)
  {
    this.warnung = warnung;
  }
}
