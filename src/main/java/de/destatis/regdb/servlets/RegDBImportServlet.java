/*
 * @(#)RegDBImportServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.dateiimport.job.EntpackenJob;
import de.destatis.regdb.db.DateiImportDaemon;
import de.destatis.regdb.db.StringUtil;
import de.destatis.regdb.session.RegDBSession;
import de.werum.sis.idev.res.conf.db.DBConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;

/**
 * Dient zum Importieren von Daten asynchron auf dem Server
 *
 * @author Stefan Toengi (Destatis)
 */
public class RegDBImportServlet extends RegDBGeneralHttpServlet
{

  public static final String INTERN = "INTERN";

  public static final String UPDATE_TBL_SERVERIMPORT = "UPDATE import_verwaltung SET GESAMT_STATUS='BEENDET',ZEITPUNKT_AENDERUNG=NOW(),BEMERKUNG='Abbruch durch Serverneustart',ERGEBNIS_STATUS='FEHLER' WHERE GESAMT_STATUS != 'BEENDET'";

  public static final String CREATE_TBL_SERVERIMPORT = "CREATE TABLE IF NOT EXISTS import_verwaltung (" + "	IMPORT_VERWALTUNG_ID INT(11) NOT NULL AUTO_INCREMENT," + "	ZEITPUNKT_START DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'," + "	ZEITPUNKT_AENDERUNG DATETIME NULL DEFAULT NULL," + "	ZEITPUNKT_ENDE DATETIME NULL DEFAULT NULL," + "	GESAMT_STATUS ENUM('AKTIV','BEENDET','ABBRUCH') NOT NULL DEFAULT 'AKTIV'," + " ERGEBNIS_STATUS ENUM('','OK','FEHLER')," + "	DATEINAME MEDIUMTEXT NOT NULL ," + "	STATISTIK_ID INT(11) NULL DEFAULT NULL," + "	AMT CHAR(2) NOT NULL DEFAULT '' ," + "	QUELL_REFERENZ_ID INT(11) NULL DEFAULT NULL," + "	BEMERKUNG MEDIUMTEXT NULL ," + "	ANZAHL_NEU INT(11) NOT NULL DEFAULT '0'," + "	ANZAHL_GEAENDERT INT(11) NOT NULL DEFAULT '0'," + "	ANZAHL_GELOESCHT INT(11) NOT NULL DEFAULT '0'," + "	SACHBEARBEITER_ID INT(11) NOT NULL DEFAULT '0'," + "	PRIMARY KEY (IMPORT_VERWALTUNG_ID)," + "	INDEX serverimport_sb_index (SACHBEARBEITER_ID)," + "	INDEX serverimport_quellref_index (QUELL_REFERENZ_ID)" + ") COLLATE='utf8mb4_unicode_ci' ENGINE=InnoDB;";
  public static final String CREATE_TBL_SERVERIMPORT_TEIL = "CREATE TABLE IF NOT EXISTS import_teil (" + " IMPORT_TEIL_ID INT(11) NOT NULL AUTO_INCREMENT," + "	IMPORT_VERWALTUNG_ID INT(11) NOT NULL DEFAULT 0," + "	ZEITPUNKT_START DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'," + "	ZEITPUNKT_ENDE DATETIME NULL DEFAULT NULL," + "	ERGEBNIS_STATUS ENUM('','OK','FEHLER') NOT NULL DEFAULT '' ," + "	VORGANG VARCHAR(100) NOT NULL DEFAULT '' ," + "	BEMERKUNG MEDIUMTEXT NULL ," + "	ANZAHL_NEU INT(11) NOT NULL DEFAULT '0'," + "	ANZAHL_GEAENDERT INT(11) NOT NULL DEFAULT '0'," + "	ANZAHL_GELOESCHT INT(11) NOT NULL DEFAULT '0'," + "	SACHBEARBEITER_ID INT(11) NOT NULL DEFAULT '0'," + "	PRIMARY KEY (IMPORT_TEIL_ID)" + ") COLLATE='utf8mb4_unicode_ci' ENGINE=InnoDB;";

  /**
   * The Constant KONFIGURATION_MAX_FILEROWS.
   */
  public static final String KONFIGURATION_MAX_FILEROWS = "int_dateiimport_limit";

  public static final String KONFIGURATION_MAX_LOESCHROWS = "int_dateiimport_loeschlimit";

  /**
   * The Constant JOB_CONTEXT_IMPORTZIPDATEI.
   */
  public static final String JOB_CONTEXT_IMPORTZIPDATEI = "importzipdatei";

  public static final String JOB_CONTEXT_SACHBEARBEITER_ID = "sbid";
  public static final Object JOB_CONTEXT_JOBEAN = "jobbean";

  /**
   * Instantiates a new reg DB import servlet.
   */
  public RegDBImportServlet()
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
    super.init(RegDBImportServlet.class);
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
    this.log.debug("Import Service...");
    DBConfig config = new DBConfig();
    String mainJobId = req.getParameter("mainJobId");
    if (mainJobId == null)
    {
      throw new Exception("Job ID Parameter fehlt");
    }

    Path dest = Paths.get(MelderDatenService.getInstance().getDateiImportDir(), mainJobId);
    this.log.info("Destination:" + dest);
    Files.createDirectories(dest);
    File f = new File(dest.toString(), mainJobId + ".zip");
    this.log.info("File = " + f.toString());
    Files.copy(req.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
    this.log.debug("Datei wurde nach " + f.toString() + " kopiert");

    String maxFileRows = config.getParameter(conn, KONFIGURATION_MAX_FILEROWS);
    if (maxFileRows == null)
    {
      maxFileRows = "2000";
      config.setParameter(conn, KONFIGURATION_MAX_FILEROWS, maxFileRows, INTERN);
    }
    String maxLoeschRows = config.getParameter(conn, KONFIGURATION_MAX_LOESCHROWS);
    if (maxLoeschRows == null)
    {
      maxLoeschRows = "4000";
      config.setParameter(conn, KONFIGURATION_MAX_LOESCHROWS, maxLoeschRows, INTERN);
    }
    JobBean bean = new JobBean();
    bean.getImportdatei().importVerzeichnis = dest.toString();
    bean.importBlockGroesse = StringUtil.getInt(maxFileRows);
    bean.loeschBlockGroesse = StringUtil.getInt(maxLoeschRows);
    bean.getImportdatei().gezippterDateiname = f.getName();
    bean.sachbearbeiterId = StringUtil.getInt(session.getSachbearbeiterId());
    bean.sachbearbeiterKennung = session.getSachbearbeiterKennung();
    bean.sachbearbeiterPasswort = session.getSachbearbeiterPasswort();
    bean.jobId = StringUtil.getInt(mainJobId);

    // Starte Job
    DateiImportDaemon.getInstance().addJob(new EntpackenJob(bean));
    res.setContentType("text/html");
    this.log.info("Upload von Zip Datei " + f.getName() + " fuer DateiImport erfolgreich. ");
  }

}
