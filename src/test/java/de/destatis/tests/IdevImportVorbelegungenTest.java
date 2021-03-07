package de.destatis.tests;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.AbstractJob;
import de.destatis.regdb.dateiimport.job.AuswirkungenJob;
import de.destatis.regdb.dateiimport.job.LoeschenJob;
import de.destatis.regdb.dateiimport.job.PruefenJob;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportJob;
import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.PreparedInsert;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.job.LogLevel;
import de.werum.sis.idev.res.log.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class IdevImportVorbelegungenTest
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
      // Lege Daten an, damit die Zuordnung passt"
      PreparedInsert ps = sqlUtil.createPreparedInsert("INSERT INTO adressen (AMT,QUELL_REFERENZ_ID,QUELL_REFERENZ_OF,STATUS) VALUES(?,?,?,?)");
      ps.addValue("00");
      ps.addValue(1);
      ps.addValue("10000000");
      ps.addValue("NEU");
      ps.insert();
      ps.addValues("00", 1, "10000001", "NEU");
      ps.insert();
      // Firmen
      ps = sqlUtil.createPreparedInsert("INSERT INTO firmen (NAME,STATUS) VALUES(?,?)");
      ps.addValue("firma1");
      ps.addValue("NEU");
      ps.insert();
      ps.addValues("Firma2", "NEU");
      ps.insert();
      // FirmenAdressen
      ps = sqlUtil.createPreparedInsert("INSERT INTO firmen_adressen (FIRMEN_ID,ADRESSEN_ID,STATUS) VALUES(?,?,?)");
      ps.addValues(1,1, "AKTIV");
      ps.insert();
      ps.addValues(2,2, "AKTIV");
      ps.insert();

      // Neuanlage
      AbstractJob importJob = pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(3, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(importJob instanceof VorbelegungsImportJob);
      importJob.setSqlUtil(sqlUtil);
      importJob.verarbeiteJob();
      assertEquals(2, bean.getVorbelegungen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(0, bean.getVorbelegungen().getIdentifikatoren().getAenderung().getAnzahl());
      // Pruefe Datenbank
      ResultRow row = sqlUtil.fetchOne("SELECT COUNT(*) FROM vorbelegung_verwaltung WHERE STATUS='NEU'");
      assertEquals(2, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM vorbelegung_wert");
      assertEquals(6, row.getInt(1));
      // Update
      bean = createBean();
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      importJob = pruefJob.verarbeiteJob();
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(3, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(importJob instanceof VorbelegungsImportJob);
      importJob.setSqlUtil(sqlUtil);
      importJob.verarbeiteJob();
      assertEquals(0, bean.getVorbelegungen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(2, bean.getVorbelegungen().getIdentifikatoren().getAenderung().getAnzahl());
      // Pruefe Datenbank
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM vorbelegung_verwaltung WHERE STATUS='AEND'");
      assertEquals(2, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM vorbelegung_wert");
      assertEquals(6, row.getInt(1));
     // Kurze Verzögerung, da Löschungen mit Zeitpunkt_aenderung arbeitet
      try
      {
        Thread.sleep(1000);
      } catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      // Löschung
      bean = createBean();
      bean.getImportdatei().dateiName = "vorbelegung_1.csv";
      bean.getImportdatei().originalDateiname = bean.getImportdatei().dateiName;
      bean.loescheDaten = true;
      pruefJob = new PruefenJob(bean);
      pruefJob.setSqlUtil(sqlUtil);
      AbstractJob auswirkungenJob = pruefJob.verarbeiteJob();
      // Nächster Job beim Loeschen msus Auswirkungen sein!
      assertEquals(0, bean.getFormatPruefung().anzahlFehler);
      assertEquals(1, bean.getImportdatei().anzahlDatensaetze);
      assertTrue(auswirkungenJob instanceof AuswirkungenJob);
      // Auswirkungen ausführen! Nächster Job muss Vorbelegungsimport sein
      auswirkungenJob.setSqlUtil(sqlUtil);
      importJob = auswirkungenJob.verarbeiteJob();
      assertTrue(bean.getSimulation().bestandErmittelt);
      assertTrue(importJob instanceof VorbelegungsImportJob);
      importJob.setSqlUtil(sqlUtil);
      AbstractJob loeschJob = importJob.verarbeiteJob();
      assertEquals(0, bean.getVorbelegungen().getIdentifikatoren().getNeu().getAnzahl());
      assertEquals(1, bean.getVorbelegungen().getIdentifikatoren().getAenderung().getAnzahl());
      assertEquals(1, bean.getVorbelegungen().getIdentifikatoren().getLoeschung().getAnzahl());

      // Pruefe Datenbank
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM vorbelegung_verwaltung WHERE STATUS='LOESCH'");
      assertEquals(1, row.getInt(1));
      row = sqlUtil.fetchOne("SELECT COUNT(*) FROM vorbelegung_wert");
      assertEquals(1, row.getInt(1));
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
    bean.getImportdatei().importFormat = ImportFormat.VORBELEGUNGSIMPORT;
    bean.getImportdatei().importVerzeichnis = Tool.getTestPath().resolve("importvorbelegungen").toString();
    bean.getImportdatei().dateiName = "vorbelegungentest.csv";
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
