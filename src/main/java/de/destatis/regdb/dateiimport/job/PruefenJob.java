/*
 * @(#)PruefenJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.dateiimport.job.pruefen.*;
import de.werum.sis.idev.res.job.JobException;

import java.text.MessageFormat;

/**
 * The Class PruefenJob.
 */
public class PruefenJob extends AbstractJob
{


  /**
   * The Constant MSG_PRUEFUNG_GESTARTET.
   */
  private static final String MSG_PRUEFUNG_GESTARTET = "Prüfung der Importdatei \"{0}\" gestartet...";

  /**
   * Instantiates a new pruefen job.
   *
   * @param jobBean the job Bean
   */
  public PruefenJob(JobBean jobBean)
  {
    super("Pruefen", jobBean);
  }

  /**
   * Verarbeite job.
   *
   * @throws JobException the job exception
   */
  @Override
  public AbstractJob verarbeiteJob() throws JobException
  {
    this.log.info(MessageFormat.format(MSG_PRUEFUNG_GESTARTET, this.jobBean.getImportdatei().originalDateiname));
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Prüfung der Importdatei gestartet...");
    AbstractPruefeImport<?> pruefKlasse;
    this.jobBean.getImportdatei().anzahlDatensaetze = 0;
    {
      switch (this.jobBean.getImportdatei().importFormat)
      {
        case IMPORTOHNEZUSATZFELDER:
        case IMPORTMITZUSATZFELDER:
        case IMPORTMITANSPRECHPARTNER:
          pruefKlasse = new PruefeIdevImport(jobBean, sqlUtil);
          break;
        case REGISTERIMPORT:
          pruefKlasse = new PruefeRegisterImport(jobBean, sqlUtil);
          break;
        case VORBELEGUNGSIMPORT:
        case VORBELEGUNGDOWNLOADIMPORT:
          pruefKlasse = new PruefeVorbelegungenImport(jobBean, sqlUtil);
          break;
        case XMLIMPORT:
          pruefKlasse = new PruefeXmlImport(jobBean, sqlUtil);
          break;
        case MELDERKONTOIMPORT:
          pruefKlasse = new PruefeMelderkontoImport(jobBean, sqlUtil);
          break;
        default:
          throw new JobException("Ungueltiges Format:" + this.jobBean.getImportdatei().importFormat.toString());
      }
    }
    return pruefKlasse.checkFile();
  }

}
