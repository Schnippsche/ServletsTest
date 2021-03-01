package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.db.SqlUtil;

import java.text.MessageFormat;

public class PruefUtil
{
  private final JobBean jobBean;
  private boolean fehlerLimitNichtErreicht;
  private SqlUtil sqlUtil;

  public PruefUtil(JobBean jobBean, SqlUtil sqlutil)
  {
    this.jobBean = jobBean;
    this.fehlerLimitNichtErreicht = jobBean.getFormatPruefung().anzahlFehler < jobBean.getFormatPruefung().maximaleAnzahlFehler;
  }

  public boolean isFehlerLimitNichtErreicht()
  {
    return this.fehlerLimitNichtErreicht;
  }

  public boolean checkMinLen(String pruefWert, String hinweis, int minimum, int rowNumber)
  {
    if (pruefWert.length() < minimum)
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format("Zeile {0}: {1} ist kleiner als {2}", rowNumber, hinweis, minimum));
      }
      return false;
    }
    return true;
  }

  public boolean checkMaxLen(String pruefWert, String hinweis, int maximum, int rowNumber)
  {
    if (pruefWert.length() > maximum)
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format("Zeile {0}: {1} ist grösser als {2}", rowNumber, hinweis, maximum));
      }
      return false;
    }
    return true;
  }

  public boolean checkNotEmpty(String pruefWert, String hinweis, int rowNumber)
  {
    if (pruefWert == null || pruefWert.isEmpty())
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format("Zeile {0}: {1} ist leer!", rowNumber, hinweis));
      }
      return false;
    }
    return true;
  }

  public void addError(String message)
  {
    if (fehlerLimitNichtErreicht)
    {
      this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
        .addFehler(message);
    }

  }
}
