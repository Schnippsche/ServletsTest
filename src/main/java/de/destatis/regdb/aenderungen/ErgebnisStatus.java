package de.destatis.regdb.aenderungen;

/**
 * Die Klasse ErgebnisStatus beinhaltet ein Statusfeld und ein Meldungssfeld. Mit
 * dem Statusfeld kann angezeigt werden, ob es sich um einen Fehler, eine Warnung oder
 * um einen korrekten Aufruf handelt. Das Meldungsfeld enthaelt erlaeuternden Text.
 *
 * @author Toengi-S
 */
public class ErgebnisStatus
{

  public static final int STATUS_OK = 0;

  public static final int STATUS_WARNUNG = 1;

  public static final int STATUS_FEHLER = 2;

  private final int status;

  private final String meldung;

  /**
   * erzeugt ein neues Status-Objekt
   *
   * @param status int Art des Status
   */
  public ErgebnisStatus(int status)
  {
    this(status, "");
  }

  /**
   * erzeugt ein neues Status-Objekt
   *
   * @param status  int Art des Status
   * @param meldung String Meldungstext
   */
  public ErgebnisStatus(int status, String meldung)
  {
    this.status = status;
    this.meldung = meldung;
  }

  /**
   * liefert den Statustyp
   *
   * @return int Status
   */
  public int getStatus()
  {
    return this.status;
  }

  /**
   * liefert die Meldung
   *
   * @return String Meldung
   */
  public String getMeldung()
  {
    return this.meldung;
  }

  /**
   * zeigt an, ob der Status OK gesetzt ist
   *
   * @return true oder false
   */
  public boolean isOK()
  {
    return (this.status == STATUS_OK);
  }

  /**
   * zeigt an, ob der Status WARNUNG gesetzt ist
   *
   * @return true oder false
   */
  public boolean isWarnung()
  {
    return (this.status == STATUS_WARNUNG);
  }

  /**
   * zeigt an, ob der Status FEHLER gesetzt ist
   *
   * @return true oder false
   */
  public boolean isFehler()
  {
    return (this.status == STATUS_FEHLER);
  }

  /**
   * liefert den Meldungstext
   */
  @Override
  public String toString()
  {
    return this.meldung;
  }
}
