package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

public abstract class AbstractPruefeImport
{
  protected static final String MSG_PRUEFSTART = "Pruefe Segment {0} bis {1} von Datei {2}..";
  protected static final String MSG_PRUEFENDE = "Prüfung mit {0} Fehlern abgeschlossen";
  protected final LoggerIfc log;
  protected final JobBean jobBean;
  protected final SqlUtil sqlUtil;
  protected final PruefUtil pruefUtil;

  public AbstractPruefeImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    log = Logger.getInstance()
      .getLogger(this.getClass());
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.pruefUtil = new PruefUtil(jobBean, sqlUtil);
  }

  public abstract void pruefeDatei() throws JobException;
}
