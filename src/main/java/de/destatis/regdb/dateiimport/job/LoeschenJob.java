/*
 * @(#)LoeschenJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.db.AufraeumUtil;
import de.destatis.regdb.db.LoeschUtil;
import de.werum.sis.idev.res.job.JobException;

/**
 * The Class LoeschenJob.
 */
public class LoeschenJob extends AbstractJob
{

  /** The loesch adressen. */
  private HashSet<Integer> loeschAdressen;
  private HashSet<Integer> loeschFirmen;
  private HashSet<Integer> loeschMelder;

  private LoeschUtil loeschUtil;

  /**
   * Instantiates a new loeschen job.
   *
   * @param jobBean the job Bean
   */
  public LoeschenJob(JobBean jobBean)
  {
    super("LoeschenJob", jobBean);
    // Anzahl der Loeschungen ist einstellbar

  }

  /**
   * Verarbeite job.
   *
   * @throws JobException the job exception
   */
  @Override
  public AbstractJob verarbeiteJob() throws JobException
  {
    // TODO; Testen!
    this.loeschUtil = new LoeschUtil(sqlUtil);
    int maximumSegment = Math.max(this.jobBean.getSimulation()
        .getAdressIdentifikatoren()
        .getLoeschung()
        .getAnzahl(), this.jobBean.getSimulation()
            .getFirmenIdentifikatoren()
            .getLoeschung()
            .getAnzahl());
    maximumSegment = Math.max(maximumSegment, this.jobBean.getSimulation()
        .getMelderIdentifikatoren()
        .getLoeschung()
        .getAnzahl());
    this.log.debug("maximumSegment:" + maximumSegment);
    if (this.jobBean.loescheDaten && maximumSegment > 0)
    {
      this.jobBean.setStatusAndInfo(JobStatus.AKTIV, MessageFormat.format("Lösche {0} bis {1} von {2} Einträgen...", this.jobBean.loeschOffset, this.jobBean.loeschOffset+ this.jobBean.loeschBlockGroesse, maximumSegment));

      // Adressen
      this.loeschAdressen = this.holeTeilBereich(this.jobBean.loeschOffset, this.jobBean.getSimulation()
          .getAdressIdentifikatoren()
          .getLoeschung()
          .getValues());
      this.jobBean.getAdressen()
          .getIdentifikatoren()
          .getLoeschung()
          .getValues()
          .addAll(this.loeschAdressen);
      // Firmen
      this.loeschFirmen = this.holeTeilBereich(this.jobBean.loeschOffset, this.jobBean.getSimulation()
          .getFirmenIdentifikatoren()
          .getLoeschung()
          .getValues());
      this.jobBean.getFirmen()
          .getIdentifikatoren()
          .getLoeschung()
          .getValues()
          .addAll(this.loeschFirmen);
      // Melder
      this.loeschMelder = this.holeTeilBereich(this.jobBean.loeschOffset, this.jobBean.getSimulation()
          .getMelderIdentifikatoren()
          .getLoeschung()
          .getValues());
      this.jobBean.getMelder()
          .getIdentifikatoren()
          .getLoeschung()
          .getValues()
          .addAll(this.loeschMelder);
      this.doLoeschung();
    }
    this.jobBean.loeschOffset += this.jobBean.loeschBlockGroesse;

    // Mehr zum loeschen ?
    if (maximumSegment > this.jobBean.loeschOffset)
    {
      this.log.info(MessageFormat.format("Es muessen noch {0} Einträge gelöscht werden, starte weiteren LoeschJob...", maximumSegment - this.jobBean.loeschOffset));
      return new LoeschenJob(this.jobBean);
    }
    this.log.info("Alle Loeschungen verarbeitet!");
    AufraeumUtil aufraeumUtil = new AufraeumUtil();
    aufraeumUtil.entferneDateien(this.jobBean.jobId);
    String info;
    switch (this.jobBean.getImportdatei().importFormat)
    {
      case VORBELEGUNGSIMPORT:
        info = "Vorbelegungs-Import";
        break;
      case XMLIMPORT:
        info = "Xml-Import";
        break;
      default:
        info = "Adress-Import";
    }

    this.jobBean.setStatusAndInfo(JobStatus.BEENDET, info + " wurde durchgeführt");
    return null;
  }

  /**
   * Lade adressen.
   */
  private HashSet<Integer> holeTeilBereich(int startIndex, Set<Integer> set)
  {
    HashSet<Integer> result = new HashSet<>(this.jobBean.loeschBlockGroesse);
    if (startIndex < set.size())
    {
      Iterator<Integer> it = set.iterator();
      int counter = 0;
      int index = 0;
      while (it.hasNext() && counter < this.jobBean.loeschBlockGroesse)
      {
        Integer i = it.next();
        if (index >= startIndex)
        {
          result.add(i);
          counter++;
        }
        index++;
      }
    }
    return result;
  }

  /**
   * Do loeschung.
   *
   * @throws JobException the job exception
   */
  private void doLoeschung() throws JobException
  {
    this.loeschUtil.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    this.loeschUtil.setZeitpunkt(this.jobBean.zeitpunktEintrag);
    this.loeschUtil.setLoeschAdressen(this.loeschAdressen);
    this.loeschUtil.setLoeschFirmen(this.loeschFirmen);
    this.loeschUtil.setLoeschMelder(this.loeschMelder);
    this.loeschUtil.setImportVerzeichnis(this.jobBean.getImportdatei().importVerzeichnis);
    this.loeschUtil.doLoeschung();
  }

}
