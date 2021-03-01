/*
 * @(#)RegDBAenderungenEintragenServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.destatis.regdb.aenderungen.AenderungenHolen;
import de.destatis.regdb.session.RegDBSession;

public class RegDBAenderungenEintragenServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB aenderungen eintragen servlet.
   */
  public RegDBAenderungenEintragenServlet()
  {
    super();
  }

  /**
   * Initialisiert.
   */
  @Override
  public void init()
  {
    super.init(RegDBAenderungenEintragenServlet.class);
  }

  /**
   * Do service.
   *
   * @param req the req
   * @param res the res
   * @param conn the conn
   * @param session the session
   * @throws Exception the exception
   */
  @Override
  public void doService(HttpServletRequest req, HttpServletResponse res, Connection conn, RegDBSession session) throws Exception
  {
    Object command = this.readCommand(req);
    String kennung = session.getSachbearbeiterKennung();
    String sb_id = session.getSachbearbeiterId();
    String amt = null;
    String statistik_id = null;
    String spalten = null;
    String typ = null;
    int aenderungsart = 15;
    if (command instanceof Properties)
    {
      Properties prop = (Properties) command;
      amt = String.valueOf(prop.get("amt"));
      statistik_id = String.valueOf(prop.get("statistik_id"));
      aenderungsart = Integer.parseInt(prop.getProperty("aenderungsart"));
      typ = String.valueOf(prop.getProperty("typ"));
      spalten = (String) prop.get("spalten"); // kann null sein ( = alle Spalten )
    }
    if (amt == null || amt.length() == 0)
    {
      throw new Exception("Der erforderliche Parameter amt wurde nicht gesetzt.");
    }
    if (statistik_id == null || statistik_id.length() == 0)
    {
      throw new Exception("Der erforderliche Parameter statistik_id wurde nicht gesetzt.");
    }
    AenderungenHolen aenderungenHolen = new AenderungenHolen(sb_id, kennung, amt, statistik_id, aenderungsart, typ, conn);
    aenderungenHolen.setClient(req.getRemoteAddr());
    aenderungenHolen.validateDirekteintrag();
    File file = aenderungenHolen.starteDirektEintrag(spalten);
    if (file == null)
    {
      this.sendErgebnis(res, "keine Aenderungen vorhanden!");
    }
    else
    {
      this.log.debug("Datei " + file + " bereitgestellt. (" + file.length() + " bytes)");
      res.setContentType("text/plain");
      res.setContentLength((int) file.length());
      res.setHeader("filename", aenderungenHolen.getZipFileName());
      res.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
      try (BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream(), 4000); BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 4000))
      {
        for (int c; (c = bis.read()) != -1;)
        {
          bos.write((byte) c);
        }
        bos.flush();
      }
      file.delete();
    }

  }

}
