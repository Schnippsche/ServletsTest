package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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

  private List<FormatError> errors;

  /**
   * Instantiates a new Format pruefung.
   */
  public FormatPruefung()
  {
    this.fehlerfrei = true;
    this.anzahlFehler = 0;
    this.maximaleAnzahlFehler = 1000;
  }

  @XmlElement
  public List<FormatError> getErrors()
  {
    return this.errors;
  }

  /**
   * Gets error.
   *
   * @return the error
   */
  public List<FormatError> getSortedErrors()
  {
    if (this.errors == null)
    {
      return Collections.emptyList();
    }

    Collections.sort(this.errors);
    return this.errors;
  }

  /**
   * Add fehler
   *
   * @param fehler the fehler
   * @return the boolean false if limit reached
   */
  public boolean addFehler(FormatError fehler)
  {
    if (this.errors == null)
    {
      this.errors = new ArrayList<>(this.maximaleAnzahlFehler);
    }
    this.fehlerfrei = false;
    if (this.errors.size() < this.maximaleAnzahlFehler)
    {
      this.errors.add(fehler);
      this.anzahlFehler++;
      return true;
    }
    return false;
  }
}
