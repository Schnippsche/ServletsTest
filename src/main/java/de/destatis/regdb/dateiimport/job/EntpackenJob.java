/*
 * @(#)EntpackenJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.db.FileUtil;
import de.werum.sis.idev.res.job.JobException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * The Class EntpackenJob.
 */
public class EntpackenJob extends AbstractJob
{

  /**
   * Instantiates a new entpacken job.
   *
   * @param jobBean the job bean
   */
  public EntpackenJob(JobBean jobBean)
  {
    super("Entpacken", jobBean);
  }

  /**
   * Verarbeite job.
   *
   * @throws JobException the job exception
   */
  @Override
  public AbstractJob verarbeiteJob() throws JobException
  {
    String dateiImportDir = this.jobBean.getImportdatei().importVerzeichnis;
    // Lese Dateinamen aus und entpacke Datei
    String dateiname = this.jobBean.getImportdatei().gezippterDateiname;
    File zipFile = new File(dateiImportDir, dateiname);
    Path destinationDir = Paths.get(dateiImportDir);
    AbstractJob nextJob = this.entzippe(zipFile, destinationDir);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Entpacken der Daten gestartet...");
    Path propPath = ladeProperties();
    FileUtil.delete(zipFile);
    FileUtil.delete(propPath);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Entpacken der Daten abgeschlossen");
    return nextJob;
  }

  /**
   * Entzippe.
   *
   * @param zipFile        the zip file
   * @param destinationDir the destination dir
   * @throws JobException the job exception
   */
  private AbstractJob entzippe(File zipFile, Path destinationDir) throws JobException
  {
    if (!zipFile.exists())
    {
      throw new JobException("Zip-Datei " + zipFile + " wurde nicht gefunden");
    }

    FileUtil.entzippen(zipFile, destinationDir);

    // Alles erfolgreich dann starte naechsten Job
    this.jobBean.getImportdatei().dateiName = "importdatei.txt";
    return new PruefenJob(this.jobBean);

  }

  private Path ladeProperties() throws JobException
  {
    Path propPath = Paths.get(this.jobBean.getImportdatei().importVerzeichnis, "parameter.prop");
    if (!propPath.toFile().exists())
    {
      throw new JobException("Properties nicht gefunden in Verzeichnis " + this.jobBean.getImportdatei().importVerzeichnis);
    }
    Properties prop = new Properties();
    try (InputStream is = Files.newInputStream(propPath, StandardOpenOption.READ))
    {
      prop.load(is);
    }
    catch (IOException e)
    {
      throw new JobException("Fehler beim Lesen der Properties Datei " + propPath.toString() + ":" + e.getMessage());
    }
    this.jobBean.setzeWerteAusProperties(prop);
    return propPath;
  }

}
