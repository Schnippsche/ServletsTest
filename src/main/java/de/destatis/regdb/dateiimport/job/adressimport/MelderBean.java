/*
 * @(#)MelderBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.PreparedInsert;
import de.destatis.regdb.db.PreparedUpdate;
import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.intern.actions.util.MelderDaten;
import de.werum.sis.idev.res.job.JobException;

/**
 * The Class MelderBean.
 */
public class MelderBean extends AbstractBean
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Constant SQL_INSERT_MELDER.
   */
  public static final String SQL_INSERT_MELDER = "INSERT INTO melder (FIRMEN_ID, ADRESSEN_ID, ANSPRECHPARTNER_ID, SACHBEARBEITER_ID, KENNUNG, SYSTEM_PASSWORT, PASSWORT, PRIVATER_SCHLUESSEL, OEFFENTLICHER_SCHLUESSEL, PRIVATER_SCHLUESSEL_GESCHUETZT, OEFFENTLICHER_SCHLUESSEL_GESCHUETZT, PASSWORT_AENDERBAR, PASSWORT_AENDERUNG, ZUSAMMENFUEHRBAR, STATUS, ZEITPUNKT_REGISTRIERUNG) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,'NEU',?)";

  /**
   * The Constant SQL_UPDATE_MELDER.
   */
  public static final String SQL_UPDATE_MELDER = "UPDATE melder, ansprechpartner SET ansprechpartner.ANREDE=?,ansprechpartner.NAME=?,ansprechpartner.VORNAME=?, ansprechpartner.ABTEILUNG=?,ansprechpartner.TELEFON=?,ansprechpartner.MOBIL=?,ansprechpartner.FAX=?,ansprechpartner.EMAIL=?,ansprechpartner.SACHBEARBEITER_ID=?,ansprechpartner.STATUS='AEND',ansprechpartner.ZEITPUNKT_AENDERUNG=?,melder.SACHBEARBEITER_ID=?, melder.STATUS='AEND', melder.ZEITPUNKT_AENDERUNG=?,melder.FIRMEN_ID=? WHERE melder.ANSPRECHPARTNER_ID = ansprechpartner.ANSPRECHPARTNER_ID AND melder.MELDER_ID = ? AND ansprechpartner.ANSPRECHPARTNER_ID=?";

  /**
   * The Constant SQL_UPDATE_MELDER.
   */
  public static final String SQL_UPDATE_EXTENDED_MELDER = "UPDATE melder, ansprechpartner SET ansprechpartner.ANREDE=?,ansprechpartner.NAME=?,ansprechpartner.VORNAME=?, ansprechpartner.ABTEILUNG=?,ansprechpartner.TELEFON=?,ansprechpartner.MOBIL=?,ansprechpartner.FAX=?,ansprechpartner.EMAIL=?,ansprechpartner.SACHBEARBEITER_ID=?,ansprechpartner.STATUS='AEND',ansprechpartner.ZEITPUNKT_AENDERUNG=?, melder.PASSWORT_AENDERBAR=?, melder.PASSWORT_AENDERUNG=?, melder.ZUSAMMENFUEHRBAR=?, melder.SACHBEARBEITER_ID=?, melder.STATUS='AEND', melder.ZEITPUNKT_AENDERUNG=?,melder.FIRMEN_ID=? WHERE melder.ANSPRECHPARTNER_ID = ansprechpartner.ANSPRECHPARTNER_ID AND melder.MELDER_ID = ? AND ansprechpartner.ANSPRECHPARTNER_ID=?";

  /**
   * The Constant SQL_UPDATE_MELDER.
   */
  public static final String SQL_UPDATE_MELDER_REGISTER = "UPDATE melder, ansprechpartner SET ansprechpartner.ANREDE=?,ansprechpartner.NAME=?,ansprechpartner.VORNAME=?,ansprechpartner.SACHBEARBEITER_ID=?,ansprechpartner.STATUS='AEND',ansprechpartner.ZEITPUNKT_AENDERUNG=?,melder.SACHBEARBEITER_ID=?, melder.STATUS='AEND', melder.ZEITPUNKT_AENDERUNG=?,melder.FIRMEN_ID=? WHERE melder.ANSPRECHPARTNER_ID = ansprechpartner.ANSPRECHPARTNER_ID AND melder.MELDER_ID = ? AND ansprechpartner.ANSPRECHPARTNER_ID=?";

  /**
   * The constant SQL_SELECT_FUER_PASSWOERTER.
   */
  public static final String SQL_SELECT_FUER_PASSWOERTER = "SELECT MELDER_ID,PRIVATER_SCHLUESSEL,PRIVATER_SCHLUESSEL_GESCHUETZT,OEFFENTLICHER_SCHLUESSEL,OEFFENTLICHER_SCHLUESSEL_GESCHUETZT,SYSTEM_PASSWORT,PASSWORT,PASSWORT_AENDERUNG,ZEITPUNKT_AENDERUNG,STATUS FROM melder WHERE MELDER_ID IN({0})";

  /**
   * The melder id.
   */
  private Integer melderId;

  /**
   * The firmen id.
   */
  private Integer firmenId;

  /**
   * The adressen id.
   */
  private Integer adressenId;

  /**
   * The ansprechpartner.
   */
  private final AnsprechpartnerBean ansprechpartner;

  /**
   * The sachbearbeiter id.
   */
  private Integer sachbearbeiterId;

  /**
   * The kennung.
   */
  private String kennung;

  /**
   * The melder daten.
   */
  private transient MelderDaten melderDaten;

  /**
   * The passwort aenderbar.
   */
  private boolean passwortAenderbar;

  /**
   * The passwort aenderung.
   */
  private boolean passwortAenderung;

  /**
   * The zusammenfuehrbar.
   */
  private boolean zusammenfuehrbar;

  /**
   * The zeitpunkt registrierung.
   */
  private String zeitpunktRegistrierung;

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
   * Instantiates a new melder bean.
   */
  public MelderBean()
  {
    super();
    this.setNeu(true);
    this.ansprechpartner = new AnsprechpartnerBean();
    this.firmenId = 0;
    this.passwortAenderbar = true;
    this.passwortAenderung = true;
    this.zusammenfuehrbar = true;

  }

  /**
   * Gets the melder id.
   *
   * @return the melder id
   */
  public Integer getMelderId()
  {
    return this.melderId;
  }

  /**
   * Sets the melder id.
   *
   * @param melderId the new melder id
   */
  public void setMelderId(Integer melderId)
  {
    this.melderId = melderId;
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
   * Gets the ansprechpartner.
   *
   * @return the ansprechpartner
   */
  public AnsprechpartnerBean getAnsprechpartner()
  {
    return this.ansprechpartner;
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
   * Gets the kennung.
   *
   * @return the kennung
   */
  public String getKennung()
  {
    return this.kennung;
  }

  /**
   * Sets the kennung.
   *
   * @param kennung the new kennung
   */
  public void setKennung(String kennung)
  {
    this.kennung = kennung;
  }

  /**
   * Gets the melder daten.
   *
   * @return the melder daten
   */
  public MelderDaten getMelderDaten()
  {
    return this.melderDaten;
  }

  /**
   * Sets the melder daten.
   *
   * @param melderDaten the new melder daten
   */
  public void setMelderDaten(MelderDaten melderDaten)
  {
    this.melderDaten = melderDaten;
  }

  /**
   * Checks if is passwort aenderbar.
   *
   * @return true, if is passwort aenderbar
   */
  public boolean isPasswortAenderbar()
  {
    return this.passwortAenderbar;
  }

  /**
   * Sets the passwort aenderbar.
   *
   * @param passwortAenderbar the new passwort aenderbar
   */
  public void setPasswortAenderbar(boolean passwortAenderbar)
  {
    this.passwortAenderbar = passwortAenderbar;
  }

  /**
   * Checks if is passwort aenderung.
   *
   * @return true, if is passwort aenderung
   */
  public boolean isPasswortAenderung()
  {
    return this.passwortAenderung;
  }

  /**
   * Sets the passwort aenderung.
   *
   * @param passwortAenderung the new passwort aenderung
   */
  public void setPasswortAenderung(boolean passwortAenderung)
  {
    this.passwortAenderung = passwortAenderung;
  }

  /**
   * Checks if is zusammenfuehrbar.
   *
   * @return true, if is zusammenfuehrbar
   */
  public boolean isZusammenfuehrbar()
  {
    return this.zusammenfuehrbar;
  }

  /**
   * Sets the zusammenfuehrbar.
   *
   * @param zusammenfuehrbar the new zusammenfuehrbar
   */
  public void setZusammenfuehrbar(boolean zusammenfuehrbar)
  {
    this.zusammenfuehrbar = zusammenfuehrbar;
  }

  /**
   * Gets the zeitpunkt registrierung.
   *
   * @return the zeitpunkt registrierung
   */
  public String getZeitpunktRegistrierung()
  {
    return this.zeitpunktRegistrierung;
  }

  /**
   * Sets the zeitpunkt registrierung.
   *
   * @param zeitpunktRegistrierung the new zeitpunkt registrierung
   */
  public void setZeitpunktRegistrierung(String zeitpunktRegistrierung)
  {
    this.zeitpunktRegistrierung = zeitpunktRegistrierung;
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
    ps.addValue(this.getFirmenId()); //
    ps.addValue(this.getAdressenId()); //
    ps.addValue(this.ansprechpartner.getAnsprechpartnerId()); //
    ps.addValue(this.getSachbearbeiterId()); //
    ps.addValue(this.notNull(this.getKennung()));
    MelderDaten daten = this.getMelderDaten();
    ps.addValue(this.notNull(daten.getSystemPasswort()));
    ps.addValue(this.notNull(daten.getPasswort()));
    ps.addValue(daten.getPrivaterSchluessel());
    ps.addValue(daten.getOeffentlicherSchluessel());
    ps.addValue(daten.getPrivaterSchluesselGeschuetzt());
    ps.addValue(daten.getOeffentlicherSchluesselGeschuetzt());
    ps.addValue(this.isPasswortAenderbar() ? "J" : "N"); // PASSWORT_AENDERBAR
    ps.addValue(this.isPasswortAenderung() ? "J" : "N"); // PASSWORT_AENDERUNG
    ps.addValue(this.isZusammenfuehrbar() ? "J" : "N"); // ZUSAMMENFUEHRBAR
    ps.addValue(this.getZeitpunktRegistrierung());
    int result = ps.insert();
    ResultRow keys = ps.getGeneratedKeys();
    this.setMelderId(keys != null ? keys.getInt(1) : 0);
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
    updateValues(ps);
    updateZeiten(ps);
    return ps.update();
  }

  /**
   * Update.
   *
   * @param ps the ps
   * @return the int
   * @throws JobException the SQL exception
   */
  public int updateExtended(PreparedUpdate ps) throws JobException
  {
    updateValues(ps);
    ps.addValue(this.isPasswortAenderbar() ? "J" : "N"); // PASSWORT_AENDERBAR
    ps.addValue(this.isPasswortAenderung() ? "J" : "N"); // PASSWORT_AENDERUNG
    ps.addValue(this.isZusammenfuehrbar() ? "J" : "N"); // ZUSAMMENFUEHRBAR
    updateZeiten(ps);

    return ps.update();
  }

  /**
   * Update.
   *
   * @param ps the ps
   * @return the int
   * @throws JobException the SQL exception
   */
  public int updateRegisterimport(PreparedUpdate ps) throws JobException
  {
    ps.addValue(this.notNull(this.ansprechpartner.getAnrede())); //
    ps.addValue(this.notNull(this.ansprechpartner.getName())); //
    ps.addValue(this.notNull(this.ansprechpartner.getVorname())); //
    updateZeiten(ps);

    return ps.update();
  }

  private void updateValues(PreparedUpdate ps)
  {
    ps.addValue(this.notNull(this.ansprechpartner.getAnrede())); //
    ps.addValue(this.notNull(this.ansprechpartner.getName())); //
    ps.addValue(this.notNull(this.ansprechpartner.getVorname())); //
    ps.addValue(this.notNull(this.ansprechpartner.getAbteilung())); //
    ps.addValue(this.notNull(this.ansprechpartner.getTelefon())); //
    ps.addValue(this.notNull(this.ansprechpartner.getMobil())); //
    ps.addValue(this.notNull(this.ansprechpartner.getFax())); //
    ps.addValue(this.notNull(this.ansprechpartner.getEmail())); //
    ps.addValue(this.ansprechpartner.getSachbearbeiterId()); // SB_ID
    ps.addValue(this.ansprechpartner.getZeitpunktAenderung());
  }

  private void updateZeiten(PreparedUpdate ps)
  {
    ps.addValue(this.notNull(this.sachbearbeiterId)); // SB_ID
    ps.addValue(this.getZeitpunktAenderung());
    ps.addValue(this.firmenId);
    ps.addValue(this.getMelderId());
    ps.addValue(this.ansprechpartner.getAnsprechpartnerId());
  }
}
