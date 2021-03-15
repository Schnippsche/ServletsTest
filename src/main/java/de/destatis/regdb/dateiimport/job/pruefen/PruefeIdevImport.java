package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob;
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
        this.anzahlSpalten = 18;
        break;
      case IMPORTMITZUSATZFELDER:
        this.anzahlSpalten = 28;
        break;
      case IMPORTMITANSPRECHPARTNER:
        this.anzahlSpalten = 16;
        break;
      default:
        this.pruefUtil.addError(null, "Ungueltiges Format:" + this.jobBean.getImportdatei().importFormat.toString());
    }
  }

  @Override
  protected boolean validateBeforeFileLoad() throws JobException
  {
    return this.pruefUtil.checkAdressbestand(this.jobBean.quellReferenzId);
  }

  @Override
  protected void validate(ArrayList<String[]> rows, int offset)
  {
    int rowNumber = offset;
    for (String[] cols : rows)
    {
      rowNumber++;
      this.pruefUtil.checkFixSpaltenLaenge(cols.length, this.anzahlSpalten, rowNumber);
      if (cols.length > 0)
      {
        String of = cols[0];
        this.pruefUtil.checkOrdnungsfeld(of, rowNumber);
        // Alles okay, nehme Ordnungsfeld auf
        if (this.jobBean.getFormatPruefung().fehlerfrei)
        {
          this.jobBean.getAdressen().getOrdnungsfelder().add(of);
        }
      }
    }
  }

  @Override
  protected AbstractJob jobAfterValidation()
  {
    return new AdressImportJob(this.jobBean);
  }

}
