/*
 * @(#)ServerimportStatusBean.java 1.00.30.01.2020
 * Copyright 2020 Statistisches Bundesamt
 */
package de.destatis.regdb;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ServerimportStatusBean implements Serializable
{

  private static final long serialVersionUID = -5732112362189506470L;

  public static final String SQL_SERVERIMPORTSTATUS_SELECT = "SELECT IMPORT_VERWALTUNG_ID, ZEITPUNKT_START,ZEITPUNKT_AENDERUNG,ZEITPUNKT_ENDE, GESAMT_STATUS, ERGEBNIS_STATUS,DATEINAME,STATISTIK_ID, AMT, QUELL_REFERENZ_ID, BEMERKUNG, SACHBEARBEITER_ID,ANZAHL_NEU,ANZAHL_GEAENDERT,ANZAHL_GELOESCHT FROM import_verwaltung";

  private int serverimportId;
  private Date zeitpunktStart;
  private Date zeitpunktAenderung;
  private Date zeitpunktEnde;
  private String status;
  private String dateiname;
  private int statistikId;
  private String amt;
  private int quellReferenzId;
  private String ergebnisStatus;
  private String bemerkung;
  private int sachbearbeiterId;
  private int summeNeu;
  private int summeGeaendert;
  private int summeGeloescht;

  /**
   * Instantiates a new serverimport status bean.
   */
  public ServerimportStatusBean()
  {
    super();
  }

  /**
   * Instantiates a new serverimport status bean.
   *
   * @param rs the rs
   * @throws SQLException the SQL exception
   */
  public ServerimportStatusBean(ResultSet rs) throws SQLException
  {
    this.serverimportId = rs.getInt("IMPORT_VERWALTUNG_ID");
    Timestamp ts = rs.getTimestamp("ZEITPUNKT_START");
    this.zeitpunktStart = (ts == null) ? null : new java.util.Date(ts.getTime());
    ts = rs.getTimestamp("ZEITPUNKT_AENDERUNG");
    this.zeitpunktAenderung = (ts == null) ? null : new java.util.Date(ts.getTime());
    ts = rs.getTimestamp("ZEITPUNKT_ENDE");
    this.zeitpunktEnde = (ts == null) ? null : new java.util.Date(ts.getTime());
    this.status = rs.getString("GESAMT_STATUS");
    this.dateiname = rs.getString("DATEINAME");
    this.statistikId = rs.getInt("STATISTIK_ID");
    this.amt = rs.getString("AMT");
    this.quellReferenzId = rs.getInt("QUELL_REFERENZ_ID");
    this.ergebnisStatus = rs.getString("ERGEBNIS_STATUS");
    this.bemerkung = rs.getString("BEMERKUNG");
    this.sachbearbeiterId = rs.getInt("SACHBEARBEITER_ID");
    this.summeNeu = rs.getInt("ANZAHL_NEU");
    this.summeGeaendert = rs.getInt("ANZAHL_GEAENDERT");
    this.summeGeloescht = rs.getInt("ANZAHL_GELOESCHT");
  }

  /**
   * Liefert serverimport id.
   *
   * @return serverimport id
   */
  public int getServerimportId()
  {
    return this.serverimportId;
  }

  /**
   * Liefert zeitpunk start.
   *
   * @return zeitpunk start
   */
  public Date getZeitpunktStart()
  {
    return this.zeitpunktStart;
  }

  /**
   * Liefert zeitpunk aenderung.
   *
   * @return zeitpunk aenderung
   */
  public Date getZeitpunktAenderung()
  {
    return this.zeitpunktAenderung;
  }

  /**
   * Liefert zeitpunk ende.
   *
   * @return zeitpunk ende
   */
  public Date getZeitpunktEnde()
  {
    return this.zeitpunktEnde;
  }

  /**
   * Liefert status.
   *
   * @return status
   */
  public String getStatus()
  {
    return this.status;
  }

  /**
   * Liefert dateiname.
   *
   * @return dateiname
   */
  public String getDateiname()
  {
    return this.dateiname;
  }

  /**
   * Liefert statistik id.
   *
   * @return statistik id
   */
  public int getStatistikId()
  {
    return this.statistikId;
  }

  /**
   * Liefert amt.
   *
   * @return amt
   */
  public String getAmt()
  {
    return this.amt;
  }

  /**
   * Liefert quell referenz id.
   *
   * @return quell referenz id
   */
  public int getQuellReferenzId()
  {
    return this.quellReferenzId;
  }

  /**
   * Liefert ergebnis status.
   *
   * @return ergebnis status
   */
  public String getErgebnisStatus()
  {
    return this.ergebnisStatus;
  }

  /**
   * Liefert bemerkung.
   *
   * @return bemerkung
   */
  public String getBemerkung()
  {
    return this.bemerkung;
  }

  /**
   * Liefert sachbearbeiter id.
   *
   * @return sachbearbeiter id
   */
  public int getSachbearbeiterId()
  {
    return this.sachbearbeiterId;
  }

  public int getSummeNeu()
  {
    return this.summeNeu;
  }

  public int getSummeGeaendert()
  {
    return this.summeGeaendert;
  }

  public int getSummeGeloescht()
  {
    return this.summeGeloescht;
  }

}
