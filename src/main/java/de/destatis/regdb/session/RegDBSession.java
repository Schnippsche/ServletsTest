/*
 * @(#)RegDBSession.java 1.00.23.03.2020
 * Copyright 2020 Statistisches Bundesamt
 */
package de.destatis.regdb.session;

import java.sql.Connection;
import java.util.Date;

/**
 * The Class RegDBSession.
 */
public class RegDBSession
{

  /** The session id. */
  private String sessionId;

  /** The last use time. */
  private Date lastUseTime;

  /** The sachbearbeiter kennung. */
  private final String sachbearbeiterKennung;

  /** The sachbearbeiter id. */
  private final String sachbearbeiterId;

  /** The sachbearbeiter sperre. */
  private boolean sachbearbeiterSperre;

  /** The sachbearbeiter passwort. */
  private String sachbearbeiterPasswort;

  /** The datenbank sperre. */
  private boolean datenbankSperre;

  /** The root user. */
  private boolean rootUser;

  /** The connection. */
  private Connection connection;

  /**
   * Instantiates a new reg DB session.
   *
   * @param sachbearbeiterKennung the sachbearbeiter kennung
   * @param sachbearbeiterId the sachbearbeiter id
   */
  public RegDBSession(String sachbearbeiterKennung, String sachbearbeiterId)
  {
    this.sachbearbeiterKennung = sachbearbeiterKennung;
    this.sachbearbeiterId = sachbearbeiterId;
    this.sessionId = null;
    this.lastUseTime = new Date();
    this.rootUser = false;
    this.connection = null;
  }

  /**
   * Liefert session id.
   *
   * @return session id
   */
  public String getSessionId()
  {
    return this.sessionId;
  }

  /**
   * Setzt session id.
   *
   * @param sessionId session id
   */
  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }

  /**
   * Liefert last use time.
   *
   * @return last use time
   */
  public Date getLastUseTime()
  {
    return (Date) this.lastUseTime.clone();
  }

  /**
   * Update last use time.
   */
  public void updateLastUseTime()
  {
    this.lastUseTime = new Date();
  }

  /**
   * Liefert sachbearbeiter id.
   *
   * @return sachbearbeiter id
   */
  public String getSachbearbeiterId()
  {
    return this.sachbearbeiterId;
  }

  /**
   * Liefert sachbearbeiter kennung.
   *
   * @return sachbearbeiter kennung
   */
  public String getSachbearbeiterKennung()
  {
    return this.sachbearbeiterKennung;
  }

  /**
   * Liefert sachbearbeiter passwort.
   *
   * @return sachbearbeiter passwort
   */
  public String getSachbearbeiterPasswort()
  {
    return this.sachbearbeiterPasswort;
  }

  /**
   * Setzt sachbearbeiter passwort.
   *
   * @param sachbearbeiterPasswort sachbearbeiter passwort
   */
  public void setSachbearbeiterPasswort(String sachbearbeiterPasswort)
  {
    this.sachbearbeiterPasswort = sachbearbeiterPasswort;
  }

  /**
   * Liefert sachbearbeiter sperre.
   *
   * @return sachbearbeiter sperre
   */
  public boolean getSachbearbeiterSperre()
  {
    return this.sachbearbeiterSperre;
  }

  /**
   * Setzt sachbearbeiter sperre.
   *
   * @param value sachbearbeiter sperre
   */
  public void setSachbearbeiterSperre(boolean value)
  {
    this.sachbearbeiterSperre = value;
  }

  /**
   * Setzt datenbank sperre.
   *
   * @param value datenbank sperre
   */
  public void setDatenbankSperre(boolean value)
  {
    this.datenbankSperre = value;
  }

  /**
   * Liefert datenbank sperre.
   *
   * @return datenbank sperre
   */
  public boolean getDatenbankSperre()
  {
    return this.datenbankSperre;
  }

  /**
   * Checks if is root user.
   *
   * @return true, if is root user
   */
  public boolean isRootUser()
  {
    return this.rootUser;
  }

  /**
   * Setzt root user.
   *
   * @param rootUser root user
   */
  public void setRootUser(boolean rootUser)
  {
    this.rootUser = rootUser;
  }

  /**
   * Gets the connection.
   *
   * @return the connection
   */
  public Connection getConnection()
  {
    return this.connection;
  }

  /**
   * Sets the connection.
   *
   * @param connection the new connection
   */
  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }

}
