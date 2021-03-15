/*
 * @(#)RegDBMeldungenHolenServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.db.FileUtil;
import de.destatis.regdb.meldungen.MeldungenHolen;
import de.destatis.regdb.session.RegDBSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

public class RegDBMeldungenHolenServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB meldungen holen servlet.
   */
  public RegDBMeldungenHolenServlet()
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
    super.init(RegDBMeldungenHolenServlet.class);
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
    Object command = this.readCommand(req);
    String kennung = session.getSachbearbeiterKennung();
    String passwort = "";
    String sb_id = session.getSachbearbeiterId();
    String amt = null;
    String statistik_id = null;
    String bzr = null;
    String meldungs_ids = null;
    String statusUmsetzen = null;
    String meldeart = null;
    if (command instanceof Properties)
    {
      Properties prop = (Properties) command;
      amt = String.valueOf(prop.get("amt"));
      statistik_id = String.valueOf(prop.get("statistik_id"));
      bzr = String.valueOf(prop.get("bzr"));
      meldungs_ids = String.valueOf(prop.get("meldungs_ids"));
      statusUmsetzen = String.valueOf(prop.get("statusumsetzen"));
      passwort = String.valueOf(prop.get("regdbpasswort"));
      meldeart = req.getParameter("meldeart");
    }

    if (amt == null || amt.length() == 0)
    {
      throw new Exception("Der erforderliche Parameter amt wurde nicht gesetzt.");
    }

    if (statistik_id == null || statistik_id.length() == 0)
    {
      throw new Exception("Der erforderliche Parameter statistik_id wurde nicht gesetzt.");
    }

    // Restlichen Parameter auslesen
    boolean doStatusChange = ("true".equals(statusUmsetzen) || "ja".equals(statusUmsetzen));
    MeldungenHolen meldungenHolen;
    if (meldungs_ids != null && meldungs_ids.length() > 0)
    {
      meldungenHolen = new MeldungenHolen(sb_id, kennung, passwort, amt, statistik_id, meldungs_ids, doStatusChange, conn);
    }
    else
    {
      meldungenHolen = new MeldungenHolen(sb_id, kennung, passwort, amt, statistik_id, bzr, meldeart, doStatusChange, conn);
    }
    meldungenHolen.setServerHost(interneAblaeufeHost);
    meldungenHolen.setServerPort("" + interneAblaeufePort);
    // meldungenHolen.setClient(req.getRemoteHost() + "(" + req.getRemoteAddr() + ")");
    meldungenHolen.setClient(req.getRemoteAddr());
    meldungenHolen.validate();
    File file = meldungenHolen.starteVerarbeitung();
    this.log.debug("Meldungsdatei " + file + " bereitgestellt. (" + file.length() + " bytes)");
    res.setContentType("application/zip");
    res.setContentLength((int) file.length());
    res.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
    res.setHeader("OriginalFile", meldungenHolen.getFileName());
    try (BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream(), 4000); BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 4000))
    {
      for (int c; (c = bis.read()) != -1; )
      {
        bos.write((byte) c);
      }
      bos.flush();
    }
    FileUtil.delete(file);
  }

}
