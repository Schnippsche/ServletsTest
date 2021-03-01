package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.PreparedInsert;
import de.destatis.regdb.db.PreparedUpdate;
import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.res.job.JobException;

/**
 * The type Erhebung bean.
 *
 * @author Stefan
 */
public class ErhebungBean extends AbstractBean
{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * The constant SQL_SELECT_ERHEBUNG.
   */
  public static final String SQL_SELECT_ERHEBUNG = "SELECT STATISTIK_ID,AMT,BZR,ERSTER_MELDUNGSTERMIN,LETZTER_MELDUNGSTERMIN,STATSPEZ_KEY,FORMULAR_ID,VORBELEGUNGSABHAENGIG,SENDEN,ZURUECKSETZEN,PRUEFUNG,LOKALSICHERUNG,SERVERSICHERUNG,ARCHIVIERUNG,WEITERE_MELDUNG,ZEITPUNKT_EINTRAG, ZEITPUNKT_AENDERUNG FROM erhebung WHERE STATISTIK_ID=? AND AMT=? AND BZR=? AND STATUS != \"LOESCH\"";

  /**
   * The constant SQL_INSERT_ERHEBUNGEN.
   */
  public static final String SQL_INSERT_ERHEBUNGEN = "REPLACE INTO erhebung (STATISTIK_ID,AMT,BZR,ERSTER_MELDUNGSTERMIN,LETZTER_MELDUNGSTERMIN,STATSPEZ_KEY,FORMULAR_ID,VORBELEGUNGSABHAENGIG,SENDEN,ZURUECKSETZEN,PRUEFUNG,LOKALSICHERUNG,SERVERSICHERUNG,ARCHIVIERUNG,WEITERE_MELDUNG,STATUS,ZEITPUNKT_EINTRAG) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'AKTIV',?)";

  /**
   * The constant SQL_UPDATE_ERHEBUNGEN.
   */
  public static final String SQL_UPDATE_ERHEBUNGEN = "UPDATE erhebung SET ERSTER_MELDUNGSTERMIN=?,LETZTER_MELDUNGSTERMIN=?,STATSPEZ_KEY=?,FORMULAR_ID=?,VORBELEGUNGSABHAENGIG=?,SENDEN=?,ZURUECKSETZEN=?,PRUEFUNG=?,LOKALSICHERUNG=?,SERVERSICHERUNG=?,ARCHIVIERUNG=?,WEITERE_MELDUNG=?,STATUS='AKTIV',ZEITPUNKT_AENDERUNG=? WHERE STATISTIK_ID=? AND AMT=? AND BZR=?";
  /** The neu. */
  private boolean neu;
  private Integer statistikId;
  private String amt;
  private String bzr;
  private String ersterMeldungstermin;
  private String letzterMeldungstermin;
  private String statspezKey;
  private String formularId;
  private String vorbelegungsabhaengig;
  private String senden;
  private String zuruecksetzen;
  private String pruefung;
  private String lokalsicherung;
  private String serversicherung;
  private String archivierung;
  private String weitereMeldung;
  /** The zeitpunkt eintrag. */
  private String zeitpunktEintrag;

  /** The zeitpunkt aenderung. */
  private String zeitpunktAenderung;

  /**
   * Instantiates a new Erhebung bean.
   */
  public ErhebungBean()
  {
    super();
    this.setNeu(true);
    this.setVorbelegungsabhaengig("N");
    this.setSenden("J");
    this.setZuruecksetzen("J");
    this.setPruefung("J");
    this.setLokalsicherung("J");
    this.setServersicherung("J");
    this.setArchivierung("J");
    this.setWeitereMeldung("J");
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
   * Gets statistik id.
   *
   * @return the statistik id
   */
  public Integer getStatistikId()
  {
    return this.statistikId;
  }

  /**
   * Sets statistik id.
   *
   * @param statistikId the statistik id
   */
  public void setStatistikId(Integer statistikId)
  {
    this.statistikId = statistikId;
  }

  /**
   * Gets amt.
   *
   * @return the amt
   */
  public String getAmt()
  {
    return this.amt;
  }

  /**
   * Sets amt.
   *
   * @param amt the amt
   */
  public void setAmt(String amt)
  {
    this.amt = amt;
  }

  /**
   * Gets bzr.
   *
   * @return the bzr
   */
  public String getBzr()
  {
    return this.bzr;
  }

  /**
   * Sets bzr.
   *
   * @param bzr the bzr
   */
  public void setBzr(String bzr)
  {
    this.bzr = bzr;
  }

  /**
   * Gets erster meldungstermin.
   *
   * @return the erster meldungstermin
   */
  public String getErsterMeldungstermin()
  {
    return this.ersterMeldungstermin;
  }

  /**
   * Sets erster meldungstermin.
   *
   * @param ersterMeldungstermin the erster meldungstermin
   */
  public void setErsterMeldungstermin(String ersterMeldungstermin)
  {
    this.ersterMeldungstermin = ersterMeldungstermin;
  }

  /**
   * Gets letzter meldungstermin.
   *
   * @return the letzter meldungstermin
   */
  public String getLetzterMeldungstermin()
  {
    return this.letzterMeldungstermin;
  }

  /**
   * Sets letzter meldungstermin.
   *
   * @param letzterMeldungstermin the letzter meldungstermin
   */
  public void setLetzterMeldungstermin(String letzterMeldungstermin)
  {
    this.letzterMeldungstermin = letzterMeldungstermin;
  }

  /**
   * Gets statspez key.
   *
   * @return the statspez key
   */
  public String getStatspezKey()
  {
    return this.statspezKey;
  }

  /**
   * Sets statspez key.
   *
   * @param statspezKey the statspez key
   */
  public void setStatspezKey(String statspezKey)
  {
    this.statspezKey = statspezKey;
  }

  /**
   * Gets formular id.
   *
   * @return the formular id
   */
  public String getFormularId()
  {
    return this.formularId;
  }

  /**
   * Sets formular id.
   *
   * @param formularId the formular id
   */
  public void setFormularId(String formularId)
  {
    this.formularId = formularId;
  }

  /**
   * Gets vorbelegungsabhaengig.
   *
   * @return the vorbelegungsabhaengig
   */
  public String getVorbelegungsabhaengig()
  {
    return this.vorbelegungsabhaengig;
  }

  /**
   * Sets vorbelegungsabhaengig.
   *
   * @param vorbelegungsabhaengig the vorbelegungsabhaengig
   */
  public void setVorbelegungsabhaengig(String vorbelegungsabhaengig)
  {
    this.vorbelegungsabhaengig = vorbelegungsabhaengig;
  }

  /**
   * Gets senden.
   *
   * @return the senden
   */
  public String getSenden()
  {
    return this.senden;
  }

  /**
   * Sets senden.
   *
   * @param senden the senden
   */
  public void setSenden(String senden)
  {
    this.senden = senden;
  }

  /**
   * Gets zuruecksetzen.
   *
   * @return the zuruecksetzen
   */
  public String getZuruecksetzen()
  {
    return this.zuruecksetzen;
  }

  /**
   * Sets zuruecksetzen.
   *
   * @param zuruecksetzen the zuruecksetzen
   */
  public void setZuruecksetzen(String zuruecksetzen)
  {
    this.zuruecksetzen = zuruecksetzen;
  }

  /**
   * Gets pruefung.
   *
   * @return the pruefung
   */
  public String getPruefung()
  {
    return this.pruefung;
  }

  /**
   * Sets pruefung.
   *
   * @param pruefung the pruefung
   */
  public void setPruefung(String pruefung)
  {
    this.pruefung = pruefung;
  }

  /**
   * Gets lokalsicherung.
   *
   * @return the lokalsicherung
   */
  public String getLokalsicherung()
  {
    return this.lokalsicherung;
  }

  /**
   * Sets lokalsicherung.
   *
   * @param lokalsicherung the lokalsicherung
   */
  public void setLokalsicherung(String lokalsicherung)
  {
    this.lokalsicherung = lokalsicherung;
  }

  /**
   * Gets serversicherung.
   *
   * @return the serversicherung
   */
  public String getServersicherung()
  {
    return this.serversicherung;
  }

  /**
   * Sets serversicherung.
   *
   * @param serversicherung the serversicherung
   */
  public void setServersicherung(String serversicherung)
  {
    this.serversicherung = serversicherung;
  }

  /**
   * Gets archivierung.
   *
   * @return the archivierung
   */
  public String getArchivierung()
  {
    return this.archivierung;
  }

  /**
   * Sets archivierung.
   *
   * @param archivierung the archivierung
   */
  public void setArchivierung(String archivierung)
  {
    this.archivierung = archivierung;
  }

  /**
   * Gets weitere meldung.
   *
   * @return the weitere meldung
   */
  public String getWeitereMeldung()
  {
    return this.weitereMeldung;
  }

  /**
   * Sets weitere meldung.
   *
   * @param weitereMeldung the weitere meldung
   */
  public void setWeitereMeldung(String weitereMeldung)
  {
    this.weitereMeldung = weitereMeldung;
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
   * Insert int.
   *
   * @param ps the ps
   * @return the int
   * @throws JobException the job exception
   */
  public int insert(PreparedInsert ps) throws JobException
  {
    ps.addValue(this.getStatistikId());
    ps.addValue(this.getAmt());
    ps.addValue(this.getBzr());
    ps.addValue(this.getErsterMeldungstermin());
    ps.addValue(this.getLetzterMeldungstermin());
    ps.addValue(this.getStatspezKey());
    ps.addValue(this.getFormularId());
    ps.addValue(this.getVorbelegungsabhaengig());
    ps.addValue(this.getSenden());
    ps.addValue(this.getZuruecksetzen());
    ps.addValue(this.getPruefung());
    ps.addValue(this.getLokalsicherung());
    ps.addValue(this.getServersicherung());
    ps.addValue(this.getArchivierung());
    ps.addValue(this.getWeitereMeldung());
    ps.addValue(this.getZeitpunktEintrag());
    return ps.insert();
  }

  /**
   * Update int.
   *
   * @param ps the ps
   * @return the int
   * @throws JobException the job exception
   */
  public int update(PreparedUpdate ps) throws JobException
  {
    ps.addValue(this.getErsterMeldungstermin());
    ps.addValue(this.getLetzterMeldungstermin());
    ps.addValue(this.getStatspezKey());
    ps.addValue(this.getFormularId());
    ps.addValue(this.getVorbelegungsabhaengig());
    ps.addValue(this.getSenden());
    ps.addValue(this.getZuruecksetzen());
    ps.addValue(this.getPruefung());
    ps.addValue(this.getLokalsicherung());
    ps.addValue(this.getServersicherung());
    ps.addValue(this.getArchivierung());
    ps.addValue(this.getWeitereMeldung());
    ps.addValue(this.getZeitpunktAenderung());
    // Keys
    ps.addValue(this.getStatistikId());
    ps.addValue(this.getAmt());
    ps.addValue(this.getBzr());
    return ps.update();
  }

  /**
   * Load.
   *
   * @param rs the rs
   */
  public void load(ResultRow rs)
  {
    // ZEITPUNKT_EINTRAG, ZEITPUNKT_AENDERUNG
    this.setStatistikId(rs.getInt("STATISTIK_ID"));
    this.setAmt(rs.getString("AMT"));
    this.setBzr(rs.getString("BZR"));
    this.setErsterMeldungstermin(rs.getString("ERSTER_MELDUNGSTERMIN"));
    this.setLetzterMeldungstermin(rs.getString("LETZTER_MELDUNGSTERMIN"));
    this.setStatspezKey(rs.getString("STATSPEZ_KEY"));
    this.setFormularId(rs.getString("FORMULAR_ID"));
    this.setVorbelegungsabhaengig(rs.getString("VORBELEGUNGSABHAENGIG"));
    this.setSenden(rs.getString("SENDEN"));
    this.setZuruecksetzen(rs.getString("ZURUECKSETZEN"));
    this.setPruefung(rs.getString("PRUEFUNG"));
    this.setLokalsicherung(rs.getString("LOKALSICHERUNG"));
    this.setServersicherung(rs.getString("SERVERSICHERUNG"));
    this.setArchivierung(rs.getString("ARCHIVIERUNG"));
    this.setWeitereMeldung(rs.getString("WEITERE_MELDUNG"));
    this.setZeitpunktEintrag(rs.getString("ZEITPUNKT_EINTRAG"));
    this.setZeitpunktAenderung(rs.getString("ZEITPUNKT_AENDERUNG"));
  }
}
