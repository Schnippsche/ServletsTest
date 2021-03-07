package de.destatis.regdb.dateiimport.job.xmlimport;

import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.res.job.JobException;

import java.io.Serializable;

/**
 * @author Stefan
 */
public class OrdnungsfeldInfo implements Serializable, Comparable<OrdnungsfeldInfo>
{

  private static final long serialVersionUID = 1L;
  public static final int ADRESSEN_ID = 0;
  public static final int FIRMEN_ID = 1;
  public static final int MELDER_ID = 2;
  public static final int FIRMEN_PARTNER_ID = 3;
  public static final int MELDER_PARTNER_ID = 4;
  public static final int VORBELEGUNG_ID = 5;
  private static final String[] names = {"Adresse", "Firma", "Melder", "Firma", "Melder", "Vorbelegung"};
  private String ordnungsfeld;
  private int[] ids;
  private boolean manuell;

  /**
   *
   */
  @SuppressWarnings("unused")
  private OrdnungsfeldInfo()
  {
    // Private
  }

  public OrdnungsfeldInfo(String newOrdnungsfeld)
  {
    this.ids = new int[6];
    this.setOrdnungsfeld(newOrdnungsfeld);
  }

  public String getOrdnungsfeld()
  {
    return this.ordnungsfeld;
  }

  public void setOrdnungsfeld(String ordnungsfeld)
  {
    this.ordnungsfeld = ordnungsfeld;
  }

  public Integer getAdressenId()
  {
    return this.ids[ADRESSEN_ID];
  }

  public void setAdressenId(int adressenId)
  {
    this.ids[ADRESSEN_ID] = adressenId;
  }

  public int getFirmenId()
  {
    return this.ids[FIRMEN_ID];
  }

  public void setFirmenId(int firmenId)
  {
    this.ids[FIRMEN_ID] = firmenId;
  }

  public int getMelderId()
  {
    return this.ids[MELDER_ID];
  }

  public void setMelderId(int melderId)
  {
    this.ids[MELDER_ID] = melderId;
  }

  public int getFirmenPartnerId()
  {
    return this.ids[FIRMEN_PARTNER_ID];
  }

  public void setFirmenPartnerId(int firmenPartnerId)
  {
    this.ids[FIRMEN_PARTNER_ID] = firmenPartnerId;
  }

  public int getMelderPartnerId()
  {
    return this.ids[MELDER_PARTNER_ID];
  }

  public void setMelderPartnerId(int melderPartnerId)
  {
    this.ids[MELDER_PARTNER_ID] = melderPartnerId;
  }

  public int getVorbelegungsId()
  {
    return this.ids[VORBELEGUNG_ID];
  }

  public void setVorbelegungsId(int vorbelegungId)
  {
    this.ids[VORBELEGUNG_ID] = vorbelegungId;
  }

  public int getId(int id)
  {
    return this.ids[id];
  }

  public boolean isManuell()
  {
    return this.manuell;
  }

  public void setManuell(boolean manuell)
  {
    this.manuell = manuell;
  }

  public void setValuesFromResultSet(ResultRow rs)
  {
    this.setAdressenId(rs.getInt("ADRESSEN_ID"));
    this.setFirmenId(rs.getInt("FIRMEN_ID"));
    this.setMelderId(rs.getInt("MELDER_ID"));
    this.setFirmenPartnerId(rs.getInt("FIRMA_PARTNER_ID"));
    this.setMelderPartnerId(rs.getInt("MELDER_PARTNER_ID"));
    this.setManuell(rs.getBoolean("MANUELLE_ADRESSE"));
  }

  public void ersetzeAktionAny(XmlBean bean, int id) throws JobException
  {
    if (bean.isAktionAny())
    {
      bean.setAktion(this.getId(id) == 0 ? XmlImportJob.AKTION_NEU : XmlImportJob.AKTION_UPDATE);
      return;
    }
    if (bean.isAktionNeu() && this.getId(id) != 0)
    {
      throw new JobException("Aktion 'NEU' auf vorhandener " + names[id] + " mit Ordnungsfeld " + bean.getQuellReferenzOf());
    }
    else if (bean.isAktionUpdate() && this.getId(id) == 0)
    {
      throw new JobException("Aktion 'UPDATE' auf nicht vorhandener " + names[id] + " mit Ordnungsfeld " + bean.getQuellReferenzOf());
    }
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.ordnungsfeld == null) ? 0 : this.ordnungsfeld.hashCode());
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
    OrdnungsfeldInfo other = (OrdnungsfeldInfo) obj;
    if (this.ordnungsfeld == null)
    {
      return other.ordnungsfeld == null;
    }
    else
    {
      return this.ordnungsfeld.equals(other.ordnungsfeld);
    }
  }

  @Override
  public int compareTo(OrdnungsfeldInfo other)
  {
    return this.getOrdnungsfeld().compareTo(other.getOrdnungsfeld());
  }

  @Override
  public String toString()
  {
    return "OrdnungsfeldInfo [ordnungsfeld=" + this.ordnungsfeld + ", adressenId=" + this.getAdressenId() + ", firmenId=" + this.getFirmenId() + ", melderId=" + this.getMelderId() + ", firmenPartnerId=" + this.getFirmenPartnerId() + ", melderPartnerId=" + this.getMelderPartnerId() + ", manuell=" + this.manuell + "]";
  }

}
