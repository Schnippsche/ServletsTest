package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AenderungenVerteilenNeu
{
  private static final String DIREKTEINTRAG_FEHLERTEXT = "Fehler beim Direkteintrag der {0} (ID={1}):";
  private static final String SQL_DIREKTEINTRAG = "UPDATE {0} SET {1}, SACHBEARBEITER_ID={2}, STATUS=\"AEND\", ZEITPUNKT_AENDERUNG = NOW() WHERE {3} = {4}";
  private static final String[] adressenSpalten = {"ANREDE", "NAME", "NAME_ERGAENZUNG", "KURZTEXT", "ABTEILUNG", "STRASSE", "HAUSNUMMER", "POSTLEITZAHL", "ORT", "POSTFACH", "POSTFACH_PLZ", "POSTFACH_ORT", "LAND", "TELEFON", "FAX", "EMAIL", "ZUSATZ1", "ZUSATZ2", "ZUSATZ3", "ZUSATZ4", "ZUSATZ5", "ZUSATZ6", "ZUSATZ7", "ZUSATZ8", "ZUSATZ9", "ZUSATZ10", "URS1", "URS2", "URS3", "URS4", "URS5", "URS6", "URS7"};
  private static final String[] ansprechpartnerSpalten = {"AN_ANREDE", "AN_NAME", "AN_VORNAME", "AN_ABTEILUNG", "AN_TELEFON", "AN_MOBIL", "AN_FAX", "AN_EMAIL"};
  private static final String[] firmenSpalten = {"FA_NAME", "FA_NAME_ERGAENZUNG", "FA_KURZTEXT"};
  private static final String SQL_SELECT_STANDARD_KONFIGURATION = "SELECT UCASE(KONFIGURATION_ID) AS ID, WERT_STRING FROM konfiguration WHERE STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_TRANSFERZIEL = "SELECT transfer.AMT, transfer.STATISTIK_ID, transfer.AKTION, CAST(transfer.AENDERUNGSART AS UNSIGNED) AS AENDERUNGSARTVALUE, transferziel.KONVERTER, transferziel.KONVERTER_OPTIONEN, transferziel.TRANSFER_FORM, transferziel.TRANSFER_PLATTFORM, transferziel.TRANSFER_HOST, transferziel.TRANSFER_USER, transferziel.TRANSFER_PASSWORT, transferziel.TRANSFER_ACCOUNT, transferziel.TRANSFER_ZIEL_VERZEICHNIS, transferziel.TRANSFER_MODUS, transferziel.TRANSFER_MAIL_EMPFAENGER, transferziel.TRANSFER_MAIL_ABSENDER, transferziel.TRANSFER_MAIL_BETREFF, transferziel.TRANSFER_MAIL_TEXT, transferziel.AENDERUNGS_EXPORT_SPALTEN FROM transfer INNER JOIN transferziel USING(TRANSFERZIEL_ID) WHERE transfer.amt=\"{0}\"  AND LEFT(transfer.AKTION,5) = \"AEND_\" AND transfer.STATUS != \"LOESCH\" AND transferziel.STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_SACHBEARBEITER = "SELECT SACHBEARBEITER_ID FROM sachbearbeiter WHERE kennung=\"{0}\" AND status != \"LOESCH\"";
  private static final String SQL_SELECT_AENDERUNGEN = "SELECT CAST(AENDERUNGSART AS UNSIGNED) AS AENDERUNGSARTVALUE, aenderung.* FROM aenderung WHERE AMT IN(\"\",\"{0}\") AND STATISTIK_ID IN(0, {1}) AND TYP = \"{2}\" AND STATUS IN(\"NEU\",\"ERLEDIGT\",\"BEARBEITET\") AND (AENDERUNGSART & {3}) AND (STATUS_EXPORT_AENDERUNG & {3} = 0)  ORDER BY AENDERUNG_ID LIMIT 1000";
  private static final LoggerIfc log = Logger.getInstance().getLogger(AenderungenVerteilenNeu.class);
  private final Connection connection;
  private final ArrayList<TransferDaten> transferDatenList;
  private final String amt;
  private final String sbKennung;
  private final SimpleDateFormat sdf;
  private String zielZeichensatz;
  private boolean disableHostKeyCheck;
  private String knownHostDatei;
  private boolean exportMitUeberschrift;
  private int sbId;
  private HashMap<String, String> defaultKonfigurationMap;

  public AenderungenVerteilenNeu(Connection connection, String amt, String kennung)
  {
    this.connection = connection;
    this.transferDatenList = new ArrayList<>();
    this.amt = amt;
    this.sbKennung = kennung;
    this.zielZeichensatz = StandardCharsets.ISO_8859_1.name();
    this.disableHostKeyCheck = false;
    this.knownHostDatei = null;
    this.exportMitUeberschrift = false;
    this.sdf = new SimpleDateFormat("yyyyMMdd.HHmmss.SSSS");
  }

  private HashMap<String, String> ermittleStandardKonfiguration() throws JobException
  {
    HashMap<String, String> result = new HashMap<>();
    try (ResultSet rs = this.connection.createStatement().executeQuery(SQL_SELECT_STANDARD_KONFIGURATION))
    {
      while (rs.next())
      {
        result.put(rs.getString(1), rs.getString(2));
      }
    } catch (SQLException e)
    {
      throw new JobException(e.getMessage(), e);
    }
    return result;
  }

  private void ermittleSachbearbeiter() throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_SACHBEARBEITER, this.sbKennung);
    try (ResultSet rs = this.connection.createStatement().executeQuery(sql))
    {
      if (rs.next())
        this.sbId = rs.getInt(1);
    } catch (SQLException e)
    {
      throw new JobException(e.getMessage(), e);
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
    String sql = MessageFormat.format(SQL_SELECT_TRANSFERZIEL, this.amt);
    log.debug(sql);
    try (ResultSet rs = this.connection.createStatement().executeQuery(sql))
    {
      while (rs.next())
      {
        this.transferDatenList.add(new TransferDaten(rs));
      }
    } catch (SQLException e)
    {
      throw new JobException(e.getMessage(), e);
    }
    return !this.transferDatenList.isEmpty();
  }

  public void verarbeiteDirektEintraege() throws JobException
  {
    // Alle Transferziele mit Direkteintrag
    List<TransferDaten> direktEintraege = this.transferDatenList.stream().filter(TransferDaten::isDirekteintrag).collect(Collectors.toList());
    for (TransferDaten td : direktEintraege)
    {
      List<Aenderung> aenderungen = ermittleAenderungen(td);
      for (Aenderung aenderung : aenderungen)
      {
        doDirektEintrag(aenderung);
      }
    }
  }

  private void doDirektEintrag(Aenderung aenderungen)
  {
    String sql;
    String fehlertext;
    boolean result = true;
    // Adressen ?
    String set = aenderungen.getSqlUpdateWithColumns(adressenSpalten, "");
    if (StringUtil.notEmpty(set))
    {
      sql = MessageFormat.format(SQL_DIREKTEINTRAG, "adressen", set, "" + this.sbId, "adressen_id", "" + aenderungen.getAdressenId());
      fehlertext = MessageFormat.format(DIREKTEINTRAG_FEHLERTEXT, "Adresse", "" + aenderungen.getAdressenId());
      if (!doSqlDirekteintrag(sql, fehlertext))
      {
        result = false;
      }
    }
    // Firmen ?
    set = aenderungen.getSqlUpdateWithColumns(firmenSpalten, "FA_");
    if (StringUtil.notEmpty(set))
    {
      sql = MessageFormat.format(SQL_DIREKTEINTRAG, "firmen", set, "" + this.sbId, "firmen_id", "" + aenderungen.getFirmenId());
      fehlertext = MessageFormat.format(DIREKTEINTRAG_FEHLERTEXT, "Firma", "" + aenderungen.getFirmenId());
      if (!doSqlDirekteintrag(sql, fehlertext))
        result = false;
    }
    // Ansprechpartner ?
    set = aenderungen.getSqlUpdateWithColumns(ansprechpartnerSpalten, "AN_");
    if (StringUtil.notEmpty(set))
    {
      sql = MessageFormat.format(SQL_DIREKTEINTRAG, "ansprechpartner", set, "" + this.sbId, "ansprechpartner_id", "" + aenderungen.getAnsprechpartnerId());
      fehlertext = MessageFormat.format(DIREKTEINTRAG_FEHLERTEXT, "Ansprechpartner", "" + aenderungen.getAnsprechpartnerId());
      if (!doSqlDirekteintrag(sql, fehlertext))
        result = false;
    }
    aenderungen.setDirektEintragErfolgreich(result);

  }

  private boolean doSqlDirekteintrag(String sql, String fehlertext)
  {
    try (Statement stmt = this.connection.createStatement())
    {
      stmt.executeUpdate(sql);
      return true;
    } catch (SQLException e)
    {
      log.error(fehlertext + ":" + e.getMessage());
    }
    return false;
  }

  public void verarbeiteDateiexporte() throws JobException
  {
    // Alle Transferziele mit Direkteintrag
    List<TransferDaten> direktEintraege = this.transferDatenList.stream().filter(TransferDaten::isDateiexport).collect(Collectors.toList());
    for (TransferDaten td : direktEintraege)
    {
      List<Aenderung> aenderungen = ermittleAenderungen(td);
      if (!aenderungen.isEmpty())
      {
        erzeugeExportDatei(aenderungen, td);
      }
    }
  }

  private void erzeugeExportDatei(List<Aenderung> aenderungen, TransferDaten transferDaten)
  {
    String dateiname = "IDEV." + transferDaten.getAktion() + "." + this.amt + "." + transferDaten.getStatistikId() + "." + sdf.format(new Date()) + ".zip";
    // Erzeuge Datei und speichere die Eintraege als CSV
    log.debug("Erzeuge datei " + dateiname + " mit " + aenderungen.size() + " Eintraegen");
    for (Aenderung ae : aenderungen)
    {

    }
  }

  private List<Aenderung> ermittleAenderungen(TransferDaten td) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_AENDERUNGEN, td.getAmt(), "" + td.getStatistikId(), td.isMelderAenderung() ? "AEND_MELDER" : "AEND_AUSKUNFTPFL", "" + td.getAenderungsart());
    log.debug(sql);
    String[] spalten = td.getExportSpalten().split("[,;]");
    List<Aenderung> aenderungsList = new ArrayList<>();
    try (ResultSet rs = this.connection.createStatement().executeQuery(sql))
    {
      while (rs.next())
      {
        Aenderung aenderung = new Aenderung(spalten);
        aenderung.convertResultset(rs);
        aenderungsList.add(aenderung);
      }
      return aenderungsList;
    } catch (SQLException e)
    {
      log.error(e.getMessage());
      throw new JobException(e.getMessage(), e);
    }
  }
}
