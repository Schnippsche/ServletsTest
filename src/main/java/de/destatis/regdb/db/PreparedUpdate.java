package de.destatis.regdb.db;

import de.werum.sis.idev.res.job.JobException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The type Prepared update.
 */
public class PreparedUpdate extends PreparedSql
{

  /**
   * Instantiates a new Prepared update.
   *
   * @param connection the connection
   * @param sql        the sql
   * @throws JobException the job exception
   */
  public PreparedUpdate(Connection connection, String sql) throws JobException
  {
    super();
    try
    {
      this.ps = connection.prepareStatement(sql);
    }
    catch (SQLException e)
    {
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Update int.
   *
   * @return the int
   * @throws JobException the job exception
   */
  public int update() throws JobException
  {
    try
    {
      setPsValues();
      return this.ps.executeUpdate();

    }
    catch (SQLException e)
    {
      throw new JobException(e.getMessage());
    }
  }

}
