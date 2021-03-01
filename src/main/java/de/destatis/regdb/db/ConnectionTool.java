/*
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.javaexchange.dbConnectionBroker.DbConnectionBroker;

import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.db.ConnectionManager;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

/**
 * The Class ConnectionTool.
 */
public class ConnectionTool
{

  /** The instance. */
  private static ConnectionTool instance = null;

  /** The broker. */
  private final DbConnectionBroker broker;

  /** The log. */
  private final LoggerIfc log;

  /**
   * Instantiates a new connection tool.
   */
  private ConnectionTool()
  {
    this.log = Logger.getInstance()
        .getLogger(this.getClass());
    this.broker = ConnectionManager.getConnectionBroker(DBConfig.INT_DBNAME, ConnectionManager.TYP_DEFAULT);
  }

  /**
   * Gets the single instance of ConnectionTool.
   *
   * @return single instance of ConnectionTool
   */
  public static synchronized ConnectionTool getInstance()
  {
    if (instance == null)
    {
      instance = new ConnectionTool();
    }
    return instance;
  }

  /**
   * Gets the connection.
   *
   * @return the connection
   */
  public Connection getConnection()
  {
    Connection conn = null;
    try
    {
      conn = this.broker.getConnection();
      if (conn == null || !conn.isValid(5))
      {
        this.log.error("Es konnte keine Verbindung zur Datenbank aufgebaut werden!");
        this.broker.freeConnection(conn);
        conn = null;
      }
    }
    catch (SQLException e)
    {
      this.log.error("Fehler beim Verbinden zur Datenbank:" + e.getMessage());
    }
    return conn;
  }

  /**
   * Free connection.
   *
   * @param conn the conn
   */
  public void freeConnection(Connection conn)
  {
    this.broker.freeConnection(conn);
  }
}