package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

/**
 * The type Pruefe vorbelegungen import.
 */
public class PruefeVorbelegungenImport extends AbstractPruefeImport
{
  private List<String[]> rows;
  private int anzahlSpalten;

  /**
   * Instantiates a new Pruefe vorbelegungen import.
   *
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  public PruefeVorbelegungenImport(JobBean jobBean, SqlUtil sqlUtil)
  {
    super(jobBean, sqlUtil);
  }

  @Override
  public void pruefeDatei() throws JobException
  {
    SegmentedCsvFileReader reader = new SegmentedCsvFileReader();
    int offset = 0;
    anzahlSpalten = 4;
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

  private void validate(int offset) throws JobException
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
        melderIdRows.put(melderId, rowNumber);
      }
    }
    pruefUtil.checkOrdnungsfelderExistieren(quellOfRows);
    pruefUtil.checkMelderExistieren(melderIdRows);
    //
  }

}
