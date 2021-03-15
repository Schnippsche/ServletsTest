package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.ConnectionTool;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.sql.Connection;

public class AenderungenHolenDaemon
{

  private static AenderungenHolenDaemon instance;
  private final LoggerIfc log;
  private String amt;
  private String kennung;

  private AenderungenHolenDaemon()
  {
    this.log = Logger.getInstance().getLogger(this.getClass());
    this.log.info("initing " + this.getClass());
    this.amt = "00";
    this.kennung="batch-adm";
  }

  public static synchronized AenderungenHolenDaemon getInstance()
  {
    if (instance == null)
    {
      instance = new AenderungenHolenDaemon();
    }
    return instance;
  }

  public void starteAenderungenHolen()
  {
    Connection conn = ConnectionTool.getInstance().getConnection();
    try
    {
      AenderungenVerteilen verteilen = new AenderungenVerteilen(conn, amt, kennung);
      if (verteilen.ermittleTransferziele())
      {
        verteilen.verteileAenderungen();
       // verteilen.versendeMails();
       // verteilen.macheDirekteintraege();
       // verteilen.setzeExportStatusAenderungen();
      }
      else
      {
        log.info("keine neue Änderungen mit Transferziel vorhanden");
      }
    } catch (JobException e)
    {
      this.log.error("Aenderungen konnten nicht serverseitig verarbeitet werden");
    } finally
    {
      ConnectionTool.getInstance().freeConnection(conn);
    }

  }

}
