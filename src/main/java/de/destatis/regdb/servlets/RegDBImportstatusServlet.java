/*
 * @(#)RegDBImportstatusServlet.java 1.00.19.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.destatis.regdb.ServerimportStatusBean;
import de.destatis.regdb.db.DateiImportDaemon;
import de.destatis.regdb.session.RegDBSession;

/**
 * Dient zum Ermitteln des serverseitigen Importstatus eines Sachbearbeiters
 *
 * @author Stefan Toengi (Destatis)
 */
public class RegDBImportstatusServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB importstatus servlet.
   */
  public RegDBImportstatusServlet()
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
    super.init(RegDBImportstatusServlet.class);
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
    String cmd = String.valueOf(this.readCommand(req));
    if ("INIT".equals(cmd))
    {
      this.log.debug("ImportstatusServlet aktualisiert Statusliste");
      DateiImportDaemon.getInstance()
          .updateStatusList(conn);
    }

    ArrayList<ServerimportStatusBean> list = DateiImportDaemon.getInstance()
        .getStatusBeans(session.getSachbearbeiterId());
    this.sendErgebnis(res, list);
  }

  @Override
  public void destroy()
  {
    super.destroy();
    DateiImportDaemon.getInstance()
        .destroy();
  }
}
