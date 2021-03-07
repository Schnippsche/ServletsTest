package de.destatis.regdb.dateiimport.job.adressimport;

import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The type Kennung tool.
 *
 * @author Stefan
 */
public class KennungTool
{

  /**
   * The Constant SQL_SELECT_MELDERKENNUNG.
   */
  private static final String SQL_SELECT_MELDERKENNUNG = "SELECT KENNUNG FROM melder WHERE KENNUNG IN({0})";
  /**
   * The Log.
   */
  protected final LoggerIfc log = Logger.getInstance().getLogger(this.getClass());
  private final SecureRandom secureRandom;
  private final SqlUtil sqlUtil;
  private static final int MAX_SIZE = 500;
  private String sbLand;

  /**
   * Instantiates a new Kennung tool.
   *
   * @param sqlUtil the sql util
   */
  public KennungTool(SqlUtil sqlUtil)
  {
    this.secureRandom = new SecureRandom();
    this.sqlUtil = sqlUtil;

  }

  /**
   * Erzeuge eindeutige kennungen.
   *
   * @param anzahl the anzahl
   * @param sbLand the sb Land
   * @return the array list
   * @throws JobException the job exception
   */
  public ArrayList<String> erzeugeEindeutigeKennungen(int anzahl, String sbLand) throws JobException
  {
    long start = System.currentTimeMillis();
    this.sbLand = StringUtil.substring(sbLand + "XX", 0, 2);
    HashSet<String> result = new HashSet<>(anzahl);
    int grenze = Math.min(anzahl, MAX_SIZE);
    do
    {
      HashSet<String> pruefKennungen = new HashSet<>(MAX_SIZE);
      for (int i = 0; i < grenze; i++)
      {
        pruefKennungen.add(this.erzeugeIdevKennung());
      }
      this.entferneVorhandeneKennungen(pruefKennungen);
      result.addAll(pruefKennungen);
    } while (result.size() < anzahl);
    this.log.debug("Anzahl Kennungen:" + result.size());
    String elapsed = (System.currentTimeMillis() - start) / 1000 + " Sekunden";
    this.log.debug(anzahl + " Kennungen erzeugt in " + elapsed);
    return new ArrayList<>(result);

  }

  private void entferneVorhandeneKennungen(HashSet<String> pruefKennungen) throws JobException
  {
    String kennungen = this.sqlUtil.convertStringList(pruefKennungen);
    String sql = MessageFormat.format(SQL_SELECT_MELDERKENNUNG, kennungen);
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      pruefKennungen.remove(row.getString(1));
    }

  }

  /**
   * prueft, ob die Kennung bereits existiert
   *
   * @param pruefKennung die Kennung
   * @return true wenn Kennung existiert
   * @throws JobException the job exception
   */
  public boolean existiertKennung(String pruefKennung) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_MELDERKENNUNG, "'" + pruefKennung + "'");
    ResultRow row = this.sqlUtil.fetchOne(sql);
    return (row != null);
  }

  /**
   * Erzeuge idev kennung.
   *
   * @return the string
   */
  private String erzeugeIdevKennung()
  {
    StringBuilder builder = new StringBuilder();
    // Stelle 1 und 2 sind das AMT
    builder.append(this.sbLand);
    // Stelle 3 bis 10 Zahlen von 0-9
    for (int i = 3; i <= 10; i++)
    {
      builder.append(this.secureRandom.nextInt(10));
    }
    // 10. Stelle pruefziffer
    // builder.append(this.getPruefziffer(builder.toString()));
    return builder.toString();
  }

  /**
   * Liefert pruefziffer.
   *
   * @param idnrString the idnr string
   * @return pruefziffer pruefziffer
   */
  public int getPruefziffer(String idnrString)
  {
    final int n = 11;
    final int m = 10;
    int stringLength = idnrString.length();
    char[] idnr = idnrString.toCharArray();
    int sum;
    int product = m;
    int cipher;
    for (int index = 0; index < stringLength; index++)
    {
      cipher = Character.getNumericValue(idnr[index]);
      sum = (cipher + product) % m;
      if (sum == 0)
      {
        sum = m;
      }
      product = (2 * sum) % n;
    }
    int checkCipher = n - product;
    if (checkCipher == 10)
    {
      checkCipher = 0;
    }
    return checkCipher;
  }
}
