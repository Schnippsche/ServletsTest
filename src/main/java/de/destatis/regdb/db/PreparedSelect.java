package de.destatis.regdb.db;

import de.werum.sis.idev.res.job.JobException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Prepared select.
 */
public class PreparedSelect extends PreparedSql
{

  /**
   * Instantiates a new Prepared select.
   *
   * @param connection the connection
   * @param sql        the sql
   * @throws JobException the job exception
   */
  public PreparedSelect(Connection connection, String sql) throws JobException
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
   * Fetch one data row.
   *
   * @return the data row
   * @throws JobException the job exception
   */
  public ResultRow fetchOne() throws JobException
  {
    try
    {
      setPsValues();
      ResultSet rs = this.ps.executeQuery();
      if (rs.next())
      {
        return new ResultRow(rs);
      }
      return null;
    }
    catch (SQLException e)
    {
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Fetch many list.
   *
   * @return the list
   * @throws JobException the job exception
   */
  public List<ResultRow> fetchMany() throws JobException
  {
    try
    {
      List<ResultRow> rows = new ArrayList<>();
      setPsValues();
      try (ResultSet rs = this.ps.executeQuery())
      {
        while (rs.next())
        {
          rows.add(new ResultRow(rs));
        }
        return rows;
      }
    }
    catch (SQLException e)
    {
      throw new JobException(e.getMessage());
    }

  }

}
