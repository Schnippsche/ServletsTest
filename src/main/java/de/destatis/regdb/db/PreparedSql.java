package de.destatis.regdb.db;

import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.job.LogLevel;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Prepared sql.
 */
public abstract class PreparedSql implements AutoCloseable
{
  private static final LoggerIfc log = Logger.getInstance()
    .getLogger(PreparedSql.class);
  protected final List<Object> values;
  /**
   * The Ps.
   */
  protected PreparedStatement ps;

  /**
   * Instantiates a new Prepared sql.
   */
  protected PreparedSql()
  {
    this.values = new ArrayList<>();
  }

  /**
   * Adds the values.
   *
   * @param args the args
   */
  public void addValues(Object... args)
  {
    this.values.addAll(Arrays.asList(args));
  }

  /**
   * Add a value.
   *
   * @param object the object
   */
  public void addValue(Object object)
  {
    this.values.add(object);
  }

  /**
   * Sets the ps values.
   *
   * @throws SQLException the SQL exception
   */
  protected void setPsValues() throws SQLException
  {
    int row = 0;
    for (Object o : values)
    {
      ps.setObject(++row, o);
    }
    if (log.getLogLevel() >= LogLevel.DEBUG)
    {
      log.debug(ps.toString());
    }
    this.values.clear();
  }

  /**
   * Close prepared statement.
   */
  protected void closePreparedStatement()
  {
    log.debug("Autoclose PreparedStatement");
    try
    {
      if (ps != null)
        ps.close();
    } catch (SQLException e)
    {
      log.debug("Fehler beim Schliessen:" + e.getMessage());
    }
  }

  @Override
  public void close() throws JobException
  {
    closePreparedStatement();
  }
}
