package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.db.PreparedSelect;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PruefUtil
{
  private static final Pattern numberPattern = Pattern.compile("\\d+");
  private static final String MSG_OF_UNGLEICHE_LAENGE = "Zeile {0}: Ordnungsfeldlänge {1} ungleich der geforderten Länge {2}";
  private static final String MSG_OF_NICHT_NUMERISCH = "Zeile {0}: Ordnungsfeld \"{1}\" ist nicht numerisch";
  private static final String MSG_SPALTENANZAHL = "Zeile {0}: Spaltenzahl {1} ungleich erforderlicher Anzahl {2}";
  private static final String MSG_MIN_SPALTENANZAHL = "Zeile {0}: Spaltenzahl {1} kleiner als erforderliche Anzahl {2}";
  private static final String MSG_MIN_LENGTH = "Zeile {0}: {1} ist kleiner als {2}";
  private static final String MSG_MAX_LENGTH = "Zeile {0}: {1} ist grösser als {2}";
  private static final String MSG_EMPTY = "Zeile {0}: {1} ist leer!";
  private static final String MSG_LEERZEICHEN = "Zeile {0}: {1} enthält unerlaubt Leerzeichen";
  private static final String MSG_OF_NICHT_VORHANDEN = "Zeile {0}: Ordnungsfeld \"{1}\" nicht in Adressbestand gefunden";
  private static final String MSG_MELDER_NICHT_VORHANDEN = "Zeile {0}: Melder-ID \"{1}\" nicht gefunden";
  private static final String MSG_ADRESSBESTAND_INVALID = "Adressbestand mit der ID {0} existiert nicht!";
  private static final String SQL_SELECT_ORDNUNGSFELD = "SELECT QUELL_REFERENZ_OF_LAENGE,QUELL_REFERENZ_OF_TYP, QUELL_REFERENZ_KUERZEL FROM quell_referenz_verwaltung WHERE QUELL_REFERENZ_ID = ?";
  private static final String SQL_SELECT_OF_EXISTS = "SELECT QUELL_REFERENZ_OF FROM adressen WHERE QUELL_REFERENZ_ID = {0} AND QUELL_REFERENZ_OF IN({1}) AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_MELDER_EXISTS = "SELECT MELDER_ID FROM melder WHERE MELDER_ID IN({1}) AND STATUS != \"LOESCH\"";
  private final JobBean jobBean;
  private boolean fehlerLimitNichtErreicht;
  private final SqlUtil sqlUtil;

  public PruefUtil(JobBean jobBean, SqlUtil sqlUtil)
  {
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.fehlerLimitNichtErreicht = jobBean.getFormatPruefung().anzahlFehler < jobBean.getFormatPruefung().maximaleAnzahlFehler;
  }

  public boolean isFehlerLimitNichtErreicht()
  {
    return this.fehlerLimitNichtErreicht;
  }

  public boolean checkMinSpaltenLaenge(int pruefWert, int minimum, int rowNumber)
  {
    if (pruefWert < minimum)
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format(MSG_MIN_SPALTENANZAHL, rowNumber, pruefWert, minimum));
      }
      return false;
    }
    return true;
  }

  public boolean checkFixSpaltenLaenge(int pruefWert, int anzahlErlaubt, int rowNumber)
  {
    // Spaltenlänge +1 erlaubt, da CSV mit abschliessendem Semikolon erlaubt wurde!
    if (pruefWert < anzahlErlaubt || pruefWert > anzahlErlaubt + 1)
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format(MSG_SPALTENANZAHL, rowNumber, pruefWert, anzahlErlaubt));
      }
      return false;
    }
    return true;
  }

  public boolean checkMinStringLaenge(String pruefWert, String hinweis, int minimum, int rowNumber)
  {
    if (pruefWert.length() < minimum)
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format(MSG_MIN_LENGTH, rowNumber, hinweis, minimum));
      }
      return false;
    }
    return true;
  }

  public boolean checkMaxStringLaenge(String pruefWert, String hinweis, int maximum, int rowNumber)
  {
    if (pruefWert.length() > maximum)
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format(MSG_MAX_LENGTH, rowNumber, hinweis, maximum));
      }
      return false;
    }
    return true;
  }

  public boolean checkOrdnungsfeld(String pruefWert, int rowNumber)
  {
    boolean result = true;
    // Laengenpruefung des OF
    if (pruefWert.length() != this.jobBean.getAdressen().ordnungsfeldLaenge)
    {
      addError(MessageFormat.format(MSG_OF_UNGLEICHE_LAENGE, rowNumber, pruefWert.length(), this.jobBean.getAdressen().ordnungsfeldLaenge));
      result = false;
    }
    // Typpruefung OF wenn Typ = NOV
    if (this.jobBean.quellReferenzNumerisch && !numberPattern.matcher(pruefWert)
      .matches())
    {
      addError(MessageFormat.format(MSG_OF_NICHT_NUMERISCH, rowNumber, pruefWert));
      result = false;
    }
    return result;
  }

  public boolean checkNichtLeer(String pruefWert, String hinweis, int rowNumber)
  {
    if (pruefWert == null || pruefWert.isEmpty())
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format(MSG_EMPTY, rowNumber, hinweis));
      }
      return false;
    }
    return true;
  }
  public boolean checkOhneLeerzeichen(String pruefWert, String hinweis, int rowNumber)
  {
    if (pruefWert != null && pruefWert.contains(" "))
    {
      if (fehlerLimitNichtErreicht)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler(MessageFormat.format(MSG_LEERZEICHEN, rowNumber, hinweis));
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

  public boolean checkAdressbestand(Integer quellRefId) throws JobException
  {
    try (PreparedSelect ps = sqlUtil.createPreparedSelect(SQL_SELECT_ORDNUNGSFELD))
    {
      ps.addValue(quellRefId);
      ResultRow rs = ps.fetchOne();
      if (rs != null)
      {
        this.jobBean.getAdressen().ordnungsfeldLaenge = rs.getInt("QUELL_REFERENZ_OF_LAENGE");
        this.jobBean.quellReferenzName = rs.getString("QUELL_REFERENZ_KUERZEL");
        this.jobBean.getAdressen().ordnungsfeldTyp = rs.getString("QUELL_REFERENZ_OF_TYP");
        this.jobBean.quellReferenzNumerisch = "NOV".equals(this.jobBean.getAdressen().ordnungsfeldTyp);
        this.jobBean.quellReferenzId = quellRefId;
        return true;
      } else
      {
        addError(MessageFormat.format(MSG_ADRESSBESTAND_INVALID, quellRefId));
      }
    }
    return false;
  }

  public boolean checkOrdnungsfelderExistieren(Map<String, Integer> ofRows) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_OF_EXISTS, this.jobBean.quellReferenzId, sqlUtil.convertStringList(ofRows.keySet()));
    try (PreparedSelect ps = sqlUtil.createPreparedSelect(sql))
    {
      List<ResultRow> rows = ps.fetchMany();
      for (ResultRow row : rows)
      {
        String of = row.getString(1); // OF
        // Alle Ordnungsfelder, die existieren, aus Liste löschen
        // Übrig bleiben die Ordnungsfelder mit Zeilennummer, die nicht existieren!
        ofRows.remove(of);
      }
    }
    if (ofRows.isEmpty())
      return true;
    for (Map.Entry<String, Integer> entry : ofRows.entrySet())
    {
      addError(MessageFormat.format(MSG_OF_NICHT_VORHANDEN, entry.getValue(), entry.getKey()));
    }
    return false;
  }

  public boolean checkMelderExistieren(Map<String, Integer> melderIdRows) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_MELDER_EXISTS, sqlUtil.convertStringList(melderIdRows.keySet()));
    try (PreparedSelect ps = sqlUtil.createPreparedSelect(sql))
    {
      List<ResultRow> rows = ps.fetchMany();
      for (ResultRow row : rows)
      {
        String melderId = row.getString(1);
        // Alle MelderIDs, die existieren, aus Liste löschen
        // Übrig bleiben die Melder mit Zeilennummer, die nicht existieren!
        melderIdRows.remove(melderId);
      }
    }
    if (melderIdRows.isEmpty())
      return true;
    for (Map.Entry<String, Integer> entry : melderIdRows.entrySet())
    {
      addError(MessageFormat.format(MSG_MELDER_NICHT_VORHANDEN, entry.getValue(), entry.getKey()));
    }
    return false;
  }

}
