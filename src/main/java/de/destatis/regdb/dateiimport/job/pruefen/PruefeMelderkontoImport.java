package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.melderkontoimport.MelderkontoImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.PreparedSelect;
import de.destatis.regdb.db.SqlUtil;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Pruefe idev import.
 */
public class PruefeMelderkontoImport extends AbstractPruefeImport<String[]>
{
  private static final String SQL_SELECT_MELDERKONTO = "SELECT MKTO_ID from melderkonto WHERE MELDUNG_ID = ? AND AMT=? and STATISTIK_ID = ?";
  /**
   * The Quell ref id.
   */
  int quellRefId = 0;

  /**
   * Instantiates a new Pruefe idev import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeMelderkontoImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(new SegmentedCsvFileReader(), jobBean, sqlUtil);
  }

  @Override
  protected boolean validateBeforeFileLoad() throws JobException
  {
    return pruefUtil.checkAdressbestand(this.jobBean.quellReferenzId);
  }

  @Override
  protected void validate(ArrayList<String[]> rows, int offset) throws JobException
  {
    int rowNumber = offset;
    HashMap<String, Integer> meldungIdRowNr = new HashMap<>(rows.size());
    try (PreparedSelect ps = sqlUtil.createPreparedSelect(SQL_SELECT_MELDERKONTO))
    {
      for (String[] cols : rows)
      {
        rowNumber++;
        pruefUtil.checkFixSpaltenLaenge(cols.length, 18, rowNumber);
        if (cols.length > 3 && pruefUtil.checkIstZahl(cols[3], rowNumber))
        {
          int quellId = StringUtil.getInt(cols[3]);
          if (quellRefId == 0)
          {
            quellRefId = quellId;
            pruefUtil.checkAdressbestand(quellRefId);
          } else if (quellId != quellRefId)
          {
            pruefUtil.addError(null, "Die Importdatei enthält Daten zu unterschiedlichen Adressbeständen");
          }
          int meldungsId = 0;
          if (cols[0].trim().isEmpty() && pruefUtil.checkIstZahl(cols[0], rowNumber))
          {
            meldungsId = StringUtil.getInt(cols[0]);
          }
          if (meldungsId > 0)
          {
            ps.addValue(meldungsId); // MeldungsId
            ps.addValue(cols[2]); // AMT
            ps.addValue(cols[1]); // StatistikId
            if (ps.fetchOne() == null)
            {
              pruefUtil.addError(rowNumber, MessageFormat.format("Kombination aus MeldungsID {0}, Amt {1} und StatistikId {2} existiert nicht!", meldungsId, cols[2], cols[1]));
            }
          }
        }
      }
    }
    pruefUtil.checkMeldungsIdsExistieren(meldungIdRowNr);
  }

  @Override
  protected AbstractJob jobAfterValidation()
  {
    return new MelderkontoImportJob(this.jobBean);
  }

}
