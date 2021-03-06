package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.db.AufraeumUtil;
import de.destatis.regdb.db.LoeschUtil;
import de.destatis.regdb.db.ResultRow;
import de.werum.sis.idev.res.job.JobException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuswirkungenJob extends AbstractJob
{
  public static final String LOESCH_PROTOKOLL_ADRESSEN_KANDIDATEN = "zuloeschendeAdressen.csv";
  public static final String LOESCH_PROTOKOLL_FIRMEN_KANDIDATEN = "zuloeschendeFirmen.csv";
  public static final String LOESCH_PROTOKOLL_MELDER_KANDIDATEN = "zuloeschendeMelder.csv";
  /**
   * The Constant SQL_SELECT_LOESCHKANDIDATEN.
   * Optimierte Version mit Order by und where clause, da LIMIT mit OFFSET bei grossen Angaben sehr langsam ist!
   */
  private static final String SQL_SELECT_LOESCHKANDIDATEN = "SELECT ADRESSEN_ID,QUELL_REFERENZ_OF FROM adressen WHERE adressen.STATUS != \"LOESCH\" AND adressen.QUELL_REFERENZ_ID = {0} AND adressen.QUELL_REFERENZ_TYP != \"MANUELL\" AND ADRESSEN_ID > {1} ORDER BY ADRESSEN_ID ASC LIMIT {2}";

  /**
   * The Constant SQL_SELECTCOUNT_LOESCHKANDIDATEN.
   */
  private static final String SQL_SELECTCOUNT_LOESCHKANDIDATEN = "SELECT COUNT(ADRESSEN_ID) FROM adressen WHERE STATUS != \"LOESCH\" AND QUELL_REFERENZ_ID = {0} AND QUELL_REFERENZ_TYP != \"MANUELL\"";

  /**
   * Instantiates a new Auswirkungen Job
   *
   * @param jobBean the job Bean
   */
  public AuswirkungenJob(JobBean jobBean)
  {
    super("AuswirkungenJob", jobBean);
  }

  /**
   * Verarbeite job.
   *
   * @throws JobException the job exception
   */
  @Override
  public AbstractJob verarbeiteJob() throws JobException
  {

    if (!this.jobBean.getSimulation().bestandErmittelt)
    {
      this.ermittleAnzahl();
    }
    this.ermittleAuswirkungen();
    if (this.jobBean.getSimulation().adressenOffset < this.jobBean.getSimulation().anzahlAdressenImBestand)
    {
      // neuen Job starten da noch nicht fertig!     
      return new AuswirkungenJob(this.jobBean);
    }

    // Protokolle zippen
    AufraeumUtil aufraeumUtil = new AufraeumUtil();
    aufraeumUtil.erzeugeProtokollArchiv("" + this.jobBean.jobId, AufraeumUtil.PROKOLL_DATEINAME);
    // Falls nur geprüft werden soll, dann ist der Job hier beendet
    if (this.jobBean.getSimulation().importSimulieren)
    {
      this.jobBean.setStatusAndInfo(JobStatus.BEENDET, "Auswirkungen ermittelt");
      return null;
    }

    // Nächsten regulären Job starten
    return JobFactory.createJob(this.jobBean.getImportdatei().importFormat, this.jobBean);
  }

  /**
   * Ermittle anzahl.
   *
   * @throws JobException the job exception
   */
  protected void ermittleAnzahl() throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECTCOUNT_LOESCHKANDIDATEN, this.jobBean.quellReferenzId);
    this.log.debug(sql);
    this.jobBean.getSimulation().anzahlAdressenImBestand = 0;
    ResultRow row = sqlUtil.fetchOne(sql);
    if (row != null)
    {
      this.jobBean.getSimulation().anzahlAdressenImBestand = row.getInt(1);
      this.jobBean.getSimulation().bestandErmittelt = true;
    }
  }

  /**
   * Ermittle loesch kandidaten.
   *
   * @throws JobException the job exception
   */
  protected void ermittleAuswirkungen() throws JobException
  {
    this.beginStopWatch();
    // Lade zuerst die Ordnungsfelder und generiere daraus eine HashList
    Set<String> bestehendeOfs = this.jobBean.getAdressen()
      .getOrdnungsfelder();
    int adressenInImport = bestehendeOfs.size();

    // Falls Loeschung erwuenscht, dann ermittle die IDs aller bestehenden Adressen
    // Verwende dazu kleinere Päckchen, da sonst bei grossen zu loeschenden Beständen Probleme auftreten

    int max = Math.min(this.jobBean.getSimulation().adressenOffset + this.jobBean.loeschBlockGroesse, this.jobBean.getSimulation().anzahlAdressenImBestand);
    String info;
    int prozent;
    if (this.jobBean.getSimulation().anzahlAdressenImBestand == 0)
    {
      prozent = 100;
    } else
    {
      prozent = (max * 100) / this.jobBean.getSimulation().anzahlAdressenImBestand;
    }
    this.log.debug(MessageFormat.format("Auswirkungen {0} bis {1} von {2} werden ermittelt...", this.jobBean.getSimulation().adressenOffset + 1, max, this.jobBean
      .getSimulation().anzahlAdressenImBestand));
    info = MessageFormat.format("Auswirkungen des Imports werden ermittelt, {0}%", prozent);
    this.log.info(info);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, info);
    int anzahl = queryOrdnungsfelder(bestehendeOfs);
    info = MessageFormat.format("Auswirkungen des Imports wurden zu {0}% ermittelt", prozent);
    this.jobBean.getSimulation().adressenOffset += anzahl;
    this.jobBean.getSimulation()
      .getAdressIdentifikatoren()
      .getNeu()
      .setAnzahl(adressenInImport - this.jobBean.getSimulation()
        .getAdressIdentifikatoren()
        .getAenderung()
        .getAnzahl());
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, info);
    this.log.info(info);
  }

  private int queryOrdnungsfelder(Set<String> bestehendeOfs) throws JobException
  {
    int anzahl = 0;
    String sql = MessageFormat.format(SQL_SELECT_LOESCHKANDIDATEN, "" + this.jobBean.quellReferenzId, "" + this.jobBean.getSimulation().lastAdressenId, "" + this.jobBean.loeschBlockGroesse);
    HashSet<Integer> loeschAdressen = new HashSet<>(bestehendeOfs.size());
    List<ResultRow> rows = sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      int adressenId = row.getInt(1);
      this.jobBean.getSimulation().lastAdressenId = adressenId;
      String of = row.getString(2); // QUELL_REFERENZ_OF
      // OF aus Datenbank ist in Import enthalten
      if (bestehendeOfs.contains(of))
      {
        this.jobBean.getSimulation()
          .getAdressIdentifikatoren()
          .getAenderung()
          .getValues()
          .add(adressenId);
      } else if (this.jobBean.loescheDaten)
      {
        // Ordnungsfeld ist nicht in Importdatei und wird daher zum Löschen markiert, falls Loeschen aktiv ist
        loeschAdressen.add(adressenId); // ADRESSEN_ID
      }
      anzahl++;
    }
    if (this.jobBean.loescheDaten)
    {
      erzeugeInfoDateien(loeschAdressen);
    }
    return anzahl;
  }

  /**
   * @param loeschAdressen die Loeschadressen
   * @throws JobException the exception
   */
  public void erzeugeInfoDateien(Set<Integer> loeschAdressen) throws JobException
  {
    // Protokoll erstellen
    LoeschUtil loeschUtil = new LoeschUtil(sqlUtil);
    loeschUtil.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    loeschUtil.setZeitpunkt(this.jobBean.zeitpunktEintrag);
    this.log.debug("erzeugeInfoDateien mit " + loeschAdressen.size() + " Kandidaten");

    // Wenn keine Adressen zu loeschen sind dann ueberspringe diesen Block
    if (!loeschAdressen.isEmpty())
    {
      // Pafed fuer die einzelnen Protokolldateien festlegen
      String importVerzeichnis = this.jobBean.getImportdatei().importVerzeichnis;
      Path adressProtokoll = Paths.get(importVerzeichnis, LOESCH_PROTOKOLL_ADRESSEN_KANDIDATEN);
      Path firmenProtokoll = Paths.get(importVerzeichnis, LOESCH_PROTOKOLL_FIRMEN_KANDIDATEN);
      Path melderProtokoll = Paths.get(importVerzeichnis, LOESCH_PROTOKOLL_MELDER_KANDIDATEN);
      // zu loeschende Adressen sind bekannt, schreibe Details in Protokolldatei
      this.jobBean.getSimulation()
        .getAdressIdentifikatoren()
        .getLoeschung()
        .getValues()
        .addAll(loeschAdressen);

      loeschUtil.schreibeAdressInfos(adressProtokoll, loeschAdressen);

      // Ermittle Firmen
      Set<Integer> loeschFirmen = loeschUtil.pruefeAdressVerweise(loeschAdressen, LoeschUtil.SQL_SELECT_FIRMEN_ADRESSEN);
      loeschUtil.schreibeFirmenInfos(firmenProtokoll, loeschFirmen);
      this.jobBean.getSimulation()
        .getFirmenIdentifikatoren()
        .getLoeschung()
        .getValues()
        .addAll(loeschFirmen);

      // Ermittle Melder
      Set<Integer> loeschMelder = loeschUtil.pruefeAdressVerweise(loeschAdressen, LoeschUtil.SQL_SELECT_LOESCH_MELDER);
      loeschUtil.schreibeMelderInfos(melderProtokoll, loeschMelder);
      this.jobBean.getSimulation()
        .getMelderIdentifikatoren()
        .getLoeschung()
        .getValues()
        .addAll(loeschMelder);

      // Zippen
      AufraeumUtil aufraeumUtil = new AufraeumUtil();
      List<Path> pfade = new ArrayList<>();
      pfade.add(adressProtokoll);
      pfade.add(firmenProtokoll);
      pfade.add(melderProtokoll);
      Path ziel = Paths.get(importVerzeichnis, AufraeumUtil.PROKOLL_DATEINAME);
      aufraeumUtil.zippeDateien(pfade, ziel);
    }
  }

}
