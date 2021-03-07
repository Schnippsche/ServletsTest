package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.FormatError;
import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.DateiImportDaemon;
import de.destatis.regdb.db.PreparedUpdate;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.sql.Connection;

/**
 * The Class AbstractJob.
 */
public abstract class AbstractJob implements Runnable
{
  private static final String SQL_UPDATE_MAIN_JOB = "UPDATE import_verwaltung SET GESAMT_STATUS=?, ZEITPUNKT_AENDERUNG=NOW(), STATISTIK_ID=?, AMT=?, QUELL_REFERENZ_ID=?, BEMERKUNG=?,ANZAHL_NEU = ?, ANZAHL_GEAENDERT = ?, ANZAHL_GELOESCHT = ? WHERE IMPORT_VERWALTUNG_ID=?";

  /**
   * The log.
   */
  protected final LoggerIfc log = Logger.getInstance().getLogger(this.getClass());
  /**
   * The job name.
   */
  protected final String jobName;
  /**
   * The properties.
   */
  protected final JobBean jobBean;
  /**
   * The connection.
   */
  protected Connection connection;
  protected long start;
  protected SqlUtil sqlUtil;

  /**
   * Instantiates a new abstract job.
   *
   * @param jobName the job name
   * @param jobBean the job Bean
   */

  protected AbstractJob(String jobName, JobBean jobBean)
  {
    this.jobName = jobName;
    this.jobBean = jobBean;
  }

  protected AbstractJob(String jobName, JobBean jobBean, SqlUtil sqlUtil)
  {
    this.jobName = jobName;
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
  }

  /**
   * Run.
   */
  @Override
  public void run()
  {
    this.connection = ConnectionTool.getInstance().getConnection();

    if (this.connection == null)
    {
      // gleichen Job nochmal starten
      DateiImportDaemon.getInstance().addJob(this);
      return;
    }

    try
    {
      this.sqlUtil = new SqlUtil(this.connection);
      // ueberpruefe, ob die Datenbank gesperrt ist
      if (this.sqlUtil.dbIsLocked())
      {
        throw new JobException("Die Datenbank ist gesperrt!");
      }
      long startTime = System.currentTimeMillis();
      final AbstractJob nextJob = this.verarbeiteJob();
      long endTime = (System.currentTimeMillis() - startTime) / 1000;
      this.log.info("Job beendet in " + endTime + " Sekunden");
      this.log.debug("Nächster Job:" + nextJob);
      updateDBJob();
      if (nextJob != null)
      {
        DateiImportDaemon.getInstance().addJob(nextJob);

      }
    }
    catch (Throwable e)
    {
      this.jobBean.getFormatPruefung().addFehler(new FormatError(null, e.getMessage()));
      this.jobBean.setStatusAndInfo(JobStatus.FEHLER, "Job abgebrochen:" + e.getMessage());
      this.log.error("Fehler beim Job " + this.jobName + ":" + e.toString(), e);
      updateDBJob();
    }
    finally
    {
      ConnectionTool.getInstance().freeConnection(this.connection);
    }

  }

  /**
   * Verarbeite job.
   *
   * @return the abstract job
   * @throws JobException the job exception
   */
  public abstract AbstractJob verarbeiteJob() throws JobException;

  /**
   * Begin stop watch.
   */
  protected void beginStopWatch()
  {
    this.start = System.currentTimeMillis();
  }

  /**
   * Gets the elapsed time.
   *
   * @return the elapsed time
   */
  protected String getElapsedTime()
  {
    return (System.currentTimeMillis() - this.start) / 1000 + " Sekunden";
  }

  /**
   * Checks if is cancelled.
   *
   * @return true, if is cancelled
   */
  public boolean isCancelled()
  {
    boolean result = DateiImportDaemon.getInstance().isJobAborted(this.jobBean.jobId);

    this.log.debug("isAbort für " + this.jobBean.jobId + " liefert " + result);
    return result;
  }

  /**
   * Update main job.
   */
  protected void updateDBJob()
  {
    this.log.debug("update MainJob");
    try (PreparedUpdate pu = this.sqlUtil.createPreparedUpdate(SQL_UPDATE_MAIN_JOB))
    {
      pu.addValue(this.jobBean.getStatus().toString());
      pu.addValue(this.jobBean.statistikId);
      pu.addValue(this.jobBean.amt);
      pu.addValue(this.jobBean.quellReferenzId);
      pu.addValue(this.jobBean.getInfo());
      pu.addValue(this.jobBean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      pu.addValue(this.jobBean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      pu.addValue(this.jobBean.getAdressen().getIdentifikatoren().getLoeschung().getAnzahl());
      pu.addValue(this.jobBean.jobId);
      pu.update();
    }
    catch (JobException e)
    {
      this.log.error("Job konnte nicht in Datenbank aktualisert werden:" + e.getMessage());
    }
    DateiImportDaemon.getInstance().updateStatusList(this.connection);
  }

  /**
   * @return liefert sqlUtil
   */
  public SqlUtil getSqlUtil()
  {
    return this.sqlUtil;
  }

  /**
   * @param sqlUtil setzt sqlUtil
   */
  public void setSqlUtil(SqlUtil sqlUtil)
  {
    this.sqlUtil = sqlUtil;
  }

}
