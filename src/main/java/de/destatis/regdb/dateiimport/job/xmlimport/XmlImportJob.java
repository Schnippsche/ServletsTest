package de.destatis.regdb.dateiimport.job.xmlimport;

import de.destatis.regdb.FormatError;
import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.dateiimport.job.AbstractImportJob;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.LoeschenJob;
import de.destatis.regdb.dateiimport.job.adressimport.*;
import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportBean;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportJob;
import de.destatis.regdb.dateiimport.reader.SegmentedXmlFileReader;
import de.destatis.regdb.db.*;
import de.werum.sis.idev.intern.actions.util.MelderDaten;
import de.werum.sis.idev.res.job.JobException;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Xml import job.
 */
public class XmlImportJob extends AbstractImportJob
{

  /**
   * The constant SQL_SELECT_MELDERKENNUNG.
   */
  public static final String SQL_SELECT_MELDERKENNUNG = "SELECT MELDER_ID FROM melder WHERE KENNUNG = \"{0}\" AND STATUS != \"LOESCH\"";
  /**
   * The constant AKTION_NEU.
   */
  public static final String AKTION_NEU = "NEU";
  /**
   * The constant AKTION_UPDATE.
   */
  public static final String AKTION_UPDATE = "UPDATE";
  /**
   * The constant XML_ADRESSE.
   */
  public static final String XML_ADRESSE = "adresse";
  /**
   * The constant XML_FIRMA.
   */
  public static final String XML_FIRMA = "firma";
  /**
   * The constant XML_MELDER.
   */
  public static final String XML_MELDER = "melder";
  /**
   * The constant XML_ERHEBUNG.
   */
  public static final String XML_ERHEBUNG = "erhebung";
  /**
   * The constant XML_VORBELEGUNG.
   */
  public static final String XML_VORBELEGUNG = "vorbelegung";
  /**
   * The constant AKTION_LOESCH.
   */
  protected static final String AKTION_LOESCH = "LOESCH";
  private static final String XML_AUTO = "AUTO";
  private final VorbelegungsImportJob vorbelegungsJob;
  /**
   * The Ordnungsfelder.
   */
  protected HashMap<String, OrdnungsfeldInfo> ordnungsfelder;
  /**
   * The Xml beans.
   */
  protected ArrayList<XmlBean> xmlBeans;
  /**
   * The Sorted elements.
   */
  protected ArrayList<XmlBean> sortedElements;
  /**
   * The Erhebungen.
   */
  protected HashSet<String> erhebungen;
  private HashMap<String, Integer> amtStatOnlineKeys;
  private boolean createVorbelegungen;

  /**
   * Instantiates a new Xml import job.
   *
   * @param jobBean the job bean
   */
  public XmlImportJob(JobBean jobBean)
  {
    this("ImportXml", jobBean);
  }

  /**
   * Instantiates a new Xml import job.
   *
   * @param jobName the job name
   * @param jobBean the job bean
   */
  public XmlImportJob(String jobName, JobBean jobBean)
  {
    super(jobName, jobBean);
    this.vorbelegungsJob = new VorbelegungsImportJob(jobBean, this.sqlUtil);
  }

  @Override
  protected void doNormalImport() throws JobException
  {
    Path path = this.jobBean.getImportdatei().getPath();
    if (!this.leseTeilbereich(path))
    {
      return;
    }
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Import aktiv");
    this.ermittleIndizes();
    this.ermittleErhebungen();
    this.startTransaction();
  }

  @Override
  protected AbstractJob nextImportJob()
  {
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, "Import aktiv");
    return new XmlImportJob(this.jobBean);
  }

  @Override
  protected AbstractJob doAfterLastImport()
  {
    if (this.jobBean.loescheDaten && !this.isCancelled())
    {
      return new LoeschenJob(this.jobBean);
    }

    // Job beendet, Dateien loeschen
    AufraeumUtil util = new AufraeumUtil();
    util.entferneDateien(this.jobBean.jobId);
    this.jobBean.setStatusAndInfo(JobStatus.BEENDET, "XML-Import wurde durchgeführt");
    return null;
  }

  @Override
  protected void doInTransaction() throws JobException
  {
    this.erzeugeNeueErhebungen();
    this.erzeugeNeueAdressen();
    this.erzeugeNeueMelderAnsprechpartner();
    this.erzeugeNeueFirmenAnsprechpartner();
    this.erzeugeNeueFirmen();
    this.erzeugeNeueFirmenAdressen();
    this.erzeugeNeueMelder();
    this.aktualisiereBestehendeAdressen();
    this.aktualisiereBestehendeFirmen();
    this.aktualisiereBestehendeMelder();
    this.aktualisiereBestehendeErhebungen();
    // Ermittle Indizes der Vorbelegungen
    this.ermittleVorbelegungsIndizes();
    if (this.createVorbelegungen)
    {
      this.vorbelegungsJob.verdichteDaten();
      this.vorbelegungsJob.erzeugeNeueVerwaltungsEintraege();
      this.vorbelegungsJob.aktualisiereVerwaltungEintraege();
      this.vorbelegungsJob.erzeugeOderAktualisiereWertEintraege();
    }
    this.ermittleAdressLoeschEintraege();
    this.ermittleFirmenLoeschEintraege();
    this.ermittleMelderLoeschEintraege();
    this.ermittleUndLoescheVorbelegungen();

  }

  private void ermittleVorbelegungsIndizes() throws JobException
  {
    this.log.info("ermittle Indizes der Vorbelegungen...");
    // Ermittle Vorbelegungen und wandle in VorbelegungsBeans um
    try (PreparedSelect psBestand = this.sqlUtil.createPreparedSelect(VorbelegungsImportJob.SQL_SELECT_VORBELEGUNG_BESTAND))
    {
      for (XmlBean bean : this.xmlBeans)
      {
        if (XML_VORBELEGUNG.equalsIgnoreCase(bean.getName()))
        {
          this.log.debug("Vorbelegungsbean gefunden:" + bean);
          OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
          if (info.getAdressenId() == 0)
          {
            String fehler = "Adresse zur Vorbelegung mit dem Ordnungsfeld '" + bean.getQuellReferenzOf() + "' existiert nicht";
            this.log.info(fehler);
            this.jobBean.getFormatPruefung().addFehler(new FormatError(null, fehler));
          }
          else
          {
            VorbelegungsImportBean vb = this.convertIntoVorbelegung(bean, info);
            // Falls Kennung angegeben ist, dann pruefe auf Vorhandensein!
            String kennung = bean.getWithDefaultValue("melder_kennung", "");
            boolean okay = true;
            if (!kennung.isEmpty())
            {
              Integer tmpMelderId = this.ermittleMelderKennung(kennung);
              if (tmpMelderId == 0)
              {
                String fehler = "Melder mit angegebener Kennung '" + kennung + "' existiert nicht!";
                this.log.info(fehler);
                this.jobBean.getFormatPruefung().addFehler(new FormatError(null, fehler));
                okay = false;
              }
              else
              {
                vb.setMelderId(this.ermittleMelderKennung(kennung));
              }
            }
            if (okay)
            {
              this.log.debug("Vorbelegung:" + vb);
              psBestand.addValue(vb.getAmt());
              psBestand.addValue(vb.getStatistikId());
              psBestand.addValue(vb.getBzr());
              psBestand.addValue(vb.getQuellReferenzOf());
              psBestand.addValue(vb.getQuellReferenzInt());
              psBestand.addValue(vb.getMelderId());
              psBestand.addValue(vb.getFirmenId());
              ResultRow row = psBestand.fetchOne();
              if (row != null)
              {
                vb.setVorbelegungId(row.getInt("VORBELEGUNG_ID"));
                info.setVorbelegungsId(vb.getVorbelegungId());
                vb.setVbWerteIndex(row.getInt("VB_WERTE_INDX"));
                vb.setNeueVorbelegung(false);
                this.jobBean.getVorbelegungen().vbWerteIndex = vb.getVbWerteIndex();
              }
              // Prufe Aktionen
              info.ersetzeAktionAny(bean, OrdnungsfeldInfo.VORBELEGUNG_ID);
              this.vorbelegungsJob.getVorbelegungsImportBeans().add(vb);
              this.createVorbelegungen = true;
            }
          }
        }
      }
    }
  }

  /**
   * Ermittle melder kennung integer.
   *
   * @param kennung the kennung
   * @return the integer
   * @throws JobException the job exception
   */
  protected Integer ermittleMelderKennung(String kennung) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_MELDERKENNUNG, kennung);
    ResultRow row = this.sqlUtil.fetchOne(sql);
    return (row != null) ? row.getInt(1) : 0;
  }

  /**
   * Lese teilbereich boolean.
   *
   * @param path the path
   * @return the boolean
   * @throws JobException the job exception
   */
  protected boolean leseTeilbereich(Path path) throws JobException
  {
    this.ordnungsfelder = new HashMap<>();
    this.amtStatOnlineKeys = new HashMap<>();
    this.vorbelegungsJob.setVorbelegungsImportBeans(new ArrayList<>(this.jobBean.importBlockGroesse));
    SegmentedXmlFileReader reader = new SegmentedXmlFileReader();
    this.xmlBeans = reader.readSegment(path, this.jobBean.getImportdatei().getCharset(), this.jobBean.getImportdatei().datensatzOffset, this.jobBean.importBlockGroesse);
    this.ermittleStatistikIds();
    return !this.xmlBeans.isEmpty();
  }

  /**
   * Ermittle indizes.
   *
   * @throws JobException the job exception
   */
  protected void ermittleIndizes() throws JobException
  {
    if (this.xmlBeans.isEmpty())
    {
      return;
    }
    this.log.info("Ermittle Indizes...");
    this.beginStopWatch();
    int anzahlBestehendeEintraege = 0;
    for (XmlBean bean : this.xmlBeans)
    {
      String of = bean.getQuellReferenzOf();
      if (of != null)
      {
        this.ordnungsfelder.put(of, new OrdnungsfeldInfo(of));
      }
    }
    String ids = this.sqlUtil.convertStringList(this.ordnungsfelder.values().stream().map(OrdnungsfeldInfo::getOrdnungsfeld).collect(Collectors.toSet()));
    String sql = MessageFormat.format(AdressImportJob.SQL_SELECT_BESTAND, "" + this.jobBean.quellReferenzId, ids);
    // Ermittle vorhandene Daten
    List<ResultRow> rows = this.sqlUtil.fetchMany(sql);
    for (ResultRow row : rows)
    {
      OrdnungsfeldInfo info = this.ordnungsfelder.get(row.getString("QUELL_REFERENZ_OF"));
      info.setValuesFromResultSet(row);
    }
    // Anzahl ermitteln
    for (XmlBean bean : this.xmlBeans)
    {
      String of = bean.getQuellReferenzOf();
      OrdnungsfeldInfo info = this.ordnungsfelder.get(of);
      if (info != null && info.getAdressenId() != 0)
      {
        anzahlBestehendeEintraege++;
      }
    }
    int anzahlNeueintraege = this.xmlBeans.size() - anzahlBestehendeEintraege;
    this.log.info(MessageFormat.format("Indizes Ermittlung ({0} neue und {1} bestehende Eintraege) beendet in {2}", anzahlNeueintraege, anzahlBestehendeEintraege, this.getElapsedTime()));
  }

  /**
   * Ermittle erhebungen.
   *
   * @throws JobException the job exception
   */
  protected void ermittleErhebungen() throws JobException
  {
    this.erhebungen = new HashSet<>();
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(ErhebungBean.SQL_SELECT_ERHEBUNG))
    {
      for (XmlBean bean : this.xmlBeans)
      {
        if (XML_ERHEBUNG.equalsIgnoreCase(bean.getName()))
        {
          String bzr = bean.getBzr();
          String key = this.getErhebungsKey(bean);
          if (this.erhebungen.contains(key))
          {
            if (bean.isAktionAny())
            {
              bean.setAktion(XmlImportJob.AKTION_UPDATE);
            }
          }
          else
          {
            ps.addValue(bean.getStatistikId());
            ps.addValue(bean.getAmt());
            ps.addValue(bzr);
            ResultRow row = ps.fetchOne();
            if (row != null)
            {
              this.erhebungen.add(key);
            }
          }
        }
      }
    }
  }

  private String getErhebungsKey(XmlBean bean)
  {
    return bean.getStatistikId() + "|" + bean.getAmt() + "|" + bean.getWithDefaultValue("bzr", "");
  }

  /**
   * Erzeuge neue adressen.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueAdressen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_ADRESSE, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.log.info("Erzeuge Neue Adresse...");
    this.beginStopWatch();
    int anzahl = 0;
    try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(AdresseBean.SQL_INSERT_ADRESSEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        // Hole passende Infos
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.ADRESSEN_ID);
        if (!info.isManuell() && bean.isAktionNeu())
        {
          anzahl++;
          AdresseBean ab = this.convertIntoAdresse(bean, info);
          ab.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          ab.setNeu(true);
          ab.insert(ps);
          info.setAdressenId(ab.getAdressenId());
          this.jobBean.getAdressen().getIdentifikatoren().getNeu().getValues().add(ab.getAdressenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Adressen erzeugt in {1} ", anzahl, this.getElapsedTime()));

  }

  /**
   * Erzeuge neue firmen.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueFirmen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_FIRMA, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.log.info("Erzeuge Neue Firmen...");
    this.beginStopWatch();
    int anzahl = 0;
    try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(FirmenBean.SQL_INSERT_FIRMEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.FIRMEN_ID);
        if (!info.isManuell() && bean.isAktionNeu())
        {
          anzahl++;
          FirmenBean fb = this.convertIntoFirma(bean, info);
          fb.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          fb.setNeu(true);
          fb.insert(ps);
          info.setFirmenId(fb.getFirmenId());
          this.jobBean.getFirmen().getIdentifikatoren().getNeu().getValues().add(fb.getFirmenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen erzeugt in {1}", anzahl, this.getElapsedTime()));

  }

  /**
   * Erzeuge neue firmen adressen.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueFirmenAdressen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_FIRMA, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.log.info("Erzeuge Neue Firmen-Adressen...");
    int anzahl = 0;
    this.beginStopWatch();
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(FirmenAdressenBean.SQL_INSERT_FIRMEN_ADRESSEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        if (!info.isManuell() && info.getFirmenId() != 0 && info.getAdressenId() != 0)
        {
          anzahl++;
          FirmenAdressenBean fab = new FirmenAdressenBean();
          fab.setAdressenId(info.getAdressenId());
          fab.setFirmenId(info.getFirmenId());
          fab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
          fab.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          fab.insert(ps);
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen-Adressen erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue melder.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueMelder() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_MELDER, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Erzeuge Neue Melder...");

    // Ermittle zuerst die Anzahl der benoetigten Kennungen!
    ArrayList<XmlBean> neueMelder = new ArrayList<>(this.sortedElements.size());
    for (XmlBean bean : this.sortedElements)
    {
      OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
      info.ersetzeAktionAny(bean, OrdnungsfeldInfo.MELDER_ID);
      if (!info.isManuell() && bean.isAktionNeu())
      {
        neueMelder.add(bean);
      }
    }

    if (!neueMelder.isEmpty())
    {
      // Erstelle für jede Ämter Kennungen
      Map<String, Long> anzahlKennungenJeAmt = neueMelder.stream().collect(Collectors.groupingBy(XmlBean::getAmt, Collectors.counting()));
      Map<String, List<String>> alleKennungen = new HashMap<>();

      KennungTool kennungTool = new KennungTool(this.sqlUtil);
      for (Map.Entry<String, Long> entry : anzahlKennungenJeAmt.entrySet())
      {
        String amt = entry.getKey();
        int anzahl = entry.getValue().intValue();
        ArrayList<String> amtsKennungen = kennungTool.erzeugeEindeutigeKennungen(anzahl, this.jobBean.sachbearbeiterLand);
        alleKennungen.put(amt, amtsKennungen);
      }

      try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(MelderBean.SQL_INSERT_MELDER))
      {
        for (XmlBean bean : neueMelder)
        {
          OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
          MelderBean mb = this.convertIntoMelder(bean, info);
          String kennung = mb.getKennung();
          List<String> kennungen = alleKennungen.get(bean.getAmt());

          if (kennung == null || kennung.isEmpty())
          {
            mb.setKennung(kennungen.get(0));
            kennungen.remove(0);
          }
          else
          {
            // Kennung angegeben, existiert diese ? Wenn ja, dann nimm neue Kennung
            if (kennungTool.existiertKennung(kennung))
            {
              mb.setKennung(kennungen.get(0));
              kennungen.remove(0);
              this.log.info("angegebene Kennung '" + kennung + "' existiert bereits und wurde durch '" + mb.getKennung() + "' ersetzt!");
            }
          }
          MelderDaten melderDaten = MelderDatenService.getInstance().getMelderDaten();
          mb.setMelderDaten(melderDaten);
          mb.setZeitpunktRegistrierung(this.jobBean.zeitpunktEintrag);
          mb.insert(ps);
          info.setMelderId(mb.getMelderId());
          this.jobBean.getMelder().getIdentifikatoren().getNeu().getValues().add(mb.getMelderId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder erzeugt in {1}", neueMelder.size(), this.getElapsedTime()));
    this.erzeugeMelderStatistiken();
  }

  /**
   * Erzeuge neue melder ansprechpartner.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueMelderAnsprechpartner() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_MELDER, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Erzeuge Neue Melder-Ansprechpartner...");
    int anzahl = 0;
    try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(AnsprechpartnerBean.SQL_INSERT_ANSPRECHPARTNER))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.MELDER_PARTNER_ID);
        if (!info.isManuell() && bean.isAktionNeu())
        {
          anzahl++;
          MelderBean mb = this.convertIntoMelder(bean, info);
          AnsprechpartnerBean ab = mb.getAnsprechpartner();
          ab.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          ab.setNeu(true);
          ab.insert(ps);
          info.setMelderPartnerId(ab.getAnsprechpartnerId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder-Ansprechpartner erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue erhebungen.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueErhebungen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_ERHEBUNG, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    int anzahl = 0;
    // Key: Statistik_id, amt, bzr
    this.log.info("Erzeuge neue Erhebungen...");
    try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(ErhebungBean.SQL_INSERT_ERHEBUNGEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        // Ist Erhebung vorhanden ?
        String key = this.getErhebungsKey(bean);
        boolean vorhanden = this.erhebungen.contains(key);
        if (bean.isAktionNeu() && vorhanden)
        {
          String fehler = "Aktion 'NEU' auf vorhandener Erhebung mit Amt '" + bean.getAmt() + "', Statistik-Id " + bean.getStatistikId() + ", Bzr '" + bean.getBzr() + "'";
          this.log.info(fehler);
          this.jobBean.getFormatPruefung().addFehler(new FormatError(null, fehler));
        }
        else if (!vorhanden)
        {
          bean.setAktion(XmlImportJob.AKTION_NEU);
          ErhebungBean eb = new ErhebungBean();
          this.convertIntoErhebung(eb, bean);
          eb.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          eb.setNeu(true);
          eb.insert(ps);
          this.erhebungen.add(key);
          anzahl++;
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Erhebungen erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Erzeuge neue firmen ansprechpartner.
   *
   * @throws JobException the job exception
   */
  protected void erzeugeNeueFirmenAnsprechpartner() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_FIRMA, AKTION_NEU);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Erzeuge Neue Firmen-Ansprechpartner...");
    int anzahl = 0;
    try (PreparedInsert ps = this.sqlUtil.createPreparedInsert(AnsprechpartnerBean.SQL_INSERT_ANSPRECHPARTNER))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.FIRMEN_PARTNER_ID);
        if (!info.isManuell() && bean.isAktionNeu())
        {
          anzahl++;
          FirmenBean fb = this.convertIntoFirma(bean, info);
          AnsprechpartnerBean ab = fb.getAnsprechpartner();
          ab.setZeitpunktEintrag(this.jobBean.zeitpunktEintrag);
          ab.setNeu(true);
          ab.insert(ps);
          info.setFirmenPartnerId(ab.getAnsprechpartnerId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen-Ansprechpartner erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende adressen.
   *
   * @throws JobException the job exception
   */
  protected void aktualisiereBestehendeAdressen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_ADRESSE, AKTION_UPDATE);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung Adressen...");
    int anzahl = 0;
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(AdresseBean.SQL_UPDATE_ADRESSEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.ADRESSEN_ID);
        if (!info.isManuell() && bean.isAktionUpdate())
        {
          anzahl++;
          AdresseBean ab = this.convertIntoAdresse(bean, info);
          ab.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          ab.setNeu(false);
          ab.update(ps);
          this.jobBean.getAdressen().getIdentifikatoren().getAenderung().getValues().add(ab.getAdressenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Adressen aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende firmen.
   *
   * @throws JobException the job exception
   */
  protected void aktualisiereBestehendeFirmen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_FIRMA, AKTION_UPDATE);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung Firmen...");
    int anzahl = 0;
    try (PreparedUpdate pu = this.sqlUtil.createPreparedUpdate(FirmenBean.SQL_UPDATE_FIRMEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.FIRMEN_ID);
        if (!info.isManuell() && bean.isAktionUpdate())
        {
          anzahl++;
          FirmenBean fb = this.convertIntoFirma(bean, info);
          fb.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          fb.setNeu(false);
          fb.update(pu);
          this.jobBean.getFirmen().getIdentifikatoren().getAenderung().getValues().add(fb.getFirmenId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende firmen ansprechpartner.
   *
   * @throws JobException the job exception
   */
  protected void aktualisiereBestehendeFirmenAnsprechpartner() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_FIRMA, AKTION_UPDATE);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung FirmenAnsprechpartner...");
    int anzahl = 0;
    try (PreparedUpdate pu = this.sqlUtil.createPreparedUpdate(AnsprechpartnerBean.SQL_UPDATE_ANSPRECHPARTNER))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.FIRMEN_PARTNER_ID);
        if (!info.isManuell() && bean.isAktionUpdate())
        {
          anzahl++;
          FirmenBean fb = this.convertIntoFirma(bean, info);
          AnsprechpartnerBean ab = fb.getAnsprechpartner();
          ab.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          ab.setNeu(false);
          ab.update(pu);
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Firmen-Ansprechpartner aktualisiert in {1}", anzahl, this.getElapsedTime()));
  }

  /**
   * Aktualisiere bestehende melder.
   *
   * @throws JobException the job exception
   */
  protected void aktualisiereBestehendeMelder() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_MELDER, AKTION_UPDATE);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisierung Melder...");
    int anzahl = 0;
    try (PreparedUpdate pu = this.sqlUtil.createPreparedUpdate(MelderBean.SQL_UPDATE_EXTENDED_MELDER))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        info.ersetzeAktionAny(bean, OrdnungsfeldInfo.MELDER_PARTNER_ID);
        if (!info.isManuell() && bean.isAktionUpdate())
        {
          anzahl++;
          MelderBean mb = this.convertIntoMelder(bean, info);
          mb.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          mb.setNeu(false);
          mb.updateExtended(pu);
          this.jobBean.getMelder().getIdentifikatoren().getAenderung().getValues().add(mb.getMelderId());
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Melder aktualisiert in {1}", anzahl, this.getElapsedTime()));
    this.erzeugeMelderStatistiken();
  }

  /**
   * Aktualisiere bestehende erhebungen.
   *
   * @throws JobException the job exception
   */
  protected void aktualisiereBestehendeErhebungen() throws JobException
  {
    this.sortedElements = this.getEintraegeMitAktionAnyUnd(XML_ERHEBUNG, AKTION_UPDATE);
    if (this.sortedElements.isEmpty())
    {
      return;
    }
    this.beginStopWatch();
    this.log.info("Aktualisiere Erhebungen...");
    int anzahl = 0;
    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(ErhebungBean.SQL_UPDATE_ERHEBUNGEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        String key = this.getErhebungsKey(bean);
        boolean vorhanden = this.erhebungen.contains(key);
        if (bean.isAktionUpdate() && !vorhanden)
        {
          String fehler = "Aktion 'UPDATE' auf nicht vorhandener Erhebung mit Amt '" + bean.getAmt() + "', Statistik-Id " + bean.getStatistikId() + ", Bzr '" + bean.getBzr() + "'";
          this.log.info(fehler);
          this.jobBean.getFormatPruefung().addFehler(new FormatError(null, fehler));
        }
        else if (vorhanden)
        {
          bean.setAktion(XmlImportJob.AKTION_UPDATE);
          ErhebungBean eb = new ErhebungBean();
          ladeErhebung(eb, bean.getStatistikId(), bean.getAmt(), bean.getBzr());
          this.convertIntoErhebung(eb, bean);
          eb.setZeitpunktAenderung(this.jobBean.zeitpunktEintrag);
          eb.setNeu(false);
          eb.update(ps);
          anzahl++;
        }
      }
    }
    this.log.info(MessageFormat.format("{0} Erhebungen aktualisiert in {1}", anzahl, this.getElapsedTime()));

  }

  /**
   * Lade erhebung.
   *
   * @param eb          the eb
   * @param statistikId the statistik id
   * @param amt         the amt
   * @param bzr         the bzr
   * @throws JobException the job exception
   */
  public void ladeErhebung(ErhebungBean eb, Integer statistikId, String amt, String bzr) throws JobException
  {
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(ErhebungBean.SQL_SELECT_ERHEBUNG))
    {
      ps.addValue(statistikId);
      ps.addValue(amt);
      ps.addValue(bzr);
      ResultRow row = ps.fetchOne();
      if (row != null)
      {
        eb.load(row);
      }
    }
  }

  private void ermittleAdressLoeschEintraege()
  {
    this.log.debug("ermittle Adressen zum Löschen...");
    int anz = 0;
    List<OrdnungsfeldInfo> ofs = ermittleLoeschEintraege(XML_ADRESSE);
    for (OrdnungsfeldInfo info : ofs)
    {
      int id = info.getAdressenId();
      if (id > 0)
      {
        this.jobBean.getSimulation().getAdressIdentifikatoren().getLoeschung().getValues().add(id);
        anz++;
        this.jobBean.loescheDaten = true;
      }
    }
    this.log.debug(anz + " Adressen zum Löschen...");
  }

  private void ermittleFirmenLoeschEintraege()
  {
    this.log.debug("ermittle Firmen zum Löschen...");
    int anz = 0;
    List<OrdnungsfeldInfo> ofs = ermittleLoeschEintraege(XML_FIRMA);
    for (OrdnungsfeldInfo info : ofs)
    {
      int id = info.getFirmenId();
      if (id > 0)
      {
        this.jobBean.getSimulation().getFirmenIdentifikatoren().getLoeschung().getValues().add(id);
        anz++;
        this.jobBean.loescheDaten = true;
      }
    }
    this.log.debug(anz + " Firmen zum Löschen...");
  }

  private void ermittleMelderLoeschEintraege()
  {
    this.log.debug("ermittle Melder zum Löschen...");
    int anz = 0;
    List<OrdnungsfeldInfo> ofs = ermittleLoeschEintraege(XML_MELDER);
    for (OrdnungsfeldInfo info : ofs)
    {
      int id = info.getMelderId();
      if (id > 0)
      {
        this.jobBean.getSimulation().getMelderIdentifikatoren().getLoeschung().getValues().add(id);
        anz++;
        this.jobBean.loescheDaten = true;
      }
    }
    this.log.debug(anz + " Melder zum Löschen...");
  }

  private void ermittleUndLoescheVorbelegungen() throws JobException
  {
    this.log.debug("ermittle Vorbelegungen zum Löschen...");
    int anz = 0;
    HashSet<Integer> ids = new HashSet<>();
    List<OrdnungsfeldInfo> ofs = ermittleLoeschEintraege(XML_VORBELEGUNG);
    for (OrdnungsfeldInfo info : ofs)
    {
      int id = info.getVorbelegungsId();
      if (id > 0)
      {
        ids.add(id);
        anz++;
      }
    }
    if (anz > 0)
    {
      this.vorbelegungsJob.loescheVorbelegungenMitIds(ids);
    }

  }

  private List<OrdnungsfeldInfo> ermittleLoeschEintraege(String xmlEntry)
  {
    ArrayList<OrdnungsfeldInfo> felder = new ArrayList<>();
    for (XmlBean bean : this.xmlBeans)
    {
      String beanAktion = bean.getAktion();
      if (AKTION_LOESCH.equalsIgnoreCase(beanAktion) && xmlEntry.equals(bean.getName()))
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        felder.add(info);
      }
    }
    return felder;
  }

  private void erzeugeMelderStatistiken() throws JobException
  {
    this.log.info("Erzeuge Neue Melder-Statistik-Eintraege...");
    int anzahl = 0;
    this.beginStopWatch();

    try (PreparedUpdate ps = this.sqlUtil.createPreparedUpdate(RegisterImportJob.SQL_INSERT_MELDER_STATISTIKEN))
    {
      for (XmlBean bean : this.sortedElements)
      {
        OrdnungsfeldInfo info = this.ordnungsfelder.get(bean.getQuellReferenzOf());
        // MELDER_ID, STATISTIK_ID, AMT, FIRMEN_ID, ROLLE, MELDERECHT_BZR, `STATUS`,
        // ERINNERUNGSSERVICE, IS_ADRESSEN_ID, IS_GRUND, IS_STATUS, SACHBEARBEITER_ID, ZEITPUNKT_EINTRAG
        anzahl++;
        ps.addValue(info.getMelderId()); //
        ps.addValue(bean.getStatistikId()); // STATISTIK_ID
        ps.addValue(bean.getAmt());
        ps.addValue(info.getFirmenId()); //
        ps.addValue("MELDER"); //
        ps.addValue(bean.getBzr()); //
        ps.addValue("NEU"); // STATUS
        ps.addValue("J"); // ERINNERUNGSSERVICE
        ps.addValue(info.getAdressenId()); // IS_ADRESSEN_ID
        ps.addValue(this.jobBean.sachbearbeiterId); //
        ps.addValue(this.jobBean.zeitpunktEintrag);
        ps.update();
      }
    }
    this.log.info(MessageFormat.format("{0}  Melder-Statistik-Eintraege erzeugt in {1}", anzahl, this.getElapsedTime()));
  }

  private AdresseBean convertIntoAdresse(XmlBean bean, OrdnungsfeldInfo info)
  {
    AdresseBean ab = new AdresseBean();
    ab.setAdressenId(info.getAdressenId());
    ab.setAbteilung(bean.getValue("abteilung"));
    ab.setAnrede(bean.getValue("anrede"));
    ab.setAmt(bean.getAmt());
    ab.setEmail(bean.getValue("email"));
    ab.setFax(bean.getValue("fax"));
    ab.setHausnummer(bean.getValue("hausnummer"));
    ab.setKurztext(bean.getValue("kurztext"));
    ab.setLand(bean.getValue("land"));
    ab.setManuelleAdresse(info.isManuell());
    ab.setMelderAenderbar("J".equalsIgnoreCase(bean.getWithDefaultValue("melder_aenderbar", "J")));
    ab.setName(bean.getValue("name"));
    ab.setNameErgaenzung(bean.getValue("name_ergaenzung"));
    ab.setOrt(bean.getValue("ort"));
    ab.setPostfach(bean.getValue("postfach"));
    ab.setPostfachOrt(bean.getValue("postfach_ort"));
    ab.setPostfachPlz(bean.getValue("postfach_plz"));
    ab.setPostleitzahl(bean.getValue("postleitzahl"));
    ab.setQuellReferenzId(this.jobBean.quellReferenzId);
    ab.setQuellReferenzOf(bean.getQuellReferenzOf()); // 40 Zeichen in Datenbank
    ab.setRolle(bean.getValue("rolle"));
    ab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    ab.setStrasse(bean.getValue("strasse"));
    ab.setTelefon(bean.getValue("telefon"));
    for (int i = 0; i < 7; i++)
    {
      ab.setUrs(i, bean.getValue("urs" + (i + 1)));
    }
    for (int i = 0; i < 10; i++)
    {
      ab.setZusatz(i, bean.getValue("zusatz" + (i + 1)));
    }
    return ab;
  }

  private FirmenBean convertIntoFirma(XmlBean bean, OrdnungsfeldInfo info)
  {
    FirmenBean fb = new FirmenBean();
    fb.setFirmenId(info.getFirmenId());
    fb.setKurztext(bean.getValue("kurztext"));
    fb.setName(bean.getValue("name"));
    fb.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    AnsprechpartnerBean ab = fb.getAnsprechpartner();
    ab.setAnsprechpartnerId(info.getFirmenPartnerId());
    ab.setAnrede(bean.getValue("ansprechpartner_anrede"));
    ab.setName(bean.getValue("ansprechpartner_name"));
    ab.setEmail(bean.getValue("ansprechpartner_email"));
    ab.setFax(bean.getValue("ansprechpartner_fax"));
    ab.setMobil(bean.getValue("ansprechpartner_mobil"));
    ab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    ab.setTelefon(bean.getValue("ansprechpartner_telefon"));
    ab.setVorname(bean.getValue("ansprechpartner_vorname"));
    return fb;
  }

  private MelderBean convertIntoMelder(XmlBean bean, OrdnungsfeldInfo info)
  {
    MelderBean mb = new MelderBean();
    mb.setAdressenId(info.getAdressenId());
    mb.setFirmenId(info.getFirmenId());
    mb.setKennung(bean.getValue("kennung"));
    mb.setMelderId(info.getMelderId());
    mb.setPasswortAenderbar("J".equalsIgnoreCase(bean.getWithDefaultValue("passwort_aenderbar", "J")));
    mb.setPasswortAenderung("J".equalsIgnoreCase(bean.getWithDefaultValue("passwort_aenderung", "J")));
    mb.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    mb.setZusammenfuehrbar("J".equalsIgnoreCase(bean.getWithDefaultValue("zusammenfuehrbar", "J")));
    AnsprechpartnerBean ab = mb.getAnsprechpartner();
    ab.setAbteilung(bean.getValue("abteilung"));
    ab.setAnrede(bean.getValue("anrede"));
    ab.setAnsprechpartnerId(info.getMelderPartnerId());
    ab.setEmail(bean.getValue("email"));
    ab.setFax(bean.getValue("fax"));
    ab.setMobil(bean.getValue("mobil"));
    ab.setName(bean.getValue("name"));
    ab.setSachbearbeiterId(this.jobBean.sachbearbeiterId);
    ab.setTelefon(bean.getValue("telefon"));
    ab.setVorname(bean.getValue("vorname"));
    return mb;
  }

  private VorbelegungsImportBean convertIntoVorbelegung(XmlBean bean, OrdnungsfeldInfo info)
  {
    VorbelegungsImportBean vb = new VorbelegungsImportBean();
    vb.setAmt(bean.getAmt());
    vb.setBzr(bean.getWithDefaultValue("bzr", ""));
    vb.setFirmenId(info.getFirmenId());
    vb.setFormularname(bean.getWithDefaultValue("formularname", ""));
    vb.setQuellReferenzInt(bean.getWithDefaultValue("quell_referenz_int", ""));
    vb.setQuellReferenzOf(bean.getQuellReferenzOf());
    vb.setStatistikId(bean.getStatistikId());
    vb.setVbWerteIndex(0);
    vb.setVorbelegungId(0);
    // Werte überspielen
    HashMap<String, String> map = bean.getMap();
    for (Map.Entry<String, String> entry : map.entrySet())
    {
      String key = entry.getKey();
      if (key != null && key.startsWith("feldname."))
      {
        key = StringUtil.substring(key, 9).trim();
        vb.getWerte().put(key, entry.getValue());
      }
    }

    return vb;
  }

  private void convertIntoErhebung(ErhebungBean eb, XmlBean bean)
  {
    eb.setStatistikId(bean.getStatistikId());
    eb.setAmt(bean.getAmt());
    eb.setBzr(bean.getWithDefaultValue("bzr", ""));
    String value;
    eb.setErsterMeldungstermin(bean.getWithDefaultValue("erster_meldungstermin", ""));
    eb.setLetzterMeldungstermin(bean.getWithDefaultValue("letzter_meldungstermin", ""));
    eb.setStatspezKey(bean.getWithDefaultValue("statspez_key", ""));
    eb.setFormularId(bean.getWithDefaultValue("formular_id", ""));
    // Wenn AUTO gesetzt ist, dann nimm den vorhandenen Wert; ansonsten überschreibe ihn
    value = bean.getWithDefaultValue("vorbelegungsabhaengig", "N");
    if (!XML_AUTO.equals(value))
    {
      eb.setVorbelegungsabhaengig(value);
    }

    value = bean.getWithDefaultValue("senden", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setSenden(value);
    }

    value = bean.getWithDefaultValue("zuruecksetzen", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setZuruecksetzen(value);
    }

    value = bean.getWithDefaultValue("pruefung", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setPruefung(value);
    }

    value = bean.getWithDefaultValue("lokalsicherung", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setLokalsicherung(value);
    }

    value = bean.getWithDefaultValue("serversicherung", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setServersicherung(value);
    }

    value = bean.getWithDefaultValue("archivierung", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setArchivierung(value);
    }

    value = bean.getWithDefaultValue("weitere_meldung", "J");
    if (!XML_AUTO.equals(value))
    {
      eb.setWeitereMeldung(value);
    }
  }

  private ArrayList<XmlBean> getEintraegeMitAktionAnyUnd(String name, String aktion)
  {
    ArrayList<XmlBean> result = new ArrayList<>();
    for (XmlBean bean : this.xmlBeans)
    {
      if (name.equalsIgnoreCase(bean.getName()))
      {
        String beanAktion = bean.getAktion();
        if ("ANY".equalsIgnoreCase(beanAktion) || aktion.equalsIgnoreCase(beanAktion))
        {
          result.add(bean);
        }
      }
    }
    Collections.sort(result);
    return result;
  }

  /**
   * Ermittle statistik ids.
   *
   * @throws JobException the job exception
   */
  protected void ermittleStatistikIds() throws JobException
  {
    for (XmlBean bean : this.xmlBeans)
    {
      // Ermittle Statistik-Id
      String tmpAmt = bean.getAmt();
      String tmpStatOnlineKey = bean.getStatOnlineKey();
      String key = tmpAmt + "|" + tmpStatOnlineKey;
      Integer statId = this.amtStatOnlineKeys.get(key);
      if (statId == null)
      {
        statId = this.getStatistikIdFromAmtStatOnlineKey(tmpAmt, tmpStatOnlineKey);
        this.amtStatOnlineKeys.put(key, statId);
      }
      if (statId == null)
      {
        statId = 0;
      }
      bean.setStatistikId(statId);
      this.jobBean.statistikId = statId;
    }
  }

  private Integer getStatistikIdFromAmtStatOnlineKey(String amt, String statOnlineKey) throws JobException
  {
    String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_STATONLINEKEY, StringUtil.escapeSqlString(amt), StringUtil.escapeSqlString(statOnlineKey));
    ResultRow row = this.sqlUtil.fetchOne(sql);
    return (row != null ? row.getInt(1) : null);
  }
}