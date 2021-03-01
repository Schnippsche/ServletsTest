/*
 * @(#)AdresseBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.PreparedInsert;
import de.destatis.regdb.db.PreparedUpdate;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;

/**
 * The Class AdresseBean.
 */
public class AdresseBean extends AbstractBean
{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * The Constant SQL_INSERT_ADRESSEN.
   */
  public static final String SQL_INSERT_ADRESSEN = "INSERT INTO adressen (SACHBEARBEITER_ID, AMT, QUELL_REFERENZ_ID, QUELL_REFERENZ_OF, QUELL_REFERENZ_TYP, ROLLE, ANREDE, NAME, NAME_ERGAENZUNG, KURZTEXT,ABTEILUNG, STRASSE, HAUSNUMMER, POSTLEITZAHL, ORT, POSTFACH, POSTFACH_PLZ, POSTFACH_ORT, LAND, TELEFON, FAX, EMAIL, ZUSATZ1, ZUSATZ2, ZUSATZ3, ZUSATZ4, ZUSATZ5, ZUSATZ6, ZUSATZ7, ZUSATZ8, ZUSATZ9, ZUSATZ10,URS1,URS2,URS3,URS4,URS5,URS6,URS7, MELDER_AENDERBAR,STATUS,ZEITPUNKT_EINTRAG) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'NEU',?)";

  /**
   * The Constant SQL_UPDATE_ADRESSEN.
   */
  public static final String SQL_UPDATE_ADRESSEN = "UPDATE adressen SET SACHBEARBEITER_ID=?, AMT=?, QUELL_REFERENZ_ID=?, QUELL_REFERENZ_OF=?, QUELL_REFERENZ_TYP=?, ROLLE=?, ANREDE=?, NAME=?, NAME_ERGAENZUNG=?, KURZTEXT=?,ABTEILUNG=?, STRASSE=?, HAUSNUMMER=?, POSTLEITZAHL=?, ORT=?, POSTFACH=?, POSTFACH_PLZ=?, POSTFACH_ORT=?, LAND=?, TELEFON=?, FAX=?, EMAIL=?, ZUSATZ1=?, ZUSATZ2=?, ZUSATZ3=?, ZUSATZ4=?, ZUSATZ5=?, ZUSATZ6=?, ZUSATZ7=?, ZUSATZ8=?, ZUSATZ9=?, ZUSATZ10=?,URS1=?,URS2=?,URS3=?,URS4=?,URS5=?,URS6=?,URS7=?,MELDER_AENDERBAR=?,STATUS='AEND',ZEITPUNKT_AENDERUNG=? WHERE ADRESSEN_ID=?";

  /** The adressen id. */
  private Integer adressenId;

  /** The sachbearbeiter id. */
  private Integer sachbearbeiterId;

  /** The amt. */
  private String amt;

  /** The quell referenz id. */
  private Integer quellReferenzId;

  /** The quell referenz of. */
  private String quellReferenzOf;

  private String quellReferenzTyp;

  /** The rolle. */
  private String rolle;

  /** The anrede. */
  private String anrede;

  /** The name. */
  private String name;

  /** The name ergaenzung. */
  private String nameErgaenzung;

  /** The kurztext. */
  private String kurztext;

  /** The abteilung. */
  private String abteilung;

  /** The strasse. */
  private String strasse;

  /** The hausnummer. */
  private String hausnummer;

  /** The postleitzahl. */
  private String postleitzahl;

  /** The ort. */
  private String ort;

  /** The postfach. */
  private String postfach;

  /** The postfach plz. */
  private String postfachPlz;

  /** The postfach ort. */
  private String postfachOrt;

  /** The land. */
  private String land;

  /** The telefon. */
  private String telefon;

  /** The fax. */
  private String fax;

  /** The email. */
  private String email;

  /** The zusatz. */
  private final String[] zusatz;

  /** The urs. */
  private final String[] urs;

  /** The melder aenderbar. */
  private boolean melderAenderbar;

  private String status;

  /** The zeitpunkt eintrag. */
  private String zeitpunktEintrag;

  /** The zeitpunkt aenderung. */
  private String zeitpunktAenderung;

  /** The neu. */
  private boolean neu;

  /** The manuelle adresse. */
  private boolean manuelleAdresse;

  /**
   * Instantiates a new adresse bean.
   */
  public AdresseBean()
  {
    super();
    this.zusatz = new String[10];
    this.urs = new String[7];
    this.setNeu(true);
    this.setManuelleAdresse(false);
    this.setMelderAenderbar(true);
    this.setQuellReferenzTyp("IMPORT");
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
   * Gets the amt.
   *
   * @return the amt
   */
  public String getAmt()
  {
    return this.amt;
  }

  /**
   * Sets the amt.
   *
   * @param amt the new amt
   */
  public void setAmt(String amt)
  {
    this.amt = amt;
  }

  /**
   * Gets the quell referenz id.
   *
   * @return the quell referenz id
   */
  public Integer getQuellReferenzId()
  {
    return this.quellReferenzId;
  }

  /**
   * Sets the quell referenz id.
   *
   * @param quellReferenzId the new quell referenz id
   */
  public void setQuellReferenzId(Integer quellReferenzId)
  {
    this.quellReferenzId = quellReferenzId;
  }

  /**
   * Gets the quell referenz of.
   *
   * @return the quell referenz of
   */
  public String getQuellReferenzOf()
  {
    return this.quellReferenzOf;
  }

  /**
   * Gets quell referenz typ.
   *
   * @return the quell referenz typ
   */
  public String getQuellReferenzTyp()
  {
    return this.quellReferenzTyp;
  }

  /**
   * Sets quell referenz typ.
   *
   * @param quellReferenzTyp the quell referenz typ
   */
  public void setQuellReferenzTyp(String quellReferenzTyp)
  {
    this.quellReferenzTyp = quellReferenzTyp;
  }

  /**
   * Sets the quell referenz of.
   *
   * @param quellReferenzOf the new quell referenz of
   */
  public void setQuellReferenzOf(String quellReferenzOf)
  {
    // 40 Zeichen in Datenbank
    this.quellReferenzOf = StringUtil.leftTrim(quellReferenzOf.trim(), 40);
  }

  /**
   * Gets the rolle.
   *
   * @return the rolle
   */
  public String getRolle()
  {
    return this.rolle;
  }

  /**
   * Sets the rolle.
   *
   * @param rolle the new rolle
   */
  public void setRolle(String rolle)
  {
    this.rolle = rolle;
  }

  /**
   * Gets the anrede.
   *
   * @return the anrede
   */
  public String getAnrede()
  {
    return this.anrede;
  }

  /**
   * Sets the anrede.
   *
   * @param anrede the new anrede
   */
  public void setAnrede(String anrede)
  {
    this.anrede = anrede;
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
   * Gets the abteilung.
   *
   * @return the abteilung
   */
  public String getAbteilung()
  {
    return this.abteilung;
  }

  /**
   * Sets the abteilung.
   *
   * @param abteilung the new abteilung
   */
  public void setAbteilung(String abteilung)
  {
    this.abteilung = abteilung;
  }

  /**
   * Gets the strasse.
   *
   * @return the strasse
   */
  public String getStrasse()
  {
    return this.strasse;
  }

  /**
   * Sets the strasse.
   *
   * @param strasse the new strasse
   */
  public void setStrasse(String strasse)
  {
    this.strasse = strasse;
  }

  /**
   * Gets the hausnummer.
   *
   * @return the hausnummer
   */
  public String getHausnummer()
  {
    return this.hausnummer;
  }

  /**
   * Sets the hausnummer.
   *
   * @param hausnummer the new hausnummer
   */
  public void setHausnummer(String hausnummer)
  {
    this.hausnummer = hausnummer;
  }

  /**
   * Gets the postleitzahl.
   *
   * @return the postleitzahl
   */
  public String getPostleitzahl()
  {
    return this.postleitzahl;
  }

  /**
   * Sets the postleitzahl.
   *
   * @param postleitzahl the new postleitzahl
   */
  public void setPostleitzahl(String postleitzahl)
  {
    this.postleitzahl = postleitzahl;
  }

  /**
   * Gets the ort.
   *
   * @return the ort
   */
  public String getOrt()
  {
    return this.ort;
  }

  /**
   * Sets the ort.
   *
   * @param ort the new ort
   */
  public void setOrt(String ort)
  {
    this.ort = ort;
  }

  /**
   * Gets the postfach.
   *
   * @return the postfach
   */
  public String getPostfach()
  {
    return this.postfach;
  }

  /**
   * Sets the postfach.
   *
   * @param postfach the new postfach
   */
  public void setPostfach(String postfach)
  {
    this.postfach = postfach;
  }

  /**
   * Gets the postfach plz.
   *
   * @return the postfach plz
   */
  public String getPostfachPlz()
  {
    return this.postfachPlz;
  }

  /**
   * Sets the postfach plz.
   *
   * @param postfachPlz the new postfach plz
   */
  public void setPostfachPlz(String postfachPlz)
  {
    this.postfachPlz = postfachPlz;
  }

  /**
   * Gets the postfach ort.
   *
   * @return the postfach ort
   */
  public String getPostfachOrt()
  {
    return this.postfachOrt;
  }

  /**
   * Sets the postfach ort.
   *
   * @param postfachOrt the new postfach ort
   */
  public void setPostfachOrt(String postfachOrt)
  {
    this.postfachOrt = postfachOrt;
  }

  /**
   * Gets the land.
   *
   * @return the land
   */
  public String getLand()
  {
    return this.land;
  }

  /**
   * Sets the land.
   *
   * @param land the new land
   */
  public void setLand(String land)
  {
    this.land = land;
  }

  /**
   * Gets the telefon.
   *
   * @return the telefon
   */
  public String getTelefon()
  {
    return this.telefon;
  }

  /**
   * Sets the telefon.
   *
   * @param telefon the new telefon
   */
  public void setTelefon(String telefon)
  {
    this.telefon = telefon;
  }

  /**
   * Gets the fax.
   *
   * @return the fax
   */
  public String getFax()
  {
    return this.fax;
  }

  /**
   * Sets the fax.
   *
   * @param fax the new fax
   */
  public void setFax(String fax)
  {
    this.fax = fax;
  }

  /**
   * Gets the email.
   *
   * @return the email
   */
  public String getEmail()
  {
    return this.email;
  }

  /**
   * Sets the email.
   *
   * @param email the new email
   */
  public void setEmail(String email)
  {
    this.email = email;
  }

  /**
   * Liefert zusatz.
   *
   * @param index the index
   * @return zusatz zusatz
   */
  public String getZusatz(int index)
  {
    return this.zusatz[index];
  }

  /**
   * Setzt zusatz.
   *
   * @param index  the index
   * @param zusatz the zusatz
   */
  public void setZusatz(int index, String zusatz)
  {
    this.zusatz[index] = zusatz;
  }

  /**
   * Liefert urs.
   *
   * @param index the index
   * @return urs urs
   */
  public String getUrs(int index)
  {
    return this.urs[index];
  }

  /**
   * Setzt urs.
   *
   * @param index the index
   * @param urs   the urs
   */
  public void setUrs(int index, String urs)
  {
    this.urs[index] = urs;
  }

  /**
   * Checks if is melder aenderbar.
   *
   * @return true, if is melder aenderbar
   */
  public boolean isMelderAenderbar()
  {
    return this.melderAenderbar;
  }

  /**
   * Sets the melder aenderbar.
   *
   * @param melderAenderbar the new melder aenderbar
   */
  public void setMelderAenderbar(boolean melderAenderbar)
  {
    this.melderAenderbar = melderAenderbar;
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
   * Checks if is manuelle adresse.
   *
   * @return true, if is manuelle adresse
   */
  public boolean isNotManuelleAdresse()
  {
    return !this.manuelleAdresse;
  }

  /**
   * Sets the manuelle adresse.
   *
   * @param manuelleAdresse the new manuelle adresse
   */
  public void setManuelleAdresse(boolean manuelleAdresse)
  {
    this.manuelleAdresse = manuelleAdresse;
  }

  /**
   * Insert.
   *
   * @param pi the ps
   * @return the int
   * @throws JobException the job exception
   */
  public int insert(PreparedInsert pi) throws JobException
  {
    pi.addValue(this.getSachbearbeiterId());
    pi.addValue(this.notNull(this.getAmt()));
    pi.addValue(this.getQuellReferenzId());
    pi.addValue(this.getQuellReferenzOf());
    pi.addValue(this.notNull("IMPORT")); // QUELL_REF_TYP
    pi.addValue(this.notNull(this.getRolle())); //
    pi.addValue(this.notNull(this.getAnrede())); //
    pi.addValue(this.notNull(this.getName())); //
    pi.addValue(this.notNull(this.getNameErgaenzung())); //
    pi.addValue(this.notNull(this.getKurztext())); //
    pi.addValue(this.notNull(this.getAbteilung())); //
    pi.addValue(this.notNull(this.getStrasse())); //
    pi.addValue(this.notNull(this.getHausnummer())); //
    pi.addValue(this.notNull(this.getPostleitzahl())); //
    pi.addValue(this.notNull(this.getOrt())); //
    pi.addValue(this.notNull(this.getPostfach())); //
    pi.addValue(this.notNull(this.getPostfachPlz())); //
    pi.addValue(this.notNull(this.getPostfachOrt())); //
    pi.addValue(this.notNull(this.getLand())); //
    pi.addValue(this.notNull(this.getTelefon())); //
    pi.addValue(this.notNull(this.getFax())); //
    pi.addValue(this.notNull(this.getEmail())); //
    for (int x = 0; x < 10; x++)
    {
      pi.addValue(this.notNull(this.getZusatz(x))); //
    }
    for (int x = 0; x < 7; x++)
    {
      pi.addValue(this.notNull(this.getUrs(x))); //
    }
    pi.addValue(this.isMelderAenderbar() ? "J" : "N"); // MELDER_AENDERBAR
    pi.addValue(this.getZeitpunktEintrag());
    int result = pi.insert();
    ResultRow keys = pi.getGeneratedKeys();
    this.setAdressenId(keys != null ? keys.getInt(1) : 0);
    return result;
  }

  /**
   * Update.
   *
   * @param pu the ps
   * @return the int
   * @throws JobException the job exception
   */
  public int update(PreparedUpdate pu) throws JobException
  {
    pu.addValue(this.getSachbearbeiterId());
    pu.addValue(this.notNull(this.getAmt()));
    pu.addValue(this.getQuellReferenzId());
    pu.addValue(this.getQuellReferenzOf());
    pu.addValue(this.notNull("IMPORT")); // QUELL_REF_TYP
    pu.addValue(this.notNull(this.getRolle())); //
    pu.addValue(this.notNull(this.getAnrede())); //
    pu.addValue(this.notNull(this.getName())); //
    pu.addValue(this.notNull(this.getNameErgaenzung())); //
    pu.addValue(this.notNull(this.getKurztext())); //
    pu.addValue(this.notNull(this.getAbteilung())); //
    pu.addValue(this.notNull(this.getStrasse())); //
    pu.addValue(this.notNull(this.getHausnummer())); //
    pu.addValue(this.notNull(this.getPostleitzahl())); //
    pu.addValue(this.notNull(this.getOrt())); //
    pu.addValue(this.notNull(this.getPostfach())); //
    pu.addValue(this.notNull(this.getPostfachPlz())); //
    pu.addValue(this.notNull(this.getPostfachOrt())); //
    pu.addValue(this.notNull(this.getLand())); //
    pu.addValue(this.notNull(this.getTelefon())); //
    pu.addValue(this.notNull(this.getFax())); //
    pu.addValue(this.notNull(this.getEmail())); //
    for (int x = 0; x < 10; x++)
    {
      pu.addValue(this.notNull(this.getZusatz(x))); //
    }
    for (int x = 0; x < 7; x++)
    {
      pu.addValue(this.notNull(this.getUrs(x))); //
    }
    pu.addValue(this.isMelderAenderbar() ? "J" : "N"); // MELDER_AENDERBAR
    pu.addValue(this.getZeitpunktAenderung());
    pu.addValue(this.getAdressenId());
    return pu.update();
  }

}
