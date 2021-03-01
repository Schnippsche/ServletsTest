package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Stefan
 */
public class Melder
{

  @XmlAttribute
  public boolean aktualisierungErlaubt;
  @XmlAttribute
  public boolean erzeugeLeereAnsprechpartner;
  @XmlAttribute
  public boolean erzeugeVorbelegungenStattAnsprechpartner;
  private Identifikatoren identifikatoren;
  @XmlAttribute
  public boolean neuanlageErlaubt;
  @XmlAttribute
  public boolean passwortUnveraenderbar;
  @XmlAttribute
  public boolean gesperrt;
  @XmlAttribute
  public boolean neuePasswoerterGenerieren;
  @XmlAttribute
  public boolean zusammenfuehrbar;

  /**
   *
   */
  public Melder()
  {
    this.aktualisierungErlaubt = true;
    this.neuanlageErlaubt = true;
    this.passwortUnveraenderbar = true;
    this.zusammenfuehrbar = true;
    this.erzeugeLeereAnsprechpartner = false;
    this.erzeugeVorbelegungenStattAnsprechpartner = false;
    this.gesperrt = false;
    this.neuePasswoerterGenerieren = false;
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
