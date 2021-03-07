/*
 * @(#)RegDBDownloadServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.dateiimport.MelderDatenService;
import de.destatis.regdb.db.AufraeumUtil;
import de.destatis.regdb.session.RegDBSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;

public class RegDBDownloadServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB download servlet.
   */
  public RegDBDownloadServlet()
  {
    super();
  }

  /**
   * Initialisiert.
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#init()
   */
  @Override
  public void init()
  {
    super.init(RegDBDownloadServlet.class);
  }

  /**
   * Do service.
   *
   * @param req     the req
   * @param res     the res
   * @param conn    the conn
   * @param session the session
   * @throws Exception the exception
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#doService(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session) throws Exception
  {
    String mainJobId = req.getParameter("mainJobId");
    String dateiImportDir = MelderDatenService.getInstance().getDateiImportDir();
    File logFile = Paths.get(dateiImportDir, mainJobId, AufraeumUtil.PROKOLL_DATEINAME).toFile();

    if (!logFile.exists())
    {
      // Logfile existiert nicht , vielleicht wurde der Server unerwartet beendet
      // Versuche, nochmal die Log zu erstellen
      {
        this.log.info("Versuche, Importlogdatei zu erstellen");
        AufraeumUtil aufraeumUtil = new AufraeumUtil();
        aufraeumUtil.erzeugeProtokollArchiv(mainJobId, AufraeumUtil.PROKOLL_DATEINAME);
      }
    }
    this.log.info("starte Download von " + logFile);
    res.setContentType("application/octet-stream");
    res.setContentLength((int) logFile.length());
    res.setHeader("Content-Disposition", "attachment; filename=" + logFile.getName());
    Files.copy(logFile.toPath(), res.getOutputStream());
    /*
     * try (BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream(), 4000); BufferedInputStream bis = new
     * BufferedInputStream(new FileInputStream(logFile), 4000))
     * {
     * for (int c; (c = bis.read()) != -1;)
     * {
     * bos.write((byte) c);
     * }
     * bos.flush();
     * }
     */
  }
}