/*
 * @(#)FirmenBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.PreparedInsert;
import de.destatis.regdb.db.PreparedUpdate;
import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.res.job.JobException;

/**
 * The Class FirmenBean.
 */
public class FirmenBean extends AbstractBean
{

  /**
   * The Constant SQL_INSERT_FIRMEN.
   */
  public static final String SQL_INSERT_FIRMEN = "INSERT INTO firmen (SACHBEARBEITER_ID, NAME, NAME_ERGAENZUNG, KURZTEXT, ANSPRECHPARTNER_ID, STATUS, ZEITPUNKT_EINTRAG) VALUES(?,?,?,?,?,'NEU',?)";

  /**
   * The Constant SQL_UPDATE_FIRMEN.
   */
  public static final String SQL_UPDATE_FIRMEN = "UPDATE firmen,ansprechpartner SET firmen.SACHBEARBEITER_ID=?, firmen.NAME=?, firmen.NAME_ERGAENZUNG=?, firmen.KURZTEXT=?, firmen.STATUS='AEND', firmen.ZEITPUNKT_AENDERUNG=?, ansprechpartner.NAME=?,ansprechpartner.VORNAME=?,ansprechpartner.TELEFON=?,ansprechpartner.FAX=?,ansprechpartner.EMAIL=?, ansprechpartner.STATUS='AEND',ansprechpartner.ZEITPUNKT_AENDERUNG=? WHERE firmen.ANSPRECHPARTNER_ID = ansprechpartner.ANSPRECHPARTNER_ID AND firmen.FIRMEN_ID=? AND ansprechpartner.ANSPRECHPARTNER_ID=?";

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The firmen id. */
  private Integer firmenId;

  /** The sachbearbeiter id. */
  private Integer sachbearbeiterId;

  /** The name. */
  private String name;

  /** The name ergaenzung. */
  private String nameErgaenzung;

  /** The kurztext. */
  private String kurztext;

  /** The zeitpunkt eintrag. */
  private String zeitpunktEintrag;

  /** The zeitpunkt aenderung. */
  private String zeitpunktAenderung;

  /** The ansprechpartner. */
  private final AnsprechpartnerBean ansprechpartner;

  /** The neu. */
  private boolean neu;

  /**
   * Instantiates a new firmen bean.
   */
  public FirmenBean()
  {
    super();
    this.setNeu(true);
    this.ansprechpartner = new AnsprechpartnerBean();
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
   * Gets the name.
   *
   * @return the name
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Gets the name ergaenzung.
   *
   * @return the name ergaenzung
   */
  public String getNameErgaenzung()
  {
    return this.nameErgaenzung;
  }

  /**
   * Sets the name ergaenzung.
   *
   * @param nameErgaenzung the new name ergaenzung
   */
  public void setNameErgaenzung(String nameErgaenzung)
  {
    this.nameErgaenzung = nameErgaenzung;
  }

  /**
   * Gets the kurztext.
   *
   * @return the kurztext
   */
  public String getKurztext()
  {
    return this.kurztext;
  }

  /**
   * Sets the kurztext.
   *
   * @param kurztext the new kurztext
   */
  public void setKurztext(String kurztext)
  {
    this.kurztext = kurztext;
  }

  /**
   * Gets the ansprechpartner.
   *
   * @return the ansprechpartner
   */
  public AnsprechpartnerBean getAnsprechpartner()
  {
    return this.ansprechpartner;
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
   * Sets the zeitpunkt aenderung.
   *
   * @param zeitpunktAenderung the new zeitpunkt aenderung
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
   * Insert.
   *
   * @param pi the pi
   * @return the int
   * @throws JobException the SQL exception
   */
  public int insert(PreparedInsert pi) throws JobException
  {
    pi.addValue(this.getSachbearbeiterId()); //
    pi.addValue(this.notNull(this.getName())); //
    pi.addValue(this.notNull(this.getNameErgaenzung())); //
    pi.addValue(this.notNull(this.getKurztext())); //
    pi.addValue(this.ansprechpartner.getAnsprechpartnerId()); //
    pi.addValue(this.getZeitpunktEintrag());
    int result = pi.insert();
    ResultRow row = pi.getGeneratedKeys();
    this.setFirmenId(row != null ? row.getInt(1) : 0);
    return result;
  }

  /**
   * Update.
   *
   * @param pu the pu
   * @return the int
   * @throws JobException the SQL exception
   */
  public int update(PreparedUpdate pu) throws JobException
  {
    pu.addValue(this.getSachbearbeiterId()); //
    pu.addValue(this.notNull(this.getName())); //
    pu.addValue(this.notNull(this.getNameErgaenzung())); //
    pu.addValue(this.notNull(this.getKurztext())); //
    pu.addValue(this.getZeitpunktAenderung());
    pu.addValue(this.notNull(this.ansprechpartner.getName()));
    pu.addValue(this.notNull(this.ansprechpartner.getVorname())); //
    pu.addValue(this.notNull(this.ansprechpartner.getTelefon())); //
    pu.addValue(this.notNull(this.ansprechpartner.getFax())); //
    pu.addValue(this.notNull(this.ansprechpartner.getEmail())); //
    pu.addValue(this.notNull(this.getZeitpunktAenderung()));
    pu.addValue(this.getFirmenId());
    pu.addValue(this.ansprechpartner.getAnsprechpartnerId());
    return pu.update();
  }

}
