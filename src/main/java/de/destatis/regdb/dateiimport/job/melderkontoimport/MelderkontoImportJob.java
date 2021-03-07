package de.destatis.regdb.dateiimport.job.melderkontoimport;

import de.destatis.regdb.DesEncrypter;
import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.AbstractImportJob;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.reader.SegmentedCsvFileReader;
import de.destatis.regdb.db.*;
import de.destatis.regdb.servlets.RegDBGeneralHttpServlet;
import de.werum.sis.idev.intern.actions.util.MelderkontoDateiDienst;
import de.werum.sis.idev.intern.actions.util.MelderkontoDateiStatus;
import de.werum.sis.idev.res.job.JobException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The type Melderkonto import job.
 *
 * @author Toengi -S
 */
public class MelderkontoImportJob extends AbstractImportJob
{

  private static final String SQL_SELECT_ADRESSEN = "SELECT ADRESSEN_ID FROM adressen WHERE QUELL_REFERENZ_OF =? AND QUELL_REFERENZ_ID=? AND STATUS != \"LOESCH\"";
  private static final String SQL_SELECT_FIRMEN = "SELECT FIRMEN_ID FROM firmen_adressen WHERE ADRESSEN_ID=? AND STATUS != \"LOESCH\"";
  private static final String SQL_INSERT_MELDERKONTO = "INSERT INTO melderkonto SET MELDEART='DATEI',FIRMEN_ID=?,STATISTIK_ID=?,AMT=?,QUELL_REFERENZ_OF=?,QUELL_REFERENZ_INT=?, BZR=?,MKF1=?, MKF2=?, MKF3=?, MKF4=?, MKF5=?, MKF6=?, MKF7=?, MKF8=?, MKF9=?, MKF10=?, SACHBEARBEITER_ID=?,STATUS=?,ZEITPUNKT_EINTRAG=?";
  private static final String SQL_UPDATE_MELDERKONTO = "UPDATE melderkonto SET MKF1=?, MKF2=?, MKF3=?, MKF4=?, MKF5=?, MKF6=?, MKF7=?, MKF8=?, MKF9=?, MKF10=?, SACHBEARBEITER_ID=?,STATUS=?,ZEITPUNKT_AENDERUNG=? WHERE MKTO_ID=?";
  private static final String SQL_SELECT_MELDERKONTOIDS = "SELECT MKTO_ID FROM melderkonto WHERE MELDUNG_ID = ? AND STATUS != \"LOESCH\"";
  private Properties verzeichnisProperties;
  private String importVerzeichnis;
  private MelderkontoDateiDienst mktoDateiDienst;
  private ArrayList<MelderkontoImportBean> melderKontoImportBeans;

  /**
   * Instantiates a new Melderkonto import job.
   *
   * @param jobBean the job bean
   */
  public MelderkontoImportJob(JobBean jobBean)
  {
    super("ImportMelderkonto", jobBean);
  }

  @Override
  protected void doBeforeFirstImport() throws JobException
  {
    super.doBeforeFirstImport();
    // Zusatzdaten entpacken
    this.importVerzeichnis = this.jobBean.getImportdatei().importVerzeichnis;
    Path destination = Paths.get(this.importVerzeichnis);
    File uploadFile = Paths.get(this.importVerzeichnis, "dateiupload.zip").toFile();
    if (uploadFile.exists())
    {
      entzippe(uploadFile, destination);
    }
  }

  @Override
  protected void doNormalImport() throws JobException
  {
    File uploadFile = Paths.get(this.importVerzeichnis, "zsdaten.properties").toFile();
    if (uploadFile.exists())
    {
      this.verzeichnisProperties = ladeVerzeichnisProperties(uploadFile);
      String kennung = this.jobBean.sachbearbeiterKennung;
      String passwort = this.jobBean.sachbearbeiterPasswort;
      DesEncrypter encrypter = new DesEncrypter("MStatRegDB key");
      passwort = encrypter.decrypt(passwort);
      this.mktoDateiDienst = new MelderkontoDateiDienst(RegDBGeneralHttpServlet.interneAblaeufeHost, String.valueOf(RegDBGeneralHttpServlet.interneAblaeufePort), kennung, passwort);
    }
    Path path = this.jobBean.getImportdatei().getPath();
    this.leseTeilbereich(path);
    if (this.melderKontoImportBeans.isEmpty())
    {
      return;
    }
    this.ermittleIndizes();
    this.startTransaction();
    transferiereDaten();
  }

  @Override
  protected AbstractJob nextImportJob()
  {
    return new MelderkontoImportJob(this.jobBean);
  }

  @Override
  protected AbstractJob doAfterLastImport()
  {
    AufraeumUtil util = new AufraeumUtil();
    util.entferneDateien(this.jobBean.jobId);
    return null;
  }

  @Override
  protected void doInTransaction() throws JobException
  {
    this.erzeugeOderAktualisiereEintraege();
  }

  private void ermittleIndizes() throws JobException
  {
    // Hole alle neuen Eintraege
    List<MelderkontoImportBean> neueBeans = this.melderKontoImportBeans.stream().filter(MelderkontoImportBean::isNeuerMelderkontoEintrag).collect(Collectors.toList());
    if (!neueBeans.isEmpty())
    {
      ermittleAdressenIds(neueBeans);
      ermittleFirmenIds(neueBeans);
    }
    // Hole alle betsehenden Eintraege
    List<MelderkontoImportBean> vorhandeneBeans = this.melderKontoImportBeans.stream().filter(b -> !b.isNeuerMelderkontoEintrag()).collect(Collectors.toList());
    if (!vorhandeneBeans.isEmpty())
    {
      ermittleMelderkontoIds(vorhandeneBeans);
    }
  }

  private void ermittleMelderkontoIds(List<MelderkontoImportBean> vorhandeneBeans) throws JobException
  {
    this.log.debug("ermittleMelderkontoIds mit " + vorhandeneBeans.size() + " Eintraegen");
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_MELDERKONTOIDS))
    {
      for (MelderkontoImportBean bean : vorhandeneBeans)
      {
        ps.addValues(bean.getMeldungId());
        ResultRow row = ps.fetchOne();
        if (row != null)
        {
          bean.setMktoId(row.getInt(1));
        }
        else
        {
          throw new JobException("Die Meldungs-ID " + bean.getMeldungId() + " konnte keinem Melderkonto zugeordnet werden!");
        }
      }
    }
  }

  private void ermittleAdressenIds(List<MelderkontoImportBean> beans) throws JobException
  {
    this.log.debug("ermittleAdressenIds mit " + beans.size() + " Eintraegen");
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_ADRESSEN))
    {
      for (MelderkontoImportBean bean : beans)
      {
        ps.addValues(bean.getQuellReferenzOf(), bean.getQuellReferenzId());
        ResultRow row = ps.fetchOne();
        if (row != null)
        {
          bean.setAdressenId(row.getInt(1));
        }
        else
        {
          throw new JobException("Das Ordnungsfeld '" + bean.getQuellReferenzOf() + "' konnte keiner Adresse zugeordnet werden!");
        }
      }
    }
  }

  private void ermittleFirmenIds(List<MelderkontoImportBean> beans) throws JobException
  {
    this.log.debug("ermittleFirmenIds mit " + beans.size() + " Eintraegen");
    try (PreparedSelect ps = this.sqlUtil.createPreparedSelect(SQL_SELECT_FIRMEN))
    {
      for (MelderkontoImportBean bean : beans)
      {
        ps.addValues(bean.getQuellReferenzOf(), bean.getQuellReferenzId());
        ResultRow row = ps.fetchOne();
        if (row != null)
        {
          bean.setFirmenId(row.getInt(1));
        }
        else
        {
          throw new JobException("Der Adresse '" + bean.getAdressenId() + "' konnte keine Firma zugeordnet werden!");
        }
      }
    }
  }

  private void erzeugeOderAktualisiereEintraege() throws JobException
  {
    this.log.debug("erzeugeOderAktualisiereEintraege mit " + this.melderKontoImportBeans.size() + " Eintraegen");
    this.beginStopWatch();
    try (PreparedInsert psInsert = this.sqlUtil.createPreparedInsert(SQL_INSERT_MELDERKONTO); PreparedUpdate psUpdate = this.sqlUtil.createPreparedUpdate(SQL_UPDATE_MELDERKONTO))
    {
      for (MelderkontoImportBean bean : this.melderKontoImportBeans)
      {
        if (bean.isNeuerMelderkontoEintrag())
        {
          insertMelderkonto(bean, psInsert);
          this.jobBean.getMelderkonto().getIdentifikatoren().getNeu().getValues().add(bean.getMktoId());
        }
        else
        {
          updateMelderkonto(bean, psUpdate);
          this.jobBean.getMelderkonto().getIdentifikatoren().getAenderung().getValues().add(bean.getMktoId());
        }
      }
    }
  }

  private void transferiereDaten()
  {
    for (MelderkontoImportBean bean : this.melderKontoImportBeans)
    {
      if (!bean.getZsDaten().isEmpty())
      {
        // Wandle Date um
        File file = Paths.get(this.importVerzeichnis, this.verzeichnisProperties.getProperty(bean.getZsDaten(), "")).toFile();
        this.log.debug("Uebertrage Datei an WerumDienst:" + file.toString() + " Originalname:" + bean.getZsDaten());
        MelderkontoDateiStatus status = this.mktoDateiDienst.importiereMelderkontoDatei("" + bean.getMktoId(), file);
        if (status.getStatus() == MelderkontoDateiStatus.STATUS_OK)
        {
          this.log.info("Meldekontodatei wurde korrekt übergeben");
        }
        else
        {
          this.log.error("Fehler beim Übertragen der Melderkontodatei " + bean.getZsDaten());
        }
      }
    }
  }

  private void insertMelderkonto(MelderkontoImportBean bean, PreparedInsert ps) throws JobException
  {
    //SET MELDEART='DATEI',FIRMEN_ID=?,STATISTIK_ID=?,AMT=?,QUELL_REFERENZ_OF=?,QUELL_REFERENZ_INT=?, BZR=?,MKF1=?, MKF2=?, MKF3=?, MKF4=?, MKF5=?, MKF6=?, MKF7=?, MKF8=?, MKF9=?, MKF10=?, SACHBEARBEITER_ID=?,STATUS=?,ZEITPUNKT_EINTRAG=?";
    ps.addValue(bean.getFirmenId());
    ps.addValue(bean.getStatistikId());
    ps.addValue(bean.getAmt());
    ps.addValue(bean.getQuellReferenzOf());
    ps.addValue(bean.getQuellReferenzInt());
    ps.addValue(bean.getBzr());
    String[] mkf = bean.getMkf();
    for (int i = 0; i < 10; i++)
    {
      ps.addValue(mkf[i]);
    }
    ps.addValue(this.jobBean.sachbearbeiterId);
    ps.addValue("NEU");
    ps.addValue(this.jobBean.zeitpunktEintrag);
    ps.insert();
    ResultRow row = ps.getGeneratedKeys();
    if (row != null)
    {
      bean.setMktoId(row.getInt(1));
    }
  }

  private int updateMelderkonto(MelderkontoImportBean bean, PreparedUpdate ps) throws JobException
  {
    // SET MKF1=?, MKF2=?, MKF3=?, MKF4=?, MKF5=?, MKF6=?, MKF7=?, MKF8=?, MKF9=?, MKF10=?, SACHBEARBEITER_ID=?,STATUS=?,ZEITPUNKT_AENDERUNG=? WHERE MKTO_ID=?
    String[] mkf = bean.getMkf();
    for (int i = 0; i < 10; i++)
    {
      ps.addValue(mkf[i]);
    }
    ps.addValue(this.jobBean.sachbearbeiterId);
    ps.addValue("AEND");
    ps.addValue(this.jobBean.zeitpunktEintrag);
    ps.addValue(bean.getMktoId());
    return ps.update();
  }

  /**
   * Lese teilbereich.
   *
   * @param path the path
   * @throws JobException the job exception
   */
  private void leseTeilbereich(Path path) throws JobException
  {
    SegmentedCsvFileReader reader = new SegmentedCsvFileReader();
    List<String[]> rows = reader.readSegment(path, this.jobBean.getImportdatei().getCharset(), this.jobBean.getImportdatei().datensatzOffset, this.jobBean.importBlockGroesse);
    this.melderKontoImportBeans = new ArrayList<>(rows.size());
    for (String[] cols : rows)
    {
      MelderkontoImportBean bean = new MelderkontoImportBean(cols);
      this.melderKontoImportBeans.add(bean);
    }
  }

  private void entzippe(File zipFile, Path destinationDir) throws JobException
  {
    this.log.debug("entzippe " + zipFile.toString());
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
    {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null)
      {
        this.log.debug("ZipEntry:" + zipEntry.toString());
        Path dest = destinationDir.resolve(zipEntry.getName());
        this.log.debug(dest.toString());
        Files.createDirectories(dest);
        if (!zipEntry.isDirectory())
        {
          this.log.debug("Kopiere Datei nach " + dest);
          Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        zipEntry = zis.getNextEntry();
      }
    }
    catch (IOException e)
    {
      this.log.error(e.getMessage());
      throw new JobException("Fehler beim Entzippen der Datei " + zipFile.getName() + ":" + e.getMessage());
    }
  }

  private Properties ladeVerzeichnisProperties(File file) throws JobException
  {
    try (FileInputStream fis = new FileInputStream(file))
    {
      Properties prop = new Properties();
      prop.load(fis);
      return prop;
    }
    catch (Exception e)
    {
      throw new JobException(e.getMessage(), e);
    }
  }
}
