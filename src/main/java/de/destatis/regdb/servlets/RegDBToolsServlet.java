/*
 * @(#)RegDBToolsServlet.java 1.00.21.02.2020
 * Copyright 2020 Statistisches Bundesamt
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.dateiimport.job.FortsetzenJob;
import de.destatis.regdb.db.DateiImportDaemon;
import de.destatis.regdb.db.FileUtil;
import de.destatis.regdb.db.LoeschUtil;
import de.destatis.regdb.db.StringUtil;
import de.destatis.regdb.session.RegDBSession;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.secure.ObfuscationAlgorithm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * The Class RegDBToolsServlet.
 */
public class RegDBToolsServlet extends RegDBGeneralHttpServlet
{

  private static final String IMPORT_GESTARTET = "Import gestartet";

  /**
   * The Constant SQL_ERZEUGE_MAINJOB.
   */
  private static final String SQL_ERZEUGE_MAINJOB = "INSERT INTO import_verwaltung SET ZEITPUNKT_START=?,GESAMT_STATUS='AKTIV', DATEINAME=?,BEMERKUNG=?,SACHBEARBEITER_ID=?,QUELL_REFERENZ_ID=?";

  /**
   * The Constant SQL_ABBURCH_MAINJOB.
   */
  private static final String SQL_ABBRUCH_MAINJOB = "UPDATE import_verwaltung SET GESAMT_STATUS=\"ABBRUCH\",BEMERKUNG=\"wird abgebrochen...\", ZEITPUNKT_AENDERUNG=NOW() WHERE IMPORT_VERWALTUNG_ID = {0} AND GESAMT_STATUS !=\"BEENDET\"";

  /**
   * Instantiates a new reg DB tools servlet.
   */
  public RegDBToolsServlet()
  {
    super();
  }

  /**
   * Initialisiert.
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#init()
   */
  @Override
  public void init()
  {
    super.init(RegDBToolsServlet.class);
  }

  /**
   * Do service.
   *
   * @param req     the req
   * @param res     the res
   * @param conn    the conn
   * @param session the session
   * @throws Exception the exception
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#doService(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session) throws Exception
  {
    Object returnMessage = "";
    Object command = this.readCommand(req);
    if (command instanceof Properties)
    {
      Properties prop = (Properties) command;
      String aktion = prop.getProperty("aktion", "none");

      switch (aktion)
      {
        case "obfuscate":
          returnMessage = ObfuscationAlgorithm.obfuscate(prop.getProperty("value").toCharArray());
          break;
        case "deobfuscate":
          returnMessage = new String(ObfuscationAlgorithm.deobfuscate(prop.getProperty("value")));
          break;
        case "erzeugeMainJob":
          returnMessage = this.erzeugeMainJob(prop, conn, session);
          break;
        case "loescheMainJob":
          returnMessage = this.loescheMainJob(prop, conn);
          break;
        case "abbruchMainJob":
          returnMessage = this.abbruchMainJob(prop, conn);
          break;
        case "jobFortsetzen":
          returnMessage = this.jobFortsetzen(prop);
          break;
        default:
          returnMessage = "Aktion " + aktion + " nicht gefunden!";
      }
    }
    else
    {
      this.log.error("Keine Properties Instanz uebergeben!");
    }

    this.sendErgebnis(res, returnMessage);
  }

  private Object jobFortsetzen(Properties prop)
  {
    int jobId = StringUtil.getInt(prop.getProperty("jobId", "0"));
    String dateiImportDir = MelderDatenService.getInstance().getDateiImportDir();
    File jobFile = Paths.get(dateiImportDir, "" + jobId, "" + jobId + ".xml").toFile();
    this.log.info("Fortsetzung von Job " + jobFile.toString());
    if (jobFile.exists())
    {
      JobBean jobBean = JAXB.unmarshal(jobFile, JobBean.class);
      jobBean.zeitpunktEintrag = jobBean.getCurrentZeitpunkt();
      jobBean.getSimulation().importSimulieren = false;
      DateiImportDaemon.getInstance().addJob(new FortsetzenJob(jobBean));
      return "";
    }
    return new FileNotFoundException("JobDaten nicht gefunden:" + jobFile.toString());
  }

  /**
   * Erzeuge main import job.
   *
   * @param prop    the properties
   * @param conn    the connection
   * @param session the session
   * @return the object (main Job Id or error)
   */
  private Object erzeugeMainJob(Properties prop, Connection conn, RegDBSession session)
  {
    // Anstarten des Services zwecks Generierung von Melderdaten
    MelderDatenService.getInstance();
    // Main Eintrag erzeugen
    String zeit = prop.getProperty("zeit");
    String dateiname = prop.getProperty("dateiname", "Dateiname nicht angegeben");
    String format = prop.getProperty("format", "Format nicht angegeben");
    String quellRefId = prop.getProperty("quellrefid", "0");
    if (zeit == null)
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      zeit = formatter.format(new Date());
    }
    try (PreparedStatement ps = conn.prepareStatement(SQL_ERZEUGE_MAINJOB, Statement.RETURN_GENERATED_KEYS))
    {
      ps.setString(1, zeit);
      ps.setString(2, dateiname);
      ps.setString(3, IMPORT_GESTARTET + " (" + format + ")");
      ps.setString(4, session.getSachbearbeiterId());
      ps.setString(5, quellRefId);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys())
      {
        if (rs.next())
        {
          return rs.getString(1);
        }
      }

      DateiImportDaemon.getInstance().updateStatusList(conn);
      throw new SQLException("insert lieferte keinen Key!");
    }
    catch (SQLException e)
    {
      this.log.error("Fehler beim Erzeugen des Main Import Jobs:" + e.getMessage());
      return e;
    }
  }

  /**
   * Loesche main job.
   *
   * @param prop the prop
   * @param conn the conn
   * @return the object
   */
  private Object loescheMainJob(Properties prop, Connection conn) throws JobException
  {
    Integer jobId = StringUtil.getInt(prop.getProperty("jobId", "0"));
    String dateiImportDir = MelderDatenService.getInstance().getDateiImportDir();
    File jobDirectory = Paths.get(dateiImportDir, "" + jobId).toFile();
    FileUtil.deleteDirectory(jobDirectory);
    LoeschUtil util = new LoeschUtil(conn);
    util.loescheStandardWerte(jobId);
    util.loescheImport(jobId);
    DateiImportDaemon.getInstance().updateStatusList(conn);
    return "";
  }

  /**
   * Abbruch main job.
   *
   * @param prop the prop
   * @param conn the conn
   * @return the object
   */
  private Object abbruchMainJob(Properties prop, Connection conn)
  {
    Integer jobId = StringUtil.getInt(prop.getProperty("jobId", "0"));
    try (Statement stmt = conn.createStatement())
    {
      stmt.executeUpdate(MessageFormat.format(SQL_ABBRUCH_MAINJOB, jobId));
      DateiImportDaemon.getInstance().abortJob(jobId);
      DateiImportDaemon.getInstance().updateStatusList(conn);

    }
    catch (SQLException e)
    {
      this.log.error(e.getMessage(), e);
      return e;
    }
    return "";
  }

  @Override
  public void destroy()
  {
    super.destroy();
    MelderDatenService.getInstance().destroy();
  }
}
