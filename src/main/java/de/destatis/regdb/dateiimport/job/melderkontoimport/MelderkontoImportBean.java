package de.destatis.regdb.dateiimport.job.melderkontoimport;

import java.util.Arrays;

/**
 * @author Toengi-S
 */
public class MelderkontoImportBean
{
  private Integer mktoId;
  private Integer meldungId;
  private String statistikId;
  private String amt;
  private String quellReferenzId;
  private String quellReferenzOf;
  private String quellReferenzInt;
  private String bzr;
  private Integer adressenId;
  private Integer firmenId;
  private String[] mkf; // 1 - 10
  private String zsDaten;
  private boolean neuerMelderkontoEintrag;

  public MelderkontoImportBean(String[] cols)
  {
    super();
    this.mkf = new String[10];
    this.meldungId = 0;
    this.zsDaten = "";
    for (int i = 0; i < 10; i++)
    {
      this.mkf[i] = "";
    }
    if (!cols[0].isEmpty())
    {
      this.setMeldungId(Integer.parseInt(cols[0]));
    }

    this.neuerMelderkontoEintrag = (this.meldungId <= 0);
    setStatistikId(cols[1]);
    setAmt(cols[2]);
    setQuellReferenzId(cols[3]);
    setQuellReferenzOf(cols[4]);
    setQuellReferenzInt(cols[5]);
    setBzr(cols[6]);
    for (int i = 0; i < 10 && (i + 7 < cols.length); i++)
    {
      this.mkf[i] = cols[i + 7];
    }
    if (cols.length >= 17)
    {
      setZsDaten(cols[17]);
    }
  }

  /**
   * @return liefert mktoId
   */
  public Integer getMktoId()
  {
    return this.mktoId;
  }

  /**
   * @param mktoId setzt mktoId
   */
  public void setMktoId(Integer mktoId)
  {
    this.mktoId = mktoId;
  }

  /**
   * @return liefert meldungId
   */
  public Integer getMeldungId()
  {
    return this.meldungId;
  }

  /**
   * @param meldungId setzt meldungId
   */
  public void setMeldungId(Integer meldungId)
  {
    this.meldungId = meldungId;
  }

  /**
   * @return liefert statistikId
   */
  public String getStatistikId()
  {
    return this.statistikId;
  }

  /**
   * @param statistikId setzt statistikId
   */
  public void setStatistikId(String statistikId)
  {
    this.statistikId = statistikId;
  }

  /**
   * @return liefert amt
   */
  public String getAmt()
  {
    return this.amt;
  }

  /**
   * @param amt setzt amt
   */
  public void setAmt(String amt)
  {
    this.amt = amt;
  }

  /**
   * @return liefert quellReferenzId
   */
  public String getQuellReferenzId()
  {
    return this.quellReferenzId;
  }

  /**
   * @param quellReferenzId setzt quellReferenzId
   */
  public void setQuellReferenzId(String quellReferenzId)
  {
    this.quellReferenzId = quellReferenzId;
  }

  /**
   * @return liefert quellReferenzOf
   */
  public String getQuellReferenzOf()
  {
    return this.quellReferenzOf;
  }

  /**
   * @param quellReferenzOf setzt quellReferenzOf
   */
  public void setQuellReferenzOf(String quellReferenzOf)
  {
    this.quellReferenzOf = quellReferenzOf;
  }

  /**
   * @return liefert quellReferenzInt
   */
  public String getQuellReferenzInt()
  {
    return this.quellReferenzInt;
  }

  /**
   * @param quellReferenzInt setzt quellReferenzInt
   */
  public void setQuellReferenzInt(String quellReferenzInt)
  {
    this.quellReferenzInt = quellReferenzInt;
  }

  /**
   * @return liefert bzr
   */
  public String getBzr()
  {
    return this.bzr;
  }

  /**
   * @param bzr setzt bzr
   */
  public void setBzr(String bzr)
  {
    this.bzr = bzr;
  }

  /**
   * @return liefert mkf
   */
  public String[] getMkf()
  {
    return this.mkf;
  }

  /**
   * @param mkf setzt mkf
   */
  public void setMkf(String[] mkf)
  {
    this.mkf = mkf;
  }

  /**
   * @return liefert zsDaten
   */
  public String getZsDaten()
  {
    return this.zsDaten;
  }

  /**
   * @param zsDaten setzt zsDaten
   */
  public void setZsDaten(String zsDaten)
  {
    this.zsDaten = (zsDaten == null) ? "" : zsDaten;
  }

  /**
   * @return liefert isNeuerMelderkontoEintrag
   */
  public boolean isNeuerMelderkontoEintrag()
  {
    return this.neuerMelderkontoEintrag;
  }

  /**
   * @return liefert firmenId
   */
  public Integer getFirmenId()
  {
    return this.firmenId;
  }

  /**
   * @param firmenId setzt firmenId
   */
  public void setFirmenId(Integer firmenId)
  {
    this.firmenId = firmenId;
  }

  /**
   * @return liefert adressenId
   */
  public Integer getAdressenId()
  {
    return this.adressenId;
  }

  /**
   * @param adressenId setzt adressenId
   */
  public void setAdressenId(Integer adressenId)
  {
    this.adressenId = adressenId;
  }

  /**
   * @param isNeuerMelderkontoEintrag setzt isNeuerMelderkontoEintrag
   */
  public void setNeuerMelderkontoEintrag(boolean isNeuerMelderkontoEintrag)
  {
    this.neuerMelderkontoEintrag = isNeuerMelderkontoEintrag;
  }

  @Override
  public String toString()
  {
    return "MelderkontoImportBean [mktoId=" + this.mktoId + ", meldungId=" + this.meldungId + ", statistikId=" + this.statistikId + ", amt=" + this.amt + ", quellReferenzId=" + this.quellReferenzId + ", quellReferenzOf=" + this.quellReferenzOf + ", quellReferenzInt=" + this.quellReferenzInt + ", bzr=" + this.bzr + ", adressenId=" + this.adressenId + ", firmenId=" + this.firmenId + ", mkf=" + Arrays.toString(this.mkf) + ", zsDaten=" + this.zsDaten + ", neuerMelderkontoEintrag=" + this.neuerMelderkontoEintrag + "]";
  }

}
