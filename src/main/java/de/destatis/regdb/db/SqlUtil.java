package de.destatis.regdb.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

/**
 * The type Sql util.
 */
public class SqlUtil
{
  /**
   * The constant log.
   */
  protected static final LoggerIfc log = Logger.getInstance()
      .getLogger(SqlUtil.class);

  private final Connection connection;

  /**
   * Instantiates a new Sql util.
   *
   * @param connection the connection
   */
  public SqlUtil(Connection connection)
  {
    this.connection = connection;
  }

  /**
   * Updates the sql Statemanet.
   *
   * @param sql the sql
   * @return the int
   * @throws JobException the job exception
   */
  public int update(String sql) throws JobException
  {
    log.debug(sql);
    try (Statement stmt = connection.createStatement())
    {
      return stmt.executeUpdate(sql);
    }
    catch (SQLException e)
    {
      log.error(e.getMessage());
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Executes the sql Statement.
   *
   * @param sql the sql
   * @throws JobException the job exception
   */
  public void execute(String sql) throws JobException
  {
    log.debug(sql);
    try (Statement stmt = connection.createStatement())
    {
       stmt.execute(sql);
    }
    catch (SQLException e)
    {
      log.error(e.getMessage());
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Fetch one Result row.
   *
   * @param sql the sql
   * @return the Result row
   * @throws JobException the job exception
   */
  public ResultRow fetchOne(String sql) throws JobException
  {
    log.debug(sql);
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
    {
      if (rs.next())
      {
        return new ResultRow(rs);
      }
      return null;
    }
    catch (SQLException e)
    {
      log.error(e.getMessage());
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Fetch many list.
   *
   * @param sql the sql
   * @return the list
   * @throws JobException the job exception
   */
  public List<ResultRow> fetchMany(String sql) throws JobException
  {
    log.debug(sql);
    List<ResultRow> rows = new ArrayList<>();
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
    {
      while (rs.next())
      {
        rows.add(new ResultRow(rs));
      }
      return rows;
    }
    catch (SQLException e)
    {
      log.error(e.getMessage());
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Create prepared select prepared select.
   *
   * @param sql the sql
   * @return the prepared select
   * @throws JobException the job exception
   */
  public PreparedSelect createPreparedSelect(String sql) throws JobException
  {
    return new PreparedSelect(connection, sql);
  }

  /**
   * Create prepared update prepared update.
   *
   * @param sql the sql
   * @return the prepared update
   * @throws JobException the job exception
   */
  public PreparedUpdate createPreparedUpdate(String sql) throws JobException
  {
    return new PreparedUpdate(connection, sql);
  }

  /**
   * Create prepared insert prepared insert.
   *
   * @param sql the sql
   * @return the prepared insert
   * @throws JobException the job exception
   */
  public PreparedInsert createPreparedInsert(String sql) throws JobException
  {
    return new PreparedInsert(connection, sql);
  }

  /**
   * Db commit.
   *
   * @throws JobException the job exception
   */
  public void dbCommit() throws JobException
  {
    log.debug("DB commit now, conn=" + this.connection);
    try
    {
      this.connection.commit();
    }
    catch (SQLException e)
    {
      throw new JobException("Fehler bei dbCommit:" + e.getMessage(), e);
    }
  }

  /**
   * Db rollback.
   *
   * @throws JobException the job exception
   */
  public void dbRollback() throws JobException
  {
    log.debug("DB rollback now, conn=" + this.connection);
    try
    {
      this.connection.rollback();
    }
    catch (SQLException ex)
    {
      throw new JobException("Fehler bei dbRollback:" + ex.getMessage(), ex);
    }
  }

  /**
   * Db begin transaction.
   *
   * @throws JobException the job exception
   */
  public void dbBeginTransaction() throws JobException
  {
    log.debug("begin DB Transaction");
    try
    {
      this.connection.setAutoCommit(false);
    }
    catch (SQLException ex)
    {
      throw new JobException("Fehler bei dbBeginTransaction:" + ex.getMessage(), ex);
    }

  }

  /**
   * Db end transaction.
   *
   * @throws JobException the job exception
   */
  public void dbEndTransaction() throws JobException
  {
    log.debug("end of db Transaction");
    try
    {
      this.connection.setAutoCommit(true);

    }
    catch (SQLException ex)
    {
      throw new JobException("Fehler bei dbEndTransaction:" + ex.getMessage(), ex);
    }
  }

  /**
   * Convert string list for Sql IN().
   *
   * @param elements the elements
   * @return the string
   */
  public String convertStringList(Collection<String> elements)
  {
    if (elements == null || elements.isEmpty())
      return "''";

    return elements.stream()
        .map(StringUtil::escapeSqlString)
        .collect(Collectors.joining("','", "'", "'"));
  }

  /**
   * Convert number list for Sql IN()
   *
   * @param elements the elements
   * @return the string
   */
  public String convertNumberList(Collection<? extends Number> elements)
  {
    if (elements == null || elements.isEmpty())
      return "0";

    return elements.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));

  }

  /**
   * Gets connection.
   *
   * @return the connection
   */
  public Connection getConnection()
  {
    return this.connection;
  }
}
