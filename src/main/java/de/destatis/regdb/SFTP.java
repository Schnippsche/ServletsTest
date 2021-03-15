/*
 * Copyright (c) 2004 Statistisches Bundesamt
 *
 * Datei: SFTP.java
 *
 * Aenderungshistorie:
 *
 * Nr ! Datum    ! Name            ! Aenderungsgrund
 * -----------------------------------------------------------------------------
 *  1 ! 25.11.2004 ! Schindler       ! neu erstellt
 *
 */
package de.destatis.regdb;

import com.jcraft.jsch.*;
import de.werum.sis.idev.res.job.JobException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * Die Klasse <code>SFTP</code> bietet Tools fuer das Arbeiten mittels SFTP
 *
 * @version 2 vom 31.10.2018 Toengi: Umstellung auf Jsch
 */
public class SFTP
{
  private static final Log logger = LogFactory.getLog(SFTP.class);
  private final JSch jsch;
  private ChannelSftp sftpChannel;
  private Session session;

  public SFTP()
  {
    this.jsch = new JSch();
    this.sftpChannel = null;
    this.session = null;
    File defaultKnownHosts = new File(System.getProperty("user.home") + "/.ssh/known_hosts");
    if (defaultKnownHosts.exists())
    {
      setKnownHostFile(defaultKnownHosts.getAbsolutePath());
    } else
    {
      logger.warn("Keine known_hosts unter " + System.getProperty("user.home") + "/.ssh/ gefunden... Setzen Sie explizit den Pfad zur known_hosts-Datei mit der Angabe -knownhostfile <arg> oder deaktivieren Sie die Pruefung gegen die knownHost mittels -disableHostKeyCheck, was einen Sicherheitsverlust bedeutet!");
    }
  }

  public void setKnownHostFile(String host)
  {
    try
    {
      this.jsch.setKnownHosts(host);
      logger.info("Verwende Known-Host Datei " + host);
    } catch (JSchException e)
    {
      logger.error("Fehler beim Setzen des Known-Hosts:" + e.getMessage());
    }
  }

  public void disableHostKeyChecking()
  {
    logger.warn("Host Key Checking ist deaktiviert! Dies bedeutet einen Sicherheitsverlust!");
    JSch.setConfig("StrictHostKeyChecking", "no");
  }

  /**
   * Mit der methode connect baut man eine Verbindung zu einem SFTP Server
   * auf. (hier: UNIX,LINUX,WINDOWS)
   *
   * @param server   DNS oder IP Name des Servers
   * @param usr      Benutzername auf dem SFTP Server
   * @param passwort Passwort auf dem SFTP Server
   * @throws JobException exception
   */
  public void connect(String server, String usr, String passwort) throws JobException
  {
    try
    {
      //Maximal 3 Versuche eine Verbindung herzustellen
      for (int i = 0; i < 3; i++)
      {
        if (openConnection(server, usr, passwort))
          return;
        else
          pause(5000);
      }
      throw new JobException("Konnte keine Verbindung zu Server -" + server + "- aufbauen.\n" + "User: -" + usr + "-\n" + "Passwort: -" + passwort + "-\n");
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  public void pause(long milliseconds) throws InterruptedException
  {
    Thread.sleep(milliseconds);
  }

  /**
   * oeffnet eine Verbindung zum server
   *
   * @param server   Servername des zu verbindenden Servers
   * @param usr      User zum Anmelden am Server
   * @param passwort Passwort zum Anmelden am Server
   * @return true wenn erfolgreich angemeldet; ansonsten
   */
  public boolean openConnection(String server, String usr, String passwort)
  {
    this.session = null;
    this.sftpChannel = null;
    try
    {
      this.session = this.jsch.getSession(usr, server);
      this.session.setPassword(passwort);
      HostKeyRepository hkr = this.jsch.getHostKeyRepository();
      for (HostKey hk : hkr.getHostKey())
      {
        String host = hk.getHost();
        // Hosts sieht evtl so aus:
        // konvert2, 172.20.60.222
        // konvert2
        int kommaPos = host.indexOf(",");
        if (kommaPos > 0)
        {
          host = host.substring(0, kommaPos);
        }
        if (host.equalsIgnoreCase(server))
        {
          String type = hk.getType();
          this.session.setConfig("server_host_key", type);
          logger.info("Verwende Known-Host '" + host + "' mittels " + type);
        }
      }
      this.session.connect();
      this.sftpChannel = (ChannelSftp) this.session.openChannel("sftp");
      this.sftpChannel.connect();
      return true;
    } catch (JSchException e)
    {
      if (e.getCause() instanceof java.net.UnknownHostException)
      {
        logger.error("Der angegebene Server " + server + " ist nicht bekannt!");
      } else
      {
        logger.error("Der Fingerprint des Servers " + server + " ist nicht bekannt! Bitte loggen Sie sich zuerst manuell ein und bestaetigen den Schluessel");
      }
      logger.error(e.getMessage());
      return false;
    }
  }

  /**
   * Die Methode storeFileSimple speichert eine Datei auf einem SFTP Server.
   *
   * @param filenamekomplett der Komplette Name der zu kopierenden Datei
   * @param verz             das Verzeichnis, in dem die Datei geschrieben werden soll
   * @param filename         der Ziel Verzeichnisname ohne Pfadangabe
   * @throws JobException im Fehlerfall
   */
  public void storeFileSimple(String filenamekomplett, String verz, String filename) throws JobException
  {
    cwd(verz);
    //Zunaechst als temp.-File uebertragen
    String tempfile = "Temp." + filename;
    try
    {
      this.sftpChannel.put(filenamekomplett, tempfile);
    } catch (Exception ex)
    {
      logger.error(ex.getMessage());
      throw new JobException("Datei " + tempfile + " konnte nicht kopiert werden:" + ex.getMessage());
    }
    //wieder in Originalname umbenennen
    rename(tempfile, filename);
  }

  /**
   * Bennennt Datei um
   *
   * @param filevon  alter Name
   * @param filenach neuer Name
   * @throws JobException im Fehlerfall
   */
  public void rename(String filevon, String filenach) throws JobException
  {
    try
    {
      this.sftpChannel.rename(filevon, filenach);
    } catch (Exception ex)
    {
      logger.error(ex.getMessage());
      throw new JobException("Datei konnte nicht von " + filevon + " nach " + filenach + " umbenannt werden:" + ex.getMessage());
    }
  }

  /**
   * Trennt die Verbindung zum Server
   *
   * @throws JobException im Fehlerfall
   */
  public void disconnect() throws JobException
  {
    try
    {
      this.sftpChannel.quit();
      this.session.disconnect();
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  /**
   * Wechseln des Verzeichnisses auf dem Server
   *
   * @param cwd das zu wechselnde Verzeichnis
   * @throws JobException im Fehlerfall
   */
  public void cwd(String cwd) throws JobException
  {
    try
    {
      this.sftpChannel.cd(cwd);
    } catch (Exception ex)
    {
      logger.error(ex.getMessage());
      throw new JobException("Konnte nicht in Verzeichnis " + cwd + " wechseln:" + ex.getMessage());
    }
  }
}
