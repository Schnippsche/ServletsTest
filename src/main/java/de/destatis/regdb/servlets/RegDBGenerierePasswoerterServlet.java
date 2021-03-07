/*
 * @(#)RegDBGenerierePasswoerterServlet.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.servlets;

import de.destatis.regdb.session.RegDBSession;
import de.werum.sis.idev.intern.actions.util.MelderDaten;
import de.werum.sis.idev.intern.actions.util.MelderDienst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.MessageFormat;

public class RegDBGenerierePasswoerterServlet extends RegDBGeneralHttpServlet
{

  /**
   * Instantiates a new reg DB generiere passwoerter servlet.
   */
  public RegDBGenerierePasswoerterServlet()
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
    super.init(RegDBGenerierePasswoerterServlet.class);
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
    if (command instanceof String) // Tabellenname
    {
      String kennung = session.getSachbearbeiterKennung();
      String passwort = session.getSachbearbeiterPasswort();
      MelderDienst melderDienst = new MelderDienst(interneAblaeufeHost, "" + interneAblaeufePort, kennung, passwort);
      String tabellenname = (String) command;
      // INDEX_ID muss PRIMARY KEY sein, damit die Updates im Resultset funktionieren
      String cmd = "SELECT INDEX_ID, SCHLUESSELAKTION, PASSWORT, SYSTEM_PASSWORT, PRIVATER_SCHLUESSEL,OEFFENTLICHER_SCHLUESSEL,PRIVATER_SCHLUESSEL_GESCHUETZT,OEFFENTLICHER_SCHLUESSEL_GESCHUETZT FROM " + tabellenname + " WHERE SCHLUESSELAKTION IN('NEU','UPDATE')";

      try (ResultSet rs = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery(cmd))
      {
        long ti_start = System.currentTimeMillis();
        int counterNeu = 0, counterUpdate = 0;
        while (rs.next())
        {
          String systemPasswort = rs.getString("SYSTEM_PASSWORT");
          if (systemPasswort == null || systemPasswort.trim().length() == 0)
          {
            systemPasswort = "";
          }
          MelderDaten melderDaten;
          if ("NEU".equals(rs.getString("SCHLUESSELAKTION")))
          {
            // Passwort vorgegeben ?
            // if (systemPasswort.length() > 0)
            // melderDaten = melderDienst.schluesselGenerieren(systemPasswort);
            // else
            melderDaten = melderDienst.passwortUndSchluesselGenerieren();
            counterNeu++;
            rs.updateBytes("PRIVATER_SCHLUESSEL", melderDaten.getPrivaterSchluessel());
            rs.updateBytes("OEFFENTLICHER_SCHLUESSEL", melderDaten.getOeffentlicherSchluessel());
          }
          else
          // UPDATE
          {
            // Passwort vorgegeben ?
            // if (systemPasswort.length() > 0)
            // melderDaten = melderDienst.schluesselGenerieren(systemPasswort);
            // else
            melderDaten = melderDienst.passwortGenerieren(rs.getBytes("PRIVATER_SCHLUESSEL"), rs.getBytes("OEFFENTLICHER_SCHLUESSEL"));
            counterUpdate++;
          }
          rs.updateString("PASSWORT", melderDaten.getPasswort());
          rs.updateString("SYSTEM_PASSWORT", melderDaten.getSystemPasswort());
          rs.updateBytes("PRIVATER_SCHLUESSEL_GESCHUETZT", melderDaten.getPrivaterSchluesselGeschuetzt());
          rs.updateBytes("OEFFENTLICHER_SCHLUESSEL_GESCHUETZT", melderDaten.getOeffentlicherSchluesselGeschuetzt());
          rs.updateRow();
        }
        long diff = (System.currentTimeMillis() - ti_start) / 1000;
        String info = MessageFormat.format("Schluesselgenerierung fuer {0} neue und {1} bestehende Eintraege in {2} Sekunden abgeschlossen", counterNeu, counterUpdate, diff);
        this.log.info(info);
      }
    }
    this.sendErgebnis(res, "Okay");
  }

}
