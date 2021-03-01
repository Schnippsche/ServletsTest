package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedStringFileReader;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PruefeRegisterImport
{
  protected final LoggerIfc log;
  private final JobBean jobBean;
  private final PruefUtil pruefUtil;
  private final HashMap<String, Integer> amtStatOnlineKeys;
  private final HashSet<String> amtStatistikBzr;
  private final SqlUtil sqlUtil;
  private List<String> rows;
  private Integer quellReferenzId;

  public PruefeRegisterImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    log = Logger.getInstance()
      .getLogger(this.getClass());
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.pruefUtil = new PruefUtil(jobBean, sqlUtil);
    this.amtStatOnlineKeys = new HashMap<>();
    this.amtStatistikBzr = new HashSet<>();
    this.quellReferenzId = null;
  }

  public void pruefeDatei() throws JobException
  {
    SegmentedStringFileReader reader = new SegmentedStringFileReader();
    int offset = 0;
    this.jobBean.getImportdatei().anzahlDatensaetze = 0;
    do
    {
      this.log.debug(MessageFormat.format("Pruefe Segment {0} bis {1} von Datei {2}...", offset, offset + jobBean.importBlockGroesse, jobBean.getImportdatei().getPath().getFileName()));
      rows = reader.readSegment(jobBean.getImportdatei().getPath(), jobBean.getImportdatei().getCharset(), offset, jobBean.importBlockGroesse);
      if (!rows.isEmpty())
      {
        this.jobBean.getImportdatei().anzahlDatensaetze += rows.size();
        validate(offset);
        offset += jobBean.importBlockGroesse;
      }
    } while (!rows.isEmpty() && pruefUtil.isFehlerLimitNichtErreicht());
    this.log.debug(MessageFormat.format("Prüfung mit {0} Fehlernn abgeschlossen", jobBean.getFormatPruefung().anzahlFehler));
  }

  private void validate(int offset) throws JobException
  {
    int rowNumber = offset;
    for (String row : rows)
    {
      rowNumber++;
      pruefUtil.checkMinLen(row, "Länge", 50, rowNumber);
      pruefUtil.checkMaxLen(row, "Länge", 430, rowNumber);
      String statOnlineKey = StringUtil.substring(row, 0, 10)
        .trim();
      pruefUtil.checkNotEmpty(statOnlineKey, "Stat-Online-Key", rowNumber);
      String of = StringUtil.substring(row, 10, 20)
        .trim();
      pruefUtil.checkNotEmpty(of, "Quell-Referenz-Of", rowNumber);
      String tmpAmt = StringUtil.substring(row, 20, 22)
        .trim();
      if (pruefUtil.checkNotEmpty(of, "Amt", rowNumber))
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
          if (quellRefId == null)
          {
            quellReferenzId = quellRefId;
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
