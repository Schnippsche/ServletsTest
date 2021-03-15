package de.destatis.regdb.db;

import de.destatis.regdb.aenderungen.ErgebnisStatus;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
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
   * The constant STRING_UTF8.
   */
  public static final String STRING_UTF8 = "UTF-8";

  private FileUtil()
  {
    // Nothing
  }

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
      } catch (MalformedInputException e)
      {
        throw new JobException("Falscher Zeichensatz!");
      } catch (IOException e)
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
    } catch (IOException e)
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
    } catch (Exception e)
    {
      log.error(MessageFormat.format("Datei {0} konnte nicht gelöscht werden: {1}", path, e.getMessage()));
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
        } else
        {
          delete(file);
        }
      }

      // delete the dir itself
      delete(directory);
    }
  }

  /**
   * kopiert einen InputStream (UTF-8) in eine Datei mit dem angegebenen Zeichensatz
   * Die Datei wird zeilenweise eingelesen, getrennt durch \r, \n oder \r\n  und zeilenweise im Zielzeichensatz mit \r\n gespeichert
   *
   * @param inputStream der zu lesende EingabeStrom
   * @param destination die Ausgabedatei
   * @param toCharset   der Zeichensatz der auszugebenden Datei
   * @return ErgebnisStatus ergebnis status
   */
  public static ErgebnisStatus copyUTF8StreamToExportFile(InputStream inputStream, File destination, String toCharset)
  {
    String line;
    // Falls Quelle UTF Zeichensatz ist, dann prüfe zusätzlich BOM
    if (null == toCharset || toCharset.length() == 0)
      toCharset = "ISO-8859-1";
    boolean writeBOM = (toCharset.contains("BOM"));
    if (toCharset.startsWith(STRING_UTF8))
      toCharset = STRING_UTF8;
    CharsetEncoder csEncoder = StandardCharsets.ISO_8859_1.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    boolean convertError = false;
    // Wirft keine Exception, wenn Zeichen nicht im Zielzeichensatz dargestellt werden kann....
    // Diese Zeichen werden dann als ? markiert
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(destination.toPath()), Charset.forName(toCharset))))
    {
      if (writeBOM)
      {
        writer.write("\uFEFF");
      }
      // Zeilenweise lesen und ausgeben
      while ((line = reader.readLine()) != null)
      {
        writer.write(line);
        writer.write(0x0D); // CR
        writer.write(0x0A); // LF
        if (!convertError && !isConvertible(csEncoder, line, toCharset))
        {
          convertError = true;
        }
      }
    } catch (UnmappableCharacterException e)
    {
      return new ErgebnisStatus(ErgebnisStatus.STATUS_FEHLER, "Der Dateiinhalt kann nicht in dem angegebenen Zeichensatz dargestellt werden. Definieren Sie einen anderen Zeichensatz z.B. UTF-8.");
    } catch (IOException e)
    {
      return new ErgebnisStatus(ErgebnisStatus.STATUS_FEHLER, e.getMessage());
    }
    if (convertError)
      return new ErgebnisStatus(ErgebnisStatus.STATUS_WARNUNG, destination.getAbsolutePath());
    return new ErgebnisStatus(ErgebnisStatus.STATUS_OK, destination.getAbsolutePath());
  }

  private static boolean isConvertible(CharsetEncoder csEncoder, String test, String toCharset)
  {
    if ("ISO-8859-1".equals(toCharset))
    {
      ByteBuffer inputBuffer = ByteBuffer.wrap(test.getBytes(StandardCharsets.UTF_8));
      CharBuffer data = StandardCharsets.UTF_8.decode(inputBuffer);
      try
      {
        csEncoder.encode(data);
      } catch (CharacterCodingException e)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Copy from one Stream into another Stream very fast
   *
   * @param in  InputStream Source
   * @param out OutputStream destination
   * @throws JobException exception
   */
  public static void copyFromStream(InputStream in, OutputStream out) throws JobException
  {
    byte[] buffer = new byte[100000];
    try
    {
      while (true)
      {
        int amountRead = in.read(buffer);
        if (amountRead == -1)
        {
          break;
        }
        out.write(buffer, 0, amountRead);
      }
    } catch (IOException e)
    {
      throw new JobException(e.getMessage());
    } finally
    {
      try
      {
        in.close();
      } catch (IOException e)
      {
        log.error(e.getMessage(), e);
      }
      try
      {
        out.close();
      } catch (IOException e)
      {
        log.error(e.getMessage(), e);
      }
    }

  }
}
