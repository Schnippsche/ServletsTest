/*
 * @(#)AbstractImportJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * The type Abstract import job.
 */
public abstract class AbstractImportJob extends AbstractJob
{

  /**
   * The constant MSG_AUSGEFUEHRT_IN.
   */
  protected static final String MSG_AUSGEFUEHRT_IN = " ausgefuehrt in ";
  private static final String MSG_DATEI_OFFSET = "{0} bis {1} von {2}";
  /**
   * The First set.
   */
  protected int firstSet;
  /**
   * The Last set.
   */
  protected int lastSet;

  /**
   * Instantiates a new abstract import job.
   *
   * @param jobName the job name
   * @param jobBean the job bean
   */
  protected AbstractImportJob(String jobName, JobBean jobBean)
  {
    super(jobName, jobBean);
  }

  /**
   * Instantiates a new Abstract import job.
   *
   * @param jobName the job name
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  protected AbstractImportJob(String jobName, JobBean jobBean, SqlUtil sqlUtil)
  {
    super(jobName, jobBean, sqlUtil);
  }

  /**
   * Verarbeite job.
   *
   * @throws JobException the job exception
   */
  @Override
  public AbstractJob verarbeiteJob() throws JobException
  {
    this.log.debug("Verarbeite Job");
    this.firstSet = this.jobBean.getImportdatei().datensatzOffset + 1;
    this.lastSet = Math.min(this.jobBean.getImportdatei().datensatzOffset + this.jobBean.importBlockGroesse, this.jobBean.getImportdatei().anzahlDatensaetze);
    String offset = MessageFormat.format(MSG_DATEI_OFFSET, this.firstSet, this.lastSet, this.jobBean.getImportdatei().anzahlDatensaetze);
    this.log.info("Verarbeite Saetze " + offset + " aus Datei " + this.jobBean.getImportdatei().originalDateiname);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Verarbeite Saetze " + offset);
    if (this.jobBean.getImportdatei().datensatzOffset == 0)
    {
      this.doBeforeFirstImport();
    }

    if (this.isCancelled())
    {
      this.jobBean.setStatusAndInfo(JobStatus.ABBRUCH, "Der Import wurde abgebrochen");
      return null;
    }
    this.doNormalImport();
    this.jobBean.getImportdatei().datensatzOffset += this.jobBean.importBlockGroesse;
    if (this.jobBean.getImportdatei().datensatzOffset < this.jobBean.getImportdatei().anzahlDatensaetze)
    {
      return this.nextImportJob();
    }
    return this.doAfterLastImport();
  }

  /**
   * Start transaction.
   *
   * @throws JobException the job exception
   */
  public void startTransaction() throws JobException
  {
    long st = System.currentTimeMillis();
    this.log.debug("Transaction starts...");
    try
    {
      this.sqlUtil.dbBeginTransaction();
      this.doInTransaction();
      this.sqlUtil.dbCommit();
    }
    catch (Throwable throwable)
    {
      this.log.error(throwable.getMessage(), throwable);
      this.log.info("Versuche Rollback...");
      this.sqlUtil.dbRollback();
      this.log.info("Rollback durchgeführt");
    }
    finally
    {
      this.sqlUtil.dbEndTransaction();
    }
    String zeit = (System.currentTimeMillis() - st) / 1000 + " seconds";
    this.log.debug("Transaction ends in " + zeit);
  }

  /**
   * Do before first import.
   *
   * @throws JobException the job exception
   */
  protected void doBeforeFirstImport() throws JobException
  {
    // Nothing special
  }

  /**
   * Do normal import.
   *
   * @throws JobException the job exception
   */
  protected abstract void doNormalImport() throws JobException;

  /**
   * Next import job.
   *
   * @return the abstract job
   */
  protected abstract AbstractJob nextImportJob();

  /**
   * Do after last import.
   *
   * @return the abstract job
   * @throws JobException the job exception
   */
  protected abstract AbstractJob doAfterLastImport() throws JobException;

  /**
   * Do in transaction.
   *
   * @throws SQLException the SQL exception
   * @throws JobException the job exception
   */
  protected abstract void doInTransaction() throws SQLException, JobException;
}
