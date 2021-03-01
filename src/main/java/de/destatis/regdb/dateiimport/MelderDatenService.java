/*
 * @(#)MelderDatenService.java 1.00.21.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport;

import java.io.File;
import java.sql.Connection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.servlets.RegDBImportServlet;
import de.werum.sis.idev.intern.actions.util.MelderDaten;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

/**
 * The Class MelderDatenService.
 */
public class MelderDatenService
{

  /** The Constant KONFIGURATION_DATEIIMPORT_DIRECTORY. */
  public static final String KONFIGURATION_DATEIIMPORT_DIRECTORY = "int_dateiimport_directory";

  /** The Constant KONFIGURATION_MELDERDATEN_POOLSIZE. */
  public static final String KONFIGURATION_MELDERDATEN_POOLSIZE = "int_melderschluessel_poolsize";

  /** The Constant KONFIGURATION_MELDERDATEN_THREADS. */
  public static final String KONFIGURATION_MELDERDATEN_THREADS = "int_melderschluessel_threads";

  /** The Constant log. */
  protected static final LoggerIfc log = Logger.getInstance()
      .getLogger(MelderDatenService.class);
  private static MelderDatenService instance;
  private BlockingQueue<MelderDaten> blockingQueue;
  private String dateiImportDir;
  private ExecutorService executor;

  /**
   * Gets the single instance of MelderDatenService.
   *
   * @return single instance of MelderDatenService
   */
  public static synchronized MelderDatenService getInstance()
  {
    if (instance == null)
    {
      instance = new MelderDatenService();
      instance.init();
    }
    return instance;
  }

  /**
   * Initialisiert.
   */
  private void init()
  {
    DBConfig config = new DBConfig();
    Connection conn = ConnectionTool.getInstance()
        .getConnection();
    String poolSize = "2000";
    String poolThreads = "4";
    String strTmpDir = System.getProperty("java.io.tmpdir");

    if (conn != null)
    {
      String newValue = config.getParameter(conn, KONFIGURATION_MELDERDATEN_POOLSIZE);
      poolSize = (newValue != null) ? newValue : poolSize;
      newValue = config.getParameter(conn, KONFIGURATION_MELDERDATEN_THREADS);
      poolThreads = (newValue != null) ? newValue : poolThreads;
      newValue = config.getParameter(conn, DBConfig.INT_TEMP_DIRECTORY);
      strTmpDir = (newValue != null ? newValue : strTmpDir);
      this.dateiImportDir = config.getParameter(conn, KONFIGURATION_DATEIIMPORT_DIRECTORY);
    }
    log.debug("Pruefe Serverimport-Verzeichnis...");
    if (this.dateiImportDir == null)
    {
      this.dateiImportDir = new File(strTmpDir).getParent();
      File importDir = new File(this.dateiImportDir, "dateiimport");
      if (!importDir.exists())
      {
        importDir.mkdir();
      }
      this.dateiImportDir = importDir.toString();
      // Ersetze \ durch /
      this.dateiImportDir = this.dateiImportDir.replace('\\', '/');
      if (conn != null)
      {
        config.setParameter(conn, KONFIGURATION_DATEIIMPORT_DIRECTORY, this.dateiImportDir, RegDBImportServlet.INTERN);
      }
    }
    else
    {
      File importDir = new File(this.dateiImportDir);
      if (!importDir.exists())
      {
        importDir.mkdir();
      }
    }
    log.debug("Verwende Verzeichnis " + this.dateiImportDir + " fuer Serverimporte");
    ConnectionTool.getInstance()
        .freeConnection(conn);
    int maximumMelderDaten;
    int maximumThreads;

    try
    {
      maximumMelderDaten = Integer.parseInt(poolSize);
      if (maximumMelderDaten < 100 || maximumMelderDaten > 500000)
      {
        throw new NumberFormatException();
      }
    }
    catch (NumberFormatException e)
    {
      log.error("Wert fuer " + KONFIGURATION_MELDERDATEN_POOLSIZE + " ist ungueltig:" + poolSize + " muss zwischen 100 und 500000 liegen!");
      maximumMelderDaten = 2000;
    }

    try
    {
      maximumThreads = Integer.parseInt(poolThreads);
      if (maximumThreads < 1 || maximumThreads > 256)
      {
        throw new NumberFormatException();
      }
    }
    catch (NumberFormatException e)
    {
      log.error("Wert fuer " + KONFIGURATION_MELDERDATEN_THREADS + " ist ungueltig:" + poolThreads + " muss zwischen 1 und 256 liegen!");
      maximumThreads = 4;
    }

    log.info("Erzeuge MelderDatenService mit " + maximumThreads + " Threads und " + maximumMelderDaten + " Melderdatensaetzen im Pool");
    this.blockingQueue = new LinkedBlockingDeque<>(maximumMelderDaten);

    this.executor = Executors.newFixedThreadPool(maximumThreads, new OwnThreadFactory());
    for (int i = 0; i < maximumThreads; i++)
    {
      this.executor.execute(new MelderDatenProducer(this.blockingQueue));
    }    
  }

  /**
   * Liefert melder daten.
   *
   * @return melder daten
   */
  public MelderDaten getMelderDaten()
  {
    try
    {
      return this.blockingQueue.poll(60, TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      log.error("MelderDaten abholen wegen Timeout abgebrochen!");
      Thread.currentThread()
          .interrupt();
    }
    return null;
  }

  /**
   * Liefert datei import dir.
   *
   * @return datei import dir
   */
  public String getDateiImportDir()
  {
    return this.dateiImportDir;
  }

  /**
   * Beendet den Service und damit auch alle Producer.
   */
  public void destroy()
  {
    if (this.executor != null)
    {
      log.info("Beende MelderDatenService...");
      this.executor.shutdownNow();
    }
  }
}
