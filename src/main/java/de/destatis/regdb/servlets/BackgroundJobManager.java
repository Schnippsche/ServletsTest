package de.destatis.regdb.servlets;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import de.destatis.regdb.db.ConnectionTool;
import de.destatis.regdb.db.DateiImportDaemon;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

@WebListener
public class BackgroundJobManager implements ServletContextListener
{
  private final LoggerIfc log = Logger.getInstance()
      .getLogger(this.getClass());
  public static final String KONFIGURATION_DAEMON_INTERVAL = "int_dateiimport_interval";
  public static final String KONFIGURATION_LOESCH_INTERVAL = "int_dateiimport_loeschinterval";
  private ScheduledExecutorService scheduler;

  @Override
  public void contextInitialized(ServletContextEvent event)
  {
    this.log.info("BackgroundManager gestartet");
    int daemonInterval = pruefeUndSetzeKonfigurationsWert(KONFIGURATION_DAEMON_INTERVAL, 10, 2, 180);
    int loeschInterval = pruefeUndSetzeKonfigurationsWert(KONFIGURATION_LOESCH_INTERVAL, 3, 1, 24);
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.log.info("Starte DateiImportDaemon, laeuft alle " + daemonInterval + " Sekunden");
    this.scheduler.scheduleAtFixedRate(() -> DateiImportDaemon.getInstance()
        .checkJobs(), 4, daemonInterval, TimeUnit.SECONDS);
    //this.log.info("Starte AufraeumImportDaemon, laeuft alle " + loeschInterval + " Stunden");
    //this.scheduler.scheduleAtFixedRate(() -> DateiImportDaemon.getInstance()
    //    .checkJobs(), 4, loeschInterval, TimeUnit.HOURS);
    
   
  }

 
  private int pruefeUndSetzeKonfigurationsWert(String configValue, int defaultValue, int min, int max)
  {
    Connection conn = ConnectionTool.getInstance()
        .getConnection();
    DBConfig config = new DBConfig();
    int result = defaultValue;
    String newValue = config.getParameter(conn, configValue);
    if (newValue == null)
    {
      config.setParameter(conn, configValue, "" + defaultValue, RegDBImportServlet.INTERN);
    }
    else
    {
      try
      {
        result = Integer.parseInt(newValue);
        if (result < min || result > max)
        {
          throw new NumberFormatException();
        }
      }
      catch (NumberFormatException e)
      {
        this.log.error(MessageFormat.format("angegebener Konfigurationswert ''{3}'' fuer ''{0}'' ist ungueltig: Muss zwischen {1} und {2} liegen! Verwende Default-Wert {4}", configValue, "" + min, "" + max, newValue, ""
            + defaultValue));
        result = defaultValue;
      }
    }
    ConnectionTool.getInstance()
        .freeConnection(conn);
    return result;
  }

  @Override
  public void contextDestroyed(ServletContextEvent event)
  {
    this.scheduler.shutdownNow();
    this.log.info("BackgroundManager gestoppt");
  }

}
