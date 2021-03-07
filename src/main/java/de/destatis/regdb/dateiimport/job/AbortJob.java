package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.db.LoeschUtil;
import de.werum.sis.idev.res.job.JobException;

/**
 * @author Stefan Töngi
 * Hilfsklasse zum Abbrechen von Jobs, die nicht in der Verarbeitungsschlange stehen sondern nur in der Datenbank
 */
public class AbortJob extends AbstractJob
{

  /**
   * @param jobBean the job bean
   */
  public AbortJob(JobBean jobBean)
  {
    super("AbbruchJob", jobBean);
  }

  /**
   * Verarbeite job.
   *
   * @return the abstract job
   */
  @Override
  public AbstractJob verarbeiteJob()
  {

    try
    {
      new LoeschUtil(this.sqlUtil).loescheStandardWerte(this.jobBean.jobId);
    }
    catch (JobException e)
    {
      this.log.error(e.getMessage(), e);
    }

    return null;
  }

}
