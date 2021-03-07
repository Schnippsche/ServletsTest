package de.destatis.regdb;

import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.dateiimport.PropertyValues;
import de.destatis.regdb.db.StringUtil;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * The Class JobBean.
 * Die Setter muessen drin bleiben, da die Daten sonst per JAXB nicht alle korrekt geladen werden!
 */
@XmlRootElement
public class JobBean
{

  protected final LoggerIfc log = Logger.getInstance().getLogger(JobBean.class);
  private Adressen adressen;
  @XmlAttribute
  public String amt;
  private FormatPruefung formatPruefung;
  @XmlAttribute
  public String berichtszeitraum;
  private Firmen firmen;
  private Importdatei importdatei;
  @XmlAttribute
  public boolean loescheDaten;
  @XmlAttribute
  public boolean versendeMails;
  @XmlAttribute
  public int jobId;
  /**
   * maximale Grosse des Importblocks
   */
  @XmlElement
  public int importBlockGroesse;
  /**
   * maximale Grösse des Loeschblocks
   */
  @XmlElement
  public int loeschBlockGroesse;
  @XmlElement
  public int loeschOffset;

  private Melder melder;
  private Melderkonto melderkonto;
  private Simulation simulation;
  @XmlAttribute
  public int quellReferenzId;
  @XmlAttribute
  public String quellReferenzName;
  @XmlAttribute
  public boolean quellReferenzNumerisch;
  @XmlAttribute
  public int sachbearbeiterId;
  @XmlElement
  public String sachbearbeiterLand;
  @XmlElement
  public String sachbearbeiterKennung;
  @XmlTransient
  public String sachbearbeiterPasswort;
  @XmlAttribute
  public int statistikId;
  private JobStatus status;
  private String info;
  private Vorbelegungen vorbelegungen;
  @XmlAttribute
  public String zeitpunktAenderung;
  @XmlAttribute
  public String zeitpunktEintrag;

  private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  /**
   * Instantiates a new job bean.
   */
  public JobBean()
  {
    this.importBlockGroesse = 2000;
    this.loeschBlockGroesse = 5000;
    this.sachbearbeiterLand = "";
    this.berichtszeitraum = "";
    this.loescheDaten = false;
    this.amt = "";
    this.zeitpunktEintrag = getCurrentZeitpunkt();
  }

  public void setStatusAndInfo(JobStatus status, String info)
  {
    this.status = status;
    this.info = info;
    saveBean();
  }

  @XmlAttribute
  public JobStatus getStatus()
  {
    return this.status;
  }

  @XmlAttribute
  public String getInfo()
  {
    return this.info;
  }

  public void setzeWerteAusProperties(Properties prop)
  {
    this.log.debug("Setze Werte aus Properties " + prop);
    this.jobId = StringUtil.getInt(getString(prop, PropertyValues.MAINJOBID));

    if (isSet(prop, PropertyValues.AMT))
    {
      this.amt = getString(prop, PropertyValues.AMT);
    }
    if (isSet(prop, PropertyValues.QUELLREFID))
    {
      this.quellReferenzId = StringUtil.getInt(getString(prop, PropertyValues.QUELLREFID));
    }
    if (isSet(prop, PropertyValues.STATISTIKID))
    {
      this.statistikId = StringUtil.getInt(getString(prop, PropertyValues.STATISTIKID));
    }
    if (isSet(prop, PropertyValues.SBLAND))
    {
      this.sachbearbeiterLand = getString(prop, PropertyValues.SBLAND);
    }
    if (isSet(prop, PropertyValues.ORIGINALDATEINAME))
    {
      getImportdatei().originalDateiname = getString(prop, PropertyValues.ORIGINALDATEINAME);
    }
    if (isSet(prop, PropertyValues.IMPORTFORMAT))
    {
      String format = getString(prop, PropertyValues.IMPORTFORMAT);
      for (ImportFormat enumFmt : ImportFormat.values())
      {
        if (enumFmt.name().equals(format))
        {
          getImportdatei().importFormat = enumFmt;
        }
      }
    }
    if (isSet(prop, PropertyValues.ZEICHENSATZ))
    {
      getImportdatei().zeichensatz = getString(prop, PropertyValues.ZEICHENSATZ);
    }
    if (isSet(prop, PropertyValues.ZEITPUNKT))
    {
      this.zeitpunktEintrag = getString(prop, PropertyValues.ZEITPUNKT);
    }
    if (isSet(prop, PropertyValues.ORDNUNGSFELDLAENGE))
    {
      this.getAdressen().ordnungsfeldLaenge = StringUtil.getInt(getString(prop, PropertyValues.ORDNUNGSFELDLAENGE));
    }
    if (isSet(prop, PropertyValues.ORDNUNGSFELDTYP))
    {
      this.getAdressen().ordnungsfeldTyp = getString(prop, PropertyValues.ORDNUNGSFELDTYP);
    }
    if (isSet(prop, PropertyValues.ADRESSENICHTAENDERBAR))
    {
      this.getAdressen().nichtAenderbar = getBoolean(prop, PropertyValues.ADRESSENICHTAENDERBAR);
    }
    if (isSet(prop, PropertyValues.PASSWORTNICHTAENDERBAR))
    {
      getMelder().passwortUnveraenderbar = getBoolean(prop, PropertyValues.PASSWORTNICHTAENDERBAR);
    }
    if (isSet(prop, PropertyValues.MELDERZUSAMMENFUEHRBAR))
    {
      getMelder().zusammenfuehrbar = getBoolean(prop, PropertyValues.MELDERZUSAMMENFUEHRBAR);
    }
    if (isSet(prop, PropertyValues.NEUEPASSWOERTERGENERIEREN))
    {
      getMelder().neuePasswoerterGenerieren = getBoolean(prop, PropertyValues.NEUEPASSWOERTERGENERIEREN);
    }
    if (isSet(prop, PropertyValues.UPDATEFIRMA))
    {
      this.getFirmen().aktualisierungErlaubt = getBoolean(prop, PropertyValues.UPDATEFIRMA);
    }
    if (isSet(prop, PropertyValues.UPDATEMELDER))
    {
      getMelder().aktualisierungErlaubt = getBoolean(prop, PropertyValues.UPDATEMELDER);
    }
    if (isSet(prop, PropertyValues.CREATEFIRMA))
    {
      this.getFirmen().neuanlageErlaubt = getBoolean(prop, PropertyValues.CREATEFIRMA);
    }
    if (isSet(prop, PropertyValues.CREATEMELDER))
    {
      getMelder().neuanlageErlaubt = getBoolean(prop, PropertyValues.CREATEMELDER);
    }
    if (isSet(prop, PropertyValues.CREATELEEREPARTNER))
    {
      getMelder().erzeugeLeereAnsprechpartner = getBoolean(prop, PropertyValues.CREATELEEREPARTNER);
    }
    if (isSet(prop, PropertyValues.CREATEVORBELEGUNGEN))
    {
      getMelder().erzeugeVorbelegungenStattAnsprechpartner = getBoolean(prop, PropertyValues.CREATEVORBELEGUNGEN);
    }
    if (isSet(prop, PropertyValues.ALLOWLOESCH))
    {
      this.loescheDaten = getBoolean(prop, PropertyValues.ALLOWLOESCH);
    }
    if (isSet(prop, PropertyValues.BZR))
    {
      this.berichtszeitraum = getString(prop, PropertyValues.BZR);
    }
    if (isSet(prop, PropertyValues.EINTRAGINFOSCHREIBEN))
    {
      this.getVorbelegungen().eintragInfoschreiben = getBoolean(prop, PropertyValues.EINTRAGINFOSCHREIBEN);
    }
    if (isSet(prop, PropertyValues.MELDERSPERRE))
    {
      getMelder().gesperrt = getBoolean(prop, PropertyValues.MELDERSPERRE);
    }
    if (isSet(prop, PropertyValues.DATEINURPRUEFEN))
    {
      getSimulation().importSimulieren = getBoolean(prop, PropertyValues.DATEINURPRUEFEN);
    }
    if (isSet(prop, PropertyValues.MAILVERSAND))
    {
      this.versendeMails = getBoolean(prop, PropertyValues.MAILVERSAND);
    }
    if (isSet(prop, PropertyValues.SBKENNUNG))
    {
      this.sachbearbeiterKennung = getString(prop, PropertyValues.SBKENNUNG);
    }
    if (isSet(prop, PropertyValues.SBPASSWORT))
    {
      this.sachbearbeiterPasswort = getString(prop, PropertyValues.SBPASSWORT);
    }
    this.log.debug("SetzeWerte korrekt beendet");
  }

  @XmlElement
  public FormatPruefung getFormatPruefung()
  {
    if (this.formatPruefung == null)
    {
      this.formatPruefung = new FormatPruefung();
    }
    return this.formatPruefung;
  }

  public void setFormatPruefung(FormatPruefung formatPruefung)
  {
    this.formatPruefung = formatPruefung;
  }

  @XmlElement
  public Adressen getAdressen()
  {
    if (this.adressen == null)
    {
      this.adressen = new Adressen();
    }
    return this.adressen;
  }

  public void setAdressen(Adressen adressen)
  {
    this.adressen = adressen;
  }

  @XmlElement
  public Firmen getFirmen()
  {
    if (this.firmen == null)
    {
      this.firmen = new Firmen();
    }
    return this.firmen;
  }

  public void setFirmen(Firmen firmen)
  {
    this.firmen = firmen;
  }

  @XmlElement
  public Importdatei getImportdatei()
  {
    if (this.importdatei == null)
    {
      this.importdatei = new Importdatei();
    }
    return this.importdatei;
  }

  public void setImportdatei(Importdatei importdatei)
  {
    this.importdatei = importdatei;
  }

  @XmlElement
  public Melder getMelder()
  {
    if (this.melder == null)
    {
      this.melder = new Melder();
    }
    return this.melder;
  }

  public void setMelder(Melder melder)
  {
    this.melder = melder;
  }

  @XmlElement
  public Melderkonto getMelderkonto()
  {
    if (this.melderkonto == null)
    {
      this.melderkonto = new Melderkonto();
    }
    return this.melderkonto;
  }

  public void setMelderkonto(Melderkonto melderkonto)
  {
    this.melderkonto = melderkonto;
  }

  @XmlElement
  public Simulation getSimulation()
  {
    if (this.simulation == null)
    {
      this.simulation = new Simulation();
    }
    return this.simulation;
  }

  public void setSimulation(Simulation simulation)
  {
    this.simulation = simulation;
  }

  @XmlElement
  public Vorbelegungen getVorbelegungen()
  {
    if (this.vorbelegungen == null)
    {
      this.vorbelegungen = new Vorbelegungen();
    }
    return this.vorbelegungen;
  }

  /**
   * @return liefert sachbearbeiterKennung
   */
  public String getSachbearbeiterKennung()
  {
    return this.sachbearbeiterKennung;
  }

  public void setVorbelegungen(Vorbelegungen vorbelegungen)
  {
    this.vorbelegungen = vorbelegungen;
  }

  private String getString(Properties prop, PropertyValues key)
  {
    return prop.getProperty(key.toString());
  }

  private boolean getBoolean(Properties prop, PropertyValues key)
  {
    return Boolean.parseBoolean(prop.getProperty(key.toString()));
  }

  private boolean isSet(Properties prop, PropertyValues key)
  {
    return prop.containsKey(key.toString());
  }

  private void saveBean()
  {
    String verzeichnis = getImportdatei().importVerzeichnis;
    this.log.debug("SaveBean, Dir=" + verzeichnis);
    if (verzeichnis == null)
    {
      verzeichnis = MelderDatenService.getInstance().getDateiImportDir();
    }

    String dateiname = this.jobId + ".xml";
    Path destination = Paths.get(verzeichnis).resolve(dateiname);
    File file = destination.toFile();
    this.zeitpunktAenderung = this.getCurrentZeitpunkt();
    if (destination.getParent().toFile().exists())
    {
      try
      {
        this.log.debug("Speichere Bean in Datei " + file.toString());
        JAXBContext jc = JAXBContext.newInstance(JobBean.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(this, file);
      }
      catch (Exception e)
      {
        this.log.error(e.getMessage(), e);
      }
    }
    else
    {
      this.log.info("Verzeichnis zum Speichern der Bean existiert nicht:" + verzeichnis);
    }
  }

  public String getCurrentZeitpunkt()
  {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_PATTERN));
  }

  @Override
  public String toString()
  {
    return "JobBean [amt=" + this.amt + ", berichtszeitraum=" + this.berichtszeitraum + ", importdatei=" + this.importdatei + ", loescheDaten=" + this.loescheDaten + ", jobId=" + this.jobId + ", importBlockGroesse=" + this.importBlockGroesse + ", loeschBlockGroesse=" + this.loeschBlockGroesse + ", loeschOffset=" + this.loeschOffset + ", quellReferenzId=" + this.quellReferenzId + ", sachbearbeiterId=" + this.sachbearbeiterId + ", sachbearbeiterLand=" + this.sachbearbeiterLand + ", statistikId=" + this.statistikId + ", info=" + this.info + ", zeitpunktAenderung=" + this.zeitpunktAenderung + ", zeitpunktEintrag=" + this.zeitpunktEintrag + "]";
  }

}
