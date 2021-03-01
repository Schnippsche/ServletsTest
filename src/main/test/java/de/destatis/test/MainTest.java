package de.destatis.test;

import de.destatis.regdb.db.*;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest
{
  private SqlUtil sqlUtil;
  private final LoggerIfc  log = Logger.getInstance()
    .getLogger(MainTest.class);
  @DisplayName("Test MessageService.get()")
  @Test
  void testGet()
  {
    assertEquals(1, 1);
  }
  @DisplayName("Connection")
  @Test
  void test2()
  {
    try
    {
      sqlUtil = new SqlUtil(getConnection());
      initDatabase();

      try (PreparedInsert pi = sqlUtil.createPreparedInsert("INSERT INTO testfirmen (SACHBEARBEITER_ID, NAME, ANSPRECHPARTNER_ID, STATUS) VALUES(?,?,?,?)"))
      {
        int sum = 0;
        for (int i = 1; i < 100; i++)
        {
          pi.addValues(1);
          pi.addValues("NAME" + i);
          pi.addValues(i);
          pi.addValues("NEU");
          sum += pi.insert();
          this.log.debug(pi.getGeneratedKeys()
            .toString());
        }
        this.log.debug("Inserts:" + sum);
      }
      this.log.debug("PreparedSelect...");
      try (PreparedSelect ps = sqlUtil.createPreparedSelect("SELECT * FROM testfirmen WHERE STATUS != ? AND SACHBEARBEITER_ID=? LIMIT 10"))
      {
        ps.addValues("NEU");
        ps.addValues(1);
        List<ResultRow> rows = ps.fetchMany();
        for (ResultRow row : rows)
        {
          System.out.println(row.toString());
        }
      }

      try (PreparedUpdate pu = sqlUtil.createPreparedUpdate("UPDATE testfirmen SET STATUS = ?, ZEITPUNKT_AENDERUNG = ? WHERE FIRMEN_ID = ?"))
      {
        int sum = 0;
        for (int i = 1; i <=10; i++)
        {
          pu.addValues("AEND");
          pu.addValues("2021-03-01 10:00:00");
          pu.addValues(i);
          sum += pu.update();
        }
        this.log.debug("Updates:" + sum);
        assertEquals(10, sum);
      }
    } catch (JobException e)
    {
      System.err.println(e.getMessage());
    }
  }

  private Connection getConnection() throws JobException
  {
    try
    {
      Class.forName("com.mysql.jdbc.Driver");
      return DriverManager
        .getConnection("jdbc:mysql://localhost:3306/test?zeroDateTimeBehavior=convertToNull&jdbcCompliantTruncation=false&useCursorFetch=true&useSSL=false", "root", "root");
    } catch (Exception e)
    {
      e.printStackTrace();
      throw new JobException(e.getMessage(), e);
    }
  }

  private void initDatabase() throws JobException
  {
    // Lege Tabelle an
    String sql = "DROP TABLE IF EXISTS testfirmen";
    sqlUtil.execute(sql);
    sql = "CREATE TABLE `testfirmen` (" +
      "`FIRMEN_ID` INT(11) NOT NULL AUTO_INCREMENT," +
      "`SACHBEARBEITER_ID` INT(11) NOT NULL DEFAULT '0'," +
      "`NAME` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_unicode_ci'," +
      "`NAME_ERGAENZUNG` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_unicode_ci'," +
      "`KURZTEXT` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_unicode_ci'," +
      "`ANSPRECHPARTNER_ID` INT(11) NOT NULL DEFAULT '0'," +
      "`FIRMEN_IDENTIFIKATOR` VARCHAR(100) NOT NULL DEFAULT '' COLLATE 'utf8mb4_unicode_ci'," +
      "`STATUS` ENUM('NEU','AEND','SPERRE','LOESCH','EXPORT','LOESCHKANDIDAT') NOT NULL DEFAULT 'NEU' COLLATE 'utf8mb4_unicode_ci'," +
      "`KOMMENTAR` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_unicode_ci'," +
      "`ZEITPUNKT_EINTRAG` DATETIME NULL DEFAULT NULL," +
      "`ZEITPUNKT_AENDERUNG` DATETIME NULL DEFAULT NULL," +
      "`ZEITPUNKT_FREMDEXPORT` DATETIME NULL DEFAULT NULL," +
      "`ZEITPUNKT_WWW` DATETIME NULL DEFAULT NULL," +
      "PRIMARY KEY (`FIRMEN_ID`) " +
      ") ENGINE=InnoDB;";
    sqlUtil.execute(sql);



  }


}