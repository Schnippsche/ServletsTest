package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.reader.SegmentedFileReader;
import de.destatis.regdb.dateiimport.reader.SegmentedStringFileReader;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Abstract pruefe import.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractPruefeImport<T>
{
  /**
   * The constant MSG_PRUEFSTART.
   */
  protected static final String MSG_PRUEFSTART = "Pruefe Segment {0} bis {1} von Datei {2}..";
  /**
   * The constant MSG_PRUEFENDE.
   */
  protected static final String MSG_PRUEFENDE = "Prüfung mit {0} Fehlern abgeschlossen";
  /**
   * The Log.
   */
  protected final LoggerIfc log;
  /**
   * The Job bean.
   */
  protected final JobBean jobBean;
  /**
   * The Sql util.
   */
  protected final SqlUtil sqlUtil;
  /**
   * The Pruef util.
   */
  protected final PruefUtil pruefUtil;

  /**
   * The Reader.
   */
  protected final SegmentedFileReader<T> reader;

  /**
   * Instantiates a new Abstract pruefe import.
   *
   * @param reader  the reader
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  protected AbstractPruefeImport(SegmentedFileReader reader, JobBean jobBean, SqlUtil sqlUtil)
  {
    log = Logger.getInstance()
      .getLogger(this.getClass());
    this.reader = reader;
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.pruefUtil = new PruefUtil(jobBean, sqlUtil);
  }

  /**
   * Pruefe datei.
   *
   * @throws JobException the job exception
   */
  public void pruefeDatei() throws JobException
  {
    int offset = 0;
    this.jobBean.getImportdatei().anzahlDatensaetze = 0;
    ArrayList<T> rows;
    do
    {
      this.log.debug(MessageFormat.format(MSG_PRUEFSTART, offset, offset + jobBean.importBlockGroesse - 1, jobBean.getImportdatei().getPath().getFileName()));
      rows = reader.readSegment(jobBean.getImportdatei().getPath(), jobBean.getImportdatei().getCharset(), offset, jobBean.importBlockGroesse);
      if (!rows.isEmpty())
      {
        this.jobBean.getImportdatei().anzahlDatensaetze += rows.size();
        validate(rows, offset);
        offset += jobBean.importBlockGroesse;
      }
    } while (!rows.isEmpty() && pruefUtil.isFehlerLimitNichtErreicht() && rows.size() == jobBean.importBlockGroesse);
    this.log.debug(MessageFormat.format(MSG_PRUEFENDE, jobBean.getFormatPruefung().anzahlFehler));
  }

  /**
   * Validate.
   *
   * @param rows   the rows
   * @param offset the offset
   * @throws JobException the job exception
   */
  protected abstract void validate(ArrayList<T> rows, int offset) throws JobException;
}
