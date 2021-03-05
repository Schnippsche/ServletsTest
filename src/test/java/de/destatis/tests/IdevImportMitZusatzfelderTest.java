package de.destatis.tests;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.PruefenJob;
import de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob;
import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.job.LogLevel;
import de.werum.sis.idev.res.log.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class IdevImportMitZusatzfelderTest
{

  @BeforeAll
  public static void init()
  {
    Logger.getInstance().setLogLevel(LogLevel.DEBUG);
  }

  @Test
  public void testeIdevImport()
  {
    Connection conn = Tool.getConnection();
    assertNotNull(conn);
    ConnectionTool.getInstance().setTestConnection(conn);
    assertNotNull(ConnectionTool.getInstance().getConnection());
    SqlUtil sqlUtil = new SqlUtil(conn);
    Tool.initDatabase(conn);
    try
    {
      JobBean bean = createBean();
      PruefenJob pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      AdressImportJob importJob = new AdressImportJob(bean);
      importJob.setSqlUtil(sqlUtil);
      // Neuanlage
      pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(100, bean.getImportdatei().anzahlDatensaetze);
      importJob.verarbeiteJob();
      assertEquals(100, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      // Update
      bean = createBean();
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      importJob = new AdressImportJob(bean);
      importJob.setSqlUtil(sqlUtil);
      pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(100, bean.getImportdatei().anzahlDatensaetze);
      importJob.verarbeiteJob();
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(100, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());

    } catch (JobException e)
    {
      e.printStackTrace();
      fail();
    } finally
    {
      Tool.closeConnection(conn);
    }
  }

  public JobBean createBean()
  {
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.IMPORTMITZUSATZFELDER;
    bean.getImportdatei().importVerzeichnis = Tool.getTestPath().resolve("importmitzusatzfelder").toString();
    bean.getImportdatei().dateiName = "adressen_100.csv";
    bean.getImportdatei().originalDateiname = "adressen_100.csv";
    bean.quellReferenzId = 1;
    bean.importBlockGroesse = 1000;
    bean.quellReferenzNumerisch = true;
    bean.sachbearbeiterLand = "00";
    bean.sachbearbeiterKennung = "test";
    bean.sachbearbeiterPasswort = "test";
    bean.sachbearbeiterId = 2;
    bean.jobId = 1;
    return bean;
  }
}
