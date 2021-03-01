/*
 * @(#)MeldungenHolen.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.meldungen;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import de.destatis.regdb.db.RegDBSecurity;
import de.destatis.regdb.servlets.RegDBGeneralHttpServlet;
import de.werum.sis.idev.intern.actions.util.MeldungsBereitstellung;
import de.werum.sis.idev.intern.actions.util.MeldungsBereitstellungStatus;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

public class MeldungenHolen
{

  private final Connection myConn;
  private final LoggerIfc log;
  private final String myKennung;
  private final String myAmt;
  private final String myStatistik;
  private String myBzr;
  private String myMeldeart;
  private String myMeldungsIds;
  private String myErstesDatum;
  private String myErsteMeldungsId;
  private final String myPasswort;
  private final int mySbId;
  private Vector<String> myMeldungsDaten;
  private String defaultServerHost;
  private String defaultServerPort;
  private String client;
  private MeldungsBereitstellung werumDienst;
  private File tempDirectory = null;
  private final InhaltsverzeichnisXMLParser xmlParser;
  private final boolean bestaetigen;
  private boolean holePerMeldungsIDs;

  /**
   * Instantiates a new meldungen holen.
   *
   * @param sb_id the sb id
   * @param kennung the kennung
   * @param passwort the passwort
   * @param amt the amt
   * @param statistik the statistik
   * @param statusUmsetzen the status umsetzen
   * @param conn the conn
   */
  private MeldungenHolen(String sb_id, String kennung, String passwort, String amt, String statistik, boolean statusUmsetzen, Connection conn)
  {
    this.log = Logger.getInstance()
        .getLogger(this.getClass());
    this.mySbId = Integer.parseInt(sb_id);
    this.myKennung = kennung;
    this.myPasswort = passwort;
    this.myAmt = amt;
    this.myStatistik = statistik;
    this.myConn = conn;
    this.bestaetigen = statusUmsetzen;
    this.xmlParser = new InhaltsverzeichnisXMLParser();
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

    this.defaultServerHost = RegDBGeneralHttpServlet.interneAblaeufeHost;
    this.defaultServerPort = "" + RegDBGeneralHttpServlet.interneAblaeufePort;
    this.log.debug("DefaultServerhost:" + this.defaultServerHost);
    this.log.debug("DefaultServerPort:" + this.defaultServerPort);
  }

  /**
   * Instantiates a new meldungen holen.
   *
   * @param sb_id the sb id
   * @param kennung the kennung
   * @param passwort the passwort
   * @param amt the amt
   * @param statistik the statistik
   * @param meldungs_ids the meldungs ids
   * @param statusUmsetzen the status umsetzen
   * @param conn the conn
   */
  public MeldungenHolen(String sb_id, String kennung, String passwort, String amt, String statistik, String meldungs_ids, boolean statusUmsetzen, Connection conn)
  {
    this(sb_id, kennung, passwort, amt, statistik, statusUmsetzen, conn);
    this.myBzr = null;
    this.myMeldeart = null;
    this.myMeldungsIds = meldungs_ids;
    this.holePerMeldungsIDs = true;
  }

  /**
   * Instantiates a new meldungen holen.
   *
   * @param sb_id the sb id
   * @param kennung the kennung
   * @param passwort the passwort
   * @param amt the amt
   * @param statistik the statistik
   * @param bzr the bzr
   * @param meldeart the meldeart
   * @param statusUmsetzen the status umsetzen
   * @param conn the conn
   */
  public MeldungenHolen(String sb_id, String kennung, String passwort, String amt, String statistik, String bzr, String meldeart, boolean statusUmsetzen, Connection conn)
  {
    this(sb_id, kennung, passwort, amt, statistik, statusUmsetzen, conn);
    this.myBzr = ("-".equals(bzr)) ? null : bzr;
    this.myMeldeart = meldeart;
    this.myMeldungsIds = null;
    this.holePerMeldungsIDs = false;
  }

  /**
   * Creates the temp xml file.
   *
   * @param prefix the prefix
   * @return the file
   * @throws MeldungenHolenException the meldungen holen exception
   */
  private File createTempXmlFile(String prefix) throws MeldungenHolenException
  {
    this.log.debug("createTempXmlFile");
    try
    {
      return File.createTempFile(prefix, ".xml", this.tempDirectory);
    }
    catch (Exception e)
    {
      throw new MeldungenHolenException("Temporaere Datei " + prefix + ".xml konnte nicht erstellt werden!");
    }
  }

  /**
   * Creates the temp zip file.
   *
   * @return the file
   * @throws MeldungenHolenException the meldungen holen exception
   */
  private File createTempZipFile() throws MeldungenHolenException
  {
    this.log.debug("createTempZipFile");
    try
    {
      return File.createTempFile("meldung", ".zip", this.tempDirectory);
    }
    catch (Exception e)
    {
      throw new MeldungenHolenException("Temporaere Zip-Datei konnte nicht erstellt werden!");
    }
  }

  /**
   * Erstelle inhaltsverzeichnis.
   *
   * @param tempFile the temp file
   * @throws MeldungenHolenException the meldungen holen exception
   */
  private void erstelleInhaltsverzeichnis(File tempFile) throws MeldungenHolenException
  {
    this.log.debug("Erstelle Inhaltsverzeichnis " + tempFile.toString());
    try (BufferedWriter xmlout = Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8))
    {
      xmlout.write("<dateibereitstellung>");
      xmlout.newLine();
      xmlout.write("<statistik_id>" + this.myStatistik + "</statistik_id>");
      xmlout.newLine();
      xmlout.write("<amt>" + this.myAmt + "</amt>");
      xmlout.newLine();
      xmlout.write("<bzr>" + this.myBzr + "</bzr>");
      xmlout.newLine();
      xmlout.write("<sachbearbeiter_id>" + this.mySbId + "</sachbearbeiter_id>");
      xmlout.newLine();
      xmlout.write(" <meldungen>");
      xmlout.newLine();
      // Ausgabe der Ids
      for (int i = 0; i < this.myMeldungsDaten.size(); i++)
      {
        xmlout.write("  <meldung_id>" + this.myMeldungsDaten.get(i) + "</meldung_id>");
        xmlout.newLine();
      }
      xmlout.write(" </meldungen>");
      xmlout.newLine();
      xmlout.write("</dateibereitstellung>");
      xmlout.newLine();
    }
    catch (Exception e)
    {
      throw new MeldungenHolenException("Fehler: Beim Bearbeiten der Inhaltsverzeichnis-XML-Datei : " + tempFile + " zur Abholung von Meldungen.\n" + e);
    }
  }

  /**
   * Liefert file date.
   *
   * @return file date
   */
  public String getFileDate()
  {
    return new SimpleDateFormat("yyyyMMdd.HHmmss").format(new java.util.Date());
  }

  /**
   * Liefert file name.
   *
   * @return file name
   */
  public String getFileName()
  {
    return "Meldungen." + this.myErsteMeldungsId + "." + this.myAmt + "." + this.myStatistik + "." + this.myBzr + "." + this.myErstesDatum + ".zip";
  }

  /**
   * Hole meldungen.
   *
   * @return the file
   * @throws MeldungenHolenException the meldungen holen exception
   */
  public File holeMeldungen() throws MeldungenHolenException
  {
    this.log.debug("holeMeldungen");
    File tmpToWerum = this.createTempXmlFile("towerum");
    this.erstelleInhaltsverzeichnis(tmpToWerum);
    File tmpFromWerum = this.createTempXmlFile("fromwerum");
    // Werum-Funktion -Inhaltsverzeichnis-bereitstellen- aufrufen
    MeldungsBereitstellungStatus meldungsStatus = this.werumDienst.inhaltsverzeichnisAnfordern(tmpToWerum, tmpFromWerum);
    this.log.debug("Inhaltsverzeichnis angefordert, Status=" + meldungsStatus.getStatus());
    if (meldungsStatus.getStatus() == MeldungsBereitstellungStatus.STATUS_FEHLER)
    {
      throw new MeldungenHolenException(meldungsStatus.getMeldung());
    }

    // Suche alle Dateinamen raus
    this.xmlParser.parseDatei(tmpFromWerum);
    // Nimm die Dateinamen und erstelle neues XML File
    File tmpDatei = this.createTempXmlFile("tmpxml");
    this.parameterDateiSammelErstellen(tmpDatei);
    File ergebnisDatei = this.createTempZipFile();
    meldungsStatus = this.werumDienst.meldungenBereitstellen(tmpDatei, ergebnisDatei, false, null);
    if (meldungsStatus.getStatus() == MeldungsBereitstellungStatus.STATUS_FEHLER)
    {
      throw new MeldungenHolenException(meldungsStatus.getMeldung());
    }
    // Status der exportierten Meldungen umsetzen, falls gewuenscht
    if (this.bestaetigen)
    {
      meldungsStatus = this.werumDienst.bereitstellungBestaetigen(tmpToWerum);
      if (meldungsStatus.getStatus() == MeldungsBereitstellungStatus.STATUS_FEHLER)
      {
        throw new MeldungenHolenException(meldungsStatus.getMeldung());
      }
    }
    tmpDatei.delete();
    tmpToWerum.delete();
    tmpFromWerum.delete();
    return ergebnisDatei;
  }

  /**
   * Hole meldungs daten.
   *
   * @throws MeldungenHolenException the meldungen holen exception
   */
  private void holeMeldungsDaten() throws MeldungenHolenException
  {
    this.log.debug("holeMeldungsDaten");
    String sql;
    if (this.holePerMeldungsIDs)
    {
      sql = "SELECT MELDUNG_ID, BZR, DATE_FORMAT(DATUM, '%Y%m%d.%k%i%s') as EINGANGSDATUM FROM meldung WHERE MELDUNG_ID IN(" + this.myMeldungsIds + ")";
    }
    else
    {
      sql = "SELECT MELDUNG_ID, BZR, DATUM AS EINGANGSDATUM FROM meldung WHERE AMT = '" + this.myAmt + "' AND STATISTIK_ID = " + this.myStatistik + " AND STATUS = 'NEU' ";
      if (this.myBzr != null && this.myBzr.length() > 0)
      {
        sql += " AND BZR = '" + this.myBzr + "'";
      }
      if (this.myMeldeart != null && this.myMeldeart.length() > 0)
      {
        sql += " AND MELDEART = '" + this.myMeldeart + "'";
      }
    }
    this.myMeldungsDaten = new Vector<>();
    this.myErstesDatum = "21001231.235959";
    this.myErsteMeldungsId = "0";
    try (ResultSet rs = this.myConn.createStatement()
        .executeQuery(sql))
    {
      this.log.debug("Sql:" + sql);
      while (rs.next())
      {
        if (this.myBzr == null)
        {
          this.myBzr = rs.getString("BZR");
        }
        this.myMeldungsDaten.add(rs.getString("MELDUNG_ID"));
        String datum = rs.getString("EINGANGSDATUM");
        if (this.myErstesDatum.compareTo(datum) > 0)
        {
          this.myErstesDatum = datum;
          this.myErsteMeldungsId = rs.getString("MELDUNG_ID");
        }
      }
      if (this.myErstesDatum.equals("2100-12-31 23:59:00"))
      {
        this.myErstesDatum = this.getFileDate();
        this.myErsteMeldungsId = "0";
      }
    }
    catch (SQLException e)
    {
      this.log.error(e.getMessage(), e);
    }
    if (this.myMeldungsDaten.isEmpty())
    {
      throw new MeldungenHolenException("keine relevanten Meldungen gefunden...");
    }
  }

  /**
   * Meldungen sofort bestaetigen.
   *
   * @param xmlFile the xml file
   * @throws MeldungenHolenException the meldungen holen exception
   */
  public void meldungenSofortBestaetigen(File xmlFile) throws MeldungenHolenException
  {
    this.log.debug("Meldungen_sofort_bestaetigen " + xmlFile.toString());
    // Meldungbestätigung mit Status/Datum-Änderung
    MeldungsBereitstellungStatus meldungsStatus = this.werumDienst.bereitstellungBestaetigen(xmlFile);
    if (meldungsStatus.getStatus() == MeldungsBereitstellungStatus.STATUS_FEHLER)
    {
      throw new MeldungenHolenException(meldungsStatus.getMeldung());
    }

  }

  /**
   * Parameter datei sammel erstellen.
   *
   * @param temp the temp
   * @throws MeldungenHolenException the meldungen holen exception
   */
  public void parameterDateiSammelErstellen(File temp) throws MeldungenHolenException
  {
    this.log.debug("Parameter_Datei_Sammel_erstellen " + temp.toString());
    try (BufferedWriter xmlout = Files.newBufferedWriter(temp.toPath(), StandardCharsets.UTF_8))
    {
      xmlout.write("<dateibereitstellung>");
      xmlout.newLine();
      xmlout.write("<meldungen>");
      xmlout.newLine();
      for (int i = 0; i < this.myMeldungsDaten.size(); i++)
      {
        String meldungs_id = this.myMeldungsDaten.get(i);
        Vector<String> formular_dateien = this.xmlParser.getDateinamenFormular(meldungs_id);
        Vector<String> upload_dateien = this.xmlParser.getDateinamenUpload(meldungs_id);
        xmlout.write("<meldung meldung_id=\"" + meldungs_id + "\">");
        for (int n = 0; n < formular_dateien.size(); n++)
        {
          String originalName = String.valueOf(formular_dateien.elementAt(n));
          xmlout.write("<meldung_datei name=\"" + originalName + "\" ziel_name=\"" + originalName + "\"/>");
          xmlout.newLine();
        }
        for (int n = 0; n < upload_dateien.size(); n++)
        {
          String originalName = String.valueOf(upload_dateien.elementAt(n));
          // Keine Namensänderung
          // Originalname bleibt erhalten
          xmlout.write("<meldung_datei name=\"" + originalName + "\" ziel_name=\"" + originalName + "\"/>");
          xmlout.newLine();
        }
        xmlout.write("</meldung>");
        xmlout.newLine();
      }
      xmlout.write("</meldungen>");
      xmlout.newLine();
      xmlout.write("</dateibereitstellung>");
      xmlout.newLine();
    }
    catch (Exception e)
    {
      throw new MeldungenHolenException("Fehler: Bei der Erstellung derXML-Datei: " + temp + " zur Abholung von Meldungen.\n");
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
   * Setzt server host.
   *
   * @param host server host
   */
  public void setServerHost(String host)
  {
    this.defaultServerHost = host;
  }

  /**
   * Setzt server port.
   *
   * @param port server port
   */
  public void setServerPort(String port)
  {
    this.defaultServerPort = port;
  }

  /**
   * Starte verarbeitung.
   *
   * @return the file
   * @throws MeldungenHolenException the meldungen holen exception
   */
  public File starteVerarbeitung() throws MeldungenHolenException
  {
    this.log.info("Versuche Meldungsbereitstellungsdienst zu kontaktieren: Host:" + this.defaultServerHost + " Port:" + this.defaultServerPort);
    this.werumDienst = new MeldungsBereitstellung(this.defaultServerHost, this.defaultServerPort, this.myKennung, this.myPasswort);
    this.holeMeldungsDaten();
    return this.holeMeldungen();
  }

  /**
   * Validate.
   *
   * @throws MeldungenHolenException the meldungen holen exception
   */
  public void validate() throws MeldungenHolenException
  {
    String logInfo = "'" + this.myKennung + "' (" + this.client + " ) ruft Meldungen fuer Amt '" + this.myAmt + "', Statistik-ID " + this.myStatistik;
    if (this.myBzr != null && this.myBzr.length() > 0)
    {
      logInfo += ", Bzr '" + this.myBzr + "' ab...";
    }
    else
    {
      logInfo += " ab...";
    }
    this.log.info(logInfo);
    if (this.myAmt == null || "".equals(this.myAmt))
    {
      throw new MeldungenHolenException("Kein Amt angegeben!");
    }
    if (this.myStatistik == null || "".equals(this.myStatistik))
    {
      throw new MeldungenHolenException("Keine Statistik_id angegeben!");
    }
    if (this.myConn == null)
    {
      throw new MeldungenHolenException("Keine gueltige Connection zur Datenbank");
    }

    // Hat der SB Rechte an Amt / Statistik ?
    if (!RegDBSecurity.getInstance()
        .sbHasGrantforAmtStatistik(this.mySbId, this.myAmt, this.myStatistik, RegDBSecurity.SADMIN_RECHT_INT, this.myConn))
    {
      throw new MeldungenHolenException("Dem Sachbearbeiter "
          + this.mySbId + " mit Kennung '" + this.myKennung + "' fehlen die erforderlichen Rechte an Amt " + this.myAmt + " / Statistik " + this.myStatistik);
    }

  }
}
