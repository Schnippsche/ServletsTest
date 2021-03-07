package de.destatis.regdb.dateiimport.reader;

import de.destatis.regdb.db.FileUtil;
import de.werum.sis.idev.res.job.JobException;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * The type Segmented string file reader.
 */
public class SegmentedStringFileReader implements SegmentedFileReader<String>
{

  @Override
  public ArrayList<String> readSegment(Path path, Charset charset, int offset, int len) throws JobException
  {
    ArrayList<String> rows = new ArrayList<>(len);
    try (BufferedReader br = Files.newBufferedReader(path, charset))
    {
      FileUtil.ignoreUtfBom(br, charset.toString());
      int anzahlZeilen = 0;
      int skipRows = 0;
      String row;
      while ((row = br.readLine()) != null && anzahlZeilen < len)
      {
        if (skipRows >= offset)
        {
          rows.add(row);
          anzahlZeilen++;
        }
        else
        {
          skipRows++;
        }
      }
    }
    catch (Exception e)
    {
      throw new JobException("Fehler beim Lesen der Datei " + path.toString() + ":" + e.getMessage());
    }
    return rows;
  }
}