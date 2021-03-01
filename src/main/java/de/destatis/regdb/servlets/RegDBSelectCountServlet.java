/*
 * @(#)RegDBSelectCountServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.destatis.regdb.session.RegDBSession;

public class RegDBSelectCountServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB select count servlet.
   */
  public RegDBSelectCountServlet()
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
    super.init(RegDBSelectCountServlet.class);
  }

  /**
   * Do service.
   *
   * @param req the req
   * @param res the res
   * @param conn the conn
   * @param session the session
   * @throws Exception the exception
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#doService(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session) throws Exception
  {
    Object command = this.readCommand(req);
    try (ResultSet rs = conn.createStatement()
        .executeQuery(String.valueOf(command)))
    {
      if (rs.next())
      {
        this.sendErgebnis(res, Integer.valueOf(rs.getInt(1)));
      }
      else
      {
        this.sendErgebnis(res, Integer.valueOf(0));
      }
    }
  }

}
