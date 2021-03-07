package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan
 */
public class Adressen
{

  private Identifikatoren identifikatoren;
  @XmlAttribute
  public boolean nichtAenderbar;
  private Set<String> ordnungsfelder;
  @XmlAttribute
  public int ordnungsfeldLaenge;
  @XmlAttribute
  public String ordnungsfeldTyp;

  /**
   *
   */
  public Adressen()
  {
    this.nichtAenderbar = false;
    this.ordnungsfeldTyp = "ALN";
  }

  /**
   * @return liefert identifikatoren
   */
  @XmlElement
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

  /**
   * @return liefert ordnungsfelder
   */
  @XmlList
  public Set<String> getOrdnungsfelder()
  {
    if (this.ordnungsfelder == null)
    {
      this.ordnungsfelder = new HashSet<>();
    }
    return this.ordnungsfelder;
  }

  /**
   * @param ordnungsfelder setzt ordnungsfelder
   */
  public void setOrdnungsfelder(Set<String> ordnungsfelder)
  {
    this.ordnungsfelder = ordnungsfelder;
  }

}
