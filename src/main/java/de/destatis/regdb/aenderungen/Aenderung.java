package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.StringUtil;

import java.nio.file.Path;
import java.util.*;

public class Aenderung
{
  private final List<String> spalten;
  private final Map<String, String> values;
  private int aenderungsId;
  private int statistikId;
  private int firmenId;
  private int melderId;
  private int adressenId;
  private int aenderungsArt;
  private int statusExport;
  private int statusDirekteintrag;
  private String typ;
  private int ansprechpartnerId;
  private boolean direktEintragErfolgreich;
  private Path exportDatei;
  private String exportZieldateiName;

  public Aenderung(String[] spalten)
  {
    this.spalten = new ArrayList<>(spalten.length);
    for (String s : spalten)
      this.spalten.add(s.trim());
    this.values = new HashMap<>(spalten.length);
  }

  public void convertResultset(ResultRow rs)
  {
    for (String spalte : this.spalten)
    {
      this.values.put(spalte, rs.getString(spalte));
    }
    // StandardWerte speichern
    this.aenderungsId = rs.getInt("AENDERUNG_ID");
    this.statistikId = rs.getInt("STATISTIK_ID");
    this.firmenId = rs.getInt("FIRMEN_ID");
    this.melderId = rs.getInt("MELDER_ID");
    this.ansprechpartnerId = rs.getInt("ANSPRECHPARTNER_ID");
    this.adressenId = rs.getInt("ADRESSEN_ID");
    this.typ = rs.getString("TYP");
    this.aenderungsArt = rs.getInt("AENDERUNGSARTVALUE");
    this.statusExport = rs.getInt("STATUS_EXPORT_AENDERUNG");
    this.statusDirekteintrag = rs.getInt("STATUS_DIREKTEINTRAG");
  }

  public Map<String, String> getValues()
  {
    return this.values;
  }

  public String getSqlUpdateWithColumns(String[] columns, String ignoredPrefix)
  {
    StringBuilder builder = new StringBuilder();
    for (String key : columns)
    {
      if (this.values.containsKey(key))
      {
        String keyNeu = key.replace(ignoredPrefix, "");
        builder.append(builder.length() > 0 ? ',' : "").append(keyNeu).append("=\"").append(StringUtil.escapeSqlString(this.values.get(key))).append("\"");
      }
    }
    return builder.toString();
  }

  public String[] getValuesAsArray()
  {
    int sz = this.spalten.size();
    String[] result = new String[sz];
    for (int i = 0; i < sz; i++)
    {
      result[i] = this.values.get(this.spalten.get(i));
    }
    return result;
  }

  public List<String> getSpalten()
  {
    return this.spalten;
  }

  public int getAenderungsId()
  {
    return this.aenderungsId;
  }

  public int getStatistikId()
  {
    return this.statistikId;
  }

  public int getFirmenId()
  {
    return this.firmenId;
  }

  public int getMelderId()
  {
    return this.melderId;
  }

  public int getAdressenId()
  {
    return this.adressenId;
  }

  public int getAnsprechpartnerId()
  {
    return this.ansprechpartnerId;
  }

  public int getAenderungsArt()
  {
    return this.aenderungsArt;
  }

  public int getStatusExport()
  {
    return this.statusExport;
  }

  public int getStatusDirekteintrag()
  {
    return this.statusDirekteintrag;
  }

  public String getTyp()
  {
    return this.typ;
  }

  public boolean isDirektEintragErfolgreich()
  {
    return this.direktEintragErfolgreich;
  }

  public void setDirektEintragErfolgreich(boolean direktEintragErfolgreich)
  {
    this.direktEintragErfolgreich = direktEintragErfolgreich;
  }

  public Path getExportDatei()
  {
    return this.exportDatei;
  }

  public void setExportDatei(Path exportDatei)
  {
    this.exportDatei = exportDatei;
  }

  public String getExportZieldateiName()
  {
    return this.exportZieldateiName;
  }

  public void setExportZieldateiName(String exportZieldateiName)
  {
    this.exportZieldateiName = exportZieldateiName;
  }

  @Override
  public String toString()
  {
    return "Aenderung{" + "spalten=" + this.spalten + ", values=" + this.values + ", aenderungsId=" + this.aenderungsId + ", statistikId=" + this.statistikId + ", firmenId=" + this.firmenId + ", melderId=" + this.melderId + ", adressenId=" + this.adressenId + ", aenderungsArt=" + this.aenderungsArt + ", statusExport=" + this.statusExport + ", statusDirekteintrag=" + this.statusDirekteintrag + ", typ='" + this.typ + '\'' + '}';
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Aenderung aenderung = (Aenderung) o;
    return this.aenderungsId == aenderung.aenderungsId;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.aenderungsId);
  }
}