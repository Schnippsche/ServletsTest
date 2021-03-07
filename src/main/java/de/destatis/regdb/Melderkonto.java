package de.destatis.regdb;

/**
 * @author Toengi-S
 */
public class Melderkonto
{
  private Identifikatoren identifikatoren;

  public Melderkonto()
  {
    super();
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
