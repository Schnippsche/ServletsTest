package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.dateiimport.job.xmlimport.XmlBean;
import de.destatis.regdb.dateiimport.job.xmlimport.XmlImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedXmlFileReader;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Pruefe xml import.
 */
public class PruefeXmlImport extends AbstractPruefeImport<XmlBean>
{
  private Integer quellReferenzId;
  private final HashMap<String, Integer> amtStatOnlineKeys;

  /**
   * Instantiates a new Pruefe xml import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeXmlImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(new SegmentedXmlFileReader(), jobBean, sqlUtil);
    this.amtStatOnlineKeys = new HashMap<>();
  }

  @Override
  protected void validate(ArrayList<XmlBean> rows, int offset) throws JobException
  {
     String amt;
    for (XmlBean bean : rows)
    {
      amt = "";
      String statOnlineKey = "";
      if (pruefUtil.checkNichtLeer(bean.getAmt(), "'Amt'", bean.getRowNumber()))
      {
        amt = bean.getAmt();
      }
      if (pruefUtil.checkNichtLeer(bean.getStatOnlineKey(), "'Stat-Online-Key'", bean.getRowNumber()))
      {
        statOnlineKey = bean.getStatOnlineKey();
      }
      if (pruefUtil.checkNichtLeer(bean.getAktion(), "'Aktion'", bean.getRowNumber()))
      {
        String aktion = bean.getAktion();
        if (!bean.isAktionValid())
        {
          pruefUtil.addError(bean.getRowNumber(), "Ungültige Aktion '" + aktion + "'");
        }
      }
      if (!amt.isEmpty() && !statOnlineKey.isEmpty())
        pruefeStatOnlineKey(bean);
    }
  }


  private void pruefeStatOnlineKey(XmlBean bean) throws JobException
  {
    // Pruefe auf passenden STAT-ONLINE-KEY und ermittle dabei die StatistikId und quellReferenzId
    String key = bean.getAmt() + "|" + bean.getStatOnlineKey();
    Integer statistId = this.amtStatOnlineKeys.get(key);
    int quellRefId;
    // Neu aufnehmen?
    if (statistId == null)
    {
      String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_STATONLINEKEY, StringUtil.escapeSqlString(bean.getAmt()), StringUtil.escapeSqlString(bean.getStatOnlineKey()));
      ResultRow row = sqlUtil.fetchOne(sql);
      if (row != null)
      {
        Integer statistikId = row.getInt(1);
        this.amtStatOnlineKeys.put(key, statistikId);
        bean.setStatistikId(statistikId);
        jobBean.statistikId = statistikId;
        quellRefId = row.getInt(2);
        if (this.quellReferenzId == null)
        {
          this.quellReferenzId = quellRefId;
          jobBean.quellReferenzId = quellRefId;
        } else if (!this.quellReferenzId.equals(quellRefId))
        {
          pruefUtil.addError(bean.getRowNumber(), "Die Importdatei enthält Daten zu unterschiedlichen Adressbeständen");
        }
      } else
      {
        pruefUtil.addError(bean.getRowNumber(), "Kombination aus Amt (" + bean.getAmt() + ") und StatOnlineKey (" + bean.getStatOnlineKey() + ") existiert nicht!");
      }
    }
  }

  @Override
  protected AbstractJob jobAfterValidation()
  {
    return new XmlImportJob(this.jobBean);
  }
}
