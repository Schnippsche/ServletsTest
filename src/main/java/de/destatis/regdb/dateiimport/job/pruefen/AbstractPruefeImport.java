package de.destatis.regdb.dateiimport.job.pruefen;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.AuswirkungenJob;
import de.destatis.regdb.dateiimport.reader.SegmentedFileReader;
import de.destatis.regdb.db.LoeschUtil;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * The type Abstract pruefe import.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractPruefeImport<T>
{
  /**
   * The constant MSG_PRUEFSTART.
   */
  protected static final String MSG_PRUEFSTART = "Pruefe Segment {0} bis {1} von Datei {2}..";
  /**
   * The constant MSG_PRUEFENDE.
   */
  protected static final String MSG_PRUEFENDE = "Prüfung mit {0} Fehlern abgeschlossen";

  /**
   * The Log.
   */
  protected final LoggerIfc log;
  /**
   * The Job bean.
   */
  protected final JobBean jobBean;
  /**
   * The Sql util.
   */
  protected final SqlUtil sqlUtil;
  /**
   * The Pruef util.
   */
  protected final PruefUtil pruefUtil;

  /**
   * The Reader.
   */
  protected final SegmentedFileReader<T> reader;

  /**
   * Instantiates a new Abstract pruefe import.
   *
   * @param reader  the reader
   * @param jobBean the job bean
   * @param sqlUtil the sql util
   */
  protected AbstractPruefeImport(SegmentedFileReader<T> reader, JobBean jobBean, SqlUtil sqlUtil)
  {
    this.log = Logger.getInstance().getLogger(this.getClass());
    this.reader = reader;
    this.jobBean = jobBean;
    this.sqlUtil = sqlUtil;
    this.pruefUtil = new PruefUtil(jobBean, sqlUtil);
  }

  /**
   * check File.
   *
   * @throws JobException the job exception
   */
  public AbstractJob checkFile() throws JobException
  {

    int offset = 0;
    this.jobBean.getImportdatei().anzahlDatensaetze = 0;
    ArrayList<T> rows;
    if (validateBeforeFileLoad())
    {
      do
      {
        this.log.info(MessageFormat.format(MSG_PRUEFSTART, offset, offset + this.jobBean.importBlockGroesse - 1, this.jobBean.getImportdatei().getPath().getFileName()));
        rows = this.reader.readSegment(this.jobBean.getImportdatei().getPath(), this.jobBean.getImportdatei().getCharset(), offset, this.jobBean.importBlockGroesse);
        if (!rows.isEmpty())
        {
          this.jobBean.getImportdatei().anzahlDatensaetze += rows.size();
          validate(rows, offset);
          offset += this.jobBean.importBlockGroesse;
        }
      } while (!rows.isEmpty() && this.pruefUtil.isFehlerLimitNichtErreicht() && rows.size() == this.jobBean.importBlockGroesse);
    }
    this.pruefUtil.checkRunningImport(this.jobBean.quellReferenzId);
    this.log.info(MessageFormat.format(MSG_PRUEFENDE, this.jobBean.getFormatPruefung().anzahlFehler));
    if (!this.jobBean.getFormatPruefung().fehlerfrei)
    {
      String fehlerText;
      if (this.jobBean.getFormatPruefung().anzahlFehler == 1)
      {
        fehlerText = this.jobBean.getFormatPruefung().getSortedErrors().get(0).toString();
      }
      else
      {
        fehlerText = "Es sind " + this.jobBean.getFormatPruefung().anzahlFehler + " vorhanden; Details siehe Protokoll";
      }
      this.jobBean.setStatusAndInfo(JobStatus.FEHLER, fehlerText);
      return null;
    }

    if (this.jobBean.getSimulation().importSimulieren)
    {
      LoeschUtil util = new LoeschUtil(this.sqlUtil);
      util.loescheStandardWerte(this.jobBean.amt, this.jobBean.statistikId, this.jobBean.sachbearbeiterId);
      util.speichereStandardwerte(this.jobBean.getImportdatei().originalDateiname, this.jobBean.amt, this.jobBean.statistikId, this.jobBean.jobId, this.jobBean.sachbearbeiterId, this.jobBean.zeitpunktEintrag);
      return new AuswirkungenJob(this.jobBean);
    }
    if (this.jobBean.loescheDaten)
    {
      return new AuswirkungenJob(this.jobBean);
    }
    return jobAfterValidation();
  }

  /**
   * Validate.
   *
   * @param rows   the rows
   * @param offset the offset
   * @throws JobException the job exception
   */
  protected abstract void validate(ArrayList<T> rows, int offset) throws JobException;

  protected abstract AbstractJob jobAfterValidation();

  /**
   * Validate before file load boolean.
   *
   * @return the boolean
   * @throws JobException the job exception
   */
  protected boolean validateBeforeFileLoad() throws JobException
  {
    return true;
  }

}
