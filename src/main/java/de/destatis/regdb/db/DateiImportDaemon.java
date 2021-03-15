/*
 * @(#)DateiImportDaemon.java 1.00.19.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.db;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.ServerimportStatusBean;
import de.destatis.regdb.dateiimport.job.AbortJob;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class DateiImportDaemon
{
  private static DateiImportDaemon instance = null;
  private final LinkedList<AbstractJob> jobs;
  private final LoggerIfc log;
  private final ArrayList<ServerimportStatusBean> serverimportStatusBeans;
  private final HashSet<Integer> abortedJobs;
  private Thread thread;
  private final AufraeumUtil aufraeumUtil;

  /**
   * Instantiates a new datei import daemon.
   */
  private DateiImportDaemon()
  {
    super();
    this.jobs = new LinkedList<>();
    this.serverimportStatusBeans = new ArrayList<>();
    this.abortedJobs = new HashSet<>();
    this.aufraeumUtil = new AufraeumUtil();
    this.log = Logger.getInstance().getLogger(this.getClass());
    this.log.info("initing " + this.getClass());
  }

  /**
   * Gets the single instance of DateiImportDaemon.
   *
   * @return single instance of DateiImportDaemon
   */
  public static synchronized DateiImportDaemon getInstance()
  {
    if (instance == null)
    {
      instance = new DateiImportDaemon();
    }
    return instance;
  }

  public synchronized void checkJobs()
  {
    try
    {
      if (!this.jobs.isEmpty())
      {
        if (this.jobs.size() == 1)
        {
          this.log.info("es wartet ein Job auf Verarbeitung...");
        }
        else
        {
          this.log.info("es warten " + this.jobs.size() + " Jobs auf Verarbeitung...");
        }
        if (this.thread == null || this.thread.getState() == State.TERMINATED)
        {
          AbstractJob job = this.jobs.poll();
          if (job != null)
          {
            this.log.info("Bearbeite Job " + job.getClass().getCanonicalName());
            this.thread = new Thread(job);
            this.thread.start();
          }
        }
      }
      else
      {
        this.log.info("keine Importjobs zum Verarbeiten");
        // Sind noch Jobs zum Abbrechen markiert, obwohl kein Job mehr läuft ? Diese müssen nur in der Datenbank beendet werden
        // erzeuge dazu einen Abbruch Job und stelle ihn hinten an
        if (!this.abortedJobs.isEmpty())
        {
          for (Integer id : this.abortedJobs)
          {
            JobBean jobBean = new JobBean();
            jobBean.jobId = id;
            this.addJob(new AbortJob(jobBean));
          }
          this.abortedJobs.clear();
        }
        // Abgelaufene Jobs ermitteln
        this.aufraeumUtil.entferneErstenAbgelaufenenJob();
      }
    }

    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
    }

  }

  /**
   * Adds the job.
   *
   * @param job the job
   */
  public synchronized void addJob(AbstractJob job)
  {
    this.log.debug("add Job " + job);
    this.jobs.add(job);
  }

  public synchronized void abortJob(Integer jobId)
  {
    this.log.debug("abort Main Job " + jobId);
    this.abortedJobs.add(jobId);
  }

  public synchronized void removeAbortJob(Integer jobId)
  {
    this.log.debug("remove AbortJob " + jobId);
    this.abortedJobs.remove(jobId);
  }

  public boolean isJobAborted(int jobId)
  {
    return this.abortedJobs.contains(jobId);
  }

  /**
   * Update status list.
   *
   * @param conn the conn
   */
  public synchronized void updateStatusList(Connection conn)
  {
    this.log.debug("update ServerStatus Infos");
    this.serverimportStatusBeans.clear();
    try (ResultSet rs = conn.createStatement().executeQuery(ServerimportStatusBean.SQL_SERVERIMPORTSTATUS_SELECT))
    {
      while (rs.next())
      {
        ServerimportStatusBean sisb = new ServerimportStatusBean(rs);
        this.serverimportStatusBeans.add(sisb);
      }
    }
    catch (SQLException e)
    {
      this.log.error("Fehler beim Abfragen des Serverimport-Status!" + e.getMessage());
    }
  }

  /**
   * Liefert status beans.
   *
   * @param sbId the sb id
   * @return status beans
   */
  public synchronized ArrayList<ServerimportStatusBean> getStatusBeans(String sbId)
  {
    ArrayList<ServerimportStatusBean> result = new ArrayList<>();
    for (ServerimportStatusBean bean : this.serverimportStatusBeans)
    {
      if (String.valueOf(bean.getSachbearbeiterId()).equals(sbId))
      {
        result.add(bean);
      }
    }
    return result;
  }

  /**
   * Destroy.
   */
  public void destroy()
  {
    if (this.thread != null)
    {
      this.thread.interrupt();
    }
    this.thread = null;
  }
}
