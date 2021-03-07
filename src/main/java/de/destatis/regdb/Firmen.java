package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Stefan
 */
public class Firmen
{
  @XmlAttribute
  public boolean aktualisierungErlaubt;
  private Identifikatoren identifikatoren;
  @XmlAttribute
  public boolean neuanlageErlaubt;

  /**
   *
   */
  public Firmen()
  {
    this.aktualisierungErlaubt = true;
    this.neuanlageErlaubt = true;
  }

  /**
   * @return liefert identifikatoren
   */
  public Identifikatoren getIdentifikatoren()
  {
    if (this.identifikatoren == null)
    {
      this.identifikatoren = new Identifikatoren();
    }
    return this.identifikatoren;
  }

  /**
   * @param identifikatoren setzt identifikatoren
   */
  public void setIdentifikatoren(Identifikatoren identifikatoren)
  {
    this.identifikatoren = identifikatoren;
  }

}
