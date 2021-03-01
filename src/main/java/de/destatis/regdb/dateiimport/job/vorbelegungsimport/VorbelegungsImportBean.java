/*
 * @(#)VorbelegungsImportBean.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.vorbelegungsimport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.destatis.regdb.db.StringUtil;

public class VorbelegungsImportBean implements Serializable, Comparable<VorbelegungsImportBean>
{

  private static final long serialVersionUID = 1L;
  private Integer vorbelegungId;
  private String amt;
  private Integer firmenId;
  private Integer melderId;
  private Integer statistikId;
  private String quellReferenzOf;
  private String quellReferenzInt;
  private String bzr;
  private String formularname;
  private Integer vbWerteIndex;
  private boolean isNeueVorbelegung;
  private final HashMap<String, String> werte;

  /**
   * Instantiates a new vorbelegungs import bean.
   */
  public VorbelegungsImportBean()
  {
    super();
    this.setFirmenId(0);
    this.vbWerteIndex = 0;
    this.setNeueVorbelegung(true);
    this.setMelderId(0);
    this.werte = new HashMap<>();
  }

  /**
   * Instantiates a new vorbelegungs import bean.
   *
   * @param cols the cols
   */
  public VorbelegungsImportBean(String[] cols)
  {
    super();
    this.setNeueVorbelegung(true);
    this.setFirmenId(0);
    this.vbWerteIndex = 0;
    this.werte = new HashMap<>(cols.length / 2);
    this.setQuellReferenzOf(cols[0]);
    this.setQuellReferenzInt(cols[1]);
    this.setMelderId(StringUtil.getInt(cols[2]));
    this.setFormularname(cols[3]);
    for (int i = 4; i < cols.length; i += 2)
    {
      // Prufung auf leeres Feld
      String k = cols[i].trim();
      String v = (i + 1 < cols.length) ? cols[i + 1].trim() : "";
      if (!k.isEmpty())
      {
        this.werte.put(StringUtil.left(k, 191), v);
      }
    }
  }
  
  /**
   * Liefert vorbelegung id.
   *
   * @return vorbelegung id
   */
  public Integer getVorbelegungId()
  {
    return this.vorbelegungId;
  }

  /**
   * Setzt vorbelegung id.
   *
   * @param vorbelegungId vorbelegung id
   */
  public void setVorbelegungId(Integer vorbelegungId)
  {
    this.vorbelegungId = vorbelegungId;
  }

  /**
   * Liefert amt.
   *
   * @return amt
   */
  public String getAmt()
  {
    return this.amt;
  }

  /**
   * Setzt amt.
   *
   * @param amt amt
   */
  public void setAmt(String amt)
  {
    this.amt = amt;
  }

  /**
   * Liefert firmen id.
   *
   * @return firmen id
   */
  public Integer getFirmenId()
  {
    return this.firmenId;
  }

  /**
   * Setzt firmen id.
   *
   * @param firmenId firmen id
   */
  public void setFirmenId(Integer firmenId)
  {
    this.firmenId = firmenId;
  }

  /**
   * Liefert melder id.
   *
   * @return melder id
   */
  public Integer getMelderId()
  {
    return this.melderId;
  }

  /**
   * Setzt melder id.
   *
   * @param melderId melder id
   */
  public void setMelderId(Integer melderId)
  {

    this.melderId = melderId;
  }

  /**
   * Liefert statistik id.
   *
   * @return statistik id
   */
  public Integer getStatistikId()
  {
    return this.statistikId;
  }

  /**
   * Setzt statistik id.
   *
   * @param statistikId statistik id
   */
  public void setStatistikId(Integer statistikId)
  {
    this.statistikId = statistikId;
  }

  /**
   * Liefert quell referenz of.
   *
   * @return quell referenz of
   */
  public String getQuellReferenzOf()
  {
    return this.quellReferenzOf;
  }

  /**
   * Setzt quell referenz of.
   *
   * @param quellReferenzOf quell referenz of
   */
  public void setQuellReferenzOf(String quellReferenzOf)
  {
    this.quellReferenzOf = StringUtil.leftTrim(quellReferenzOf, 40);
  }

  /**
   * Liefert quell referenz int.
   *
   * @return quell referenz int
   */
  public String getQuellReferenzInt()
  {
    return this.quellReferenzInt;
  }

  /**
   * Setzt quell referenz int.
   *
   * @param quellReferenzInt quell referenz int
   */
  public void setQuellReferenzInt(String quellReferenzInt)
  {
    this.quellReferenzInt = StringUtil.leftTrim(quellReferenzInt, 40);
  }

  /**
   * Liefert bzr.
   *
   * @return bzr
   */
  public String getBzr()
  {
    return this.bzr;
  }

  /**
   * Setzt bzr.
   *
   * @param bzr bzr
   */
  public void setBzr(String bzr)
  {
    this.bzr = bzr;
  }

  /**
   * Liefert formularname.
   *
   * @return formularname
   */
  public String getFormularname()
  {
    return this.formularname;
  }

  /**
   * Setzt formularname.
   *
   * @param formularname formularname
   */
  public void setFormularname(String formularname)
  {
    this.formularname = StringUtil.leftTrim(formularname, 255);
  }

  /**
   * Liefert vb werte index.
   *
   * @return vb werte index
   */
  public Integer getVbWerteIndex()
  {
    return this.vbWerteIndex;
  }

  /**
   * Setzt vb werte index.
   *
   * @param vbWerteIndex vb werte index
   */
  public void setVbWerteIndex(Integer vbWerteIndex)
  {
    this.vbWerteIndex = vbWerteIndex;
  }

  /**
   * Checks if is neue vorbelegung.
   *
   * @return true, if is neue vorbelegung
   */
  public boolean isNeueVorbelegung()
  {
    return this.isNeueVorbelegung;
  }

  /**
   * Setzt neue vorbelegung.
   *
   * @param isNeueVorbelegung neue vorbelegung
   */
  public void setNeueVorbelegung(boolean isNeueVorbelegung)
  {
    this.isNeueVorbelegung = isNeueVorbelegung;
  }

  /**
   * Liefert werte.
   *
   * @return werte
   */
  public Map<String, String> getWerte()
  {
    return this.werte;
  }

  /**
   * Liefert identifier.
   *
   * @return identifier
   */
  public String getIdentifier()
  {
    return String.valueOf(this.quellReferenzOf) + "|" + String.valueOf(this.quellReferenzInt) + "|" + String.valueOf(this.melderId);
  }

  /**
   * Compare to.
   *
   * @param other the other
   * @return the int
   */
  @Override
  public int compareTo(VorbelegungsImportBean other)
  {
    return this.getIdentifier()
        .compareTo(other.getIdentifier());
  }

  /**
   * Hash code.
   *
   * @return the int
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.melderId == null) ? 0 : this.melderId.hashCode());
    result = prime * result + ((this.quellReferenzInt == null) ? 0 : this.quellReferenzInt.hashCode());
    result = prime * result + ((this.quellReferenzOf == null) ? 0 : this.quellReferenzOf.hashCode());
    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    VorbelegungsImportBean other = (VorbelegungsImportBean) obj;
    if (!this.firmenId.equals(other.firmenId))
    {
      return false;
    }
    if (this.melderId == null)
    {
      if (other.melderId != null)
      {
        return false;
      }
    }
    else if (!this.melderId.equals(other.melderId))
    {
      return false;
    }
    if (this.quellReferenzInt == null)
    {
      if (other.quellReferenzInt != null)
      {
        return false;
      }
    }
    else if (!this.quellReferenzInt.equals(other.quellReferenzInt))
    {
      return false;
    }
    if (this.quellReferenzOf == null)
    {
      return other.quellReferenzOf == null;
    }
    return this.quellReferenzOf.equals(other.quellReferenzOf);
  }

  /**
   * To string.
   *
   * @return the string
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "VorbelegungsImportBean [vorbelegungId="
        + this.vorbelegungId + ", amt=" + this.amt + ", firmenId=" + this.firmenId + ", melderId=" + this.melderId + ", statistikId=" + this.statistikId + ", quellReferenzOf=" + this.quellReferenzOf
        + ", quellReferenzInt=" + this.quellReferenzInt + ", bzr=" + this.bzr + ", formularname=" + this.formularname + ", vbWerteIndex=" + this.vbWerteIndex + ", isNeueVorbelegung="
        + this.isNeueVorbelegung + ", werteanzahl=" + this.werte.size() + "]";
  }

}
