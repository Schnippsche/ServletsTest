package de.destatis.regdb.db;

import de.werum.sis.idev.res.job.JobException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;

/**
 * The type File util.
 */
public class FileUtil
{
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
}
