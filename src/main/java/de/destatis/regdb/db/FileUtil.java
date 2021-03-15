package de.destatis.regdb.db;

import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The type File util.
 */
public class FileUtil
{
  /**
   * The constant log.
   */
  protected static final LoggerIfc log = Logger.getInstance().getLogger(FileUtil.class);
  private static final int UTF_BOM = '\ufeff';

  /**
   * Ignore utf bom.
   *
   * @param reader  the reader
   * @param charset the charset
   * @throws JobException the job exception
   */
  public static void ignoreUtfBom(BufferedReader reader, String charset) throws JobException
  {
    if (charset != null && charset.toUpperCase().startsWith("UTF"))
    {
      try
      {
        reader.mark(4);
        if (UTF_BOM != reader.read())
        {
          reader.reset();
        }
      }
      catch (MalformedInputException e)
      {
        throw new JobException("Falscher Zeichensatz!");
      }
      catch (IOException e)
      {
        throw new JobException(e.getMessage(), e);
      }
    }
  }

  /**
   * Make directory.
   *
   * @param file the file
   */
  public static void makeDirectory(File file)
  {
    if (file == null || file.exists())
    {
      return;
    }

    if (!file.mkdir())
    {
      log.debug("Verzeichnis " + file.toString() + " konnte nicht angelegt werden");
    }
  }

  /**
   * Entzippen.
   *
   * @param zipFile     the zip file
   * @param destination the destination
   * @throws JobException the job exception
   */
  public static void entzippen(File zipFile, Path destination) throws JobException
  {
    log.debug("Entpacke " + zipFile + " nach " + destination.toString());
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
    {
      int count = 0;
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null)
      {
        log.debug("ZipEntry:" + zipEntry.toString());
        Path dest = destination.resolve(zipEntry.getName());
        Files.createDirectories(dest);
        if (!zipEntry.isDirectory())
        {
          Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
          count++;
        }
        zipEntry = zis.getNextEntry();
      }
      log.debug("Es wurden " + count + " Dateien entpackt");
    }
    catch (IOException e)
    {
      log.error(e.getMessage(), e);
      throw new JobException("Fehler beim Entzippen der Datei " + zipFile.getName() + ":" + e.getMessage());
    }
  }

  /**
   * Delete.
   *
   * @param path the path
   */
  public static void delete(Path path)
  {
    if (path == null)
    {
      return;
    }
    try
    {
      log.debug("Entferne Datei " + path.toString());
      Files.deleteIfExists(path);
    }
    catch (Exception e)
    {
      log.error(MessageFormat.format("Datei {0} konnte nicht gelöscht werden: {1}", path.toString(), e.getMessage()));
    }
  }

  /**
   * Delete.
   *
   * @param file the file
   */
  public static void delete(File file)
  {
    if (file != null)
    {
      delete(file.toPath());
    }
  }

  /**
   * Loescht ein Verzeichnis samt Unterverzeichnissen und dateien.
   *
   * @param directory the directory
   */
  public static void deleteDirectory(File directory)
  {
    if (directory == null)
    {
      return;
    }
    File[] filesInDir = directory.listFiles();

    if (filesInDir != null) // else there are no directories
    {
      // delete all files in the dir including subdirectories
      for (File file : filesInDir)
      {
        if (file.isDirectory())
        {
          deleteDirectory(file);
        }
        else
        {
          delete(file);
        }
      }

      // delete the dir itself
      if (!directory.delete())
      {
        log.error("Verzeichnis " + directory + " konnte nicht entfernt werden");
      }
    }
  }
}
