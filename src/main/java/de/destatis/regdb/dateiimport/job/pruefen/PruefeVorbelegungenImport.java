package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.Vorbelegungen;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Pruefe vorbelegungen import.
 */
public class PruefeVorbelegungenImport extends AbstractPruefeImport<String[]>
{
  private final int anzahlSpalten;

  /**
   * Instantiates a new Pruefe vorbelegungen import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeVorbelegungenImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(new SegmentedCsvFileReader(), jobBean, sqlUtil);
    anzahlSpalten = 4;
  }

  @Override
  protected boolean validateBeforeFileLoad() throws JobException
  {
    return pruefUtil.checkAdressbestand(this.jobBean.quellReferenzId);
  }

  @Override
  protected void validate(ArrayList<String[]> rows, int offset) throws JobException
  {
    HashMap<String, Integer> quellOfRows = new HashMap<>(rows.size());
    HashMap<String, Integer> melderIdRows = new HashMap<>(rows.size());

    int rowNumber = offset;
    for (String[] cols : rows)
    {
      rowNumber++;
      pruefUtil.checkMinSpaltenLaenge(cols.length, anzahlSpalten, rowNumber);
      if (cols.length > 3)
      {
        String of = cols[0];
        if (pruefUtil.checkOrdnungsfeld(of, rowNumber))
        {
          quellOfRows.put(of, rowNumber);
        }
        // Bei Feldnamen, die mit _datei enden, auf Leerzeichen Pruefen
        for (int i = 4; i < cols.length; i += 2)
        {
          String feldname = cols[i];
          String feldinhalt = (i + 1 < cols.length) ? cols[i + 1].trim() : "";
          if (feldname.endsWith("_datei"))
          {
            pruefUtil.checkOhneLeerzeichen(feldinhalt, "Dateiname", rowNumber);
          }
        }
        String melderId = cols[2].trim();
        if (!"0".equals(melderId) && melderId.length() > 0)
        {
          melderIdRows.put(melderId, rowNumber);
        }
      }
    }
    pruefUtil.checkOrdnungsfelderExistieren(quellOfRows);
    pruefUtil.checkMelderExistieren(melderIdRows);
    //
  }

  @Override
  protected AbstractJob jobAfterValidation()
  {
    return new VorbelegungsImportJob(jobBean);
  }

}
