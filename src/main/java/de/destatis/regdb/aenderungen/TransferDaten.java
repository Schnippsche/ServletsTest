package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransferDaten
{
  public static final int PLATTFORM_UNKNOWN = 0;
  public static final int PLATTFORM_LOKAL = 1;
  public static final int PLATTFORM_UNIX_FTP = 2;
  public static final int PLATTFORM_UNIX_SFTP = 3;
  public static final int PLATTFORM_HOST_FTP = 4;
  public static final int PLATTFORM_HOST_SFTP = 5;
  private static final String TRANSFER_AKTION = "AKTION";
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
  private static final String TRANSFER_KONVERTER = "KONVERTER";
  private static final String TRANSFER_KONVERTER_OPTIONEN = "KONVERTER_OPTIONEN";
  private static final String AENDERUNGS_EXPORT_SPALTEN = "AENDERUNGS_EXPORT_SPALTEN";
  private final List<DateiTransfer> dateiTransferList;
  private int aenderungsart;
  // Daten aus Tabelle Aenderung
  private String amt;
  private int statistikId;
  private String aktion;
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
  private boolean direktEintragErfolgreich;
  private Set<Aenderung> aenderungen;

  public TransferDaten()
  {
    this.aenderungen = new HashSet<>();
    this.dateiTransferList = new ArrayList<>();
  }

  public TransferDaten(ResultRow rs)
  {
    this();
    this.amt = StringUtil.trim(rs.getString("AMT"));
    this.statistikId = rs.getInt("STATISTIK_ID");
    this.aktion = StringUtil.trim(rs.getString(TRANSFER_AKTION));
    this.host = StringUtil.trim(rs.getString(TRANSFER_HOST));
    this.form = StringUtil.trim(rs.getString(TRANSFER_FORM));
    this.plattform = StringUtil.trim(rs.getString(TRANSFER_PLATTFORM));
    this.user = StringUtil.trim(rs.getString(TRANSFER_USER));
    this.passwort = StringUtil.trim(rs.getString(TRANSFER_PASSWORT));
    this.account = StringUtil.trim(rs.getString(TRANSFER_ACCOUNT));
    this.zielverzeichnis = StringUtil.trim(rs.getString(TRANSFER_ZIEL_VERZEICHNIS));
    this.modus = StringUtil.trim(rs.getString(TRANSFER_MODUS));
    this.mailempfaenger = StringUtil.trim(rs.getString(TRANSFER_MAIL_EMPFAENGER));
    this.mailabsender = StringUtil.trim(rs.getString(TRANSFER_MAIL_ABSENDER));
    this.mailbetreff = StringUtil.trim(rs.getString(TRANSFER_MAIL_BETREFF));
    this.mailtext = StringUtil.trim(rs.getString(TRANSFER_MAIL_TEXT));
    this.konverter = StringUtil.trim(rs.getString(TRANSFER_KONVERTER));
    this.konverteroptionen = StringUtil.trim(rs.getString(TRANSFER_KONVERTER_OPTIONEN));
    this.exportSpalten = StringUtil.trim(rs.getString(AENDERUNGS_EXPORT_SPALTEN));
    this.aenderungsart = rs.getInt("AENDERUNGSARTVALUE");
    if (!"".equals(this.zielverzeichnis))
    {
      this.zielverzeichnis = this.zielverzeichnis.replace("\\", "/");
      if (this.zielverzeichnis.endsWith("/"))
      {
        this.zielverzeichnis = this.zielverzeichnis.substring(0, this.zielverzeichnis.length() - 1);
      }
    }
  }

  public String getAmt()
  {
    return this.amt;
  }

  public int getStatistikId()
  {
    return this.statistikId;
  }

  public boolean isMelderAenderung()
  {
    return (this.aktion.startsWith("AEND_MELDER"));
  }

  public String getAktion()
  {
    return this.aktion;
  }

  public String getHost()
  {
    return this.host;
  }

  public boolean isSammel()
  {
    return ("SAMMEL".equals(this.form));
  }

  public boolean isEinzel()
  {
    return ("EINZEL".equals(this.form));
  }

  public boolean isDirekteintrag()
  {
    return (this.aktion.endsWith("DIREKTEINTRAG") || this.aktion.endsWith("BEIDES"));
  }

  public int getAenderungsart()
  {
    return this.aenderungsart;
  }

  public void setAenderungsart(int aenderungsart)
  {
    this.aenderungsart = aenderungsart;
  }

  public boolean isDateiexport()
  {
    return (this.aktion.endsWith("DATEIEXPORT") || this.aktion.endsWith("BEIDES"));
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

  public String getPlattform()
  {
    return this.plattform;
  }

  public String getUser()
  {
    return this.user;
  }

  public String getPasswort()
  {
    return this.passwort;
  }

  public String getAccount()
  {
    return this.account;
  }

  public String getZielverzeichnis()
  {
    return this.zielverzeichnis;
  }

  public String getModus()
  {
    return this.modus;
  }

  public String getMailempfaenger()
  {
    return this.mailempfaenger;
  }

  public String getMailabsender()
  {
    return this.mailabsender;
  }

  public String getMailbetreff()
  {
    return this.mailbetreff;
  }

  public String getMailtext()
  {
    return this.mailtext;
  }

  public String getKonverter()
  {
    return this.konverter;
  }

  public String getKonverteroptionen()
  {
    return this.konverteroptionen;
  }

  public String getExportSpalten()
  {
    return this.exportSpalten;
  }

  public String[] getExportSpaltenAsArray()
  {
    String[] spalten = this.exportSpalten.split("[,;]");
    for (int i = 0; i < spalten.length; i++)
    {
      spalten[i] = spalten[i].trim();
    }
    return spalten;
  }

  public List<DateiTransfer> getDateiTransferList()
  {
    return this.dateiTransferList;
  }

  public boolean isDirektEintragErfolgreich()
  {
    return this.direktEintragErfolgreich;
  }

  public void setDirektEintragErfolgreich(boolean direktEintragErfolgreich)
  {
    this.direktEintragErfolgreich = direktEintragErfolgreich;
  }

  public Set<Aenderung> getAenderungen()
  {
    return this.aenderungen;
  }

  public void setAenderungen(Set<Aenderung> aenderungen)
  {
    this.aenderungen = aenderungen;
  }
}
