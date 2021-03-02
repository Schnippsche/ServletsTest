package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.util.ArrayList;

/**
 * The type Pruefe idev import.
 */
public class PruefeIdevImport extends AbstractPruefeImport<String[]>
{
  private int anzahlSpalten;

  /**
   * Instantiates a new Pruefe idev import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeIdevImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(new SegmentedCsvFileReader(), jobBean, sqlUtil);
    switch (this.jobBean.getImportdatei().importFormat)
    {
      case IMPORTOHNEZUSATZFELDER:
        anzahlSpalten = 18;
        break;
      case IMPORTMITZUSATZFELDER:
        anzahlSpalten = 28;
        break;
      case IMPORTMITANSPRECHPARTNER:
        anzahlSpalten = 16;
        break;
      default:
        pruefUtil.addError(null, "Ungueltiges Format:" + this.jobBean.getImportdatei().importFormat.toString());
    }
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
    for (String[] cols : rows)
    {
      rowNumber++;
      pruefUtil.checkFixSpaltenLaenge(cols.length, anzahlSpalten, rowNumber);
      if (cols.length > 0)
      {
        String of = cols[0];
        pruefUtil.checkOrdnungsfeld(of, rowNumber);
        // Alles okay, nehme Ordnungsfeld auf
        if (this.jobBean.getFormatPruefung().fehlerfrei)
        {
          this.jobBean.getAdressen()
            .getOrdnungsfelder()
            .add(of);
        }
      }
    }
  }

}
