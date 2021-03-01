/*
 * @(#)RegDBExecuteServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.destatis.regdb.session.RegDBSession;

public class RegDBExecuteServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB execute servlet.
   */
  public RegDBExecuteServlet()
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
    super.init(RegDBExecuteServlet.class);
  }

  /**
   * Do service.
   *
   * @param req the req
   * @param res the res
   * @par
   * am conn the conn
   * @param session the session
   * @throws SQLException the SQL exception
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#doService(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session) throws SQLException
  {
    Object command = this.readCommand(req);
    try (Statement stmt = conn.createStatement())
    {
      stmt.execute(String.valueOf(command));
      this.sendErgebnis(res, "");
    }
  }
}
