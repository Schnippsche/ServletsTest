/*
 * @(#)RegDBSelectServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.db.Result;
import de.destatis.regdb.session.RegDBSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Vector;

public class RegDBSelectServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB select servlet.
   */
  public RegDBSelectServlet()
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
    super.init(RegDBSelectServlet.class);
  }

  /**
   * Do service.
   *
   * @param req     the req
   * @param res     the res
   * @param conn    the conn
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
    try (ResultSet rs = conn.createStatement().executeQuery(String.valueOf(command)))
    {
      ResultSetMetaData meta = rs.getMetaData();
      Vector<Vector<Object>> tableData;
      Vector<String> columns;
      int cols = meta.getColumnCount();
      columns = new Vector<>(cols);
      int[] colTypes = new int[cols];
      for (int i = 1; i <= cols; i++)
      {
        columns.add(meta.getColumnLabel(i));
        colTypes[i - 1] = meta.getColumnType(i);
      }
      tableData = new Vector<>();
      // Hole Daten
      while (rs.next())
      {
        Vector<Object> rowData = new Vector<>(cols);
        for (int i = 1; i <= cols; i++)
        {
          if (colTypes[i - 1] == Types.BLOB)
          {
            Blob b = rs.getBlob(i);
            byte[] bytes = b.getBytes(1, (int) b.length());
            rowData.add(bytes);
          }
          else
          {
            rowData.add(rs.getObject(i));
          }
        }
        tableData.add(rowData);
      }
      this.sendErgebnis(res, new Result(tableData, columns, colTypes));
    }

  }

}
