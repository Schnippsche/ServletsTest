/*
 * @(#)RegDBGeneralHttpServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.DBLockException;
import de.destatis.regdb.db.WrongSessionException;
import de.destatis.regdb.session.RegDBSession;
import de.destatis.regdb.session.RegDBSessionManager;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class RegDBGeneralHttpServlet extends HttpServlet
{

  protected static final long serialVersionUID = 1L;
  protected LoggerIfc log;

  /* Daten fuer Werum-Servlets implementiert */
  public static String interneAblaeufeHost = null; /* Default-Parameter */

  public static int interneAblaeufePort = 8050; /* Default-Parameter */

  /**
   * Instantiates a new reg DB general http servlet.
   */
  /* INSERT-END */
  public RegDBGeneralHttpServlet()
  {
    this.log = null;
  }

  /**
   * Initialisiert.
   */
  @Override
  public void init()
  {
    this.init(RegDBGeneralHttpServlet.class);
  }

  /**
   * Initialisiert.
   *
   * @param cls the cls
   */
  public void init(Class<?> cls)
  {
    if (this.log == null)
    {
      this.log = Logger.getInstance().getLogger(cls);
      this.log.info("initing " + this.getClass());
    }

    if (interneAblaeufeHost == null)
    {
      Connection conn = ConnectionTool.getInstance().getConnection();
      this.checkConfiguration(conn);
      ConnectionTool.getInstance().freeConnection(conn);
    }

  }

  /**
   * Check configuration.
   *
   * @param conn the conn
   */
  private void checkConfiguration(Connection conn)
  {
    interneAblaeufeHost = "localhost";
    DBConfig config = new DBConfig();
    String param = config.getParameter(conn, DBConfig.INT_WEB_INTERN_URL);
    try
    {
      URI uri = new URI(param);
      String host = uri.getHost();
      int port = uri.getPort();
      if (host != null && host.length() > 0)
      {
        interneAblaeufeHost = host;
      }
      interneAblaeufePort = port;
      this.log.info("Verwende Host '" + host + "' und Port " + port + " fuer die internen Ablaeufe");
    }
    catch (URISyntaxException e)
    {
      this.log.error("Ungueltiger Parameter '" + DBConfig.INT_WEB_INTERN_URL + "' (" + param + ") \n" + e.getMessage());
    }
    this.checkeDateiImportTabelle(conn);
  }

  /**
   * Checke datei import tabelle.
   *
   * @param conn the conn
   */
  private void checkeDateiImportTabelle(Connection conn)
  {
    try (Statement stmt = conn.createStatement())
    {
      boolean verwaltungTableExists = false;
      boolean teilImportTableExists = false;
      try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'import_%'"))
      {
        while (rs.next())
        {
          if ("import_teil".equals(rs.getString(1)))
          {
            teilImportTableExists = true;
          }
          if ("import_verwaltung".equals(rs.getString(1)))
          {
            verwaltungTableExists = true;
          }
        }

        if (!verwaltungTableExists)
        {
          // Existiert nicht, neu anlegen
          stmt.execute(RegDBImportServlet.CREATE_TBL_SERVERIMPORT);
          this.log.info("Tabelle 'import_verwaltung' wurde neu angelegt!");
        }
        if (!teilImportTableExists)
        {
          // Existiert nicht, neu anlegen
          stmt.execute(RegDBImportServlet.CREATE_TBL_SERVERIMPORT_TEIL);
          this.log.info("Tabelle 'import_teil' wurde neu angelegt!");
        }
      }
      // Alle laufenden Jobs als abgebrochen markieren!
      stmt.execute(RegDBImportServlet.UPDATE_TBL_SERVERIMPORT);
    }
    catch (SQLException e)
    {
      this.log.error("Tabellen fuer serverseitigen Import konnten nicht angelegt werden!:" + e.getMessage());
    }
  }

  /**
   * Do authentication.
   *
   * @param req the req
   * @return the reg DB session
   * @throws ServletException      the servlet exception
   * @throws WrongSessionException the wrong session exception
   * @throws DBLockException       the DB lock exception
   */
  public synchronized RegDBSession doAuthentication(HttpServletRequest req) throws ServletException, WrongSessionException, DBLockException
  {
    String sessionID = req.getParameter("sid");
    if (sessionID == null)
    {
      throw new ServletException("keine Session angegeben");
    }
    RegDBSession session = RegDBSessionManager.getInstance().getSession(sessionID);
    if (session == null)
    {
      throw new WrongSessionException();
    }
    session.updateLastUseTime();
    // Wenn Datenbank oder Sachbearbeiter gesperrt sind und der Sachbearbeiter nicht Root ist, dann gib Meldung aus
    if (!session.isRootUser())
    {
      if (session.getDatenbankSperre())
      {
        throw new DBLockException("Die Datenbank ist zur Zeit gesperrt.");
      }
      if (session.getSachbearbeiterSperre())
      {
        throw new DBLockException("Sachbearbeiter '" + session.getSachbearbeiterKennung() + "' ist zur Zeit gesperrt.");
      }
    }
    return session;
  }

  /**
   * Read command.
   *
   * @param req the req
   * @return the object
   */
  public synchronized Object readCommand(HttpServletRequest req)
  {
    Object command = null;
    try
    {
      if (req.getInputStream() != null)
      {
        ObjectInputStream in = new ObjectInputStream(req.getInputStream());
        command = in.readObject();
        in.close();
        if (command == null)
        {
          throw new ServletException("kein Befehl angegeben!");
        }
      }
      else
      {
        this.log.error("kein InputStream vorhanden!");
      }
    }
    catch (Exception e)
    {
      this.log.error("Fehler beim Lesen des Befehls:" + e.getMessage());
      this.log.error("Befehl=" + command);
    }
    return command;
  }

  /**
   * Service.
   *
   * @param req the req
   * @param res the res
   */
  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public synchronized void service(HttpServletRequest req, HttpServletResponse res)
  {
    Object returnMessage;
    try
    {

      RegDBSession session = this.doAuthentication(req);
      Connection conn = session.getConnection();
      this.doService(req, res, conn, session);
      return;
    }
    catch (WrongSessionException | DBLockException e)
    {
      this.log.info(e.getMessage());
      returnMessage = e;
    }
    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
      returnMessage = e;
    }
    this.sendErgebnis(res, returnMessage);
  }

  /**
   * Do service.
   *
   * @param req     the req
   * @param res     the res
   * @param conn    the connection
   * @param session the session
   * @throws Exception the exception
   */
  public abstract void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session) throws Exception;

  /**
   * Send ergebnis.
   *
   * @param res      the res
   * @param ergebnis the ergebnis
   */
  public synchronized void sendErgebnis(HttpServletResponse res, Object ergebnis)
  {
    // Ergebnis uebermitteln
    res.setContentType("application/x-java-serialized-object");
    try (ObjectOutputStream oos = new ObjectOutputStream(res.getOutputStream()))
    {
      oos.writeObject(ergebnis);
      oos.flush();
      res.flushBuffer();
    }
    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
    }
  }

  /**
   * Destroy.
   */
  /*
   * (non-Javadoc)
   * @see javax.servlet.GenericServlet#destroy()
   */
  @Override
  public void destroy()
  {
    this.log.info(this.getClass() + " beendet");
    super.destroy();
  }
}
