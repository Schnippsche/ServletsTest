package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.List;

/**
 * The type Pruefe idev import.
 */
public class PruefeIdevImport extends AbstractPruefeImport
{
  private List<String[]> rows;
  private int anzahlSpalten;

  /**
   * Instantiates a new Pruefe idev import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeIdevImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(jobBean, sqlUtil);
  }

  @Override
  public void pruefeDatei() throws JobException
  {
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
        throw new JobException("Ungueltiges Format:" + this.jobBean.getImportdatei().importFormat.toString());

    }
    SegmentedCsvFileReader reader = new SegmentedCsvFileReader();
    int offset = 0;
    this.jobBean.getImportdatei().anzahlDatensaetze = 0;
    pruefUtil.checkAdressbestand(this.jobBean.quellReferenzId);
    do
    {
      this.log.debug(MessageFormat.format(MSG_PRUEFSTART, offset, offset + jobBean.importBlockGroesse, jobBean.getImportdatei().getPath().getFileName()));
      rows = reader.readSegment(jobBean.getImportdatei().getPath(), jobBean.getImportdatei().getCharset(), offset, jobBean.importBlockGroesse);
      if (!rows.isEmpty())
      {
        this.jobBean.getImportdatei().anzahlDatensaetze += rows.size();
        validate(offset);
        offset += jobBean.importBlockGroesse;
      }
    } while (!rows.isEmpty() && pruefUtil.isFehlerLimitNichtErreicht());
    this.log.debug(MessageFormat.format(MSG_PRUEFENDE, jobBean.getFormatPruefung().anzahlFehler));
  }

  private void validate(int offset)
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
