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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class
IdevRegisterImportTest
{

  @BeforeAll
  static void init()
  {
    Logger.getInstance().setLogLevel(LogLevel.DEBUG);
  }

  @Test
  void testeRegisterImport()
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
      // Pruefe Datenbank
      ResultRow row = sqlUtil.fetchOne("SELECT COUNT(*) FROM adressen WHERE STATUS='NEU'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen WHERE STATUS='NEU'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder WHERE STATUS='NEU'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM ansprechpartner WHERE STATUS='NEU'");
      assertEquals(20, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder_statistiken WHERE STATUS='NEU'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen_adressen WHERE STATUS='AKTIV'");
      assertEquals(10, row.getInt(1));

      // Update
      bean = createBean();
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      importJob = pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(10, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(importJob instanceof AdressImportJob);
      importJob.setSqlUtil(sqlUtil);
      importJob.verarbeiteJob();
      assertEquals(0, bean.getAdressen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(10, bean.getAdressen().getIdentifikatoren().getAenderung().getAnzahl());
      // Pruefe Datenbank
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM adressen WHERE STATUS='AEND'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen WHERE STATUS='AEND'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder WHERE STATUS='AEND'");
      assertEquals(10, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM ansprechpartner WHERE STATUS='AEND'");
      assertEquals(20, row.getInt(1));
      // Löschung
      bean = createBean();
      bean.getImportdatei().dateiName = "1_satz.txt";
      bean.getImportdatei().originalDateiname = bean.getImportdatei().dateiName;
      bean.loescheDaten = true;
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      AbstractJob auswirkungenJob = pruefJob.verarbeiteJob();
      // Nächster Job beim Loeschen muss Auswirkungen sein!
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(1, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(auswirkungenJob instanceof AuswirkungenJob);
      // Auswirkungen ausführen! Nächster Job muss Adressimport sein
      auswirkungenJob.setSqlUtil(sqlUtil);
      importJob = auswirkungenJob.verarbeiteJob();
      assertTrue(bean.getSimulation().bestandErmittelt);
      assertEquals(10, bean.getSimulation().anzahlAdressenImBestand);
      assertEquals(9, bean.getSimulation().getAdressIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(9, bean.getSimulation().getFirmenIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(9, bean.getSimulation().getMelderIdentifikatoren().getLoeschung().getAnzahl());
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
      assertEquals(9, bean.getAdressen().getIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(9, bean.getFirmen().getIdentifikatoren().getLoeschung().getAnzahl());
      assertEquals(9, bean.getMelder().getIdentifikatoren().getLoeschung().getAnzahl());
      // Pruefe Datenbank
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM adressen WHERE STATUS='LOESCH'");
      assertEquals(9, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen WHERE STATUS='LOESCH'");
      assertEquals(9, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder WHERE STATUS='LOESCH'");
      assertEquals(9, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM ansprechpartner WHERE STATUS='LOESCH'");
      assertEquals(18, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM firmen_adressen WHERE STATUS='LOESCH'");
      assertEquals(9, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM melder_statistiken WHERE STATUS='LOESCH'");
      assertEquals(9, row.getInt(1));

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
    bean.getImportdatei().importVerzeichnis = Objects.requireNonNull(Tool.getTestPath()).resolve("registerimport").toString();
    bean.getImportdatei().dateiName = "10_saetze_korrekt.txt";
    bean.getImportdatei().originalDateiname = bean.getImportdatei().dateiName;
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
