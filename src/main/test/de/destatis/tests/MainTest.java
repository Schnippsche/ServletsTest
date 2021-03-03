package de.destatis.tests;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.pruefen.PruefUtil;
import de.destatis.regdb.dateiimport.job.pruefen.PruefeIdevImport;
import de.destatis.regdb.dateiimport.job.pruefen.PruefeRegisterImport;
import de.destatis.regdb.dateiimport.job.pruefen.PruefeVorbelegungenImport;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.job.LogLevel;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest
{
  private static LoggerIfc log;

  @BeforeAll
  public static void init()
  {
    Logger.getInstance().setLogLevel(LogLevel.DEBUG);
    log = Logger.getInstance()
      .getLogger(MainTest.class);
    log.debug("Testen der Anwendung regdbservlets...");
  }

  @Test
  public void testConnection()
  {
    assertNotNull(getConnection(), "Connection failed");
  }

  @Test
  public void testPruefUtil()
  {

    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 1000;
    PruefUtil util = new PruefUtil(bean, sqlUtil);
    assertTrue(util.checkOhneLeerzeichen(null, "Spalte 'Test1'", 1));
    assertTrue(util.checkOhneLeerzeichen("ohneleer", "Spalte 'Test1'", 1));
    assertFalse(util.checkOhneLeerzeichen("mit leer", "Spalte 'Test1'", 1));
    //
    assertFalse(util.checkMinSpaltenLaenge(1, 10, 2));
    assertTrue(util.checkMinSpaltenLaenge(10, 10, 2));
    assertTrue(util.checkMinSpaltenLaenge(12, 10, 2));
    //
    assertFalse(util.checkFixSpaltenLaenge(1, 10, 3));
    assertTrue(util.checkFixSpaltenLaenge(10, 10, 3));
    assertTrue(util.checkFixSpaltenLaenge(11, 10, 3));
    assertFalse(util.checkFixSpaltenLaenge(14, 10, 3));
    //
    assertFalse(util.checkMinStringLaenge(null, "Hallotest", 6, 4));
    assertFalse(util.checkMinStringLaenge("Hallo", "Hallotest", 6, 4));
    assertTrue(util.checkMinStringLaenge("Hallo", "Hallotest", 4, 4));
    assertTrue(util.checkMinStringLaenge("Hallo", "Hallotest", 3, 4));
    //
    assertTrue(util.checkMaxStringLaenge(null, "Hallotest", 6, 5));
    assertFalse(util.checkMaxStringLaenge("Hallole", "Hallotest", 6, 5));
    assertTrue(util.checkMaxStringLaenge("Hallo", "Hallotest", 5, 5));
    assertFalse(util.checkMaxStringLaenge("Hallolili", "Hallotest", 5, 5));
    //
    assertFalse(util.checkNichtLeer(null, "Spalte5", 6));
    assertFalse(util.checkNichtLeer("", "Spalte5", 6));
    assertTrue(util.checkNichtLeer(" ", "Spalte5", 6));
    //
    bean.quellReferenzNumerisch = true;
    bean.quellReferenzId = 1;
    bean.getAdressen().ordnungsfeldLaenge = 8;
    assertFalse(util.checkOrdnungsfeld(null, 7));
    assertFalse(util.checkOrdnungsfeld("", 7));
    assertFalse(util.checkOrdnungsfeld("ABCDE", 7));
    assertFalse(util.checkOrdnungsfeld("12345678910", 7));
    assertTrue(util.checkOrdnungsfeld("12345678", 7));
    //

    try
    {
      assertFalse(util.checkAdressbestand(0));
      assertTrue(util.checkAdressbestand(1));
      assertEquals(1, bean.quellReferenzId);
      assertEquals("Test", bean.quellReferenzName);
      assertEquals("NUM", bean.getAdressen().ordnungsfeldTyp);
      assertTrue(bean.quellReferenzNumerisch);
      //
      HashMap<String, Integer>map = new HashMap<>();
      map.put("1000000",1);
      map.put("1000001",2);
      map.put("Mei's",3);
      assertFalse(util.checkOrdnungsfelderExistieren(map));
      assertFalse(util.checkMelderExistieren(map));
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }

    //
    showLogErrors(bean);

  }

  @Test
  public void pruefeFehlerhaftenRegisterImport()
  {
    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.REGISTERIMPORT;
    File file = new File(Objects.requireNonNull(MainTest.class.getClassLoader().getResource("testfiles")).getPath());
    bean.getImportdatei().importVerzeichnis= Paths.get(file.toString(), "registerimport").toString();
    bean.getImportdatei().dateiName = "fehlerhaft.txt";
    bean.importBlockGroesse = 6;
    bean.jobId = 1;
    PruefeRegisterImport imp = new PruefeRegisterImport(bean, sqlUtil);
    try
    {
      imp.checkFile();
      showLogErrors(bean);
      assertTrue( bean.getFormatPruefung().anzahlFehler> 7);
      assertEquals(bean.getImportdatei().anzahlDatensaetze, 14);
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }
  }

  @Test
  public void pruefeKorrektenRegisterImport()
  {
    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.REGISTERIMPORT;
    File file = new File(Objects.requireNonNull(MainTest.class.getClassLoader().getResource("testfiles")).getPath());
    bean.getImportdatei().importVerzeichnis= Paths.get(file.toString(), "registerimport").toString();
    bean.getImportdatei().dateiName = "10_saetze_korrekt.txt";
    bean.importBlockGroesse = 6;
    bean.jobId = 1;
    PruefeRegisterImport imp = new PruefeRegisterImport(bean, sqlUtil);
    try
    {
      imp.checkFile();
      showLogErrors(bean);
      assertEquals( bean.getFormatPruefung().anzahlFehler, 0);
      assertEquals(bean.getImportdatei().anzahlDatensaetze, 10);
      assertEquals(bean.getAdressen().getOrdnungsfelder().size(), 10);
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }

  }

  @Test
  public void pruefeFehlerhaftenIdevImport()
  {
    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.IMPORTMITZUSATZFELDER;
    File file = new File(Objects.requireNonNull(MainTest.class.getClassLoader().getResource("testfiles")).getPath());
    bean.getImportdatei().importVerzeichnis= Paths.get(file.toString(), "importmitzusatzfelder").toString();
    bean.getImportdatei().dateiName = "fehlerhaft.csv";
    bean.quellReferenzId=1;
    bean.importBlockGroesse = 6;
    bean.quellReferenzNumerisch = true;
    bean.jobId = 1;
    PruefeIdevImport imp = new PruefeIdevImport(bean, sqlUtil);
    try
    {
      imp.checkFile();
      showLogErrors(bean);
      assertTrue( bean.getFormatPruefung().anzahlFehler>9);
      assertEquals(bean.getImportdatei().anzahlDatensaetze, 6);
      assertEquals(bean.getAdressen().getOrdnungsfelder().size(), 0);
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }
  }

  @Test
  public void pruefeKorrektenIdevImport()
  {
    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.IMPORTMITZUSATZFELDER;
    File file = new File(Objects.requireNonNull(MainTest.class.getClassLoader().getResource("testfiles")).getPath());
    bean.getImportdatei().importVerzeichnis= Paths.get(file.toString(), "importmitzusatzfelder").toString();
    bean.getImportdatei().dateiName = "adressen_100.csv";
    bean.quellReferenzId=1;
    bean.importBlockGroesse = 9;
    bean.quellReferenzNumerisch = true;
    bean.jobId = 1;
    PruefeIdevImport imp = new PruefeIdevImport(bean, sqlUtil);
    try
    {
      imp.checkFile();
      showLogErrors(bean);
      assertEquals( bean.getFormatPruefung().anzahlFehler, 0);
      assertEquals(bean.getImportdatei().anzahlDatensaetze, 100);
      assertEquals(bean.getAdressen().getOrdnungsfelder().size(), 100);
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }
  }

  @Test
  public void pruefeFehlerhaftenVorbelegungsImport()
  {
    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.VORBELEGUNGSIMPORT;
    File file = new File(Objects.requireNonNull(MainTest.class.getClassLoader().getResource("testfiles")).getPath());
    bean.getImportdatei().importVerzeichnis= Paths.get(file.toString(), "importvorbelegungen").toString();
    bean.getImportdatei().dateiName = "fehlerhaft.csv";
    bean.quellReferenzId=1;
    bean.importBlockGroesse = 6;
    bean.quellReferenzNumerisch = true;
    bean.jobId = 1;
    PruefeVorbelegungenImport imp = new PruefeVorbelegungenImport(bean, sqlUtil);
    try
    {
      imp.checkFile();
      showLogErrors(bean);
      assertTrue( bean.getFormatPruefung().anzahlFehler>7);
      assertEquals(bean.getImportdatei().anzahlDatensaetze, 6);
      assertEquals(bean.getAdressen().getOrdnungsfelder().size(), 0);
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }
  }

  @Test
  public void pruefeKorrektenVorbelegungsImport()
  {
    SqlUtil sqlUtil = new SqlUtil(getConnection());
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.VORBELEGUNGSIMPORT;
    File file = new File(Objects.requireNonNull(MainTest.class.getClassLoader().getResource("testfiles")).getPath());
    bean.getImportdatei().importVerzeichnis= Paths.get(file.toString(), "importvorbelegungen").toString();
    bean.getImportdatei().dateiName = "vorbelegungen.csv";
    bean.quellReferenzId=1;
    bean.importBlockGroesse = 6;
    bean.quellReferenzNumerisch = true;
    bean.jobId = 1;
    PruefeVorbelegungenImport imp = new PruefeVorbelegungenImport(bean, sqlUtil);
    try
    {
      imp.checkFile();
      showLogErrors(bean);
      assertTrue( bean.getFormatPruefung().anzahlFehler>7);
      assertEquals(bean.getImportdatei().anzahlDatensaetze, 8);
      assertEquals(bean.getAdressen().getOrdnungsfelder().size(), 0);
    } catch (JobException e)
    {
      log.error(e.getMessage());
      fail();
    }
  }

  public void showLogErrors(JobBean bean)
  {
    log.debug("Anzahl Fehler:" + bean.getFormatPruefung().anzahlFehler);
    for (int i = 0; i < bean.getFormatPruefung().anzahlFehler; i++)
    {
      log.debug(bean.getFormatPruefung().getSortedErrors().get(i).toString());
    }
  }

  public Connection getConnection()
  {
    try
    {
      Class.forName("com.mysql.jdbc.Driver");
      return DriverManager
        .getConnection("jdbc:mysql://localhost:3306/regdbtest171?zeroDateTimeBehavior=convertToNull&jdbcCompliantTruncation=false&useCursorFetch=true&useSSL=false", "root", "root");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

}