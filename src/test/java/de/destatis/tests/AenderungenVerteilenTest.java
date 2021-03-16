package de.destatis.tests;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.aenderungen.AenderungenHolenDaemon;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.AbstractJob;
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

public class AenderungenVerteilenTest
{

  @BeforeAll
  static void init()
  {
    Logger.getInstance().setLogLevel(LogLevel.DEBUG);
  }

  @Test
  public void starteAenderungenVerteilen()
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
      // Neuanlage
      AbstractJob importJob = pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(10, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(importJob instanceof AdressImportJob);
      importJob.setSqlUtil(sqlUtil);
      importJob.verarbeiteJob();
      assertEquals(10, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      // Aenderungen zurücksetzen
      sqlUtil.execute("UPDATE aenderung SET STATUS_EXPORT_AENDERUNG=0, STATUS_DIREKTEINTRAG=0");
      // Aenderungen Testen
      AenderungenHolenDaemon.getInstance().starteAenderungenHolen();
    } catch (JobException e)
    {
      e.printStackTrace();
      fail();
    } finally
    {
      Tool.closeConnection(conn);
    }
  }

  JobBean createBean()
  {
    JobBean bean = new JobBean();
    bean.getFormatPruefung().maximaleAnzahlFehler = 100;
    bean.getImportdatei().importFormat = ImportFormat.REGISTERIMPORT;
    bean.getImportdatei().importVerzeichnis = Tool.getTestPath().resolve("registerimport").toString();
    bean.getImportdatei().dateiName = "10_saetze_korrekt.txt";
    bean.getImportdatei().originalDateiname = bean.getImportdatei().dateiName;
    bean.quellReferenzId = 1;
    bean.importBlockGroesse = 1000;
    bean.quellReferenzNumerisch = true;
    bean.sachbearbeiterLand = "00";
    bean.sachbearbeiterKennung = "test";
    bean.sachbearbeiterPasswort = "test";
    bean.amt = "00";
    bean.sachbearbeiterId = 2;
    bean.jobId = 1;
    return bean;
  }
}
