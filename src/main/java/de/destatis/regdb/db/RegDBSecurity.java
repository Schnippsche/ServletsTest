/*
 * @(#)RegDBSecurity.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.werum.sis.idev.intern.actions.util.DienstStatus;
import de.werum.sis.idev.intern.actions.util.SachbearbeiterDienst;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

public class RegDBSecurity
{

  private static RegDBSecurity instance = null;
  private static final String SPERRE = "SPERRE";
  private static final LoggerIfc log = Logger.getInstance()
      .getLogger(RegDBSecurity.class);
  public static final String ROOT_RECHT = "ROOT";
  public static final String GADMIN_RECHT = "GADMIN";
  public static final String SADMIN_RECHT = "SADMIN";
  public static final String SB_RECHT = "SB";
  public static final String BROWSE_RECHT = "BROWSE";
  public static final int ROOT_RECHT_INT = 5;
  public static final int GADMIN_RECHT_INT = 4;
  public static final int SADMIN_RECHT_INT = 3;
  public static final int SB_RECHT_INT = 2;
  public static final int BROWSE_RECHT_INT = 1;
  private static final String SQL_GRANT_AMT_STATISTIK = "SELECT DISTINCT IF(sb_gruppen_rechte_statistiken.RECHTE IS NULL, sb_gruppen.rechte, sb_gruppen_rechte_statistiken.RECHTE) FROM sb_gruppen_zuordnung LEFT JOIN sb_gruppen_rechte_statistiken USING (SB_GRUPPEN_ID) LEFT JOIN sb_gruppen ON (sb_gruppen_zuordnung.SB_GRUPPEN_ID = sb_gruppen.SB_GRUPPEN_ID) WHERE SACHBEARBEITER_ID=? AND (AMT=? OR AMT IS NULL) and (STATISTIK_ID=? OR STATISTIK_ID IS NULL)";

  /**
   * Instantiates a new reg DB security.
   */
  private RegDBSecurity()
  {
    log.info("initing RegDBSecurity");
  }

  /**
   * Gets the single instance of RegDBSecurity.
   *
   * @return single instance of RegDBSecurity
   */
  public static synchronized RegDBSecurity getInstance()
  {
    if (instance == null)
    {
      instance = new RegDBSecurity();
    }
    return instance;
  }

  /**
   * Checks if is datenbank sperre.
   *
   * @param conn the connection
   * @return true, if is datenbank sperre
   */
  public boolean isDatenbankSperre(Connection conn)
  {
    if (conn != null)
    {
      try (ResultSet rs = conn.createStatement()
          .executeQuery("SELECT status FROM version"))
      {
        if (rs.next())
        {
          return SPERRE.equals(rs.getString(1));
        }
      }
      catch (SQLException e)
      {
        log.error(e.getMessage(), e);
      }
    }
    return false;
  }

  /**
   * Liefert sachbearbeiter ID.
   *
   * @param conn the connection
   * @param kennung the kennung
   * @param passwort the passwort
   * @param host the host
   * @param port the port
   * @return sachbearbeiter ID
   */
  public int getSachbearbeiterID(Connection conn, String kennung, String passwort, String host, String port)
  {
    SachbearbeiterDienst sachbearbeiterDienst = new SachbearbeiterDienst(host, port, kennung, passwort);
    DienstStatus status;
    try
    {
      status = sachbearbeiterDienst.authentifizieren();
      if (status.getStatus() == DienstStatus.STATUS_OK)
      {
        String sql = "SELECT sachbearbeiter_id FROM sachbearbeiter WHERE kennung = '" + kennung + "'";
        try (ResultSet rs = conn.createStatement()
            .executeQuery(sql))
        {
          if (rs.next())
          {
            return rs.getInt(1);
          }
        }
      }
      else
      {
        log.error(status.getMeldung());
      }
    }
    catch (Exception e1)
    {
      log.error(e1.getMessage());
      return -2;
    }
    return -1;
  }

  /**
   * Checks if is sachbearbeiter sperre.
   *
   * @param conn the connection
   * @param sbID the sb ID
   * @return true, if is sachbearbeiter sperre
   */
  public boolean isSachbearbeiterSperre(Connection conn, String sbID)
  {
    boolean result = false;
    try (ResultSet rs = conn.createStatement()
        .executeQuery("SELECT status from sachbearbeiter where sachbearbeiter_id=" + sbID))
    {
      if (rs.next())
      {
        result = SPERRE.equals(rs.getString(1));
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return result;
  }

  /**
   * Is amt sperre.
   *
   * @param conn the connection
   * @param amt the amt
   * @return the string
   */
  public String isAmtSperre(Connection conn, String amt)
  {
    try (ResultSet rs = conn.createStatement()
        .executeQuery("SELECT status,sperre_info FROM amt WHERE amt='" + amt + "'"))
    {
      if (rs.next() && SPERRE.equals(rs.getString(1)))
      {
        return rs.getString(2);
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Checks if is root user.
   *
   * @param conn the connection
   * @param sachbearbeiterID the sachbearbeiter ID
   * @return true, if is root user
   */
  public boolean isRootUser(Connection conn, String sachbearbeiterID)
  {
    // ROOT-Recht pruefen
    String cmd = "SELECT COUNT(*) FROM sb_gruppen INNER JOIN sb_gruppen_zuordnung USING (SB_GRUPPEN_ID) WHERE RECHTE='ROOT' AND SACHBEARBEITER_ID=" + sachbearbeiterID;
    try (ResultSet rs = conn.createStatement()
        .executeQuery(cmd))
    {
      if (rs.next())
      {
        return (rs.getInt(1) > 0);
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return false;
  }

  /**
   * Prueft, ob Sachbearbeiter das angegebene Mindestrecht an Amt/Statistik hat.
   *
   * @param sbId Sachbearbeiter ID
   * @param amt Amt ID
   * @param statistik Statistik ID
   * @param minimumGrant das gewuenschte Minimalrecht
   * @param conn the conn
   * @return boolean true oder false
   */
  public boolean sbHasGrantforAmtStatistik(int sbId, String amt, String statistik, int minimumGrant, Connection conn)
  {
    boolean hasRecht = false;
    try (PreparedStatement ps = conn.prepareStatement(SQL_GRANT_AMT_STATISTIK))
    {
      ps.setInt(1, sbId);
      ps.setString(2, amt);
      ps.setString(3, statistik);
      try (ResultSet rs = ps.executeQuery())
      {
        while (rs.next() && !hasRecht)
        {
          String result = rs.getString(1);
          if (BROWSE_RECHT.equals(result) && BROWSE_RECHT_INT >= minimumGrant)
          {
            hasRecht = true;
          }
          if (SB_RECHT.equals(result) && SB_RECHT_INT >= minimumGrant)
          {
            hasRecht = true;
          }
          if (SADMIN_RECHT.equals(result) && SADMIN_RECHT_INT >= minimumGrant)
          {
            hasRecht = true;
          }
          if (GADMIN_RECHT.equals(result) && GADMIN_RECHT_INT >= minimumGrant)
          {
            hasRecht = true;
          }
          if (ROOT_RECHT.equals(result) && ROOT_RECHT_INT >= minimumGrant)
          {
            hasRecht = true;
          }
        }
      }
    }
    catch (SQLException e)
    {
      log.error(e.getMessage(), e);
    }
    return hasRecht;
  }
}
