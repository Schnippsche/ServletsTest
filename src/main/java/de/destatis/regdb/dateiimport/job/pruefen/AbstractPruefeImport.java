package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

/**
 * The type Abstract pruefe import.
 */
public abstract class AbstractPruefeImport
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
   * Instantiates a new Abstract pruefe import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public AbstractPruefeImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    log = Logger.getInstance()
      .getLogger(this.getClass());
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.pruefUtil = new PruefUtil(jobBean, sqlUtil);
  }

  /**
   * Pruefe datei.
   *
   * @throws JobException the job exception
   */
  public abstract void pruefeDatei() throws JobException;
}
