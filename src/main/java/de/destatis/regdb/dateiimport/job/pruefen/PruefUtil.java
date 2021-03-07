package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.FormatError;
import de.destatis.regdb.JobBean;
import de.destatis.regdb.db.PreparedSelect;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The type Pruef util.
 */
public class PruefUtil
{
  private static final Pattern numberPattern = Pattern.compile("\\d+");
  private static final String MSG_OF_UNGLEICHE_LAENGE = "Ordnungsfeldlänge {0} ungleich der geforderten Länge {1}";
  private static final String MSG_OF_NICHT_NUMERISCH = "Ordnungsfeld \"{0}\" ist nicht numerisch";
  private static final String MSG_SPALTENANZAHL = "Spaltenzahl {0} ungleich erforderlicher Anzahl {1}";
  private static final String MSG_MIN_SPALTENANZAHL = "Spaltenzahl {0} kleiner als erforderliche Anzahl {1}";
  private static final String MSG_MIN_LENGTH = "{0} ist kleiner als {1}";
  private static final String MSG_MAX_LENGTH = "{0} ist grösser als {1}";
  private static final String MSG_EMPTY = "{0} ist leer!";
  private static final String MSG_LEERZEICHEN = "{0} enthält nicht zulässige Leerzeichen";
  private static final String MSG_OF_NICHT_VORHANDEN = "Ordnungsfeld \"{0}\" nicht in Adressbestand gefunden";
  private static final String MSG_MELDUNG_NICHT_VORHANDEN = "Meldungs-Id {0} nicht vorhanden";
  private static final String MSG_MELDER_NICHT_VORHANDEN = "Melder-ID \"{0}\" nicht gefunden";
  private static final String MSG_ADRESSBESTAND_INVALID = "Adressbestand mit der ID {0} existiert nicht!";
  private static final String SQL_SELECT_ORDNUNGSFELD = "SELECT QUELL_REFERENZ_OF_LAENGE,QUELL_REFERENZ_OF_TYP, QUELL_REFERENZ_KUERZEL FROM quell_referenz_verwaltung WHERE QUELL_REFERENZ_ID = ?";
  private static final String SQL_SELECT_OF_EXISTS = "SELECT QUELL_REFERENZ_OF FROM adressen WHERE QUELL_REFERENZ_ID = {0} AND QUELL_REFERENZ_OF IN({1}) AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_MELDER_EXISTS = "SELECT MELDER_ID FROM melder WHERE MELDER_ID IN({0}) AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_MELDUNGSID_EXISTS = "SELECT MELDUNG_ID FROM meldung WHERE MELDUNG_ID IN({0}) AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_QUELLREFID_IMPORT = "SELECT COUNT(*), quell_referenz_verwaltung.QUELL_REFERENZ_KUERZEL FROM import_verwaltung INNER JOIN quell_referenz_verwaltung USING(QUELL_REFERENZ_ID) WHERE import_verwaltung.GESAMT_STATUS = \"AKTIV\" AND import_verwaltung.QUELL_REFERENZ_ID={0} AND import_verwaltung.IMPORT_VERWALTUNG_ID != {1}";

  private final JobBean jobBean;
  private final SqlUtil sqlUtil;
  private boolean fehlerLimitNichtErreicht;

  /**
   * Instantiates a new Pruef util.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefUtil(JobBean jobBean, SqlUtil sqlUtil)
  {
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.fehlerLimitNichtErreicht = jobBean.getFormatPruefung().anzahlFehler < jobBean.getFormatPruefung().maximaleAnzahlFehler;
  }

  /**
   * Is fehler limit nicht erreicht boolean.
   *
   * @return the boolean
   */
  public boolean isFehlerLimitNichtErreicht()
  {
    return this.fehlerLimitNichtErreicht;
  }

  /**
   * Check min spalten laenge boolean.
   *
   * @param pruefWert the pruef wert
   * @param minimum   the minimum
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkMinSpaltenLaenge(int pruefWert, int minimum, int rowNumber)
  {
    if (pruefWert < minimum)
    {
      addError(rowNumber, MessageFormat.format(MSG_MIN_SPALTENANZAHL, pruefWert, minimum));
      return false;
    }
    return true;
  }

  /**
   * Check fix spalten laenge boolean.
   *
   * @param pruefWert     the pruef wert
   * @param anzahlErlaubt the anzahl erlaubt
   * @param rowNumber     the row number
   * @return the boolean
   */
  public boolean checkFixSpaltenLaenge(int pruefWert, int anzahlErlaubt, int rowNumber)
  {
    // Spaltenlänge +1 erlaubt, da CSV mit abschliessendem Semikolon erlaubt wurde!
    if (pruefWert < anzahlErlaubt || pruefWert > anzahlErlaubt + 1)
    {
      addError(rowNumber, MessageFormat.format(MSG_SPALTENANZAHL, pruefWert, anzahlErlaubt));
      return false;
    }
    return true;
  }

  /**
   * Check min string laenge boolean.
   *
   * @param pruefWert the pruef wert
   * @param hinweis   the hinweis
   * @param minimum   the minimum
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkMinStringLaenge(String pruefWert, String hinweis, int minimum, int rowNumber)
  {
    if (pruefWert == null || pruefWert.length() < minimum)
    {
      addError(rowNumber, MessageFormat.format(MSG_MIN_LENGTH, hinweis, minimum));
      return false;
    }
    return true;
  }

  /**
   * Check max string laenge boolean.
   *
   * @param pruefWert the pruef wert
   * @param hinweis   the hinweis
   * @param maximum   the maximum
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkMaxStringLaenge(String pruefWert, String hinweis, int maximum, int rowNumber)
  {
    if (pruefWert != null && pruefWert.length() > maximum)
    {
      addError(rowNumber, MessageFormat.format(MSG_MAX_LENGTH, hinweis, maximum));
      return false;
    }
    return true;
  }

  /**
   * Check ordnungsfeld boolean.
   *
   * @param pruefWert the pruef wert
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkOrdnungsfeld(String pruefWert, int rowNumber)
  {
    boolean result = true;
    // Laengenpruefung des OF
    if (pruefWert == null)
    {
      addError(rowNumber, MessageFormat.format(MSG_OF_UNGLEICHE_LAENGE, 0, this.jobBean.getAdressen().ordnungsfeldLaenge));
      if (this.jobBean.quellReferenzNumerisch)
      {
        addError(rowNumber, MessageFormat.format(MSG_OF_NICHT_NUMERISCH, "null"));
      }
      result = false;
    }
    else
    {
      if (pruefWert.length() != this.jobBean.getAdressen().ordnungsfeldLaenge)
      {
        addError(rowNumber, MessageFormat.format(MSG_OF_UNGLEICHE_LAENGE, pruefWert.length(), this.jobBean.getAdressen().ordnungsfeldLaenge));
        result = false;
      }

      // Typpruefung OF wenn Typ = NOV
      if (this.jobBean.quellReferenzNumerisch && !numberPattern.matcher(pruefWert).matches())
      {
        addError(rowNumber, MessageFormat.format(MSG_OF_NICHT_NUMERISCH, pruefWert));
        result = false;
      }
    }
    return result;
  }

  /**
   * Check nicht leer boolean.
   *
   * @param pruefWert the pruef wert
   * @param hinweis   the hinweis
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkNichtLeer(String pruefWert, String hinweis, int rowNumber)
  {
    if (pruefWert == null || pruefWert.isEmpty())
    {
      addError(rowNumber, MessageFormat.format(MSG_EMPTY, hinweis));
      return false;
    }
    return true;
  }

  /**
   * Check ohne leerzeichen boolean.
   *
   * @param pruefWert the pruef wert
   * @param hinweis   the hinweis
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkOhneLeerzeichen(String pruefWert, String hinweis, int rowNumber)
  {
    if (pruefWert != null && pruefWert.contains(" "))
    {
      addError(rowNumber, MessageFormat.format(MSG_LEERZEICHEN, hinweis));
      return false;
    }
    return true;
  }

  /**
   * Add error.
   *
   * @param rowNumber the row number
   * @param message   the message
   */
  public void addError(Integer rowNumber, String message)
  {
    if (this.fehlerLimitNichtErreicht)
    {
      this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung().addFehler(new FormatError(rowNumber, message));
    }
  }

  /**
   * Check adressbestand boolean.
   *
   * @param quellRefId the quell ref id
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean checkAdressbestand(Integer quellRefId) throws JobException
  {
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_ORDNUNGSFELD))
    {
      ps.addValue(quellRefId);
      ResultRow rs = ps.fetchOne();
      if (rs != null)
      {
        this.jobBean.getAdressen().ordnungsfeldLaenge = rs.getInt("QUELL_REFERENZ_OF_LAENGE");
        this.jobBean.quellReferenzName = rs.getString("QUELL_REFERENZ_KUERZEL");
        this.jobBean.getAdressen().ordnungsfeldTyp = rs.getString("QUELL_REFERENZ_OF_TYP");
        this.jobBean.quellReferenzNumerisch = "NUM".equals(this.jobBean.getAdressen().ordnungsfeldTyp);
        this.jobBean.quellReferenzId = quellRefId;
        return true;
      }
      else
      {
        addError(null, MessageFormat.format(MSG_ADRESSBESTAND_INVALID, quellRefId));
      }
    }
    return false;
  }

  /**
   * Check ordnungsfelder existieren boolean.
   *
   * @param ofRows the of rows
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean checkOrdnungsfelderExistieren(Map<String, Integer> ofRows) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_OF_EXISTS, this.jobBean.quellReferenzId, this.sqlUtil.convertStringList(ofRows.keySet()));
    return removeExistingEntries(ofRows, sql, MSG_OF_NICHT_VORHANDEN);
  }

  /**
   * Check meldungs ids existieren boolean.
   *
   * @param idRows the id rows
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean checkMeldungsIdsExistieren(Map<String, Integer> idRows) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_MELDUNGSID_EXISTS, this.sqlUtil.convertStringList(idRows.keySet()));
    return removeExistingEntries(idRows, sql, MSG_MELDUNG_NICHT_VORHANDEN);
  }

  private boolean removeExistingEntries(Map<String, Integer> checkMap, String sql, String msgNichtVorhanden) throws JobException
  {
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(sql))
    {
      List<ResultRow> rows = ps.fetchMany();
      for (ResultRow row : rows)
      {
        String of = row.getString(1);
        // Alle Felder, die existieren, aus Liste löschen
        // Übrig bleiben die felder mit Zeilennummer, die nicht existieren!
        checkMap.remove(of);
      }
    }
    if (checkMap.isEmpty())
    {
      return true;
    }
    for (Map.Entry<String, Integer> entry : checkMap.entrySet())
    {
      addError(entry.getValue(), MessageFormat.format(msgNichtVorhanden, entry.getKey()));
    }
    return false;
  }

  /**
   * Check melder existieren boolean.
   *
   * @param melderIdRows the melder id rows
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean checkMelderExistieren(Map<String, Integer> melderIdRows) throws JobException
  {
    if (melderIdRows.isEmpty())
    {
      return true;
    }
    String sql = MessageFormat.format(SQL_SELECT_MELDER_EXISTS, this.sqlUtil.convertStringList(melderIdRows.keySet()));
    return removeExistingEntries(melderIdRows, sql, MSG_MELDER_NICHT_VORHANDEN);
  }

  /**
   * Check ist zahl boolean.
   *
   * @param pruefWert the pruef wert
   * @param rowNumber the row number
   * @return the boolean
   */
  public boolean checkIstZahl(String pruefWert, int rowNumber)
  {
    if (pruefWert != null && numberPattern.matcher(pruefWert).matches())
    {
      return true;
    }
    addError(rowNumber, MessageFormat.format("{0} ist keine Zahl", pruefWert));
    return false;
  }

  /**
   * Check running import boolean.
   *
   * @param quellReferenzId the quell referenz id
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean checkRunningImport(Integer quellReferenzId) throws JobException
  {
    if (quellReferenzId != null)
    {
      String sql = MessageFormat.format(SQL_SELECT_QUELLREFID_IMPORT, "" + quellReferenzId, "" + this.jobBean.jobId);
      ResultRow rs = this.sqlUtil.fetchOne(sql);
      if (rs != null && rs.getInt(1) > 0)
      {
        String fehler = MessageFormat.format("Es läuft bereits ein Import auf dem Adressbestand {0}, ID {1}", rs.getString(2), rs.getString(1));
        this.jobBean.getFormatPruefung().addFehler(new FormatError(null, fehler));
        return false;
      }
    }
    return true;
  }
}
