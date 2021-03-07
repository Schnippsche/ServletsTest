/*
 * @(#)FirmenAdressenBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.PreparedUpdate;
import de.werum.sis.idev.res.job.JobException;

/**
 * The Class FirmenAdressenBean.
 */
public class FirmenAdressenBean extends AbstractBean
{

  /**
   * The Constant SQL_INSERT_FIRMEN_ADRESSEN.
   */
  public static final String SQL_INSERT_FIRMEN_ADRESSEN = "INSERT IGNORE INTO firmen_adressen (FIRMEN_ID, ADRESSEN_ID, SACHBEARBEITER_ID, STATUS, ZEITPUNKT_EINTRAG) VALUES(?,?,?,'AKTIV',?)";

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The firmen id.
   */
  private Integer firmenId;

  /**
   * The adressen id.
   */
  private Integer adressenId;

  /**
   * The sachbearbeiter id.
   */
  private Integer sachbearbeiterId;

  /**
   * The zeitpunkt eintrag.
   */
  private String zeitpunktEintrag;

  /**
   * Instantiates a new firmen adressen bean.
   */
  public FirmenAdressenBean()
  {
    super();
    this.firmenId = 0;
    this.adressenId = 0;
  }

  /**
   * Gets the firmen id.
   *
   * @return the firmen id
   */
  public Integer getFirmenId()
  {
    return this.firmenId;
  }

  /**
   * Sets the firmen id.
   *
   * @param firmenId the new firmen id
   */
  public void setFirmenId(Integer firmenId)
  {
    this.firmenId = firmenId;
  }

  /**
   * Gets the adressen id.
   *
   * @return the adressen id
   */
  public Integer getAdressenId()
  {
    return this.adressenId;
  }

  /**
   * Sets the adressen id.
   *
   * @param adressenId the new adressen id
   */
  public void setAdressenId(Integer adressenId)
  {
    this.adressenId = adressenId;
  }

  /**
   * Sets the sachbearbeiter id.
   *
   * @param sachbearbeiterId the new sachbearbeiter id
   */
  public void setSachbearbeiterId(Integer sachbearbeiterId)
  {
    this.sachbearbeiterId = sachbearbeiterId;
  }

  /**
   * Gets the zeitpunkt eintrag.
   *
   * @return the zeitpunkt eintrag
   */
  public String getZeitpunktEintrag()
  {
    return this.zeitpunktEintrag;
  }

  /**
   * Sets the zeitpunkt eintrag.
   *
   * @param zeitpunktEintrag the new zeitpunkt eintrag
   */
  public void setZeitpunktEintrag(String zeitpunktEintrag)
  {
    this.zeitpunktEintrag = zeitpunktEintrag;
  }

  /**
   * Insert.
   *
   * @param pu the ps
   * @return the int
   * @throws JobException the SQL exception
   */
  public int insert(PreparedUpdate pu) throws JobException
  {
    pu.addValue(this.getFirmenId()); //
    pu.addValue(this.getAdressenId()); //
    pu.addValue(this.sachbearbeiterId); //
    pu.addValue(this.getZeitpunktEintrag());
    return pu.update();
  }

}
