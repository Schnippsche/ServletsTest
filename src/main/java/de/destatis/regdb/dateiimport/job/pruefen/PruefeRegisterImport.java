package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedFileReader;
import de.destatis.regdb.dateiimport.reader.SegmentedStringFileReader;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The type Pruefe register import.
 */
public class PruefeRegisterImport extends AbstractPruefeImport<String>
{

  private final PruefUtil pruefUtil;
  private final HashMap<String, Integer> amtStatOnlineKeys;
  private final HashSet<String> amtStatistikBzr;

  private Integer quellReferenzId;

  /**
   * Instantiates a new Pruefe register import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeRegisterImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(new SegmentedStringFileReader(), jobBean, sqlUtil);
    this.pruefUtil = new PruefUtil(jobBean, sqlUtil);
    this.amtStatOnlineKeys = new HashMap<>();
    this.amtStatistikBzr = new HashSet<>();
    this.quellReferenzId = null;
  }

  @Override
  protected void validate(ArrayList<String> rows, int offset) throws JobException
  {
    int rowNumber = offset;
    for (String row : rows)
    {
      rowNumber++;
      pruefUtil.checkMinStringLaenge(row, "Zeilenlänge", 50, rowNumber);
      pruefUtil.checkMaxStringLaenge(row, "Zeilenlänge", 430, rowNumber);
      String statOnlineKey = StringUtil.substring(row, 0, 10)
        .trim();
      pruefUtil.checkNichtLeer(statOnlineKey, "Stat-Online-Key", rowNumber);
      String of = StringUtil.substring(row, 10, 20)
        .trim();
      pruefUtil.checkNichtLeer(of, "Quell-Referenz-Of", rowNumber);
      String tmpAmt = StringUtil.substring(row, 20, 22)
        .trim();
      if (pruefUtil.checkNichtLeer(of, "Amt", rowNumber))
      {
        this.jobBean.amt = tmpAmt;
      }
      String key = tmpAmt + "|" + statOnlineKey;
      Integer statistikId = amtStatOnlineKeys.get(key);
      Integer quellRefId;
      // Neu aufnehmen?
      if (statistikId == null)
      {
        String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_STATONLINEKEY, StringUtil.escapeSqlString(tmpAmt), StringUtil.escapeSqlString(statOnlineKey));
        ResultRow rs = sqlUtil.fetchOne(sql);
        if (rs == null)
        {
          pruefUtil.addError(MessageFormat.format("Kombination aus Amt ({0}) und StatOnlineKey ({1}) existiert nicht!", tmpAmt, statOnlineKey));
        } else
        {
          statistikId = rs.getInt(1);
          quellRefId = rs.getInt(2);
          amtStatOnlineKeys.put(key, statistikId);
          if (quellReferenzId == null)
          {
            quellReferenzId = quellRefId;
            pruefUtil.checkAdressbestand(quellReferenzId);
          } else if (!quellReferenzId.equals(quellRefId))
          {
            pruefUtil.addError("Die Importdatei enthält Daten zu unterschiedlichen Adressbeständen");
          }
        }
      } else
      {
        this.jobBean.statistikId = statistikId;
      }
      // Pruefe Erhebung
      // Aber nur, wenn gültige Statistik vorliegt
      if (statistikId != null)
      {
        String bzr = StringUtil.substring(row, 22, 28)
          .trim();
        key = tmpAmt + "|" + statistikId + "|" + bzr;
        if (!amtStatistikBzr.contains(key))
        {
          String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_BZR, StringUtil.escapeSqlString(tmpAmt), "" + statistikId, StringUtil.escapeSqlString(bzr));
          ResultRow rs = sqlUtil.fetchOne(sql);
          if (rs == null)
          {
            pruefUtil.addError("Keine Erhebung für Amt (" + tmpAmt + "), Statistik-Id  (" + statistikId + ") und Bzr (" + bzr + ") gefunden!");
          }
          amtStatistikBzr.add(key);
        }
      }
      //
      // Alles Okay, nehme Ordnungsfeld auf
      if (this.jobBean.getFormatPruefung().fehlerfrei)
      {
        this.jobBean.getAdressen()
          .getOrdnungsfelder()
          .add(of);
      }
    }
  }

}
