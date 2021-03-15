package de.destatis.regdb.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Result Row.
 */
public class ResultRow
{
  private final List<Object> values;
  private final List<String> columnNames;

  /**
   * Instantiates a new Result row.
   *
   * @param rs the rs
   * @throws SQLException the sql exception
   */
  public ResultRow(ResultSet rs) throws SQLException
  {
    this.values = new ArrayList<>();
    this.columnNames = new ArrayList<>();
    if (rs != null)
    {
      ResultSetMetaData meta = rs.getMetaData();
      for (int c = 1; c <= meta.getColumnCount(); c++)
      {
        this.columnNames.add(meta.getColumnLabel(c));
        this.values.add(rs.getObject(c));
      }
    }

  }

  /**
   * Gets string.
   *
   * @param columnIndex the column index
   * @return the string
   */
  public String getString(int columnIndex)
  {
    return String.valueOf(this.values.get(convertColumn(columnIndex)));
  }

  /**
   * Gets string.
   *
   * @param columnLabel the column label
   * @return the string
   */
  public String getString(String columnLabel)
  {
    return getString(getColumnIndex(columnLabel));
  }

  /**
   * Gets int.
   *
   * @param columnIndex the column index
   * @return the int
   */
  public int getInt(int columnIndex)
  {
    Object o = this.values.get(convertColumn(columnIndex));
    if (o == null)
    {
      return 0;
    }
    if (o instanceof java.lang.Integer)
    {
      return (Integer) o;
    }

    return Integer.parseInt(o.toString());
  }

  /**
   * Gets int.
   *
   * @param columnLabel the column label
   * @return the int
   */
  public int getInt(String columnLabel)
  {
    return getInt(getColumnIndex(columnLabel));
  }

  /**
   * Gets long.
   *
   * @param columnIndex the column index
   * @return the long
   */
  public long getLong(int columnIndex)
  {
    Object o = this.values.get(convertColumn(columnIndex));
    if (o == null)
    {
      return 0;
    }
    if (o instanceof java.lang.Long)
    {
      return (Long) o;
    }

    return Long.parseLong(o.toString());
  }

  /**
   * Gets long.
   *
   * @param columnLabel the column label
   * @return the long
   */
  public long getLong(String columnLabel)
  {
    return getLong(getColumnIndex(columnLabel));
  }

  /**
   * Gets boolean.
   *
   * @param columnIndex the column index
   * @return the boolean
   */
  public boolean getBoolean(int columnIndex)
  {
    //If the designated column has a datatype of CHAR or VARCHAR and contains a "0" or has a datatype of BIT, TINYINT, SMALLINT, INTEGER or BIGINTand contains a 0, a value of false is returned.
    // If the designated column has a datatypeof CHAR or VARCHAR and contains a "1" or has a datatype of BIT, TINYINT, SMALLINT, INTEGER or BIGINTand contains a 1, a value of true is returned
    Object o = this.values.get(convertColumn(columnIndex));
    if (o == null)
    {
      return false;
    }
    if (o instanceof java.lang.Boolean)
    {
      return (Boolean) o;
    }
    return o.toString().equals("1");
  }

  /**
   * Gets boolean.
   *
   * @param columnLabel the column label
   * @return the boolean
   */
  public boolean getBoolean(String columnLabel)
  {
    return getBoolean(getColumnIndex(columnLabel));
  }

  /**
   * Gets column name.
   *
   * @param column the column
   * @return the column name
   */
  public String getColumnName(int column)
  {
    return columnNames.get(convertColumn(column));
  }

  /**
   * Gets column count.
   *
   * @return the column count
   */
  public int getColumnCount()
  {
    return this.columnNames.size();
  }

  /**
   * Convert column.
   *
   * @param column the column
   * @return the int
   */
  private int convertColumn(int column)
  {
    if (column < 1 || column > this.columnNames.size())
    {
      throw new IndexOutOfBoundsException("Spaltenwert " + column + " ausserhalb Range");
    }
    return column - 1;
  }

  /**
   * Gets the column index.
   *
   * @param columnLabel the column label
   * @return the column index
   */
  private int getColumnIndex(String columnLabel)
  {
    String uc = columnLabel.toUpperCase();
    for (int c = 0; c < this.columnNames.size(); c++)
    {
      if (this.columnNames.get(c).toUpperCase().equals(uc))
      {
        return c + 1;
      }
    }
    throw new IllegalArgumentException("Spalte " + columnLabel + " nicht gefunden!" + StringUtil.join(this.columnNames, ","));
  }

  @Override
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("ResultRow [");
    for (int i = 0; i < this.values.size(); i++)
    {
      buf.append(i > 0 ? "," : "");
      buf.append(this.columnNames.get(i)).append('=').append(this.values.get(i));
    }
    buf.append("]");
    return buf.toString();
  }

}
