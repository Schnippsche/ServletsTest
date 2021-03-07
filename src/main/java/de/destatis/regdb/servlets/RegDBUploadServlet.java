/*
 * @(#)RegDBUploadServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.session.RegDBSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;

/**
 * Dient zum Ausfuehren von LoadData-Anweisungen auf dem Server
 *
 * @author Stefan Toengi (Destatis)
 */
public class RegDBUploadServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB upload servlet.
   */
  public RegDBUploadServlet()
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
    super.init(RegDBUploadServlet.class);
  }

  /**
   * Do service.
   *
   * @param req     the req
   * @param res     the res
   * @param conn    the conn
   * @param session the session
   */
  /*
   * (non-Javadoc)
   * @see de.destatis.regdb.servlets.RegDBGeneralHttpServlet#doService(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session)
  {
    try
    {
      File f = File.createTempFile("tmp", null);
      try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f), 4000); BufferedInputStream bis = new BufferedInputStream(req.getInputStream(), 4000))
      {
        for (int c; (c = bis.read()) != -1; )
        {
          bos.write((byte) c);
        }
        bos.flush();
      }
      this.log.info("Kopieren erfolgreich");
      f.deleteOnExit();
      res.setContentType("text/html");
      res.addHeader("OriginalFile", f.getCanonicalPath());
      this.log.info("Upload von temporaerer Datei " + f.getName() + " erfolgreich. (" + f.length() + " bytes)");
    }
    catch (Exception e)
    {
      this.log.error(e.getMessage(), e);
      this.sendErgebnis(res, e);
    }

  }
}
