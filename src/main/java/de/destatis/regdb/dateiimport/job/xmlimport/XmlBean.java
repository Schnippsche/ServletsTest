package de.destatis.regdb.dateiimport.job.xmlimport;

import java.io.Serializable;
import java.util.HashMap;

import org.jdom.Element;

/**
 * @author Stefan
 */
public class XmlBean implements Serializable, Comparable<XmlBean>
{

  public static final String AKTION_NAME = "aktion";
  public static final String ELEMENT_NAME = "element";
  private static final long serialVersionUID = 1L;

  private String aktion;
  private Integer statistikId;
  private HashMap<String, String> values;
  /*
   * Feldname des eindeutigen Feldes, z.B. quell_referenz_of
   */
  private String sortierMerkmal;

  /**
   * 
   */
  public XmlBean(Element element)
  {
    this.aktion = this.values != null ? this.values.get(AKTION_NAME) : null;
  }

  public XmlBean(String sortierMerkmal, HashMap<String, String> values)
  {
    this.sortierMerkmal = sortierMerkmal;
    // Copy
    this.values = new HashMap<>(values.size());
    this.values.putAll(values);
    this.aktion = this.values.get(AKTION_NAME);
  }

  public String getAktion()
  {
    return this.aktion;
  }

  public void setAktion(String neuAktion)
  {
    this.aktion = neuAktion;
  }

  public boolean isAktionNeu()
  {
    return "NEU".equalsIgnoreCase(this.getAktion());
  }

  public boolean isAktionUpdate()
  {
    return "UPDATE".equalsIgnoreCase(this.getAktion());
  }

  public boolean isAktionLoesch()
  {
    return "LOESCH".equalsIgnoreCase(this.getAktion());
  }

  public boolean isAktionAny()
  {
    return "ANY".equalsIgnoreCase(this.getAktion());
  }

  public String getQuellReferenzOf()
  {
    return this.values.get("quell_referenz_of");
  }

  public String getAmt()
  {
    return this.values.get("amt");
  }

  public String getStatOnlineKey()
  {
    return this.values.get("stat_online_key");
  }

  public String getBzr()
  {
    return this.values.get("bzr");
  }

  public Integer getStatistikId()
  {
    return this.statistikId;
  }

  public void setStatistikId(Integer id)
  {
    this.statistikId = id;
  }

  public String getSortierMerkmal()
  {
    return this.sortierMerkmal;
  }

  public void setSortierMerkmal(String sortierMerkmal)
  {
    this.sortierMerkmal = sortierMerkmal;
  }

  public String getWithDefaultValue(String name, String defaultValue)
  {
    return (this.values == null || this.values.get(name) == null ? defaultValue : this.values.get(name));
  }

  public String getValue(String name)
  {
    return this.values.get(name);
  }

  public String getName()
  {
    return this.values.get(ELEMENT_NAME);
  }

  public HashMap<String, String> getMap()
  {
    return this.values;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.getValue(this.sortierMerkmal) == null) ? 0 : this.getValue(this.sortierMerkmal)
        .hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (this.getClass() != obj.getClass())
    {
      return false;
    }
    XmlBean other = (XmlBean) obj;
    if (this.getValue(this.sortierMerkmal) == null)
    {
      return other.getValue(this.sortierMerkmal) == null;
    }
    return this.getValue(this.sortierMerkmal)
        .equals(other.getValue(this.sortierMerkmal));
  }

  @Override
  public int compareTo(XmlBean other)
  {
    return this.getValue(this.sortierMerkmal)
        .compareTo(other.getValue(this.sortierMerkmal));
  }

  @Override
  public String toString()
  {
    return "XmlBean [aktion=" + this.aktion + ", statistikId=" + this.statistikId + ", sortiermerkmal=" + this.getValue(this.sortierMerkmal) + " values=" + this.values.toString() + "]";
  }

}
