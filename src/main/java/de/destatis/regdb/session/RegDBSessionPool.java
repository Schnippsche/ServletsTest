/*
 * @(#)RegDBSessionPool.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */

package de.destatis.regdb.session;

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.RegDBSecurity;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

// Referenced classes of package de.werum.sis.idev.extern.servlet.session:
// Session, SessionManager

public class RegDBSessionPool implements Runnable
{

  private final LoggerIfc log;

  private static RegDBSessionPool instance;

  private int sessionCount;

  private final Hashtable<String, RegDBSession> sessions;

  private boolean isAvailable;

  private Thread poolCleaner;

  private boolean dontStop;

  private long cleanerSleepTime;

  private long maxSessionAgeMSec;

  /**
   * Instantiates a new reg DB session pool.
   */
  private RegDBSessionPool()
  {
    this.log = Logger.getInstance()
        .getLogger(RegDBSessionPool.class);
    this.sessionCount = 0;
    this.isAvailable = false;
    this.dontStop = true;
    this.cleanerSleepTime = 40000L; // 40 sec
    this.sessions = new Hashtable<>();
    this.sessionCount = 0;
    this.maxSessionAgeMSec = 18000000L; // 1800 sec = 30 min * 60 sec
  }

  /**
   * Gets the single instance of RegDBSessionPool.
   *
   * @return single instance of RegDBSessionPool
   */
  public static synchronized RegDBSessionPool getInstance()
  {
    if (instance == null)
    {
      instance = new RegDBSessionPool();
      instance.initSessionPool();
    }
    return instance;
  }

  /**
   * Initialisiert session pool.
   */
  private void initSessionPool()
  {
    this.poolCleaner = new Thread(this);
    this.poolCleaner.setDaemon(true);
    this.poolCleaner.start();
    this.isAvailable = true;
  }

  /**
   * Destroy session pool.
   *
   * @param waitForThreadMs the wait for thread ms
   */
  public void destroySessionPool(int waitForThreadMs)
  {
    this.isAvailable = false;
    this.dontStop = false;
    if (this.poolCleaner == null)
    {
      return;
    }
    this.log.debug("destroyRegDBSessionPool");
    this.poolCleaner.interrupt();
    try
    {
      this.poolCleaner.join(waitForThreadMs);
    }
    catch (InterruptedException e)
    {
      Thread.currentThread()
          .interrupt();
    }
    this.poolCleaner = null;
  }

  /**
   * Liefert session count.
   *
   * @return session count
   */
  public int getSessionCount()
  {
    return this.sessionCount;
  }

  /**
   * Checks if is available.
   *
   * @return true, if is available
   */
  public boolean isAvailable()
  {
    return this.isAvailable;
  }

  /**
   * Liefert max session age M sec.
   *
   * @return max session age M sec
   */
  public long getMaxSessionAgeMSec()
  {
    return this.maxSessionAgeMSec;
  }

  /**
   * Setzt max session age M sec.
   *
   * @param maxSessionAgeMSec max session age M sec
   */
  public void setMaxSessionAgeMSec(int maxSessionAgeMSec)
  {
    this.maxSessionAgeMSec = (long) maxSessionAgeMSec * 60 * 1000;
  }

  /**
   * Liefert cleaner sleep time.
   *
   * @return cleaner sleep time
   */
  public long getCleanerSleepTime()
  {
    return this.cleanerSleepTime;
  }

  /**
   * Setzt cleaner sleep time.
   *
   * @param cleanerSleepTime cleaner sleep time
   */
  public void setCleanerSleepTime(int cleanerSleepTime)
  {
    this.cleanerSleepTime = (long) cleanerSleepTime * 1000;
  }

  /**
   * Liefert all session.
   *
   * @return all session
   */
  public Collection<RegDBSession> getAllSession()
  {
    return this.sessions.values();
  }

  /**
   * Contains session id.
   *
   * @param id the id
   * @return true, if successful
   */
  public boolean containsSessionId(String id)
  {
    return this.sessions.containsKey(id);
  }

  /**
   * Liefert session.
   *
   * @param sessionId the session id
   * @return session
   */
  public RegDBSession getSession(String sessionId)
  {
    return this.sessions.get(sessionId);
  }

  /**
   * Put session.
   *
   * @param session the session
   * @return true, if successful
   */
  public boolean putSession(RegDBSession session)
  {
    boolean putOk = false;
    if (session != null)
    {
      this.log.debug("putRegDBSession mit " + session.getSessionId());
      if (!this.sessions.containsKey(session.getSessionId()))
      {
        this.sessions.put(session.getSessionId(), session);
        this.sessionCount++;
        putOk = true;
      }
      else
      {
        this.log.error("Fehler beim Ablegen einer RegDBSession. Die id ist bereits vorhanden.");
      }
    }
    else
    {
      this.log.error("Fehler beim Ablegen einer RegDBSession. Die Session ist null.");
    }
    return putOk;
  }

  /**
   * Entfernt session.
   *
   * @param sessionId the session id
   * @return true, if successful
   */
  public boolean removeSession(String sessionId)
  {
    this.log.debug("remove RegDBSession " + sessionId);
    boolean found = false;
    if (this.sessions != null)
    {
      if (sessionId != null)
      {
        RegDBSession foundSession;
        synchronized (this.sessions)
        {
          foundSession = this.sessions.remove(sessionId);
        }
        if (foundSession != null)
        {
          this.log.debug("RegDBSession " + sessionId + " entfernt");
          this.sessionCount--;
          found = true;
        }
        else
        {
          this.log.error("Fehler beim Entfernen einer RegDBSession. SessionId ist nicht im Pool vorhanden.");
        }
      }
      else
      {
        this.log.error("Fehler beim Entfernen einer RegDBSession. Parameter sessionId ist null");
      }
    }
    else
    {
      this.log.error("Fehler beim Entfernen einer RegDBSession. Der Pool ist null");
    }
    return found;
  }

  /**
   * Run.
   */
  @Override
  public void run()
  {
    while (!Thread.currentThread()
        .isInterrupted() && this.dontStop)
    {
      RegDBSession[] sessionsa = this.getSessionArray();
      long nowMs = System.currentTimeMillis();
      Connection conn = ConnectionTool.getInstance()
          .getConnection();
      boolean dbSperre = RegDBSecurity.getInstance()
          .isDatenbankSperre(conn);
      for (RegDBSession session : sessionsa)
      {
        if (session != null)
        {
          Date lastUseTime = session.getLastUseTime();
          long lastUseTimeMs = lastUseTime.getTime();
          long age = nowMs - lastUseTimeMs;
          if (age > this.maxSessionAgeMSec)
          {
            this.freeSession(session);
          }
          else
          {
            String sb = session.getSachbearbeiterId();
            session.setDatenbankSperre(dbSperre);
            boolean sbSperre = RegDBSecurity.getInstance()
                .isSachbearbeiterSperre(conn, sb);
            session.setSachbearbeiterSperre(sbSperre);
            session.setRootUser(RegDBSecurity.getInstance()
                .isRootUser(conn, sb));
          }
        }
      }
      ConnectionTool.getInstance()
          .freeConnection(conn);

      try
      {
        Thread.sleep(this.cleanerSleepTime);
      }

      catch (Exception e)
      {
        this.log.info("RegDBSessionPool wurde beendet");
        Thread.currentThread()
            .interrupt();
        this.dontStop = false;
      }
    }
  }

  /**
   * Liefert session array.
   *
   * @return session array
   */
  private RegDBSession[] getSessionArray()
  {
    RegDBSession[] msessions;
    synchronized (this.sessions)
    {
      msessions = new RegDBSession[this.sessions.size()];
      msessions = this.sessions.values()
          .toArray(msessions);
    }

    return msessions;
  }

  /**
   * Free session.
   *
   * @param session the session
   */
  private void freeSession(RegDBSession session)
  {
    this.log.debug("freeRegDBSession " + session);
    if (session != null)
    {
      this.log.info("Entferne zu lange nicht benutzte RegDBSession! ID:" + session.getSessionId());
      this.removeSession(session.getSessionId());
    }
  }

}
