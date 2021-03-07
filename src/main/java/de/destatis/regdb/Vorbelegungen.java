package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan
 */
public class Vorbelegungen
{

  private Identifikatoren identifikatoren;
  @XmlAttribute
  public int vbWerteIndex;
  @XmlAttribute
  public boolean eintragInfoschreiben;
  @XmlList
  private Set<Integer> firmenIdsFuerMailVersand;

  /**
   *
   */
  public Vorbelegungen()
  {
    this.vbWerteIndex = 0;

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

  /**
   * @return liefert emailVersand
   */
  public Set<Integer> getFirmenIdsFuerMailVersand()
  {
    if (this.firmenIdsFuerMailVersand == null)
    {
      this.firmenIdsFuerMailVersand = new HashSet<>();
    }
    return this.firmenIdsFuerMailVersand;
  }

}
