package de.destatis.regdb.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.werum.sis.idev.res.job.JobException;

/**
 * The type Prepared insert.
 */
public class PreparedInsert extends PreparedSql
{
  private ResultRow keys;

  /**
   * Instantiates a new Prepared insert.
   *
   * @param connection the connection
   * @param sql the sql
   * @throws JobException the job exception
   */
  public PreparedInsert(Connection connection, String sql) throws JobException
  {
    super();
    try
    {
      ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
    catch (SQLException e)
    {
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Insert.
   *
   * @return the int
   * @throws JobException the job exception
   */
  public int insert() throws JobException
  {
    keys = null;
    try
    {
      setPsValues();
      int result = ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys())
      {
        if (rs.next())
        {
          keys = new ResultRow(rs);
        }
      }
      return result;
    }
    catch (SQLException e)
    {
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Gets generated keys.
   *
   * @return the generated keys
   */
  public ResultRow getGeneratedKeys()
  {
    return keys;
  }
 

}
