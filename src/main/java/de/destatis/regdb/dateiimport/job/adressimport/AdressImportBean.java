/*
 * @(#)AdressImportBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.StringUtil;

import java.io.Serializable;

/**
 * The Class AdressImportBean.
 */
public class AdressImportBean implements Serializable, Comparable<AdressImportBean>
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The quell referenz int.
   */
  private String quellReferenzInt;

  /**
   * The dddkisl id.
   */
  private String dddkislId;

  /**
   * The bzr.
   */
  private String bzr;

  /**
   * The stat online key.
   */
  private String statOnlineKey;

  /**
   * The auswahlkriterium.
   */
  private String auswahlkriterium;

  private boolean firmenAdressenNeu;

  /**
   * The adresse.
   */
  private final AdresseBean adresse;

  /**
   * The firma.
   */
  private final FirmenBean firma;

  /**
   * The melder.
   */
  private final MelderBean melder;

  /**
   * The ansprechpartner fachabteilung name.
   */
  private String ansprechpartnerFachabteilungName;

  /**
   * The ansprechpartner fachabteilung telefon.
   */
  private String ansprechpartnerFachabteilungTelefon;

  /**
   * The ansprechpartner fachabteilung email.
   */
  private String ansprechpartnerFachabteilungEmail;

  /**
   * Instantiates a new adress import bean.
   */
  public AdressImportBean()
  {
    this.adresse = new AdresseBean();
    this.firma = new FirmenBean();
    this.melder = new MelderBean();
    this.setFirmenAdressenNeu(true);
  }

  /**
   * Gets the ansprechpartner fachabteilung name.
   *
   * @return the ansprechpartner fachabteilung name
   */
  public String getAnsprechpartnerFachabteilungName()
  {
    return this.ansprechpartnerFachabteilungName;
  }

  /**
   * Sets the ansprechpartner fachabteilung name.
   *
   * @param ansprechpartnerFachabteilungName the new ansprechpartner fachabteilung name
   */
  public void setAnsprechpartnerFachabteilungName(String ansprechpartnerFachabteilungName)
  {
    this.ansprechpartnerFachabteilungName = ansprechpartnerFachabteilungName;
  }

  /**
   * Gets the ansprechpartner fachabteilung telefon.
   *
   * @return the ansprechpartner fachabteilung telefon
   */
  public String getAnsprechpartnerFachabteilungTelefon()
  {
    return this.ansprechpartnerFachabteilungTelefon;
  }

  /**
   * Sets the ansprechpartner fachabteilung telefon.
   *
   * @param ansprechpartnerFachabteilungTelefon the new ansprechpartner fachabteilung telefon
   */
  public void setAnsprechpartnerFachabteilungTelefon(String ansprechpartnerFachabteilungTelefon)
  {
    this.ansprechpartnerFachabteilungTelefon = ansprechpartnerFachabteilungTelefon;
  }

  /**
   * Gets the ansprechpartner fachabteilung email.
   *
   * @return the ansprechpartner fachabteilung email
   */
  public String getAnsprechpartnerFachabteilungEmail()
  {
    return this.ansprechpartnerFachabteilungEmail;
  }

  /**
   * Sets the ansprechpartner fachabteilung email.
   *
   * @param ansprechpartnerFachabteilungEmail the new ansprechpartner fachabteilung email
   */
  public void setAnsprechpartnerFachabteilungEmail(String ansprechpartnerFachabteilungEmail)
  {
    this.ansprechpartnerFachabteilungEmail = ansprechpartnerFachabteilungEmail;
  }

  /**
   * Gets the quell referenz int.
   *
   * @return the quell referenz int
   */
  public String getQuellReferenzInt()
  {
    return this.quellReferenzInt;
  }

  /**
   * Sets the quell referenz int.
   *
   * @param quellReferenzInt the new quell referenz int
   */
  public void setQuellReferenzInt(String quellReferenzInt)
  {
    this.quellReferenzInt = StringUtil.leftTrim(quellReferenzInt, 40);
  }

  /**
   * Gets the dddkisl id.
   *
   * @return the dddkisl id
   */
  public String getDddkislId()
  {
    return this.dddkislId;
  }

  /**
   * Sets the dddkisl id.
   *
   * @param dddkislId the new dddkisl id
   */
  public void setDddkislId(String dddkislId)
  {
    this.dddkislId = dddkislId;
  }

  /**
   * Gets the bzr.
   *
   * @return the bzr
   */
  public String getBzr()
  {
    return this.bzr;
  }

  /**
   * Sets the bzr.
   *
   * @param bzr the new bzr
   */
  public void setBzr(String bzr)
  {
    this.bzr = bzr;
  }

  /**
   * Gets the stat online key.
   *
   * @return the stat online key
   */
  public String getStatOnlineKey()
  {
    return this.statOnlineKey;
  }

  /**
   * Sets the stat online key.
   *
   * @param statOnlineKey the new stat online key
   */
  public void setStatOnlineKey(String statOnlineKey)
  {
    this.statOnlineKey = StringUtil.leftTrim(statOnlineKey, 20);
  }

  /**
   * Gets the auswahlkriterium.
   *
   * @return the auswahlkriterium
   */
  public String getAuswahlkriterium()
  {
    return this.auswahlkriterium;
  }

  /**
   * Sets the auswahlkriterium.
   *
   * @param auswahlkriterium the new auswahlkriterium
   */
  public void setAuswahlkriterium(String auswahlkriterium)
  {
    this.auswahlkriterium = auswahlkriterium;
  }

  /**
   * Gets the adresse.
   *
   * @return the adresse
   */
  public AdresseBean getAdresse()
  {
    return this.adresse;
  }

  /**
   * Gets the firma.
   *
   * @return the firma
   */
  public FirmenBean getFirma()
  {
    return this.firma;
  }

  /**
   * Gets the melder.
   *
   * @return the melder
   */
  public MelderBean getMelder()
  {
    return this.melder;
  }

  /**
   * Checks if is firmen adressen neu.
   *
   * @return true, if is firmen adressen neu
   */
  public boolean isFirmenAdressenNeu()
  {
    return this.firmenAdressenNeu;
  }

  /**
   * Setzt firmen adressen neu.
   *
   * @param firmenAdressenNeu firmen adressen neu
   */
  public void setFirmenAdressenNeu(boolean firmenAdressenNeu)
  {
    this.firmenAdressenNeu = firmenAdressenNeu;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.adresse.getQuellReferenzOf() == null) ? 0 : this.adresse.getQuellReferenzOf().hashCode());
    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (this.getClass() != obj.getClass())
    {
      return false;
    }
    AdressImportBean other = (AdressImportBean) obj;
    if (this.adresse.getQuellReferenzOf() == null)
    {
      return other.adresse.getQuellReferenzOf() == null;
    }
    return this.adresse.getQuellReferenzOf().equals(other.adresse.getQuellReferenzOf());
  }

  /**
   * Compare to.
   *
   * @param other the other
   * @return the int
   */
  @Override
  public int compareTo(AdressImportBean other)
  {
    return this.adresse.getQuellReferenzOf().compareTo(other.adresse.getQuellReferenzOf());
  }

}
