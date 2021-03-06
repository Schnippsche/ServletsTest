package de.destatis.tests;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.AuswirkungenJob;
import de.destatis.regdb.dateiimport.job.LoeschenJob;
import de.destatis.regdb.dateiimport.job.PruefenJob;
import de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob;
import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.job.LogLevel;
import de.werum.sis.idev.res.log.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class IdevImportMitZusatzfelderTest
{

  @BeforeAll
  static void init()
  {
    Logger.getInstance().setLogLevel(LogLevel.DEBUG);
  }

  @Test
  void testeIdevImport()
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
      assertEquals(100, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(importJob instanceof AdressImportJob);
      importJob.setSqlUtil(sqlUtil);
      importJob.verarbeiteJob();
      assertEquals(100, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      // Pruefe Datenbank
      ResultRow row = sqlUtil.fetchOne("SELECT COUNT(*) FROM adressen WHERE STATUS='NEU'");
      assertEquals(100, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen WHERE STATUS='NEU'");
      assertEquals(100, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder WHERE STATUS='NEU'");
      assertEquals(100, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM ansprechpartner WHERE STATUS='NEU'");
      assertEquals(200, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen_adressen WHERE STATUS='AKTIV'");
      assertEquals(100, row.getInt(1));
      // Update
      bean = createBean();
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      importJob = pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(100, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(importJob instanceof AdressImportJob);
      importJob.setSqlUtil(sqlUtil);
      importJob.verarbeiteJob();
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(100, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      // Pruefe Datenbank
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM adressen WHERE STATUS='AEND'");
      assertEquals(100, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen WHERE STATUS='AEND'");
      assertEquals(100, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder WHERE STATUS='AEND'");
      assertEquals(100, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM ansprechpartner WHERE STATUS='AEND'");
      assertEquals(200, row.getInt(1));
      // Löschung
      bean = createBean();
      bean.getImportdatei().dateiName = "adressen_1.csv";
      bean.getImportdatei().originalDateiname = "adressen_1.csv";
      bean.loescheDaten = true;
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      AbstractJob auswirkungenJob = pruefJob.verarbeiteJob();
      // Nächster Job beim Loeschen msus Auswirkungen sein!
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(1, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(auswirkungenJob instanceof AuswirkungenJob);
      // Auswirkungen ausführen! Nächster Job muss Adressimport sein
      auswirkungenJob.setSqlUtil(sqlUtil);
      importJob = auswirkungenJob.verarbeiteJob();
      assertTrue(bean.getSimulation().bestandErmittelt);
      assertEquals(100, bean.getSimulation().anzahlAdressenImBestand);
      assertEquals(99, bean.getSimulation().getAdressIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(99, bean.getSimulation().getFirmenIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(99, bean.getSimulation().getMelderIdentifikatoren().getLoeschung().getAnzahl());
      assertTrue(importJob instanceof AdressImportJob);
      importJob.setSqlUtil(sqlUtil);
      AbstractJob loeschJob = importJob.verarbeiteJob();
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(1, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(0, bean.getFirmen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(1, bean.getFirmen().getIdentifikatoren().getAenderung().getAnzahl());
      assertEquals(0, bean.getFirmen().getIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(0, bean.getMelder().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(1, bean.getMelder().getIdentifikatoren().getAenderung().getAnzahl());
      assertEquals(0, bean.getMelder().getIdentifikatoren().getLoeschung().getAnzahl());
      assertTrue(loeschJob instanceof LoeschenJob);
      // Eigentliches Löschen
      loeschJob.setSqlUtil(sqlUtil);
      loeschJob.verarbeiteJob();
      assertEquals(99, bean.getAdressen().getIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(99, bean.getFirmen().getIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(99, bean.getMelder().getIdentifikatoren().getLoeschung().getAnzahl());
      // Pruefe Datenbank
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM adressen WHERE STATUS='LOESCH'");
      assertEquals(99, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen WHERE STATUS='LOESCH'");
      assertEquals(99, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder WHERE STATUS='LOESCH'");
      assertEquals(99, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM ansprechpartner WHERE STATUS='LOESCH'");
      assertEquals(198, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen_adressen WHERE STATUS='LOESCH'");
      assertEquals(99, row.getInt(1));
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
