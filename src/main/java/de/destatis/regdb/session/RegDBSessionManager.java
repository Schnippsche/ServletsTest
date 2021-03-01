/*
 * @(#)RegDBSessionManager.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */

package de.destatis.regdb.session;

import java.sql.Connection;

import de.destatis.regdb.db.ConnectionTool;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

public class RegDBSessionManager
{

  private static final int maxTryForCreateSessionId = 20;
  private static RegDBSessionManager instance = null;
  private static LoggerIfc log;

  /**
   * Instantiates a new reg DB session manager.
   */
  private RegDBSessionManager()
  {

  }

  /**
   * Gets the single instance of RegDBSessionManager.
   *
   * @return single instance of RegDBSessionManager
   */
  public static synchronized RegDBSessionManager getInstance()
  {
    if (instance == null)
    {
      instance = new RegDBSessionManager();
      log = Logger.getInstance()
          .getLogger(RegDBSessionManager.class);
    }
    return instance;
  }

  /**
   * Creates the session.
   *
   * @param kennung the kennung
   * @param melderId the melder id
   * @return the reg DB session
   */
  public RegDBSession createSession(String kennung, String melderId)
  {
    RegDBSessionPool sessionPool = RegDBSessionPool.getInstance();
    RegDBSession session = null;
    String sessionId = this.createSessionId(sessionPool);
    Connection conn = ConnectionTool.getInstance()
        .getConnection();
    if (sessionId != null && conn != null)
    {
      session = new RegDBSession(kennung, melderId);
      session.setSessionId(sessionId);
      session.setConnection(conn);
      if (sessionPool.isAvailable())
      {
        sessionPool.putSession(session);
      }
      else
      {
        log.error("Kann RegDBSession von " + kennung + " nicht erstellen. Pool ist nicht bereit.");
      }
    }
    else
    {
      log.error("Kann RegDBSession von " + kennung + " nicht anlegen!");
    }
    return session;
  }

  /**
   * Liefert session.
   *
   * @param paramSessionId the param session id
   * @return session
   */
  public RegDBSession getSession(String paramSessionId)
  {
    RegDBSession session = null;
    if (paramSessionId != null)
    {
      session = this.loadSessionFromPool(paramSessionId);

    }
    return session;
  }

  /**
   * Load session from pool.
   *
   * @param sessionId the session id
   * @return the reg DB session
   */
  private RegDBSession loadSessionFromPool(String sessionId)
  {
    RegDBSession session = null;
    RegDBSessionPool sessionPool = RegDBSessionPool.getInstance();
    if (sessionPool.isAvailable())
    {
      session = sessionPool.getSession(sessionId);
    }
    else
    {
      log.error("Kann RegDBSession" + sessionId + " nicht holen. Pool ist nicht bereit.");
    }
    return session;
  }

  /**
   * Update session.
   *
   * @param session the session
   * @return true, if successful
   */
  public boolean updateSession(RegDBSession session)
  {
    if (session != null)
    {
      session.updateLastUseTime();
      return true;
    }
    return false;
  }

  /**
   * Destroy session pool.
   */
  public void destroySessionPool()
  {
    RegDBSessionPool sessionPool = RegDBSessionPool.getInstance();
    if (sessionPool == null)
    {
      return;
    }
    sessionPool.destroySessionPool(20000);
  }

  /**
   * Destroy session.
   *
   * @param session the session
   */
  public void destroySession(RegDBSession session)
  {
    RegDBSessionPool sessionPool = RegDBSessionPool.getInstance();
    if (session != null)
    {
      String sessionId = session.getSessionId();
      ConnectionTool.getInstance()
          .freeConnection(session.getConnection());
      if (sessionPool.isAvailable() && sessionId != null)
      {
        sessionPool.removeSession(sessionId);
      }
      else
      {
        log.error("Kann RegDBSession" + sessionId + " nicht entfernen. Pool ist nicht bereit.");
      }
    }
    else
    {
      log.info("destroyRegDBSessionFromPool aber RegDBSession ist null.");
    }
  }

  /**
   * Creates the session id.
   *
   * @param sessionPool the session pool
   * @return the string
   */
  private synchronized String createSessionId(RegDBSessionPool sessionPool)
  {
    RegDBSessionIdFactory factory = RegDBSessionIdFactory.getInstance();
    String nextSessionId = factory.erzeugeSessionId();
    int maxTry = maxTryForCreateSessionId;
    boolean haveUniqueId;
    for (haveUniqueId = false; !haveUniqueId && maxTry > 0;)
    {
      if (sessionPool.isAvailable() && sessionPool.containsSessionId(nextSessionId))
      {

        nextSessionId = factory.erzeugeSessionId();
        log.info("nextSessionID=" + nextSessionId);
        maxTry--;
      }
      else
      {
        haveUniqueId = true;
      }
    }
    if (!haveUniqueId)
    {
      return null;
    }
    return nextSessionId;
  }

}
