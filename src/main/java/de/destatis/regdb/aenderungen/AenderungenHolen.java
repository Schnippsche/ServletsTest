package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.RegDBSecurity;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Aenderungen holen.
 */
public class AenderungenHolen
{
  private static final String EXTENDED = ",SACHBEARBEITER_ID={0}, STATUS='AEND', ZEITPUNKT_AENDERUNG = NOW() WHERE {1} = {2}";

  // Alle Melder mit Referenz über MelderStatistik-Tabelle und Quell-Referenz
  private static final String SQL_AENDERUNGEN_DIREKTEINTRAG = "(SELECT ae.* FROM aenderung AS ae WHERE AMT = ? AND STATISTIK_ID = ? AND TYP=? AND STATUS IN('NEU', 'ERLEDIGT','BEARBEITET') AND (aenderungsart & ?) AND (ae.STATUS_DIREKTEINTRAG & ? = 0)) UNION ( SELECT ae.* FROM aenderung AS ae INNER JOIN melder AS m ON (m.melder_id = ae.melder_id) INNER JOIN adressen AS a ON (m.adressen_id = a.adressen_id) INNER JOIN quell_referenz_verwaltung AS q ON (a.quell_referenz_id = q.quell_referenz_id) INNER JOIN melder_statistiken AS ms ON (m.melder_id = ms.melder_id) WHERE (q.amt=? OR q.amt = '') AND (q.statistik_id = ? OR q.statistik_id=0) AND ae.amt='' AND ae.statistik_id=0 AND ms.amt=? AND ms.statistik_id=? AND ms.STATUS != 'LOESCH' AND ae.STATUS IN('NEU', 'ERLEDIGT','BEARBEITET') AND typ=? AND q.STATUS != 'LOESCH' AND (ae.aenderungsart & ?) AND (ae.STATUS_DIREKTEINTRAG & ? = 0) ) ORDER BY aenderung_id LIMIT 1000";
  private static final String SQL_AENDERUNGEN_EXPORT_AENDERUNG = "(SELECT ae.* FROM aenderung AS ae WHERE AMT = ? AND STATISTIK_ID = ? AND TYP=? AND STATUS IN('NEU', 'ERLEDIGT', 'BEARBEITET') AND (aenderungsart & ?) AND (ae.STATUS_EXPORT_AENDERUNG & ? = 0)) UNION (SELECT ae.* FROM aenderung AS ae INNER JOIN melder AS m ON (m.melder_id = ae.melder_id) INNER JOIN adressen as a ON (m.adressen_id = a.adressen_id) INNER JOIN quell_referenz_verwaltung as q ON (a.quell_referenz_id = q.quell_referenz_id) INNER JOIN melder_statistiken as ms ON (m.melder_id = ms.melder_id) WHERE (q.amt=? OR q.amt = '') AND (q.statistik_id = ? OR q.statistik_id=0) AND ae.amt='' AND ae.statistik_id=0 AND ms.amt=? AND ms.statistik_id=? AND ms.STATUS != 'LOESCH' AND ae.STATUS IN('NEU', 'ERLEDIGT', 'BEARBEITET') AND typ=? AND q.STATUS != 'LOESCH' AND (ae.aenderungsart & ?) AND (ae.STATUS_EXPORT_AENDERUNG & ? = 0)) ORDER BY aenderung_id LIMIT 1000";
  private static final String DELIMITER = ";";
  private static final String NEWLINE = "\r\n";
  private static final String[] _adressenSpalten = {"ANREDE", "NAME", "NAME_ERGAENZUNG", "KURZTEXT", "ABTEILUNG", "STRASSE", "HAUSNUMMER", "POSTLEITZAHL", "ORT", "POSTFACH", "POSTFACH_PLZ", "POSTFACH_ORT", "LAND", "TELEFON", "FAX", "EMAIL", "ZUSATZ1", "ZUSATZ2", "ZUSATZ3", "ZUSATZ4", "ZUSATZ5", "ZUSATZ6", "ZUSATZ7", "ZUSATZ8", "ZUSATZ9", "ZUSATZ10", "URS1", "URS2", "URS3", "URS4", "URS5", "URS6", "URS7"};
  private static final String[] _ansprechpartnerSpalten = {"AN_ANREDE", "AN_NAME", "AN_VORNAME", "AN_ABTEILUNG", "AN_TELEFON", "AN_MOBIL", "AN_FAX", "AN_EMAIL"};
  private static final String[] _firmenSpalten = {"FA_NAME", "FA_NAME_ERGAENZUNG", "FA_KURZTEXT"};
  private static final String[] _aenderungenSpalten = {"AENDERUNG_ID", "TYP", "AENDERUNGSART", "AMT", "STATISTIK_ID", "FIRMEN_ID", "ADRESSEN_ID", "ANSPRECHPARTNER_ID", "MELDER_ID", "QUELL_REFERENZ_OF", "DATUM", "FA_NAME", "FA_NAME_ERGAENZUNG", "FA_KURZTEXT", "BEMERKUNGEN", "SACHBEARBEITER_ID", "STATUS", "STATUS_EXPORT_AENDERUNG", "STATUS_DIREKTEINTRAG", "ZEITPUNKT_EINTRAG", "ZEITPUNKT_EXPORT", "ZEITPUNKT_AENDERUNG", "ZEITPUNKT_WWW"};
  private static final HashSet<String> _adressenMap = new HashSet<>(Arrays.asList(_adressenSpalten));
  private static final HashSet<String> _partnerMap = new HashSet<>(Arrays.asList(_ansprechpartnerSpalten));
  private static final HashSet<String> _firmenMap = new HashSet<>(Arrays.asList(_firmenSpalten));
  private static final HashSet<String> _aenderungTabelleMap = new HashSet<>(Arrays.asList(_aenderungenSpalten));
  private final Connection myConn;
  private final LoggerIfc log;
  private final String myKennung;
  private final String myAmt;
  private final String myStatistik;
  private final String myTyp;
  private final int mySbId;
  private final int myAenderungsart;
  private String client;
  private File tempDirectory = null;
  private File fileZipAusgabe = null;
  private StringBuilder adressenUpdate = null;
  private StringBuilder firmenUpdate = null;
  private StringBuilder partnerUpdate = null;
  private String adressenId;
  private String firmenId;
  private String partnerId;

  /**
   * Instantiates a new aenderungen holen.
   *
   * @param sbId          the sb id
   * @param kennung       the kennung
   * @param amt           the amt
   * @param statistik     the statistik
   * @param aenderungsart the aenderungsart
   * @param typ           the typ
   * @param conn          the conn
   */
  public AenderungenHolen(String sbId, String kennung, String amt, String statistik, int aenderungsart, String typ, Connection conn)
  {
    this.log = Logger.getInstance().getLogger(this.getClass());
    this.mySbId = Integer.parseInt(sbId);
    this.myKennung = kennung;
    this.myAmt = amt;
    this.myStatistik = statistik;
    this.myAenderungsart = aenderungsart;
    this.myTyp = typ;
    this.myConn = conn;
    DBConfig config = new DBConfig();
    String dir = config.getParameter(this.myConn, "INT_TEMP_DIRECTORY");
    if (dir != null)
    {
      File f = new File(dir);
      if (f.exists() && f.isDirectory())
      {
        this.tempDirectory = f;
      }
      else
      {
        this.log.info("Temporaeres Work-Verzeichnis fehlerhaft; verwende Default-Verzeichnis");
      }
    }
  }

  /**
   * Starte verarbeitung.
   *
   * @param spalten         the spalten
   * @param mitUeberschrift the mit ueberschrift
   * @return the file
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  public File starteVerarbeitung(String[] spalten, boolean mitUeberschrift) throws AenderungenHolenException
  {
    // Sind alle angegebenen Spalten korrekt ?
    checkSpalten(spalten);
    if (this.holeAenderungsDaten(spalten, mitUeberschrift))
    {
      return this.fileZipAusgabe;
    }
    return null;
  }

  /**
   * Starte verarbeitung.
   *
   * @param spalten           the spalten
   * @param mitUeberschriften the mit ueberschriften
   * @return the file
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  public File starteVerarbeitung(String spalten, boolean mitUeberschriften) throws AenderungenHolenException
  {
    if (spalten == null)
    {
      return this.starteVerarbeitung(new String[0], mitUeberschriften);
    }
    return this.starteVerarbeitung(spalten.split("[,;]"), mitUeberschriften);
  }

  /**
   * Starte direkt eintrag.
   *
   * @param spalten the spalten
   * @return the file
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  public File starteDirektEintrag(String spalten) throws AenderungenHolenException
  {
    if (spalten == null)
    {
      return this.starteDirekteintrag(new String[0]);
    }
    return this.starteDirekteintrag(spalten.split("[,;]"));
  }

  /**
   * Starte direkteintrag.
   *
   * @param spalten the spalten
   * @return the file
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  public File starteDirekteintrag(String[] spalten) throws AenderungenHolenException
  {
    this.fileZipAusgabe = null;
    StringBuilder ids = new StringBuilder();
    // Sind alle angegebenen Spalten korrekt ?
    checkSpalten(spalten);
    int counter = 0;

    try (PreparedStatement ps = this.myConn.prepareStatement(SQL_AENDERUNGEN_DIREKTEINTRAG))
    {
      setStandardValues(ps);
      try (ResultSet rs = ps.executeQuery())
      {
        String[] cols = null;
        while (rs.next())
        {
          if (counter == 0)
          {
            this.fileZipAusgabe = this.createTempFile();
          }
          // Wurden keine Spaltennamen angegeben, dann hole alle Spalten aus dem ResultSet ( nur beim ersten Mal )
          if (cols == null)
          {
            cols = getColumnsFromResult(spalten, rs);
          }
          this.initDirekteintrag(rs.getString("ADRESSEN_ID"), rs.getString("FIRMEN_ID"), rs.getString("ANSPRECHPARTNER_ID"));
          for (final String col : cols)
          {
            this.addColumnValue(col.trim(), rs.getString(col.trim()));
          }
          this.doDirekteintrag();
          // ID merken
          if (ids.length() > 0)
          {
            ids.append(',');
          }
          ids.append(rs.getString("AENDERUNG_ID"));
          counter++;
        }
        if (this.fileZipAusgabe != null)
        {
          try (PrintWriter out = new PrintWriter(new FileOutputStream(this.fileZipAusgabe)))
          {
            out.write(ids.toString());
            out.flush();
          }
        }
      }
    }
    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
      this.fileZipAusgabe = null;
      throw new AenderungenHolenException("Fehler beim Direkteintrag:" + e.getMessage());
    }
    return this.fileZipAusgabe;
  }

  private String[] getColumnsFromResult(String[] spalten, ResultSet rs) throws SQLException
  {
    String[] cols;
    if (spalten == null || spalten.length == 0)
    {
      int anzColumn = rs.getMetaData().getColumnCount();
      cols = new String[anzColumn];
      for (int x = 0; x < anzColumn; x++)
      {
        cols[x] = rs.getMetaData().getColumnName(x + 1);
      }
    }
    else
    {
      cols = spalten;
    }
    return cols;
  }

  private void checkSpalten(String[] spalten) throws AenderungenHolenException
  {
    if (spalten != null)
    {
      StringBuilder error = new StringBuilder();
      for (final String s : spalten)
      {
        String col = s.trim().toUpperCase();
        if (!_adressenMap.contains(col) && !_firmenMap.contains(col) && !_partnerMap.contains(col) && !_aenderungTabelleMap.contains(col))
        {
          if (error.length() > 0)
          {
            error.append(',');
          }
          error.append(s);
        }
      }
      if (error.length() > 0)
      {
        throw new AenderungenHolenException("Die folgenden angegebenen Spalten existieren nicht:" + error.toString() + ".Pruefen Sie das Schema oder die Angaben im Feld AENDERUNGS_EXPORT_SPALTEN der Tabelle transferziel!");
      }
    }
  }

  /**
   * Validate.
   *
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  public void validate() throws AenderungenHolenException
  {
    String logInfo = "'" + this.myKennung + "' (" + this.client + ") ruft Aenderungen ";
    logInfo += "fuer Amt '" + this.myAmt + "', Statistik-ID " + this.myStatistik + ", Typ='" + this.myTyp + "', Aenderungsart " + this.myAenderungsart + " ab";
    this.log.info(logInfo);
    checkParams();
  }

  private void checkParams() throws AenderungenHolenException
  {
    if (this.myAmt == null || "".equals(this.myAmt))
    {
      throw new AenderungenHolenException("Kein Amt angegeben!");
    }
    if (this.myStatistik == null || "".equals(this.myStatistik))
    {
      throw new AenderungenHolenException("Keine Statistik_id  angegeben!");
    }
    if (this.myTyp == null || "".equals(this.myTyp))
    {
      throw new AenderungenHolenException("Kein Typ angegeben!");
    }
    if (this.myConn == null)
    {
      throw new AenderungenHolenException("Keine gueltige Connection zur Datenbank");
    }
    // Hat der SB Rechte an Amt / Statistik ?
    if (!RegDBSecurity.getInstance().sbHasGrantforAmtStatistik(this.mySbId, this.myAmt, this.myStatistik, RegDBSecurity.SADMIN_RECHT_INT, this.myConn))
    {
      throw new AenderungenHolenException("Dem Sachbearbeiter " + this.mySbId + " mit Kennung '" + this.myKennung + "' fehlen die erforderlichen Rechte an Amt " + this.myAmt + " / Statistik " + this.myStatistik);
    }

  }

  /**
   * Validate direkteintrag.
   *
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  public void validateDirekteintrag() throws AenderungenHolenException
  {
    String logInfo = "'" + this.myKennung + "' (" + this.client + ") ruft Direkteintrag der Aenderungen ";
    logInfo += "fuer Amt '" + this.myAmt + "', Statistik-ID " + this.myStatistik + ", Typ='" + this.myTyp + "', Aenderungsart " + this.myAenderungsart + " auf";
    this.log.info(logInfo);
    checkParams();
  }

  /**
   * Hole aenderungs daten.
   *
   * @param spalten         the spalten
   * @param mitUeberschrift the mit ueberschrift
   * @return true, if successful
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  private boolean holeAenderungsDaten(String[] spalten, boolean mitUeberschrift) throws AenderungenHolenException
  {
    boolean result = false;
    this.fileZipAusgabe = null;
    String[] cols = null;
    String alterTyp = "";
    int counter = 0;

    ZipOutputStream zipStream = null;
    BufferedWriter writer = null;
    try (PreparedStatement ps = this.myConn.prepareStatement(SQL_AENDERUNGEN_EXPORT_AENDERUNG))
    {
      setStandardValues(ps);
      try (ResultSet rs = ps.executeQuery())
      {
        StringBuilder buf = new StringBuilder();
        StringBuilder ids = new StringBuilder();
        while (rs.next())
        {
          // Wurden keine Spaltennamen angegeben, dann hole alle Spalten aus dem ResultSet ( nur beim ersten Mal )
          if (cols == null)
          {
            cols = getColumnsFromResult(spalten, rs);
          }
          counter++;
          buf.setLength(0);
          // Erzeuge für jeden Typ drei Dateien:
          // Datendatei, ÄnderungsIDs, Infotext
          String typ = rs.getString("TYP");
          if (!alterTyp.equalsIgnoreCase(typ))
          {
            // neue Zip-Datei anlegen
            this.fileZipAusgabe = this.createTempZipFile();
            zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.fileZipAusgabe)), StandardCharsets.UTF_8);
            writer = new BufferedWriter(new OutputStreamWriter(zipStream, StandardCharsets.UTF_8));
            zipStream.setMethod(ZipOutputStream.DEFLATED);
            // Datendatei erstellen und in ZIP aufnehmen
            ZipEntry entry = new ZipEntry("daten");
            zipStream.putNextEntry(entry);
            if (mitUeberschrift)
            {
              for (int x = 0; x < cols.length; x++)
              {
                if (x > 0)
                {
                  buf.append(DELIMITER);
                }
                buf.append(cols[x].replace(';', ','));
              }
              buf.append(NEWLINE);
            }
            alterTyp = typ;
          }
          String data;
          // gewuenschte Spalten ausgeben
          for (int x = 0; x < cols.length; x++)
          {
            String col = cols[x].trim();
            if (x > 0)
            {
              buf.append(DELIMITER);
            }
            // Sonderregelung: ersetze AMT / Statistik
            if ("AMT".equalsIgnoreCase(col))
            {
              data = this.myAmt;
            }
            else if ("STATISTIK_ID".equalsIgnoreCase(col))
            {
              data = this.myStatistik;
            }
            else
            {
              data = rs.getString(col);
            }
            buf.append(data == null ? "" : data.replace(';', ','));
          }
          buf.append(NEWLINE);
          // ID merken
          if (ids.length() > 0)
          {
            ids.append(',');
          }
          ids.append(rs.getString("AENDERUNG_ID"));
          if (writer != null)
          {
            writer.append(buf.toString());
            writer.flush();
          }
        }

        if (zipStream != null)
        {
          // Erzeuge Datei mit ids und Datei mit infotext
          zipStream.closeEntry();
          if (ids.length() > 0)
          {
            ZipEntry entry = new ZipEntry("ids");
            zipStream.putNextEntry(entry);
            writer.append(ids);
            writer.flush();
            zipStream.closeEntry();
            entry = new ZipEntry("info");
            zipStream.putNextEntry(entry);
            writer.append(this.getMailText(ids.toString(), alterTyp, counter));
            writer.flush();
            zipStream.closeEntry();
          }
          zipStream.close();
          result = true;
        }
      }
    }
    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
      this.fileZipAusgabe = null;
      throw new AenderungenHolenException("Fehler beim Erstellen der ZIP-Datei:" + e.getMessage());
    }
    finally
    {
      try
      {
        if (zipStream != null)
        {
          zipStream.close();
        }
        if (writer != null)
        {
          writer.close();
        }
      }
      catch (IOException e)
      {
        this.log.error(e.getMessage(), e);
      }

    }
    return result;
  }

  private void setStandardValues(PreparedStatement ps) throws SQLException
  {
    ps.setString(1, this.myAmt);
    ps.setString(2, this.myStatistik);
    ps.setString(3, this.myTyp);
    ps.setInt(4, this.myAenderungsart);
    ps.setInt(5, this.myAenderungsart);
    ps.setString(6, this.myAmt);
    ps.setString(7, this.myStatistik);
    ps.setString(8, this.myAmt);
    ps.setString(9, this.myStatistik);
    ps.setString(10, this.myTyp);
    ps.setInt(11, this.myAenderungsart);
    ps.setInt(12, this.myAenderungsart);
  }

  /**
   * Initialisiert direkteintrag.
   *
   * @param adressenId        the adressen id
   * @param firmenId          the firmen id
   * @param ansprechpartnerId the ansprechpartner id
   */
  public void initDirekteintrag(String adressenId, String firmenId, String ansprechpartnerId)
  {
    if (this.adressenUpdate == null)
    {
      this.adressenUpdate = new StringBuilder();
    }
    if (this.firmenUpdate == null)
    {
      this.firmenUpdate = new StringBuilder();
    }
    if (this.partnerUpdate == null)
    {
      this.partnerUpdate = new StringBuilder();
    }
    this.adressenUpdate.setLength(0);
    this.firmenUpdate.setLength(0);
    this.partnerUpdate.setLength(0);
    this.adressenId = adressenId;
    this.firmenId = firmenId;
    this.partnerId = ansprechpartnerId;
  }

  /**
   * Do direkteintrag.
   */
  public void doDirekteintrag()
  {
    if (this.adressenUpdate.length() > 0)
    {
      this.adressenUpdate.append(MessageFormat.format(EXTENDED, "" + this.mySbId, "adressen_id", "" + this.adressenId));
      try (Statement stmt = this.myConn.createStatement())
      {
        stmt.execute(this.adressenUpdate.toString());
      }
      catch (SQLException e)
      {
        this.log.error("Fehler beim Direkteintrag der Adresse (ID=" + this.adressenId + "):" + e.getMessage());
      }
    }
    if (this.firmenUpdate.length() > 0)
    {
      this.firmenUpdate.append(MessageFormat.format(EXTENDED, "" + this.mySbId, "firmen_id", "" + this.firmenId));
      try (Statement stmt = this.myConn.createStatement())
      {
        stmt.execute(this.firmenUpdate.toString());
      }
      catch (SQLException e)
      {
        this.log.error("Fehler beim Direkteintrag der Firma (ID=" + this.firmenId + "):" + e.getMessage());
      }
    }
    if (this.partnerUpdate.length() > 0)
    {
      this.partnerUpdate.append(MessageFormat.format(EXTENDED, "" + this.mySbId, "ansprechpartner_id", "" + this.partnerId));
      try (Statement stmt = this.myConn.createStatement())
      {
        stmt.execute(this.partnerUpdate.toString());
      }
      catch (SQLException e)
      {
        this.log.error("Fehler beim Direkteintrag des Ansprechpartners (ID=" + this.partnerId + "):" + e.getMessage());
      }
    }
  }

  /**
   * Adds the column value.
   *
   * @param col   the col
   * @param value the value
   */
  public void addColumnValue(String col, String value)
  {
    if (_adressenMap.contains(col))
    {
      if (this.adressenUpdate.length() == 0)
      {
        this.adressenUpdate.append("UPDATE adressen SET ");
      }
      else
      {
        this.adressenUpdate.append(",");
      }
      this.adressenUpdate.append(col);
      this.adressenUpdate.append("=");
      this.adressenUpdate.append(StringUtil.escapeSqlString(value));
    }
    else if (_firmenMap.contains(col))
    {
      if (this.firmenUpdate.length() == 0)
      {
        this.firmenUpdate.append("UPDATE firmen SET ");
      }
      else
      {
        this.firmenUpdate.append(",");
      }
      if (col.startsWith("FA_"))
      {
        this.firmenUpdate.append(col.substring(3));
      }
      else
      {
        this.firmenUpdate.append(col);
      }
      this.firmenUpdate.append("=");
      this.firmenUpdate.append(StringUtil.escapeSqlString(value));
    }
    else if (_partnerMap.contains(col))
    {
      if (this.partnerUpdate.length() == 0)
      {
        this.partnerUpdate.append("UPDATE ansprechpartner SET ");
      }
      else
      {
        this.partnerUpdate.append(",");
      }
      if (col.startsWith("AN_"))
      {
        this.partnerUpdate.append(col.substring(3));
      }
      else
      {
        this.partnerUpdate.append(col);
      }
      this.partnerUpdate.append("=");
      this.partnerUpdate.append(StringUtil.escapeSqlString(value));
    }
  }

  /**
   * Creates the temp zip file.
   *
   * @return the file
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  private File createTempZipFile() throws AenderungenHolenException
  {
    this.log.debug("createTempZipFile");
    try
    {
      return File.createTempFile("aenderung", ".zip", this.tempDirectory);
    }
    catch (Exception e)
    {
      throw new AenderungenHolenException("Temporaere Zip-Datei konnte nicht erstellt werden!");
    }
  }

  /**
   * Creates the temp file.
   *
   * @return the file
   * @throws AenderungenHolenException the aenderungen holen exception
   */
  private File createTempFile() throws AenderungenHolenException
  {
    this.log.debug("createTempIdFile");
    try
    {
      return File.createTempFile("ids", ".txt", this.tempDirectory);
    }
    catch (Exception e)
    {
      throw new AenderungenHolenException("Temporaere Datei konnte nicht erstellt werden!");
    }
  }

  /**
   * Setzt client.
   *
   * @param client client
   */
  public void setClient(String client)
  {
    this.client = client;
  }

  /**
   * Liefert zip file name.
   *
   * @return zip file name
   */
  public String getZipFileName()
  {
    return "IDEV.SAMMEL.AENDERUNGEN." + this.myTyp + "." + this.myAmt + "." + this.myStatistik + "." + this.getFileDate() + ".zip";
  }

  /**
   * Liefert file date.
   *
   * @return file date
   */
  public String getFileDate()
  {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd.HHmmss.SSSS");
    return fmt.format(new Date());
  }

  /**
   * Liefert mail text.
   *
   * @param ids     the ids
   * @param typ     the typ
   * @param counter the counter
   * @return mail text
   */
  private String getMailText(String ids, String typ, int counter)
  {
    StringBuilder info = new StringBuilder();
    if (counter == 1)
    {
      info.append("Folgende Aenderungsmeldung vom Typ '");
      info.append(typ);
      info.append("' ist fuer Sie eingetroffen:");
    }
    else
    {
      info.append("Folgende ");
      info.append(counter);
      info.append(" Aenderungsmeldungen vom Typ '");
      info.append(typ);
      info.append("' sind fuer Sie eingetroffen:");
    }
    info.append(NEWLINE);
    // Hole Statistikname
    String statistikName = "";
    String sql = "SELECT bezeichnung FROM statistiken WHERE statistik_id = " + this.myStatistik;
    try (Statement stmt = this.myConn.createStatement())
    {
      try (ResultSet rs = stmt.executeQuery(sql))
      {
        if (rs.next())
        {
          statistikName = rs.getString(1);
        }
      }
      // Hole Amtname
      String amtName = "";
      sql = "SELECT bezeichnung FROM amt WHERE amt = '" + this.myAmt + "' AND status != 'LOESCH'";
      try (ResultSet rs = stmt.executeQuery(sql))
      {
        if (rs.next())
        {
          amtName = rs.getString(1);
        }
      }
      if (typ.startsWith("ERWEIT") || typ.startsWith("LOESCH"))
      {
        addMailtextForErweiterung(ids, info, statistikName, amtName, stmt);
      }
      if (typ.startsWith("REGISTRIERUNG"))
      {
        addMailtextForRegistrierung(ids, info, statistikName, stmt);
      }
      if (typ.startsWith("AEND"))
      {
        addMailtextForAenderung(ids, info, statistikName, amtName, stmt);
      }
    }
    catch (SQLException e)
    {
      this.log.error(e.getMessage(), e);
    }
    this.log.debug(info.toString());
    return info.toString();
  }

  private void addMailtextForErweiterung(String ids, StringBuilder info, String statistikName, String amtName, Statement stmt) throws SQLException
  {
    String sql = "SELECT aenderung_id, DATE_FORMAT(aenderung.datum, '%d.%m.%Y %T') AS datum, kennung, TRIM(CONCAT(ansprechpartner.vorname,' ',ansprechpartner.name)) as meldername, quell_referenz_of, firmen.name as firmenname from aenderung LEFT JOIN melder ON (aenderung.melder_id = melder.melder_id) LEFT JOIN ansprechpartner ON (ansprechpartner.ansprechpartner_id = melder.ansprechpartner_id) LEFT JOIN firmen ON (aenderung.firmen_id = firmen.firmen_id) WHERE aenderung_id in(" + ids + ") ORDER BY aenderung_id LIMIT 1000";
    createMailText(info, statistikName, amtName, stmt, sql);
  }

  private void createMailText(StringBuilder info, String statistikName, String amtName, Statement stmt, String sql) throws SQLException
  {
    try (ResultSet rs = stmt.executeQuery(sql))
    {
      while (rs.next())
      {
        info.append("Aenderungs-id ");
        info.append(rs.getString("aenderung_id"));
        info.append(" vom ");
        info.append(rs.getString("datum"));
        info.append(" zur Statistik '");
        info.append(statistikName);
        info.append("' des Amtes '");
        info.append(amtName);
        info.append("' des Melders (");
        info.append(rs.getString("kennung"));
        info.append(") ");
        info.append(rs.getString("meldername"));
        info.append(" fuer das Unternehmen '");
        info.append(rs.getString("firmenname"));
        info.append("' (OF:");
        info.append(rs.getString("quell_referenz_of"));
        info.append(")");
        info.append(NEWLINE);
      }
    }
  }

  private void addMailtextForRegistrierung(String ids, StringBuilder info, String statistikName, Statement stmt) throws SQLException
  {
    String sql = "SELECT aenderung_id, DATE_FORMAT(aenderung.datum, '%d.%m.%Y %T') AS datum, NAME, STRASSE, HAUSNUMMER, POSTLEITZAHL, ORT FROM aenderung WHERE aenderung_id IN(" + ids + ") ORDER BY aenderung_id limit 1000";
    try (ResultSet rs = stmt.executeQuery(sql))
    {
      while (rs.next())
      {
        info.append("Aenderungs-id ");
        info.append(rs.getString("aenderung_id"));
        info.append(" vom ");
        info.append(rs.getString("datum"));
        info.append(" für die Statistik '");
        info.append(statistikName);
        info.append("' von ");
        info.append(rs.getString("NAME"));
        info.append(" ");
        info.append(rs.getString("STRASSE"));
        info.append(" ");
        info.append(rs.getString("HAUSNUMMER"));
        info.append(" ");
        info.append(rs.getString("POSTLEITZAHL"));
        info.append(" ");
        info.append(rs.getString("ORT"));
        info.append(NEWLINE);
      }
    }
  }

  private void addMailtextForAenderung(String ids, StringBuilder info, String statistikName, String amtName, Statement stmt) throws SQLException
  {
    String sql = "SELECT aenderung_id, DATE_FORMAT(aenderung.datum, '%d.%m.%Y %T') AS datum, kennung, TRIM(CONCAT(ansprechpartner.vorname,' ',ansprechpartner.name)) AS meldername, quell_referenz_of, firmen.name AS firmenname FROM aenderung LEFT JOIN melder ON (aenderung.melder_id = melder.melder_id) LEFT JOIN ansprechpartner ON (ansprechpartner.ansprechpartner_id = melder.ansprechpartner_id) LEFT JOIN firmen ON (aenderung.firmen_id = firmen.firmen_id) WHERE aenderung_id in(" + ids + ") ORDER BY aenderung_id limit 1000";
    createMailText(info, statistikName, amtName, stmt, sql);
  }

}
