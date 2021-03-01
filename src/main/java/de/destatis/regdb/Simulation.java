package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Simulation
{

  @XmlAttribute
  public boolean importSimulieren;
  @XmlAttribute
  public boolean bestandErmittelt;
  @XmlAttribute
  public int anzahlAdressenImBestand;
  @XmlAttribute
  public int adressenOffset;
  @XmlAttribute
  public int lastAdressenId;

  private Identifikatoren adressIdentifikatoren;
  private Identifikatoren firmenIdentifikatoren;
  private Identifikatoren melderIdentifikatoren;

  public Simulation()
  {
    this.importSimulieren = false;
    this.bestandErmittelt = false;
  }

  /**
   * @return liefert adressIdentifikatoren
   */
  @XmlElement
  public Identifikatoren getAdressIdentifikatoren()
  {
    if (this.adressIdentifikatoren == null)
    {
      this.adressIdentifikatoren = new Identifikatoren();
    }
    return this.adressIdentifikatoren;
  }

  /**
   * @param adressIdentifikatoren setzt adressIdentifikatoren
   */
  public void setAdressIdentifikatoren(Identifikatoren adressIdentifikatoren)
  {
    this.adressIdentifikatoren = adressIdentifikatoren;
  }

  /**
   * @return liefert firmenIdentifikatoren
   */
  @XmlElement
  public Identifikatoren getFirmenIdentifikatoren()
  {
    if (this.firmenIdentifikatoren == null)
    {
      this.firmenIdentifikatoren = new Identifikatoren();
    }
    return this.firmenIdentifikatoren;
  }

  /**
   * @param firmenIdentifikatoren setzt firmenIdentifikatoren
   */
  public void setFirmenIdentifikatoren(Identifikatoren firmenIdentifikatoren)
  {
    this.firmenIdentifikatoren = firmenIdentifikatoren;
  }

  /**
   * @return liefert melderIdentifikatoren
   */
  @XmlElement
  public Identifikatoren getMelderIdentifikatoren()
  {
    if (this.melderIdentifikatoren == null)
    {
      this.melderIdentifikatoren = new Identifikatoren();
    }
    return this.melderIdentifikatoren;
  }

  /**
   * @param melderIdentifikatoren setzt melderIdentifikatoren
   */
  public void setMelderIdentifikatoren(Identifikatoren melderIdentifikatoren)
  {
    this.melderIdentifikatoren = melderIdentifikatoren;
  }

}
