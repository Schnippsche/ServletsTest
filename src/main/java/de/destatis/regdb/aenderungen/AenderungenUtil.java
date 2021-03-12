package de.destatis.regdb.aenderungen;

import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.util.Arrays;
import java.util.HashSet;

public class AenderungenUtil
{
  private static final String SQL_AENDERUNGEN_DIREKTEINTRAG = "(SELECT ae.* FROM aenderung AS ae WHERE AMT = ? AND STATISTIK_ID = ? AND TYP=? AND STATUS IN('NEU', 'ERLEDIGT','BEARBEITET') AND (aenderungsart & ?) AND (ae.STATUS_DIREKTEINTRAG & ? = 0)) UNION ( SELECT ae.* FROM aenderung AS ae INNER JOIN melder AS m ON (m.melder_id = ae.melder_id) INNER JOIN adressen AS a ON (m.adressen_id = a.adressen_id) INNER JOIN quell_referenz_verwaltung AS q ON (a.quell_referenz_id = q.quell_referenz_id) INNER JOIN melder_statistiken AS ms ON (m.melder_id = ms.melder_id) WHERE (q.amt=? OR q.amt = '') AND (q.statistik_id = ? OR q.statistik_id=0) AND ae.amt='' AND ae.statistik_id=0 AND ms.amt=? AND ms.statistik_id=? AND ms.STATUS != 'LOESCH' AND ae.STATUS IN('NEU', 'ERLEDIGT','BEARBEITET') AND typ=? AND q.STATUS != 'LOESCH' AND (ae.aenderungsart & ?) AND (ae.STATUS_DIREKTEINTRAG & ? = 0) ) ORDER BY aenderung_id LIMIT 1000";
  private static final String SQL_AENDERUNGEN_EXPORT_AENDERUNG = "(SELECT ae.* FROM aenderung AS ae WHERE AMT = ? AND STATISTIK_ID = ? AND TYP=? AND STATUS IN('NEU', 'ERLEDIGT', 'BEARBEITET') AND (aenderungsart & ?) AND (ae.STATUS_EXPORT_AENDERUNG & ? = 0)) UNION (SELECT ae.* FROM aenderung AS ae INNER JOIN melder AS m ON (m.melder_id = ae.melder_id) INNER JOIN adressen as a ON (m.adressen_id = a.adressen_id) INNER JOIN quell_referenz_verwaltung as q ON (a.quell_referenz_id = q.quell_referenz_id) INNER JOIN melder_statistiken as ms ON (m.melder_id = ms.melder_id) WHERE (q.amt=? OR q.amt = '') AND (q.statistik_id = ? OR q.statistik_id=0) AND ae.amt='' AND ae.statistik_id=0 AND ms.amt=? AND ms.statistik_id=? AND ms.STATUS != 'LOESCH' AND ae.STATUS IN('NEU', 'ERLEDIGT', 'BEARBEITET') AND typ=? AND q.STATUS != 'LOESCH' AND (ae.aenderungsart & ?) AND (ae.STATUS_EXPORT_AENDERUNG & ? = 0)) ORDER BY aenderung_id LIMIT 1000";
  private static final String[] _adressenSpalten = {"ANREDE", "NAME", "NAME_ERGAENZUNG", "KURZTEXT", "ABTEILUNG", "STRASSE", "HAUSNUMMER", "POSTLEITZAHL", "ORT", "POSTFACH", "POSTFACH_PLZ", "POSTFACH_ORT", "LAND", "TELEFON", "FAX", "EMAIL", "ZUSATZ1", "ZUSATZ2", "ZUSATZ3", "ZUSATZ4", "ZUSATZ5", "ZUSATZ6", "ZUSATZ7", "ZUSATZ8", "ZUSATZ9", "ZUSATZ10", "URS1", "URS2", "URS3", "URS4", "URS5", "URS6", "URS7"};
  private static final String[] _ansprechpartnerSpalten = {"AN_ANREDE", "AN_NAME", "AN_VORNAME", "AN_ABTEILUNG", "AN_TELEFON", "AN_MOBIL", "AN_FAX", "AN_EMAIL"};
  private static final String[] _firmenSpalten = {"FA_NAME", "FA_NAME_ERGAENZUNG", "FA_KURZTEXT"};
  private static final String[] _aenderungenSpalten = {"AENDERUNG_ID", "TYP", "AENDERUNGSART", "AMT", "STATISTIK_ID", "FIRMEN_ID", "ADRESSEN_ID", "ANSPRECHPARTNER_ID", "MELDER_ID", "QUELL_REFERENZ_OF", "DATUM", "FA_NAME", "FA_NAME_ERGAENZUNG", "FA_KURZTEXT", "BEMERKUNGEN", "SACHBEARBEITER_ID", "STATUS", "STATUS_EXPORT_AENDERUNG", "STATUS_DIREKTEINTRAG", "ZEITPUNKT_EINTRAG", "ZEITPUNKT_EXPORT", "ZEITPUNKT_AENDERUNG", "ZEITPUNKT_WWW"};
  private static final HashSet<String> _adressenMap = new HashSet<>(Arrays.asList(_adressenSpalten));
  private static final HashSet<String> _partnerMap = new HashSet<>(Arrays.asList(_ansprechpartnerSpalten));
  private static final HashSet<String> _firmenMap = new HashSet<>(Arrays.asList(_firmenSpalten));
  private static final HashSet<String> _aenderungTabelleMap = new HashSet<>(Arrays.asList(_aenderungenSpalten));
  private final LoggerIfc log;

  public AenderungenUtil()
  {
    this.log = Logger.getInstance().getLogger(this.getClass());
  }
}
