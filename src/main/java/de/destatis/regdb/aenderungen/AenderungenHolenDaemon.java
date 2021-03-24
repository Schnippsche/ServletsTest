package de.destatis.regdb.aenderungen;

import de.destatis.regdb.db.ConnectionTool;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class AenderungenHolenDaemon
{

  private static AenderungenHolenDaemon instance;
  private final LoggerIfc log;
  private final String amt;
  private final String kennung;

  private AenderungenHolenDaemon()
  {
    this.log = Logger.getInstance().getLogger(this.getClass());
    this.log.info("initing " + this.getClass());
    this.amt = "00"; // Aus Konfig?
    this.kennung = "batch-adm"; // Aus Konfig?
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
      AenderungenVerteilen verteilen = new AenderungenVerteilen(conn, this.amt, this.kennung);
      verteilen.setKnownHostDatei(null); // Aus Konfig?
      verteilen.disableHostKeyCheck(true); // Aus Konfig?
      verteilen.setZielZeichensatz(StandardCharsets.ISO_8859_1.name()); // Aus Konfig?
      if (verteilen.ermittleTransferziele())
      {
        verteilen.verteileAenderungen();
        verteilen.versendeMails();
        verteilen.macheDirekteintraege();
        verteilen.setzeExportStatusAenderungen();
      }
      else
      {
        this.log.info("keine neue Änderungen mit Transferziel vorhanden");
      }
    }
    catch (Exception e)
    {
      this.log.error("Aenderungen konnten nicht serverseitig verarbeitet werden:" + e.getMessage());
    }
    finally
    {
      ConnectionTool.getInstance().freeConnection(conn);
    }
  }



}
