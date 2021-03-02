package de.destatis.regdb.dateiimport.reader;

import au.com.bytecode.opencsv.CSVReader;
import de.destatis.regdb.db.FileUtil;
import de.werum.sis.idev.res.job.JobException;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * The type Segmented csv file reader.
 */
public class SegmentedCsvFileReader implements SegmentedFileReader<String[]>
{
  @Override
  public ArrayList<String[]> readSegment(Path path, Charset charset, int offset, int len) throws JobException
  {
    ArrayList<String[]> rows = new ArrayList<>(len);
    try (BufferedReader br = Files.newBufferedReader(path, charset))
    {
      FileUtil.ignoreUtfBom(br, charset.toString());
      int anzahlZeilen = 0;
      try (CSVReader reader = new CSVReader(br, ';', '"', offset))
      {
        String[] cols;
        while ((cols = reader.readNext()) != null && anzahlZeilen < len)
        {
          rows.add(cols);
          anzahlZeilen++;
        }
      }
    } catch (Exception e)
    {
      throw new JobException("Fehler beim Lesen der Datei " + path.toString() + ":" + e.getMessage());
    }
    return rows;

  }
}
