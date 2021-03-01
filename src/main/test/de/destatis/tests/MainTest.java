package de.destatis.tests;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.job.pruefen.PruefUtil;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.job.LogLevel;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

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
    assertNotNull(sqlUtil);
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

    } catch (JobException e)
    {
      log.error(e.getMessage());
    }

    //
    showLogErrors(bean);

  }

  @Test
  public void test()
  {
    assertEquals(1, 1);
  }

  public void showLogErrors(JobBean bean)
  {
    log.debug("Anzahl Fehler:" + bean.getFormatPruefung().anzahlFehler);
    for (int i = 0; i < bean.getFormatPruefung().anzahlFehler; i++)
    {
      log.debug(bean.getFormatPruefung().getError().get(i));
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