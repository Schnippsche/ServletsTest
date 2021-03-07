package de.destatis.regdb.dateiimport.job.xmlimport;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The type Xml bean.
 *
 * @author Stefan
 */
public class XmlBean implements Serializable, Comparable<XmlBean>
{

  /**
   * The constant AKTION_NAME.
   */
  public static final String AKTION_NAME = "aktion";
  /**
   * The constant ELEMENT_NAME.
   */
  public static final String ELEMENT_NAME = "element";
  private static final long serialVersionUID = 1L;

  private String aktion;
  private Integer statistikId;
  private final HashMap<String, String> values;
  private int rowNumber;
  /*
   * Feldname des eindeutigen Feldes, z.B. quell_referenz_of
   */
  private String sortierMerkmal;

  /**
   * Instantiates a new Xml bean.
   *
   * @param sortierMerkmal the sortier merkmal
   * @param values         the values
   */
  public XmlBean(String sortierMerkmal, HashMap<String, String> values)
  {
    this.sortierMerkmal = sortierMerkmal;
    // Copy
    this.values = new HashMap<>(values.size());
    this.values.putAll(values);
    this.aktion = this.values.get(AKTION_NAME);
  }

  /**
   * Gets aktion.
   *
   * @return the aktion
   */
  public String getAktion()
  {
    return this.aktion;
  }

  /**
   * Sets aktion.
   *
   * @param neuAktion the neu aktion
   */
  public void setAktion(String neuAktion)
  {
    this.aktion = neuAktion;
  }

  /**
   * Is aktion neu boolean.
   *
   * @return the boolean
   */
  public boolean isAktionNeu()
  {
    return "NEU".equalsIgnoreCase(this.getAktion());
  }

  /**
   * Is aktion update boolean.
   *
   * @return the boolean
   */
  public boolean isAktionUpdate()
  {
    return "UPDATE".equalsIgnoreCase(this.getAktion());
  }

  /**
   * Is aktion loesch boolean.
   *
   * @return the boolean
   */
  public boolean isAktionLoesch()
  {
    return "LOESCH".equalsIgnoreCase(this.getAktion());
  }

  /**
   * Is aktion any boolean.
   *
   * @return the boolean
   */
  public boolean isAktionAny()
  {
    return "ANY".equalsIgnoreCase(this.getAktion());
  }

  public boolean isAktionValid()
  {
    if (this.getAktion() == null || this.getAktion().isEmpty())
    {
      return false;
    }
    if (isAktionAny())
    {
      return true;
    }
    if (isAktionLoesch())
    {
      return true;
    }
    if (isAktionNeu())
    {
      return true;
    }
    return isAktionUpdate();
  }

  /**
   * Gets quell referenz of.
   *
   * @return the quell referenz of
   */
  public String getQuellReferenzOf()
  {
    return this.values.get("quell_referenz_of");
  }

  /**
   * Gets amt.
   *
   * @return the amt
   */
  public String getAmt()
  {
    return this.values.get("amt");
  }

  /**
   * Gets stat online key.
   *
   * @return the stat online key
   */
  public String getStatOnlineKey()
  {
    return this.values.get("stat_online_key");
  }

  /**
   * Gets bzr.
   *
   * @return the bzr
   */
  public String getBzr()
  {
    return this.values.get("bzr");
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
   * @param id the id
   */
  public void setStatistikId(Integer id)
  {
    this.statistikId = id;
  }

  /**
   * Gets sortier merkmal.
   *
   * @return the sortier merkmal
   */
  public String getSortierMerkmal()
  {
    return this.sortierMerkmal;
  }

  /**
   * Sets sortier merkmal.
   *
   * @param sortierMerkmal the sortier merkmal
   */
  public void setSortierMerkmal(String sortierMerkmal)
  {
    this.sortierMerkmal = sortierMerkmal;
  }

  /**
   * Gets with default value.
   *
   * @param name         the name
   * @param defaultValue the default value
   * @return the with default value
   */
  public String getWithDefaultValue(String name, String defaultValue)
  {
    return (this.values == null || this.values.get(name) == null ? defaultValue : this.values.get(name));
  }

  /**
   * Gets value.
   *
   * @param name the name
   * @return the value
   */
  public String getValue(String name)
  {
    return this.values.get(name);
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName()
  {
    return this.values.get(ELEMENT_NAME);
  }

  /**
   * Gets map.
   *
   * @return the map
   */
  public HashMap<String, String> getMap()
  {
    return this.values;
  }

  /**
   * Gets row number.
   *
   * @return the row number
   */
  public int getRowNumber()
  {
    return this.rowNumber;
  }

  /**
   * Sets row number.
   *
   * @param rowNumber the row number
   */
  public void setRowNumber(int rowNumber)
  {
    this.rowNumber = rowNumber;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.getValue(this.sortierMerkmal) == null) ? 0 : this.getValue(this.sortierMerkmal).hashCode());
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
    return this.getValue(this.sortierMerkmal).equals(other.getValue(this.sortierMerkmal));
  }

  @Override
  public int compareTo(XmlBean other)
  {
    return this.getValue(this.sortierMerkmal).compareTo(other.getValue(this.sortierMerkmal));
  }

  @Override
  public String toString()
  {
    return "XmlBean [aktion=" + this.aktion + ", statistikId=" + this.statistikId + ", sortiermerkmal=" + this.getValue(this.sortierMerkmal) + " values=" + this.values.toString() + "]";
  }

}
