/*
 * Copyright (c) 2004 Statistisches Bundesamt
 * Datei: FTP.java
 * Änderungshistorie:
 * Nr ! Datum ! Name ! Änderungsgrund
 * -----------------------------------------------------------------------------
 * 1 ! 7.2.2004 ! Schindler ! neu erstellt
 */
package de.destatis.regdb;

import de.werum.sis.idev.res.job.JobException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Die Klasse <code>FTP</code> bietet Tools für das
 * Arbeiten mittels FTP
 * <p>
 *
 * @author Töngi
 */
public class FTP
{

  /**
   * Field ftp
   */
  private final FTPClient ftp;

  /**
   * Instantiates a new Ftp.
   */
  public FTP()
  {
    this.ftp = new FTPClient();
    this.ftp.setDataTimeout(9999);
  }

  /**
   * Mit dem connect baut man eine Verbindung zu einem FTP Server auf.
   * (hier: UNIX,LINUX,WINDOWS)
   *
   * @param server - DNS oder IP Name des Servers
   * @param port   - Port, auf dem der FTP Server lauscht (normalerweise Port 21)
   * @param usr    - Benutzername auf dem FTP Server
   * @param pwd    - Passwort auf dem FTP Server
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean connect(String server, int port, String usr, String pwd) throws JobException
  {
    boolean result;
    try
    {
      this.ftp.connect(server, port);
      result = this.ftp.login(usr, pwd);
      this.ftp.enterLocalPassiveMode();
    } catch (Exception ex)
    {
      throw new JobException(" Es konnte keine Verbindung zum Ftp-Server: -" + server + "- hergestellt werden.\nPort: -" + port + "-\nUser: -" + usr + "-\nPasswort: -" + pwd + "-");
    }
    return result;
  }

  /**
   * Mit dem connect baut man eine Verbindung zu einem FTP Server auf.
   * (hier: BS2000)
   *
   * @param server  - DNS oder IP Name des Servers
   * @param port    - Port, auf dem der FTP Server lauscht (normalerweise Port 21)
   * @param usr     - Benutzername auf dem FTP Server
   * @param pwd     - Passwort auf dem FTP Server
   * @param account - Account auf dem FTP Server
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean connect(String server, int port, String usr, String pwd, String account) throws JobException
  {
    boolean result;
    try
    {
      this.ftp.connect(server, port);
      result = this.ftp.login(usr, pwd, account);
    } catch (Exception ex)
    {
      throw new JobException(" Es konnte keine Verbindung zum Ftp-Server: -" + server + "- hergestellt werden.\nPort: -" + port + "-\nUser: -" + usr + "-\nPasswort: -" + pwd + "-");
    }
    return result;
  }

  /*************************************************************************
   * Die Methode receiveFile empfängt eine Datei und speichert sie auf dem
   * lokalen Pfad.
   *
   * @param remoteFile - Name der zu empfangenden Datei.
   * @param localDir - Pfad für speichern der zu empfangenden Datei.
   * @return boolean true = Empfangen OK, false Empfangen fehlerhaft.
   * @throws JobException the job exception
   */
  public boolean receiveFile(String remoteFile, String localDir) throws JobException
  {
    boolean result = false;
    try
    {
      FTPFile[] files = this.ftp.listFiles();
      if (files != null)
      {
        for (FTPFile file : files)
        {
          if (file.getName().compareToIgnoreCase(remoteFile) == 0)
          {
            this.ftp.setFileType(2);
            FileOutputStream fileoutputstream = new FileOutputStream(localDir + remoteFile);
            result = this.ftp.retrieveFile(remoteFile, fileoutputstream);
            fileoutputstream.close();
          }
        }
      }
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
    return result;
  }

  /**
   * Store file simple boolean.
   * Die Methode storeFileSimple speichert eine Datei auf einem FTP Server.
   * Eine Verbindung muß bereits bstehen(siehe Constructor).
   *
   * @param sourceFile the source file
   * @param remotefile the remotefile
   * @param verz       the verz
   * @param modus      the modus
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean storeFileSimple(File sourceFile, String remotefile, String verz, String modus) throws JobException
  {
    try
    {
      // Falls Verzeichnis angegeben dann versuche dahin zu wechseln
      if (verz != null && verz.length() > 0 && !this.ftp.changeWorkingDirectory(verz))
        throw new JobException("Es konnte nicht aus Verzeichnisebene " + getDirectory() + " zum Zielverzeichnis -" + verz + "- gewechselt werden.");

      if ("BINARY".equals(modus))
        this.ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
      else
        this.ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);

      FileInputStream fileinputstream = new FileInputStream(sourceFile);
      boolean result = this.ftp.storeFile("tmp" + remotefile, fileinputstream);
      fileinputstream.close();
      if (!result)
        throw new JobException("Datei -" + sourceFile + "- konnte nicht kopiert werden!");
      // Umbenennen
      return this.ftp.rename("tmp" + remotefile, remotefile);
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  /**
   * Gets files list.
   *
   * @return the files list
   * @throws JobException the job exception
   */
  public List<String> getFilesList() throws JobException
  {
    try
    {
      FTPFile[] files = this.ftp.listFiles();
      if (files != null)
      {
        List<String> hm = new ArrayList<>();
        for (FTPFile file : files)
        {
          hm.add(file.getName());
        }
        return hm;
      }
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
    return Collections.emptyList();
  }

  /**
   * Rename boolean.
   *
   * @param filevon  the filevon
   * @param filenach the filenach
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean rename(String filevon, String filenach) throws JobException
  {
    try
    {
      return this.ftp.rename(filevon, filenach);
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  /**
   * Gets directory.
   *
   * @return the directory
   * @throws JobException the job exception
   */
  public String getDirectory() throws JobException
  {
    try
    {
      return this.ftp.printWorkingDirectory();
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  /**
   * Check file boolean.
   *
   * @param remoteFile the remote file
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean checkFile(String remoteFile) throws JobException
  {
    try
    {
      FTPFile[] files = this.ftp.listFiles();
      if (files != null)
      {
        for (FTPFile file : files)
        {
          if (file.getName().compareToIgnoreCase(remoteFile) == 0)
          {
            return true;
          }
        }
      }
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
    return false;
  }

  /**
   * Check file size long.
   *
   * @param remoteFile the remote file
   * @return the long
   * @throws JobException the job exception
   */
  public long checkFileSize(String remoteFile) throws JobException
  {
    try
    {
      FTPFile[] files = this.ftp.listFiles();
      if (files != null)
      {
        for (FTPFile file : files)
        {
          if (file.getName().equalsIgnoreCase(remoteFile))
          {
            return file.getSize();
          }
        }
      }
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
    return 0;
  }

  /**
   * Delete file boolean.
   *
   * @param remoteFile the remote file
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean deleteFile(String remoteFile) throws JobException
  {
    boolean result = false;
    try
    {
      if (checkFile(remoteFile))
      {
        result = this.ftp.deleteFile(remoteFile);
      }
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
    return result;
  }

  /**
   * Make dir boolean.
   *
   * @param directory the directory
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean makeDir(String directory) throws JobException
  {
    try
    {
      return this.ftp.makeDirectory(directory);
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  /**
   * Disconnect boolean.
   *
   * @return the boolean
   */
  public boolean disconnect()
  {
    try
    {
      this.ftp.logout();
      this.ftp.disconnect();
      return true;
    } catch (Exception ex)
    {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Cwd boolean.
   *
   * @param cwd the cwd
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean cwd(String cwd) throws JobException
  {
    try
    {
      return this.ftp.changeWorkingDirectory(cwd);
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }

  /**
   * Login boolean.
   *
   * @param usr the usr
   * @param pwd the pwd
   * @return the boolean
   * @throws JobException the job exception
   */
  public boolean login(String usr, String pwd) throws JobException
  {
    try
    {
      return this.ftp.login(usr, pwd);
    } catch (Exception ex)
    {
      throw new JobException(ex.getMessage());
    }
  }
}
