/*
 * @(#)RegDBLoadDataServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.destatis.regdb.session.RegDBSession;

/**
 * Dient zum Ausfuehren von LoadData-Anweisungen auf dem Server
 *
 * @author Stefan Toengi (Destatis)
 */
public class RegDBLoadDataServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB load data servlet.
   */
  public RegDBLoadDataServlet()
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
    super.init(RegDBLoadDataServlet.class);
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
    try (Statement stmt = conn.createStatement())
    {
      int anz = stmt.executeUpdate(String.valueOf(command));
      this.sendErgebnis(res, Integer.toString(anz));
    }
  }

}