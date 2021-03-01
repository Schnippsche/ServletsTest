package de.destatis.regdb;

/**
 * @author Stefan
 */

public class Identifikatoren
{

  private Range neu;
  private Range aenderung;
  private Range loeschung;

  /**
   *
   */

  public Identifikatoren()
  {
  }

  public Range getLoeschung()
  {
    if (this.loeschung == null)
    {
      this.loeschung = new Range();
    }
    return this.loeschung;
  }

  public void setLoeschung(Range geloescht)
  {
    this.loeschung = geloescht;
  }

  public Range getNeu()
  {
    if (this.neu == null)
    {
      this.neu = new Range();
    }
    return this.neu;
  }

  public void setNeu(Range neu)
  {
    this.neu = neu;
  }

  public Range getAenderung()
  {
    if (this.aenderung == null)
    {
      this.aenderung = new Range();
    }
    return this.aenderung;
  }

  public void setAenderung(Range geaendert)
  {
    this.aenderung = geaendert;
  }

}
