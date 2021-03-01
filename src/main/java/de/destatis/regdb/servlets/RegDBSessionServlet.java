/*
 * @(#)RegDBSessionServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.RegDBSecurity;
import de.destatis.regdb.session.RegDBSession;
import de.destatis.regdb.session.RegDBSessionManager;

public class RegDBSessionServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB session servlet.
   */
  public RegDBSessionServlet()
  {
    super();
  }

  /**
   * Initialisiert.
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#init()
   */
  @Override
  public void init()
  {
    super.init(RegDBSessionServlet.class);
  }

  /**
   * Service.
   *
   * @param req the req
   * @param res the res
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#service(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public synchronized void service(HttpServletRequest req, HttpServletResponse res)
  {
    Object returnMessage = null;
    try
    {
      String action = req.getParameter("action");
      if ("createsession".equals(action))
      {
        String kennung = req.getParameter("regdbkennung");
        String passwort = req.getParameter("regdbpasswort");
        this.log.info("Sachbearbeiter mit Kennung " + kennung + " (" + req.getRemoteHost() + ") meldet sich an");
        // Check kennung & passwort
        Connection conn = ConnectionTool.getInstance()
            .getConnection();
        if (conn == null)
        {
          throw new Exception("Die Datenbank ist nicht erreichbar!");
        }
        int sbID = RegDBSecurity.getInstance()
            .getSachbearbeiterID(conn, kennung, passwort, interneAblaeufeHost, String.valueOf(interneAblaeufePort));
        ConnectionTool.getInstance()
            .freeConnection(conn);
        if (sbID == -1)
        {
          throw new Exception("Kombination aus Benutzerkennung und Passwort nicht bekannt!");
        }
        if (sbID == -2)
        {
          throw new Exception("Benutzer ist zur Zeit nicht aktiv!");
        }

        RegDBSession session = RegDBSessionManager.getInstance()
            .createSession(kennung, "" + sbID);
        if (session == null)
        {
          throw new Exception("Konnte keine neue Session erzeugen!");
        }
        session.setSachbearbeiterPasswort(passwort);
        session.setRootUser(RegDBSecurity.getInstance()
            .isRootUser(conn, "" + sbID));
        returnMessage = session.getSessionId();
      }
      else if ("destroysession".equals(action))
      {
        String sid = req.getParameter("sid");
        RegDBSession session = RegDBSessionManager.getInstance()
            .getSession(sid);
        if (session != null)
        {
          String kennung = session.getSachbearbeiterKennung();
          this.log.info("Sachbearbeiter mit Kennung " + kennung + " (" + req.getRemoteHost() + ") meldet sich ab");
          RegDBSessionManager.getInstance()
              .destroySession(session);
        }
      }
      else
      {
        throw new Exception("keine gueltige Action-Anweisung erhalten!");
      }
    }
    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
      returnMessage = e;
    }
    this.sendErgebnis(res, returnMessage);
  }

  /**
   * Destroy.
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#destroy()
   */
  @Override
  public void destroy()
  {
    this.log.info("RegDBSessionServlet beendet");
    super.destroy();
    RegDBSessionManager.getInstance()
        .destroySessionPool();
  }

  /**
   * Do service.
   *
   * @param req the req
   * @param res the res
   * @param conn the conn
   * @param session the session
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#doService(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session)
  {
    // Nothing!

  }
}
