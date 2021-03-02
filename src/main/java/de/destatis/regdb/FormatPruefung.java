package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Format pruefung.
 */
public class FormatPruefung
{

  /**
   * The Fehlerfrei.
   */
  @XmlAttribute
  public boolean fehlerfrei;
  /**
   * The Anzahl fehler.
   */
  @XmlAttribute
  public int anzahlFehler;
  /**
   * The Maximale anzahl fehler.
   */
  @XmlAttribute
  public int maximaleAnzahlFehler;

  private List<FormatError> error;

  /**
   * Instantiates a new Format pruefung.
   */
  public FormatPruefung()
  {
    this.fehlerfrei = true;
    this.anzahlFehler = 0;
    this.maximaleAnzahlFehler = 1000;
  }

  /**
   * Gets error.
   *
   * @return the error
   */
  public List<FormatError> getSortedErrors()
  {
    if (this.error == null)
      return Collections.emptyList();

    Collections.sort(error);
    return this.error;
  }

  /**
   * Add fehler
   *
   * @param fehler the fehler
   * @return the boolean false if limit reached
   */
  public boolean addFehler(FormatError fehler)
  {
    if (this.error == null)
    {
      this.error = new ArrayList<>(this.maximaleAnzahlFehler);
    }
    this.fehlerfrei = false;
    if (this.error.size() < this.maximaleAnzahlFehler)
    {
      this.error.add(fehler);
      this.anzahlFehler++;
      return true;
    }
    return false;
  }
}
