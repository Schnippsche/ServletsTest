/*
 * @(#)EntpackenJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.werum.sis.idev.res.job.JobException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    try
    {
      Files.deleteIfExists(zipFile.toPath());
      Files.deleteIfExists(propPath);
    }
    catch (IOException e)
    {
      this.log.error("Datei konnte nicht geloescht werden:" + e.getMessage());
    }
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
    this.log.debug("entzippe " + zipFile.toString());
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
    {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null)
      {
        this.log.debug("ZipEntry:" + zipEntry.toString());
        Path dest = destinationDir.resolve(zipEntry.getName());
        this.log.debug(dest.toString());
        Files.createDirectories(dest);
        if (!zipEntry.isDirectory())
        {
          this.log.debug("Kopiere Datei nach " + dest);
          Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        zipEntry = zis.getNextEntry();
      }
      // Alles erfolgreich dann starte naechsten Job     
      this.jobBean.getImportdatei().dateiName = "importdatei.txt";
      return new PruefenJob(this.jobBean);
    }
    catch (IOException e)
    {
      this.log.error(e.getMessage(), e);
      throw new JobException("Fehler beim Entzippen der Datei " + zipFile.getName() + ":" + e.getMessage());
    }
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
