package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.db.LoeschUtil;
import de.destatis.regdb.db.StringUtil;
import de.destatis.regdb.servlets.RegDBImportServlet;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.job.JobException;

public class FortsetzenJob extends AbstractJob
{

  public FortsetzenJob(JobBean jobBean)
  {
    super("FortsetzenJob", jobBean);
  }

  @Override
  public AbstractJob verarbeiteJob() throws JobException
  {
    this.log.debug("Verarbeite FortsetzenJob");
    DBConfig config = new DBConfig();
    this.jobBean.importBlockGroesse = StringUtil.getInt(config.getParameter(this.connection, RegDBImportServlet.KONFIGURATION_MAX_FILEROWS));
    this.jobBean.loeschBlockGroesse = StringUtil.getInt(config.getParameter(this.connection, RegDBImportServlet.KONFIGURATION_MAX_LOESCHROWS));
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Setze Import fort..");
    new LoeschUtil(this.sqlUtil).loescheStandardWerte(this.jobBean.jobId);
    return JobFactory.createJob(this.jobBean.getImportdatei().importFormat, this.jobBean);
  }
}
