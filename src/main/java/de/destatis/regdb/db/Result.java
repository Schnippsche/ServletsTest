/*
 * @(#)Result.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.db;

import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Result implements Serializable
{

  private static final long serialVersionUID = 2679626230655573191L;
  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
  private static final LoggerIfc log = Logger.getInstance().getLogger(Result.class);
  int _currentRow;
  private Vector<Vector<Object>> _tableData;
  private Vector<Object> _rowData;
  private Vector<String> _colNames;
  private int[] _columnTypes;

  /**
   * Instantiates a new result.
   */
  public Result()
  {
    this._rowData = null;
    this._tableData = new Vector<>();
    this._currentRow = 0;
    this._colNames = new Vector<>();
    this._columnTypes = null;
  }

  /**
   * Instantiates a new result.
   *
   * @param tableData   the table data
   * @param columns     the columns
   * @param columnTypes the column types
   */
  public Result(Vector<Vector<Object>> tableData, Vector<String> columns, int[] columnTypes)
  {
    this();
    if (tableData != null)
    {
      this._tableData = tableData;
    }
    if (columns != null)
    {
      this._colNames = columns;
    }
    if (columnTypes != null)
    {
      this._columnTypes = new int[columnTypes.length];
      System.arraycopy(columnTypes, 0, this._columnTypes, 0, columnTypes.length);
    }
  }

  /**
   * Ueberprueft next.
   *
   * @return true, if successful
   */
  public boolean hasNext()
  {
    if (this._currentRow < this._tableData.size())
    {
      this._rowData = this._tableData.get(this._currentRow);
      this._currentRow++;
      return true;
    }
    return false;
  }

  /**
   * Liefert string.
   *
   * @param column the column
   * @return string
   */
  public String getString(int column)
  {
    String result = null;
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      Object o = this._rowData.get(column - 1);
      if (o instanceof byte[])
      {
        result = new String((byte[]) o, StandardCharsets.UTF_8);
      }
      else if (o != null)
      {
        result = o.toString();
      }
    }
    return result;
  }

  /**
   * Liefert string.
   *
   * @param column the column
   * @return string
   */
  public String getString(String column)
  {
    return this.getString(this.getColumnRow(column));
  }

  /**
   * Liefert date.
   *
   * @param column the column
   * @return date
   */
  public Date getDate(String column)
  {
    return this.getDate(this.getColumnRow(column));
  }

  /**
   * Liefert date.
   *
   * @param column the column
   * @return date
   */
  public Date getDate(int column)
  {
    Date result = null;
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      Object o = this._rowData.get(column - 1);
      if (o != null)
      {
        try
        {
          result = this.sdf.parse(o.toString());
        }
        catch (ParseException e)
        {
          log.error("Konnte Datum nicht konvertieren:" + o.toString());
        }
      }
    }
    return result;
  }

  /**
   * Liefert object.
   *
   * @param column the column
   * @return object
   */
  public Object getObject(String column)
  {
    return this.getObject(this.getColumnRow(column));
  }

  /**
   * Liefert object.
   *
   * @param column the column
   * @return object
   */
  public Object getObject(int column)
  {
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      return this._rowData.get(column - 1);
    }
    return null;

  }

  /**
   * Liefert int.
   *
   * @param column the column
   * @return int
   */
  public int getInt(int column)
  {
    int result = 0;
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      Object o = this._rowData.get(column - 1);
      if (o != null)
      {
        if (o instanceof java.lang.Integer)
        {
          result = ((Integer) o).intValue();
        }
        else
        {
          try

          {
            result = Integer.parseInt(o.toString());
          }
          catch (NumberFormatException e)
          {
            log.error("Konnte Wert nicht in int umwandeln:" + o.toString());
          }
        }
      }
    }
    return result;
  }

  /**
   * Liefert int.
   *
   * @param column the column
   * @return int
   */
  public int getInt(String column)
  {
    return this.getInt(this.getColumnRow(column));
  }

  /**
   * Liefert long.
   *
   * @param column the column
   * @return long
   */
  public long getLong(int column)
  {
    long result = 0;
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      Object o = this._rowData.get(column - 1);
      if (o != null)
      {
        if (o instanceof java.lang.Long)
        {
          result = ((Long) o).longValue();
        }
        else
        {
          try
          {
            result = Long.parseLong(o.toString());
          }
          catch (Exception e)
          {
            log.error("Konnte Wert nicht in Long umwandeln:" + o.toString());
          }
        }
      }
    }
    return result;
  }

  /**
   * Liefert long.
   *
   * @param column the column
   * @return long
   */
  public long getLong(String column)
  {
    return this.getLong(this.getColumnRow(column));
  }

  /**
   * Liefert columns.
   *
   * @return columns
   */
  public Vector<String> getColumns()
  {
    return this._colNames;
  }

  /**
   * Liefert array.
   *
   * @param column the column
   * @return array
   */
  public byte[] getArray(int column)
  {
    byte[] result = null;
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      Object o = this._rowData.get(column - 1);
      if (o instanceof byte[])
      {
        result = ((byte[]) o);
      }
    }
    return result;
  }

  /**
   * Liefert array.
   *
   * @param column the column
   * @return array
   */
  public byte[] getArray(String column)
  {
    return this.getArray(this.getColumnRow(column));
  }

  /**
   * Liefert column count.
   *
   * @return column count
   */
  public int getColumnCount()
  {
    return (this._colNames != null) ? this._colNames.size() : 0;
  }

  /**
   * Liefert column name.
   *
   * @param column the column
   * @return column name
   */
  public String getColumnName(int column)
  {
    if (this._colNames != null && column > 0 && column <= this._colNames.size())
    {
      return this._colNames.get(column - 1);
    }
    return null;
  }

  /**
   * Liefert column row.
   *
   * @param column the column
   * @return column row
   */
  private int getColumnRow(String column)
  {
    int col = 0;
    if (this._colNames != null && column != null)
    {
      for (int i = 0; i < this._colNames.size(); i++)
      {
        if (column.equalsIgnoreCase(this._colNames.get(i)))
        {
          col = i + 1;
          i = this._colNames.size();
        }
      }
    }
    if (col == 0)
    {
      log.error("Fehler bei getColumnRow(" + column + ")");
      log.error("Column not found:" + column);
    }
    return col;
  }

  /**
   * Liefert class.
   *
   * @param column the column
   * @return the class
   */
  public Class<?> getClass(int column)
  {
    if (this._rowData != null && column > 0 && column <= this._rowData.size())
    {
      return (this._rowData.get(column - 1)).getClass();
    }
    log.error("Fehler bei getClass(" + column + ")");
    return null;
  }

  /**
   * Liefert column type.
   *
   * @param column the column
   * @return column type
   */
  public int getColumnType(int column)
  {
    if (this._columnTypes != null && column > 0 && column <= this._rowData.size())
    {
      return this._columnTypes[column - 1];
    }
    log.error("Fehler bei getColumnType(" + column + ")");
    return 0;
  }

  /**
   * Liefert table data.
   *
   * @return table data
   */
  public Vector<Vector<Object>> getTableData()
  {
    return this._tableData;
  }

  /**
   * Liefert table map.
   *
   * @return table map
   */
  public Map<String, Object> getTableMap()
  {
    HashMap<String, Object> map = new HashMap<>(this._colNames.size());
    if (this._rowData != null && !this._colNames.isEmpty())
    {
      for (int i = 0; i < this._colNames.size(); i++)
      {
        map.put(this._colNames.get(i), this._rowData.get(i));
      }
    }
    return map;
  }
}
