package de.destatis.regdb.dateiimport.reader;

import de.werum.sis.idev.res.job.JobException;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * The interface Segmented file reader.
 *
 * @param <T> the type parameter
 */
public interface SegmentedFileReader<T>
{
  /**
   * Read segment t.
   *
   * @param path    the path
   * @param charset the charset
   * @param offset  the offset
   * @param len     the len
   * @return the t
   * @throws JobException the job exception
   */
  public T readSegment(Path path, Charset charset, int offset, int len) throws JobException;
}
