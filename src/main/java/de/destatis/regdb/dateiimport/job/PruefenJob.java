/*
 * @(#)PruefenJob.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job;

import au.com.bytecode.opencsv.CSVReader;
import de.destatis.regdb.JobBean;
import de.destatis.regdb.JobStatus;
import de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob;
import de.destatis.regdb.dateiimport.job.melderkontoimport.MelderkontoImportJob;
import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportJob;
import de.destatis.regdb.dateiimport.job.xmlimport.XmlImportJob;
import de.destatis.regdb.dateiimport.job.xmlimport.XmlValidHandler;
import de.destatis.regdb.db.*;
import de.werum.sis.idev.res.job.JobException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The Class PruefenJob.
 */
public class PruefenJob extends AbstractJob
{

  /**
   * The Constant numberPattern.
   */
  private static final Pattern numberPattern = Pattern.compile("\\d+");

  /**
   * The Constant MSG_PRUEFUNG_FEHLGESCHLAGEN.
   */
  private static final String MSG_PRUEFUNG_FEHLGESCHLAGEN = "Prüfung der Importdatei \"{0}\" fehlgeschlagen";

  /**
   * The Constant MSG_PRUEFUNG_FEHLERFREI.
   */
  private static final String MSG_PRUEFUNG_FEHLERFREI = "Prüfung der Importdatei \"{0}\" mit {1} Sätzen fehlerfrei abgeschlossen";

  /**
   * The Constant SQL_SELECT_ORDNUNGSFELD.
   */
  private static final String SQL_SELECT_ORDNUNGSFELD = "SELECT QUELL_REFERENZ_OF_LAENGE,QUELL_REFERENZ_OF_TYP, QUELL_REFERENZ_KUERZEL FROM quell_referenz_verwaltung WHERE QUELL_REFERENZ_ID = ?";

  /**
   * The Constant SQL_SELECT_OF_ADRESSE.
   */
  private static final String SQL_SELECT_OF_ADRESSE = "SELECT adressen_id FROM adressen WHERE QUELL_REFERENZ_ID = ? AND QUELL_REFERENZ_OF = ?";

  /**
   * The Constant SQL_SELECT_OF_MELDER.
   */
  private static final String SQL_SELECT_OF_MELDER = "SELECT melder_id FROM melder WHERE melder_id = ? AND STATUS != \"LOESCH\"";

  /**
   * The Constant SQL_SELECT_QUELLREFID_IMPORT.
   */
  private static final String SQL_SELECT_QUELLREFID_IMPORT = "SELECT COUNT(*) FROM import_verwaltung WHERE GESAMT_STATUS = \"AKTIV\" AND QUELL_REFERENZ_ID={0} AND IMPORT_VERWALTUNG_ID <> {1}";

  private static final String SQL_SELECT_MELDERKONTO = "SELECT MKTO_ID from melderkonto WHERE MELDUNG_ID = ? AND AMT=? and STATISTIK_ID = ?";

  private static final String SQL_SELECT_MELDERKONTO2 = "SELECT MKTO_ID from melderkonto WHERE MELDUNG_ID = ?";

  /**
   * The Constant MSG_OF_NICHT_NUMERISCH.
   */
  private static final String MSG_OF_NICHT_NUMERISCH = "Ordnungsfeld \"{0}\" ist nicht numerisch bei Zeile {1}";

  /**
   * The Constant MSG_OF_UNGLEICHE_LAENGE.
   */
  private static final String MSG_OF_UNGLEICHE_LAENGE = "Ordnungsfeldlänge {0} ungleich der geforderten Länge {1} bei Zeile {2}";

  /**
   * The Constant MSG_OF_UNGLEICHE_SPALTENANZAHL.
   */
  private static final String MSG_UNGLEICHE_SPALTENANZAHL = "Spaltenzahl {0} ungleich erforderlicher Anzahl {1} bei Zeile {2}";

  /**
   * The Constant MSG_PRUEFUNG_GESTARTET.
   */
  private static final String MSG_PRUEFUNG_GESTARTET = "Prüfung der Importdatei \"{0}\" gestartet...";

  /**
   * The Constant MSG_PRUEFUNG_ABGESCHLOSSEN.
   */
  private static final String MSG_PRUEFUNG_ABGESCHLOSSEN = "Prüfung von {0} Sätzen erfolgreich abgeschlossen";

  private String quellRefName;

  private boolean isNumerisch;

  private Path importDateiPath;

  private String fehler;
  private boolean fehlerLimitNichtErreicht;

  /**
   * Instantiates a new pruefen job.
   *
   * @param jobBean the job Bean
   */
  public PruefenJob(JobBean jobBean)
  {
    super("Pruefen", jobBean);
    this.fehlerLimitNichtErreicht = true;
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
    this.importDateiPath = this.jobBean.getImportdatei()
      .getPath();
    AbstractJob next;
    this.jobBean.getImportdatei().anzahlDatensaetze = 0;
    {
      switch (this.jobBean.getImportdatei().importFormat)
      {
        case IMPORTOHNEZUSATZFELDER:
          next = this.verarbeiteFormatIdev(18);
          break;
        case IMPORTMITZUSATZFELDER:
          next = this.verarbeiteFormatIdev(28);
          break;
        case REGISTERIMPORT:
          next = this.verarbeiteRegisterImport();
          break;
        case VORBELEGUNGSIMPORT:
        case VORBELEGUNGDOWNLOADIMPORT:
          next = this.verarbeiteFormatVorbelegungen();
          break;
        case IMPORTMITANSPRECHPARTNER:
          next = this.verarbeiteFormatIdev(16);
          break;
        case XMLIMPORT:
          next = this.verarbeiteXmlImport();
          break;
        case MELDERKONTOIMPORT:
          next = this.verarbeiteMelderkontoImport();
          break;
        default:
          throw new JobException("Ungueltiges Format:" + this.jobBean.getImportdatei().importFormat.toString());
      }
    }
    return next;
  }

  /**
   * Verarbeite register import.
   *
   * @return the abstract job
   * @throws JobException the job exception
   */
  private AbstractJob verarbeiteRegisterImport() throws JobException
  {
    this.pruefeRegisterdaten();
    return pruefeSimulation(new RegisterImportJob(this.jobBean));
  }

  /**
   * Verarbeite xml import.
   *
   * @return the abstract job
   * @throws JobException the job exception
   */
  private AbstractJob verarbeiteXmlImport() throws JobException
  {
    this.pruefeXmlDaten();
    this.jobBean.loescheDaten = false; // Wird eh ermittelt und bei true würden erst die Auswirkungen ermittlet
    return pruefeSimulation(new XmlImportJob(this.jobBean));
  }

  /**
   * Verarbeite format idev.
   *
   * @param anzahlSpalten the anzahl spalten
   * @return the abstract job
   * @throws JobException the job exception
   */
  private AbstractJob verarbeiteFormatIdev(int anzahlSpalten) throws JobException
  {
    this.pruefeFehlerhaftenAdressbestand();
    this.pruefeOrdnungsfelder(anzahlSpalten);
    return pruefeSimulation(new AdressImportJob(this.jobBean));
  }

  private AbstractJob pruefeSimulation(AbstractJob nextJob) throws JobException
  {
    if (!this.jobBean.getFormatPruefung().fehlerfrei)
    {
      return this.fehlerAufraeumenJob();
    }
    String msg = MessageFormat.format(MSG_PRUEFUNG_FEHLERFREI, this.jobBean.getImportdatei().originalDateiname, this.jobBean.getImportdatei().anzahlDatensaetze);
    this.log.info(msg);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, MessageFormat.format(MSG_PRUEFUNG_ABGESCHLOSSEN, this.jobBean.getImportdatei().anzahlDatensaetze));
    if (this.jobBean.getSimulation().importSimulieren)
    {
      LoeschUtil util = new LoeschUtil(sqlUtil);
      util.loescheStandardWerte(this.jobBean.amt, this.jobBean.statistikId, this.jobBean.sachbearbeiterId);
      util.speichereStandardwerte(this.jobBean
        .getImportdatei().originalDateiname, this.jobBean.amt, this.jobBean.statistikId, this.jobBean.jobId, this.jobBean.sachbearbeiterId, this.jobBean.zeitpunktEintrag);
      return new AuswirkungenJob(this.jobBean);
    }
    if (this.jobBean.loescheDaten)
    {
      return new AuswirkungenJob(this.jobBean);
    }
    return nextJob;
  }

  /**
   * Pruefe fehlerhaften adressbestand.
   *
   * @throws JobException the job exception
   */
  private void pruefeFehlerhaftenAdressbestand() throws JobException
  {
    boolean adressBestandOk = this.pruefeAdressbestand(this.jobBean.quellReferenzId);
    if (!adressBestandOk)
    {
      this.jobBean.getFormatPruefung()
        .addFehler("Adressbestand mit der ID " + this.jobBean.quellReferenzId + " existiert nicht!");
    }
  }

  /**
   * Fehler aufraeumen job.
   *
   * @return the abstract job
   */
  private AbstractJob fehlerAufraeumenJob()
  {
    this.log.info(MessageFormat.format(MSG_PRUEFUNG_FEHLGESCHLAGEN, this.jobBean.getImportdatei().originalDateiname));
    this.fehler = (this.jobBean.getFormatPruefung().anzahlFehler == 1) ? this.jobBean.getFormatPruefung()
      .getError()
      .get(0) : this.jobBean.getFormatPruefung().anzahlFehler + " Fehler siehe Protokoll";
    for (String err : this.jobBean.getFormatPruefung()
      .getError())
    {
      this.log.info(err);
    }
    this.jobBean.setStatusAndInfo(JobStatus.FEHLER, this.fehler);
    return null;
  }

  /**
   * Pruefe adressbestand.
   *
   * @param quellRefId the quell ref id
   * @return true, if successful
   * @throws JobException the job exception
   */
  private boolean pruefeAdressbestand(Integer quellRefId) throws JobException
  {
    try (PreparedSelect ps = sqlUtil.createPreparedSelect(SQL_SELECT_ORDNUNGSFELD))
    {
      ps.addValue(quellRefId);
      ResultRow rs = ps.fetchOne();
      if (rs != null)
      {
        this.jobBean.getAdressen().ordnungsfeldLaenge = rs.getInt("QUELL_REFERENZ_OF_LAENGE");
        this.quellRefName = rs.getString("QUELL_REFERENZ_KUERZEL");
        this.jobBean.getAdressen().ordnungsfeldTyp = rs.getString("QUELL_REFERENZ_OF_TYP");
        this.isNumerisch = "NOV".equals(this.jobBean.getAdressen().ordnungsfeldTyp);
        return true;
      }
    }
    return false;
  }

  /**
   * Verarbeite format vorbelegungen.
   *
   * @return the abstract job
   * @throws JobException the job exception
   */
  private AbstractJob verarbeiteFormatVorbelegungen() throws JobException
  {
    this.pruefeFehlerhaftenAdressbestand();
    this.pruefeOrdnungsfelderVorbelegungen();
    if (!this.jobBean.getFormatPruefung().fehlerfrei)
    {
      return this.fehlerAufraeumenJob();
    }
    String msg = MessageFormat.format(MSG_PRUEFUNG_FEHLERFREI, this.jobBean.getImportdatei().originalDateiname, this.jobBean.getImportdatei().anzahlDatensaetze);
    this.log.info(msg);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, MessageFormat.format(MSG_PRUEFUNG_ABGESCHLOSSEN, this.jobBean.getImportdatei().anzahlDatensaetze));
    return new VorbelegungsImportJob(this.jobBean);
  }

  /**
   * Pruefe ordnungsfelder vorbelegungen.
   *
   * @throws JobException the job exception
   */
  private void pruefeOrdnungsfelderVorbelegungen() throws JobException
  {
    try (BufferedReader br = Files.newBufferedReader(this.importDateiPath, this.jobBean.getImportdatei()
      .getCharset()))
    {
      FileUtil.ignoreUtfBom(br, this.jobBean.getImportdatei().zeichensatz);
      try (PreparedSelect psAdresse = sqlUtil.createPreparedSelect(SQL_SELECT_OF_ADRESSE); PreparedSelect psMelder = sqlUtil.createPreparedSelect(SQL_SELECT_OF_MELDER);
           CSVReader reader = new CSVReader(br, ';', '"'))
      {
        String[] cols;
        while ((cols = reader.readNext()) != null && this.fehlerLimitNichtErreicht)
        {
          pruefeOrdnungsfelderVorbelegungenSatz(cols, psAdresse, psMelder);
        }
      }
    } catch (IOException e)
    {
      throw new JobException(e.getMessage());
    }
    // Pruef auf laufenden Import
    this.checkRunningImport(this.jobBean.quellReferenzId);
  }

  private AbstractJob verarbeiteMelderkontoImport() throws JobException
  {
    pruefeFehlerhaftenAdressbestand();
    this.pruefeMelderkontoDaten();
    if (!this.jobBean.getFormatPruefung().fehlerfrei)
    {
      return this.fehlerAufraeumenJob();
    }
    String msg = MessageFormat.format(MSG_PRUEFUNG_FEHLERFREI, this.jobBean.getImportdatei().originalDateiname, this.jobBean.getImportdatei().anzahlDatensaetze);
    this.log.info(msg);
    this.jobBean.setStatusAndInfo(JobStatus.AKTIV, MessageFormat.format(MSG_PRUEFUNG_ABGESCHLOSSEN, this.jobBean.getImportdatei().anzahlDatensaetze));
    return new MelderkontoImportJob(this.jobBean);
  }

  private void pruefeMelderkontoDaten() throws JobException
  {
    try (BufferedReader br = Files.newBufferedReader(this.importDateiPath, this.jobBean.getImportdatei()
      .getCharset()); PreparedSelect psMelderkonto1 = sqlUtil.createPreparedSelect(SQL_SELECT_MELDERKONTO); PreparedSelect psMelderkonto2 = sqlUtil.createPreparedSelect(SQL_SELECT_MELDERKONTO2))
    {
      FileUtil.ignoreUtfBom(br, this.jobBean.getImportdatei().zeichensatz);
      String quellReferenzId = null;
      try (CSVReader reader = new CSVReader(br, ';', '"'))
      {
        String[] cols;
        while ((cols = reader.readNext()) != null && this.fehlerLimitNichtErreicht)
        {
          this.jobBean.getImportdatei().anzahlDatensaetze++;
          if (cols.length < 17 || cols.length > 18)
          {
            // Spaltenanzahl stimmt nicht
            this.fehler = MessageFormat.format(MSG_UNGLEICHE_SPALTENANZAHL, cols.length, 18, this.jobBean.getImportdatei().anzahlDatensaetze);
            this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
              .addFehler(this.fehler);
          }
          if (quellReferenzId == null)
            quellReferenzId = cols[3];
          if (!Objects.equals(quellReferenzId, cols[3]))
          {
            this.fehler = "Die Importdatei enthält Daten zu unterschiedlichen Adressbeständen";
            this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
              .addFehler(this.fehler);
            quellReferenzId = cols[3];
          }
          String meldungsId = cols[0].trim();
          if (meldungsId.isEmpty())
            meldungsId = "0";
          if (!("0".equals(meldungsId)))
          {
            // Meldungs ID in melderkonto vorhanden ?
            psMelderkonto2.addValue(meldungsId);
            ResultRow rs = psMelderkonto2.fetchOne();
            if (rs == null)
            {
              this.fehler = MessageFormat.format("Meldungs-ID {0} existiert nicht im Tabelle melderkonto!", meldungsId);
              this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
                .addFehler(this.fehler);
            }

            psMelderkonto1.addValue(meldungsId); // MeldungsId
            psMelderkonto1.addValue(cols[2]); // AMT
            psMelderkonto1.addValue(cols[1]); // StatistikId
            rs = psMelderkonto1.fetchOne();
            if (rs == null)
            {
              this.fehler = MessageFormat.format("Kombination aus MeldungsID {0}, Amt {1} und StatistikId {2} existiert nicht!", meldungsId, cols[2], cols[1]);
              this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
                .addFehler(this.fehler);
            }
          }
        }
      }
    } catch (IOException e)
    {
      throw new JobException(e.getMessage());
    }
    // Pruefe auf laufenden Import
    this.checkRunningImport(this.jobBean.quellReferenzId);
  }

  private void pruefeOrdnungsfelderVorbelegungenSatz(String[] cols, PreparedSelect psAdresse, PreparedSelect psMelder) throws JobException
  {
    this.jobBean.getImportdatei().anzahlDatensaetze++;
    if (cols.length < 4)
    {
      // Spaltenanzahl stimmt nicht
      this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
        .addFehler("Spaltenzahl muss mindestens 4 betragen, nicht erfüllt bei Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze);
    }
    String of = cols[0];
    // Pruefe ob OF in Adressbestand ist
    psAdresse.addValue(this.jobBean.quellReferenzId);
    psAdresse.addValue(of);
    String adressenId = null;
    ResultRow row = psAdresse.fetchOne();
    {
      if (row != null)
      {
        adressenId = row.getString(1);
      }
      if (adressenId == null)
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler("Ordnungsfeld '" + of + "' nicht in Adressbestand gefunden bei Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze);
      }
    }
    // Bei Feldnamen, die mit _datei enden, auf Leerzeichen Pruefen
    for (int i = 4; i < cols.length; i += 2)
    {
      String feldname = cols[i];
      String feldinhalt = (i + 1 < cols.length) ? cols[i + 1].trim() : "";
      if (feldname.endsWith("_datei") && feldinhalt.contains(" "))
      {
        this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
          .addFehler("Der Dateiname '" + feldinhalt + "' enthält Leerzeichen in Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze);
      }
    }
    // Pruefe Melder ID
    String melderId = cols[2].trim();
    if (!"0".equals(melderId) && melderId.length() > 0)
    {
      psMelder.addValue(melderId);
      ResultRow mrow = psMelder.fetchOne();
      {
        if (mrow == null)
        {
          this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
            .addFehler("Die Melder-ID '" + melderId + "' existiert nicht in Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze);
        }
      }
    }
  }

  /**
   * Pruefe ordnungsfelder.
   *
   * @param anzahlSpalten the anzahl spalten
   * @throws JobException the job exception
   */
  private void pruefeOrdnungsfelder(int anzahlSpalten) throws JobException
  {
    try (BufferedReader br = Files.newBufferedReader(this.importDateiPath, this.jobBean.getImportdatei()
      .getCharset()))
    {
      FileUtil.ignoreUtfBom(br, this.jobBean.getImportdatei().zeichensatz);
      try (CSVReader reader = new CSVReader(br, ';', '"'))
      {
        String[] cols;
        while ((cols = reader.readNext()) != null && this.fehlerLimitNichtErreicht)
        {
          this.jobBean.getImportdatei().anzahlDatensaetze++;
          if (cols.length < anzahlSpalten || cols.length > anzahlSpalten + 1)
          {
            // Spaltenanzahl stimmt nicht
            this.fehler = MessageFormat.format(MSG_UNGLEICHE_SPALTENANZAHL, cols.length, anzahlSpalten, this.jobBean.getImportdatei().anzahlDatensaetze);
            this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
              .addFehler(this.fehler);
          }
          String of = cols[0];
          // Laengenpruefung des OF
          if (of.length() != this.jobBean.getAdressen().ordnungsfeldLaenge)
          {
            this.fehler = MessageFormat.format(MSG_OF_UNGLEICHE_LAENGE, of.length(), this.jobBean.getAdressen().ordnungsfeldLaenge, this.jobBean.getImportdatei().anzahlDatensaetze);
            this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
              .addFehler(this.fehler);
          }
          // Typpruefung OF wenn Typ = NOV
          if (this.isNumerisch && !numberPattern.matcher(of)
            .matches())
          {
            this.fehler = MessageFormat.format(MSG_OF_NICHT_NUMERISCH, of, this.jobBean.getImportdatei().anzahlDatensaetze);
            this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
              .addFehler(this.fehler);
          }
          // Alles okay, nehme Ordnungsfeld auf
          if (this.jobBean.getFormatPruefung().fehlerfrei)
          {
            this.jobBean.getAdressen()
              .getOrdnungsfelder()
              .add(of);
          }
        }
      }
    } catch (IOException e)
    {
      throw new JobException(e.getMessage());
    }

    // Pruef auf laufenden Import

    this.checkRunningImport(this.jobBean.quellReferenzId);
  }

  /**
   * Pruefe registerdaten.
   *
   * @throws JobException the job exception
   */
  private void pruefeRegisterdaten() throws JobException
  {
    HashMap<String, Integer> amtStatOnlineKeys = new HashMap<>();
    HashSet<String> amtStatistikBzr = new HashSet<>();
    Integer quellReferenzId = null;
    try (BufferedReader br = Files.newBufferedReader(this.importDateiPath, this.jobBean.getImportdatei()
      .getCharset()))
    {
      FileUtil.ignoreUtfBom(br, this.jobBean.getImportdatei().zeichensatz);
      String row;
      while ((row = br.readLine()) != null && this.fehlerLimitNichtErreicht)
      {
        this.jobBean.getImportdatei().anzahlDatensaetze++;
        if (row.length() < 50 || row.length() > 440)
        {
          this.fehler = "Zeilenlänge (" + row.length() + ") passt nicht in Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze;
          this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
            .addFehler(this.fehler);
        }
        String statOnlineKey = StringUtil.substring(row, 0, 10)
          .trim();
        if (statOnlineKey.isEmpty())
        {
          this.fehler = "StatOnline-Key ist leer in Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze;
          this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
            .addFehler(this.fehler);
        }
        String of = StringUtil.substring(row, 10, 20)
          .trim();
        if (of.isEmpty())
        {
          this.fehler = "Quell-Referenz-Of ist leer in Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze;
          this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
            .addFehler(this.fehler);

        }
        String tmpAmt = StringUtil.substring(row, 20, 22)
          .trim();
        if (tmpAmt.isEmpty())
        {
          this.fehler = "Amt ist leer in Zeile " + this.jobBean.getImportdatei().anzahlDatensaetze;
          this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
            .addFehler(this.fehler);
        } else
        {
          this.jobBean.amt = tmpAmt;
        }
        // Pruefe auf passenden STAT-ONLINE-KEY und ermittle dabei die StatistikId und quellReferenzId
        String key = tmpAmt + "|" + statOnlineKey;
        Integer statistikId = amtStatOnlineKeys.get(key);
        // Neu aufnehmen?
        if (statistikId == null)
        {
          Object ergebnis = this.checkAmtStatOnlineKey(tmpAmt, statOnlineKey);
          if (ergebnis instanceof Integer[])
          {
            Integer[] werte = (Integer[]) ergebnis;
            statistikId = werte[0];
            amtStatOnlineKeys.put(key, statistikId);
            if (quellReferenzId == null)
            {
              quellReferenzId = werte[1];
            } else if (!quellReferenzId.equals(werte[1]))
            {
              this.fehler = "Die Importdatei enthält Daten zu unterschiedlichen Adressbeständen";
              this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
                .addFehler(this.fehler);
            }
            this.jobBean.quellReferenzId = quellReferenzId;
          } else
          {
            this.fehler = String.valueOf(ergebnis);
            this.fehlerLimitNichtErreicht = this.jobBean.getFormatPruefung()
              .addFehler(this.fehler);
          }
        } else
        {
          this.jobBean.statistikId = statistikId;
        }
        // Pruefe Erhebung
        String bzr = StringUtil.substring(row, 22, 28)
          .trim();
        key = tmpAmt + "|" + statistikId + "|" + bzr;
        if (!amtStatistikBzr.contains(key))
        {
          this.checkErhebung(tmpAmt, statistikId, bzr);
          amtStatistikBzr.add(key);
        }
        // Alles OKAy, nehme Ordnungsfeld in Datei auf
        if (this.jobBean.getFormatPruefung().fehlerfrei)
        {
          this.jobBean.getAdressen()
            .getOrdnungsfelder()
            .add(of);
        }

      }
    } catch (IOException e)
    {
      throw new JobException(e.getMessage());
    }
    // Fehlerfrei, hole Daten zu Adressbestand
    this.pruefeAdressbestand(quellReferenzId);

    // Pruef auf laufenden Import
    this.checkRunningImport(quellReferenzId);
  }

  /**
   * Pruefe xml daten.
   *
   * @throws JobException the job exception
   */
  private void pruefeXmlDaten() throws JobException
  {

    try (InputStream is = Files.newInputStream(this.importDateiPath, StandardOpenOption.READ))
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      XmlValidHandler handler = new XmlValidHandler(sqlUtil);
      factory.setSchema(handler.getSchema());
      factory.setValidating(true); // Soll auf true
      SAXParser saxParser = factory.newSAXParser();
      // saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
      // saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
      saxParser.parse(is, handler);
      this.jobBean.statistikId = StringUtil.getInt(handler.getStatistikId());
      this.jobBean.quellReferenzId = handler.getQuellReferenzId();
      this.jobBean.getImportdatei().anzahlDatensaetze = handler.getAnzahlDatensaetze();
      // Pruef auf laufenden Import
      this.checkRunningImport(this.jobBean.quellReferenzId);
    } catch (IOException | ParserConfigurationException | SAXException e)
    {
      throw new JobException(e.getMessage());
    }
  }

  /**
   * Check running import.
   *
   * @param quellReferenzId the quell referenz id
   */
  private void checkRunningImport(Integer quellReferenzId) throws JobException
  {
    String sql = MessageFormat.format(SQL_SELECT_QUELLREFID_IMPORT, "" + quellReferenzId, "" + this.jobBean.jobId);
    ResultRow rs = sqlUtil.fetchOne(sql);
    if (rs != null && rs.getInt(1) > 0)
    {
      this.fehler = "Es läuft bereits ein Import auf dem Adressbestand ";
      if (this.quellRefName == null)
      {
        this.fehler += "mit der ID " + quellReferenzId;
      } else
      {
        this.fehler += "auf dem Adressbestand '" + this.quellRefName + "', ID " + quellReferenzId;
      }
      this.jobBean.getFormatPruefung()
        .addFehler(this.fehler);
    }
  }

  /**
   * Check amt stat online key.
   *
   * @param amt           the amt
   * @param statOnlineKey the stat online key
   * @return the object
   */
  private Object checkAmtStatOnlineKey(String amt, String statOnlineKey) throws JobException
  {
    String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_STATONLINEKEY, StringUtil.escapeSqlString(amt), StringUtil.escapeSqlString(statOnlineKey));
    ResultRow rs = sqlUtil.fetchOne(sql);
    if (rs != null)
    {
      int statistikId = rs.getInt(1);
      int quellRefId = rs.getInt(2);
      Integer[] ergebnis = new Integer[2];
      ergebnis[0] = statistikId;
      ergebnis[1] = quellRefId;
      return ergebnis;
    }
    return "Kombination aus Amt (" + amt + ") und StatOnlineKey (" + statOnlineKey + ") existiert nicht!";

  }

  /**
   * Check erhebung.
   *
   * @param amt         the amt
   * @param statistikId the statistik id
   * @param bzr         the bzr
   */
  private void checkErhebung(String amt, Integer statistikId, String bzr) throws JobException
  {
    String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_BZR, StringUtil.escapeSqlString(amt), "" + statistikId, StringUtil.escapeSqlString(bzr));
    ResultRow rs = sqlUtil.fetchOne(sql);
    if (rs == null)
    {
      this.jobBean.getFormatPruefung()
        .addFehler("Keine Erhebung für Amt (" + amt + "), Statistik-Id  (" + statistikId + ") und Bzr (" + bzr + ") gefunden!");
    }
  }

}
