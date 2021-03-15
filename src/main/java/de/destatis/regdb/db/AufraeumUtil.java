package de.destatis.regdb.db;

import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.dateiimport.job.AuswirkungenJob;
import de.destatis.regdb.servlets.RegDBImportServlet;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AufraeumUtil
{

  public static final String KONFIGURATION_LOESCH_INTERVAL = "int_dateiimport_loeschinterval";
  public static final String PROKOLL_DATEINAME = "protokolle.zip";
  protected static final String SQL_ABGELAUFENE_JOBS = "SELECT KEY3 FROM standardwerte WHERE KONFIGURATION_ID=\"PRUEFLAUF\" AND NOW() > DATE_ADD(ZEITPUNKT_EINTRAG, INTERVAL {0} MINUTE) LIMIT 1";

  /**
   * The log.
   */
  protected final LoggerIfc log;
  private final String dateiImportDir;
  private String aufraeumInterval;

  /**
   * Instantiates a new aufraeum util.
   */
  public AufraeumUtil()
  {
    this.dateiImportDir = MelderDatenService.getInstance().getDateiImportDir();
    this.log = Logger.getInstance().getLogger(this.getClass());
  }

  /**
   * Entferne dateien.
   *
   * @param mainJobId the main job id
   */
  public void entferneDateien(Integer mainJobId)
  {
    String importVerzeichnis = Paths.get(this.dateiImportDir, "" + mainJobId).toString();
    this.log.debug("entferneDateien aus Verzeichnis " + importVerzeichnis);
    Path importDateiPath = Paths.get(importVerzeichnis, "importdatei.txt");
    FileUtil.delete(importDateiPath);
  }

  /**
   * Erzeuge protokoll archiv.
   *
   * @param mainJobId     the main job id
   * @param zielDateiName the ziel datei name
   */
  public void erzeugeProtokollArchiv(String mainJobId, String zielDateiName)
  {
    String importVerzeichnis = Paths.get(this.dateiImportDir, mainJobId).toString();
    this.log.debug("erzeuge Protokoll Archiv " + zielDateiName + " in Verzeichnis " + importVerzeichnis);
    // LoeschProtokolle zippen
    Path protokoll = Paths.get(importVerzeichnis, zielDateiName);
    List<Path> pfade = new ArrayList<>();
    pfade.add(Paths.get(importVerzeichnis, LoeschUtil.LOESCH_PROTOKOLL_ADRESSEN));
    pfade.add(Paths.get(importVerzeichnis, LoeschUtil.LOESCH_PROTOKOLL_FIRMEN));
    pfade.add(Paths.get(importVerzeichnis, LoeschUtil.LOESCH_PROTOKOLL_MELDER));
    pfade.add(Paths.get(importVerzeichnis, AuswirkungenJob.LOESCH_PROTOKOLL_ADRESSEN_KANDIDATEN));
    pfade.add(Paths.get(importVerzeichnis, AuswirkungenJob.LOESCH_PROTOKOLL_FIRMEN_KANDIDATEN));
    pfade.add(Paths.get(importVerzeichnis, AuswirkungenJob.LOESCH_PROTOKOLL_MELDER_KANDIDATEN));
    zippeDateien(pfade, protokoll);
  }

  /**
   * Zippe dateien.
   *
   * @param sources     the sources
   * @param destination the destination
   * @return true, if successful
   */
  public boolean zippeDateien(List<Path> sources, Path destination)
  {
    if (destination == null || !Files.exists(destination.getParent()))
    {
      this.log.debug("Parent Pfad existiert nicht: " + destination);
      return false;
    }
    boolean fileExists = false;
    try (OutputStream os = Files.newOutputStream(destination); ZipOutputStream zos = new ZipOutputStream(os))
    {
      for (Path path : sources)
      {
        if (Files.exists(path))
        {
          this.log.debug("zippe " + path);
          ZipEntry e = new ZipEntry(path.getFileName().toString());
          zos.putNextEntry(e);
          Files.copy(path, zos);
          zos.closeEntry();
          fileExists = true;
        }
        else
        {
          this.log.debug("Datei zum Zippen existiert nicht:" + path);
        }
      }
    }
    catch (IOException e)
    {
      this.log.error("Fehler beim Zippen:" + e.getMessage(), e);
    }
    return fileExists;
  }

  public void entferneErstenAbgelaufenenJob()
  {
    this.log.debug("ermittleAbgelaufeneJobs");
    Connection conn = ConnectionTool.getInstance().getConnection();
    DBConfig config = new DBConfig();
    SqlUtil sqlUtil = new SqlUtil(conn);
    if (this.aufraeumInterval == null)
    {
      String newValue = config.getParameter(conn, KONFIGURATION_LOESCH_INTERVAL);
      this.log.debug("Ermittle Konfigurationswert fuer " + KONFIGURATION_LOESCH_INTERVAL);
      if (newValue == null)
      {
        this.aufraeumInterval = "3";
        config.setParameter(conn, KONFIGURATION_LOESCH_INTERVAL, this.aufraeumInterval, RegDBImportServlet.INTERN);
      }
      else
      {
        this.aufraeumInterval = newValue;
      }
      this.log.debug("Loeschinterval betraegt " + this.aufraeumInterval + " Stunden");
    }
    int minutes = Integer.parseInt(this.aufraeumInterval) * 60;
    String sql = MessageFormat.format(SQL_ABGELAUFENE_JOBS, "" + minutes);
    try
    {
      ResultRow row = sqlUtil.fetchOne(sql);
      if (row != null)
      {
        int mainJobId = row.getInt(1);
        this.log.debug("Job " + mainJobId + " ist abgelaufen...");
        LoeschUtil loeschUtil = new LoeschUtil(conn);
        loeschUtil.loescheStandardWerte(mainJobId);
        loeschUtil.loescheImport(mainJobId);
        DateiImportDaemon.getInstance().updateStatusList(conn);
        entferneDateien(mainJobId);
      }
      else
      {
        this.log.debug("keinen abgelaufenen Job ermittelt");
      }
    }
    catch (Throwable e)
    {
      this.log.error("Fehler :" + e.toString(), e);
    }
    finally
    {
      ConnectionTool.getInstance().freeConnection(conn);
    }
  }
}
