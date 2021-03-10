/*
 * @(#)RegDBExecuteServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.session.RegDBSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The type Reg db execute servlet.
 */
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
