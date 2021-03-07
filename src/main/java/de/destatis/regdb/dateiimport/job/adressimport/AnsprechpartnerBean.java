/*
 * @(#)AnsprechpartnerBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.PreparedInsert;
import de.destatis.regdb.db.PreparedUpdate;
import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.res.job.JobException;

/**
 * The Class AnsprechpartnerBean.
 */
public class AnsprechpartnerBean extends AbstractBean
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Constant SQL_INSERT_ANSPRECHPARTNER.
   */
  public static final String SQL_INSERT_ANSPRECHPARTNER = "INSERT INTO ansprechpartner (ANREDE, NAME, VORNAME, ABTEILUNG,TELEFON,MOBIL,FAX,EMAIL,SACHBEARBEITER_ID,STATUS,ZEITPUNKT_EINTRAG) VALUES (?,?,?,?,?,?,?,?,?,'NEU',?)";

  /**
   * The constant SQL_UPDATE_ANSPRECHPARTNER.
   */
  public static final String SQL_UPDATE_ANSPRECHPARTNER = "UPDATE ansprechpartner SET ANREDE=?, NAME=?, VORNAME=?, ABTEILUNG=?,TELEFON=?,MOBIL=?,FAX=?,EMAIL=?,SACHBEARBEITER_ID=?,STATUS='AEND',ZEITPUNKT_AENDERUNG=? WHERE ANSPRECHPARTNER_ID=?";

  /**
   * The ansprechpartner id.
   */
  private Integer ansprechpartnerId;

  /**
   * The anrede.
   */
  private String anrede;

  /**
   * The name.
   */
  private String name;

  /**
   * The vorname.
   */
  private String vorname;

  /**
   * The abteilung.
   */
  private String abteilung;

  /**
   * The telefon.
   */
  private String telefon;

  /**
   * The mobil.
   */
  private String mobil;

  /**
   * The fax.
   */
  private String fax;

  /**
   * The email.
   */
  private String email;

  /**
   * The sachbearbeiter id.
   */
  private Integer sachbearbeiterId;

  /**
   * The zeitpunkt eintrag.
   */
  private String zeitpunktEintrag;

  /**
   * The zeitpunkt aenderung.
   */
  private String zeitpunktAenderung;

  /**
   * The neu.
   */
  private boolean neu;

  private String status;

  /**
   * Instantiates a new ansprechpartner bean.
   */
  public AnsprechpartnerBean()
  {
    super();
    this.neu = true;
  }

  /**
   * Liefert anrede.
   *
   * @return anrede anrede
   */
  public String getAnrede()
  {
    return this.anrede;
  }

  /**
   * Setzt anrede.
   *
   * @param anrede anrede
   */
  public void setAnrede(String anrede)
  {
    this.anrede = anrede;
  }

  /**
   * Liefert name.
   *
   * @return name name
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Setzt name.
   *
   * @param name name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Liefert vorname.
   *
   * @return vorname vorname
   */
  public String getVorname()
  {
    return this.vorname;
  }

  /**
   * Setzt vorname.
   *
   * @param vorname vorname
   */
  public void setVorname(String vorname)
  {
    this.vorname = vorname;
  }

  /**
   * Liefert abteilung.
   *
   * @return abteilung abteilung
   */
  public String getAbteilung()
  {
    return this.abteilung;
  }

  /**
   * Setzt abteilung.
   *
   * @param abteilung abteilung
   */
  public void setAbteilung(String abteilung)
  {
    this.abteilung = abteilung;
  }

  /**
   * Liefert telefon.
   *
   * @return telefon telefon
   */
  public String getTelefon()
  {
    return this.telefon;
  }

  /**
   * Setzt telefon.
   *
   * @param telefon telefon
   */
  public void setTelefon(String telefon)
  {
    this.telefon = telefon;
  }

  /**
   * Liefert mobil.
   *
   * @return mobil mobil
   */
  public String getMobil()
  {
    return this.mobil;
  }

  /**
   * Setzt mobil.
   *
   * @param mobil mobil
   */
  public void setMobil(String mobil)
  {
    this.mobil = mobil;
  }

  /**
   * Liefert fax.
   *
   * @return fax fax
   */
  public String getFax()
  {
    return this.fax;
  }

  /**
   * Setzt fax.
   *
   * @param fax fax
   */
  public void setFax(String fax)
  {
    this.fax = fax;
  }

  /**
   * Liefert email.
   *
   * @return email email
   */
  public String getEmail()
  {
    return this.email;
  }

  /**
   * Setzt email.
   *
   * @param email email
   */
  public void setEmail(String email)
  {
    this.email = email;
  }

  /**
   * Gets the ansprechpartner id.
   *
   * @return the ansprechpartner id
   */
  public Integer getAnsprechpartnerId()
  {
    return this.ansprechpartnerId;
  }

  /**
   * Sets the ansprechpartner id.
   *
   * @param ansprechpartnerId the new ansprechpartner id
   */
  public void setAnsprechpartnerId(Integer ansprechpartnerId)
  {
    this.ansprechpartnerId = ansprechpartnerId;
  }

  /**
   * Gets the sachbearbeiter id.
   *
   * @return the sachbearbeiter id
   */
  public Integer getSachbearbeiterId()
  {
    return this.sachbearbeiterId;
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
   * Gets the zeitpunkt aenderung.
   *
   * @return the zeitpunkt aenderung
   */
  public String getZeitpunktAenderung()
  {
    return this.zeitpunktAenderung;
  }

  /**
   * Setzt zeitpunkt aenderung.
   *
   * @param zeitpunktAenderung zeitpunkt aenderung
   */
  public void setZeitpunktAenderung(String zeitpunktAenderung)
  {
    this.zeitpunktAenderung = zeitpunktAenderung;
  }

  /**
   * Checks if is neu.
   *
   * @return true, if is neu
   */
  public boolean isNeu()
  {
    return this.neu;
  }

  /**
   * Sets the neu.
   *
   * @param neu the new neu
   */
  public void setNeu(boolean neu)
  {
    this.neu = neu;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public String getStatus()
  {
    return this.status;
  }

  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(String status)
  {
    this.status = status;
  }

  /**
   * Insert.
   *
   * @param ps the ps
   * @return the int
   * @throws JobException the SQL exception
   */
  public int insert(PreparedInsert ps) throws JobException
  {
    ps.addValue(this.notNull(this.getAnrede())); //
    ps.addValue(this.notNull(this.getName())); //
    ps.addValue(this.notNull(this.getVorname())); //
    ps.addValue(this.notNull(this.getAbteilung())); //
    ps.addValue(this.notNull(this.getTelefon())); //
    ps.addValue(this.notNull(this.getMobil())); //
    ps.addValue(this.notNull(this.getFax())); //
    ps.addValue(this.notNull(this.getEmail())); //
    ps.addValue(this.notNull(this.getSachbearbeiterId())); //
    ps.addValue(this.getZeitpunktEintrag());
    int result = ps.insert();
    ResultRow row = ps.getGeneratedKeys();
    this.setAnsprechpartnerId(row.getInt(1));
    return result;
  }

  /**
   * Update.
   *
   * @param ps the ps
   * @return the int
   * @throws JobException the SQL exception
   */
  public int update(PreparedUpdate ps) throws JobException
  {
    ps.addValue(this.notNull(this.getAnrede())); //
    ps.addValue(this.notNull(this.getName())); //
    ps.addValue(this.notNull(this.getVorname())); //
    ps.addValue(this.notNull(this.getAbteilung())); //
    ps.addValue(this.notNull(this.getTelefon())); //
    ps.addValue(this.notNull(this.getMobil())); //
    ps.addValue(this.notNull(this.getFax())); //
    ps.addValue(this.notNull(this.getEmail())); //
    ps.addValue(this.notNull(this.getSachbearbeiterId())); //
    ps.addValue(this.getZeitpunktAenderung());
    ps.addValue(this.getAnsprechpartnerId());
    return ps.update();
  }

  /**
   * Sets values from result set.
   *
   * @param row the row
   */
  public void setValuesFromResultSet(ResultRow row)
  {
    setAnrede(row.getString("ap_anrede"));
    setName(row.getString("ap_name"));
    setVorname(row.getString("ap_vorname"));
    setAbteilung(row.getString("ap_abteilung"));
    setTelefon(row.getString("ap_telefon"));
    setMobil(row.getString("ap_mobil"));
    setFax(row.getString("ap_fax"));
    setEmail(row.getString("ap_email"));
    setSachbearbeiterId(row.getInt("ap_sachbearbeiter_id"));
    setStatus(row.getString("ap_status"));
    setZeitpunktAenderung(row.getString("ap_zeitpunkt_aenderung"));
  }

}
