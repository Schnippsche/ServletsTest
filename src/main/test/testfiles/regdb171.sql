DROP TABLE IF EXISTS `adressen`;
CREATE TABLE IF NOT EXISTS `adressen` (
  `ADRESSEN_ID` int(11) NOT NULL AUTO_INCREMENT,
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_ID` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_TYP` enum('MANUELL','IMPORT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MANUELL',
  `ROLLE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STRASSE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `HAUSNUMMER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTLEITZAHL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH_PLZ` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH_ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LAND` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ8` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ9` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ10` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDER_AENDERBAR` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `STATUSCODE_AENDERUNG` char(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DATUM_AENDERUNG` datetime DEFAULT NULL,
  `STATUS` enum('NEU','AEND','SPERRE','LOESCH','EXPORT','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_FREMDEXPORT` datetime DEFAULT NULL,
  PRIMARY KEY (`ADRESSEN_ID`),
  KEY `amt_index` (`AMT`),
  KEY `quell_id_index` (`QUELL_REFERENZ_ID`),
  KEY `quell_of_index` (`QUELL_REFERENZ_OF`),
  KEY `kurztext_index` (`KURZTEXT`(20)),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `adressen_melder_zp_www`;
CREATE TABLE IF NOT EXISTS `adressen_melder_zp_www` (
  `ADRESSEN_ID` int(11) NOT NULL,
  `MELDER_ID` int(11) NOT NULL,
  `ZEITPUNKT` datetime NOT NULL,
  PRIMARY KEY (`ADRESSEN_ID`,`MELDER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `aenderung`;
CREATE TABLE IF NOT EXISTS `aenderung` (
  `AENDERUNG_ID` int(11) NOT NULL AUTO_INCREMENT,
  `TYP` enum('REGISTRIERUNG','AEND_MELDER','AEND_AUSKUNFTPFL','ERWEIT_STATISTIK','ERWEIT_FIRMA','LOESCH_STATISTIK','LOESCH_FIRMA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'REGISTRIERUNG',
  `AENDERUNGSART` set('ADRESSE','FIRMA','ANSPRECHPARTNER','URS') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `ADRESSEN_ID` int(11) NOT NULL DEFAULT '0',
  `ANSPRECHPARTNER_ID` int(11) NOT NULL DEFAULT '0',
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATUM` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `FA_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FA_NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FA_KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STRASSE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `HAUSNUMMER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTLEITZAHL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH_PLZ` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH_ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LAND` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ8` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ9` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ10` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_VORNAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_MOBIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BEMERKUNGEN` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','LOESCH','ERLEDIGT','FEHLER','LOESCHKANDIDAT','BEARBEITET') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `STATUS_EXPORT_AENDERUNG` set('ADRESSE','FIRMA','ANSPRECHPARTNER','URS') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS_DIREKTEINTRAG` set('ADRESSE','FIRMA','ANSPRECHPARTNER','URS') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_EXPORT` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`AENDERUNG_ID`),
  KEY `amt_statistik_index` (`AMT`,`STATISTIK_ID`),
  KEY `firmen_index` (`FIRMEN_ID`),
  KEY `melder_index` (`MELDER_ID`),
  KEY `adressen_index` (`ADRESSEN_ID`),
  KEY `datum_index` (`DATUM`),
  KEY `ansprechpartner_index` (`ANSPRECHPARTNER_ID`),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `aenderung_auftrag`;
CREATE TABLE IF NOT EXISTS `aenderung_auftrag` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AENDERUNG_DATUM` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `STATUS` enum('NEU','BEAUFTRAGT','BESTAETIGT','UEBERNEHMEN','ABGEWIESEN','LOESCHEN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `STATUSCODE_AENDERUNG` char(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `REQUESTID` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATID` varchar(4) COLLATE utf8mb4_unicode_ci NOT NULL,
  `EDB_BZR` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL,
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ANSPRECHPARTNER_ID` int(11) NOT NULL DEFAULT '0',
  `AN_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_VORNAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ADRESSEN_ID` int(11) NOT NULL DEFAULT '0',
  `ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STRASSE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `HAUSNUMMER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTLEITZAHL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LAND` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `amt`;
CREATE TABLE IF NOT EXISTS `amt` (
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BEZEICHNUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AMT_NAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SB_FA` int(11) NOT NULL DEFAULT '0',
  `SB_IT` int(11) NOT NULL DEFAULT '0',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `START_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LOGO_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LOGO_LANG_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LOGO_GROSS_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TEXT_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `GAST_KENNUNG` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `GAST_PASSWORT` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `STATUS` enum('AKTIV','INAKTIV','SPERRE','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `SPERRE_INFO` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EXTERNE_FREIGABE` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`AMT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `amt` (`AMT`, `BEZEICHNUNG`, `AMT_NAME`, `SB_FA`, `SB_IT`, `KOMMENTAR`, `START_URL`, `LOGO_URL`, `LOGO_LANG_URL`, `LOGO_GROSS_URL`, `TEXT_URL`, `GAST_KENNUNG`, `GAST_PASSWORT`, `STATUS`, `SPERRE_INFO`, `EXTERNE_FREIGABE`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_AENDERUNG`, `ZEITPUNKT_WWW`) VALUES
	('00', 'Statistisches Bundesamt', NULL, 1, 1, '', '', 'img/BundLogo.gif', 'img/BundLogoLang.gif', '', '', NULL, NULL, 'AKTIV', '', 'J', '2003-12-11 15:45:31', '2004-01-07 08:51:05', '2004-01-07 08:51:05');


DROP TABLE IF EXISTS `ansprechpartner`;
CREATE TABLE IF NOT EXISTS `ansprechpartner` (
  `ANSPRECHPARTNER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VORNAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MOBIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','AEND','EXPORT','LOESCH','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_FREMDEXPORT` datetime DEFAULT NULL,
  PRIMARY KEY (`ANSPRECHPARTNER_ID`),
  KEY `name_index` (`NAME`(20)),
  KEY `vorname_index` (`VORNAME`(20)),
  KEY `status_index` (`STATUS`),
  KEY `sachbearbeiter_id_index` (`SACHBEARBEITER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `ansprechpartner_melder_zp_www`;
CREATE TABLE IF NOT EXISTS `ansprechpartner_melder_zp_www` (
  `ANSPRECHPARTNER_ID` int(11) NOT NULL,
  `MELDER_ID` int(11) NOT NULL,
  `ZEITPUNKT` datetime NOT NULL,
  PRIMARY KEY (`ANSPRECHPARTNER_ID`,`MELDER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `auto_pw_ruecksetzung`;
CREATE TABLE IF NOT EXISTS `auto_pw_ruecksetzung` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MELDER_ID` bigint(20) DEFAULT NULL,
  `MELDER_PW_RUECKSETZUNG_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PW_RUECKSETZUNG_ID` bigint(20) DEFAULT NULL,
  `STATUS` enum('OK','UNGUELTIG','UNZULAESSIG','NICHT_BESTAETIGT','FEHLER','ABGELAUFEN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `PROZESS_STATUS` enum('INITIIERT','BESTAETIGT','GESTARTET','ABGESCHLOSSEN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `STATUS_MELDUNG` mediumtext COLLATE utf8mb4_unicode_ci,
  `TICKET` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ZEITPUNKT_EINTRAG` datetime NOT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_ticket` (`TICKET`),
  KEY `melder_id_index` (`MELDER_ID`),
  KEY `status_index` (`STATUS`),
  KEY `prozess_status_index` (`PROZESS_STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `downloadinfo`;
CREATE TABLE IF NOT EXISTS `downloadinfo` (
  `DOWNLOAD_ID` int(11) NOT NULL AUTO_INCREMENT,
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_INT` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEI` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATUM` datetime DEFAULT NULL,
  `STATUS` enum('NEU','EXPORT','LOESCH','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`DOWNLOAD_ID`),
  KEY `amt_index` (`AMT`),
  KEY `statistik_index` (`STATISTIK_ID`),
  KEY `firmen_index` (`FIRMEN_ID`),
  KEY `melder_index` (`MELDER_ID`),
  KEY `quell_of_index` (`QUELL_REFERENZ_OF`),
  KEY `bzr_index` (`BZR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `erhebung`;
CREATE TABLE IF NOT EXISTS `erhebung` (
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AUF_BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ERSTER_MELDUNGSTERMIN` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LETZTER_MELDUNGSTERMIN` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATSPEZ_KEY` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FORMULAR_ID` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SB_GRUPPEN_ID` int(11) NOT NULL DEFAULT '0',
  `URL_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SERVLET_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `JSP_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDEDATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `PASSWD_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VBDATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VORBELEGUNGSABHAENGIG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `SENDEN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ZURUECKSETZEN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `PRUEFUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `LOKALSICHERUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `SERVERSICHERUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ARCHIVIERUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `WEITERE_MELDUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `EXPORT_CSV` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `UMFRAGE_URL` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UMFRAGE_FELD_NAME_AMT` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UMFRAGE_FELD_NAME_BZR` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UMFRAGE_FELD_NAME_ERHEBUNGS_ID` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UMFRAGE_FELD_NAME_QUELLREFERENZ_OF` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `BERICHTSPFLICHTIGE` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('AKTIV','INAKTIV','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_FREMDEXPORT` datetime DEFAULT NULL,
  PRIMARY KEY (`STATISTIK_ID`,`AMT`,`BZR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `filter`;
CREATE TABLE IF NOT EXISTS `filter` (
  `FILTER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) DEFAULT '0',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` varchar(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TYP` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEN` mediumblob,
  `DATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATUS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`FILTER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `firmen`;
CREATE TABLE IF NOT EXISTS `firmen` (
  `FIRMEN_ID` int(11) NOT NULL AUTO_INCREMENT,
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ANSPRECHPARTNER_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_IDENTIFIKATOR` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('NEU','AEND','SPERRE','LOESCH','EXPORT','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_FREMDEXPORT` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`FIRMEN_ID`),
  KEY `kurztext_index` (`KURZTEXT`(20)),
  KEY `name_index` (`NAME`(20)),
  KEY `ansprechpartner_index` (`ANSPRECHPARTNER_ID`),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `firmen_adressen`;
CREATE TABLE IF NOT EXISTS `firmen_adressen` (
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `ADRESSEN_ID` int(11) NOT NULL DEFAULT '0',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('AKTIV','LOESCH','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`FIRMEN_ID`,`ADRESSEN_ID`),
  KEY `adressen_index` (`ADRESSEN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `formular`;
CREATE TABLE IF NOT EXISTS `formular` (
  `FORMULAR_ID` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VERSION` int(11) NOT NULL DEFAULT '0',
  `FORMULAR_TITEL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TYP` enum('ERHEBUNG','STANDARD_EXT','STANDARD_INT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ERHEBUNG',
  `ZEITPUNKT_FORMULAR` datetime DEFAULT NULL,
  `SP_ERHEBUNG_KEY` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_SP_ERHEBUNG` datetime DEFAULT NULL,
  `SACHBEARBEITER_ID` int(11) DEFAULT NULL,
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_TEST` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `DATEN` mediumblob,
  `DATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `RESSOURCEN` mediumblob,
  `STATUS` enum('NEU','VORGAENGER','INAKTIV','TEST','WWW','SPERRE','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  PRIMARY KEY (`FORMULAR_ID`,`VERSION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `import_teil`;
CREATE TABLE IF NOT EXISTS `import_teil` (
  `IMPORT_TEIL_ID` int(11) NOT NULL AUTO_INCREMENT,
  `IMPORT_VERWALTUNG_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_START` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ZEITPUNKT_ENDE` datetime DEFAULT NULL,
  `ERGEBNIS_STATUS` enum('','OK','FEHLER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VORGANG` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BEMERKUNG` mediumtext COLLATE utf8mb4_unicode_ci,
  `ANZAHL_NEU` int(11) NOT NULL DEFAULT '0',
  `ANZAHL_GEAENDERT` int(11) NOT NULL DEFAULT '0',
  `ANZAHL_GELOESCHT` int(11) NOT NULL DEFAULT '0',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`IMPORT_TEIL_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `import_verwaltung`;
CREATE TABLE IF NOT EXISTS `import_verwaltung` (
  `IMPORT_VERWALTUNG_ID` int(11) NOT NULL AUTO_INCREMENT,
  `ZEITPUNKT_START` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_ENDE` datetime DEFAULT NULL,
  `GESAMT_STATUS` enum('AKTIV','BEENDET','ABBRUCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `ERGEBNIS_STATUS` enum('','OK','FEHLER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DATEINAME` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `STATISTIK_ID` int(11) DEFAULT NULL,
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_ID` int(11) DEFAULT NULL,
  `BEMERKUNG` mediumtext COLLATE utf8mb4_unicode_ci,
  `ANZAHL_NEU` int(11) NOT NULL DEFAULT '0',
  `ANZAHL_GEAENDERT` int(11) NOT NULL DEFAULT '0',
  `ANZAHL_GELOESCHT` int(11) NOT NULL DEFAULT '0',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`IMPORT_VERWALTUNG_ID`),
  KEY `serverimport_sb_index` (`SACHBEARBEITER_ID`),
  KEY `serverimport_quellref_index` (`QUELL_REFERENZ_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `job`;
CREATE TABLE IF NOT EXISTS `job` (
  `JOB_ID` int(11) NOT NULL AUTO_INCREMENT,
  `ZEITPUNKT_START` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ZEITPUNKT_ENDE` datetime DEFAULT NULL,
  `STATUS` enum('AKTIV','BEENDET') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `JOB_KLASSE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `JOB_NAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ERGEBNIS_STATUS` enum('','OK','FEHLER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ERGEBNIS_INT` int(11) NOT NULL DEFAULT '0',
  `LOG` mediumtext COLLATE utf8mb4_unicode_ci,
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`JOB_ID`),
  KEY `job_klasse_index` (`JOB_KLASSE`(50)),
  KEY `job_name_index` (`JOB_NAME`(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `konfiguration`;
CREATE TABLE IF NOT EXISTS `konfiguration` (
  `KONFIGURATION_ID` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `WERT_STRING` varchar(4000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `WERT_INT` int(11) DEFAULT NULL,
  `WERT_DATE` datetime DEFAULT NULL,
  `WERT_BLOB` blob,
  `TYP` enum('INTERN','EXTERN','BEIDE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INTERN',
  `STATUS` enum('AKTIV','INAKTIV','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  PRIMARY KEY (`KONFIGURATION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `konfiguration` (`KONFIGURATION_ID`, `WERT_STRING`, `WERT_INT`, `WERT_DATE`, `WERT_BLOB`, `TYP`, `STATUS`, `SACHBEARBEITER_ID`, `ZEITPUNKT_AENDERUNG`, `ZEITPUNKT_WWW`, `ZEITPUNKT_EINTRAG`) VALUES
	('EMAIL_SMTP', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('ext_admindaemon_intervall', '60', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_adresse', 'false', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_amt', 'false', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_bzr', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_firma', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_formularsequenz', 'false', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_statistik', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_auswahl_vorbelegung', 'false', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_adresse', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_berichtspflichtige', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_erinnerungsservice', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_melderkonto', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_passwort', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_registrierung', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_benutzerdaten_zusammenfuehrung', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_certificate_dir', 'C:/idev47/idev/programme/tomcat/conf/certs', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_datei_segment_size', '3145728', NULL, NULL, NULL, 'BEIDE', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_download_datei_path', 'C:/idev47/idev/work_ext/dateien', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_file_location_adressen', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_file_location_ansprechpartner', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_file_location_filter', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('ext_file_location_melderkonto', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_file_location_satzmuster', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_file_location_template', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_file_location_vorbelegung_verwaltung', 'database', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_formular_dir', 'C:/idev47/idev/work_ext/form', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('ext_formularauswahl', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_hilfe_allgemein', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_hilfe_kurzreferenz', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_hilfe_statistik', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_idev_laufdir', 'C:/idev47/idev/work_ext/lauf', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_idev_programdir', 'C:/idev47/idev/programme', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_installations_kennung', '0000', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_kontext_amt', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_kontext_bzr', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_login_informationen', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_login_registrierung', 'true', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_max_sessions', '1000000', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_maximale_import_direkt_dateigroesse', '1048576', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('ext_maximale_import_in_formular_dateigroesse', '102400', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('ext_meldungs_directory', 'C:/idev47/idev/work_ext/daten', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_recovery_dir', 'C:/idev47/idev/work_ext/recovery', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_sicherung_directory', 'C:/idev47/idev/work_ext/sicherung', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_sicherung_warnung_minuten', '30', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_sicherung_zeilen_limit', '1500', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_update_archivempfangsdir', 'C:/idev47/idev/work_ext/import', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_vorbel_dir', 'C:/idev47/idev/work_ext/vorbel', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('ext_webdirpath', 'C:/idev47/idev/programme/tomcat/webapps/idev', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('freigabe_melderkonto', 'freigegeben', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('freigabe_meldeverfahren', 'freigegeben', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('freigabe_meldewege', 'Statistik/Amt', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('freigabe_meldezugang', 'Meldeberechtigung', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('HTTP_PROXY_HOST', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('HTTP_PROXY_PASSWORT', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('HTTP_PROXY_PORT', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('HTTP_PROXY_USER', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('int_admindaemonserver_url', 'https://localhost:8070', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_archiv_directory', 'C:/idev47/idev/work/archive', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_archivdaemon_start_stunde', '3', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_auto_adrchg_intervall', '60', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_auto_export_intervall', '60', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_auto_import_dbsicherung_intervall', '60', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_auto_import_intervall', '60', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_auto_transfer_dir', 'C:/idev47/idev/work/transfer', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_auto_transfer_meldung', 'false', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_dateiimport_directory', 'C:/idev47/idev/work/dateiimport', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-12 11:08:19', '0000-00-00 00:00:00', '2021-02-12 11:08:19'),
	('int_dateiimport_interval', '2', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-12 11:08:19', '0000-00-00 00:00:00', '2021-02-12 11:08:19'),
	('int_dateiimport_limit', '1555', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-12 11:10:42', '0000-00-00 00:00:00', '2021-02-12 11:10:42'),
	('int_dateiimport_loeschinterval', '1', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-12 11:08:19', '0000-00-00 00:00:00', '2021-02-12 11:08:19'),
	('int_dateiimport_loeschlimit', '999', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-12 11:10:42', '0000-00-00 00:00:00', '2021-02-12 11:10:42'),
	('int_dateitransfer_targetdir', 'C:/idev47/idev/work/import', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_db_export_targetdir', 'C:/idev47/idev/work/export', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_dbsicherungtransfer_targetdir', 'C:/idev47/idev/work/sicherung', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_edb_basedir', 'int_edb_basedir', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_edb_endpointuri', 'endpointuri', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_edb_passwd', 'passwd', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_edb_username', 'username', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_email_host', 'mail.ads.stba.de', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_email_passwort', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_email_port', '25', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_email_protokoll', 'smtp', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_email_user', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_file_location_meldung', 'database', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_formular_dir', 'C:/idev47/idev/work/form', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_idev_laufdir', 'C:/idev47/idev/work/lauf/config', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_idev_programdir', 'C:/idev47/idev/programme', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_idev_url', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_logs_dir', 'C:/idev47/idev/work/logs', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_meldung_max_size_db', '10M', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('int_meldungstatistikdaemon_start_stunde', '1', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '2021-02-10 08:11:27', NULL, '2021-02-10 08:11:27'),
	('int_process_melderkonto_export', 'true', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_pw_email_absender_adresse', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_pw_email_absender_name', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_pw_email_betreff', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_pw_email_inhalt', NULL, NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_recovery_dir', 'C:/idev47/idev/work/recovery', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_sicherung_directory', 'C:/idev47/idev/work/sicherung', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_sicherung_warnung_minuten', '30', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_sicherung_zeilen_limit', '1500', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_temp_directory', 'C:/idev47/idev/work/temp', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_wartung_servicedir', 'C:/idev47/idev/work/service', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_web_intern_url', 'https://localhost:8050', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('int_webdirpath', 'C:/idev47/idev/programme/tomcat/internalwebapps/idev_web_intern', NULL, NULL, NULL, 'INTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('KONSISTENZENDIALOG_UPDATE_INTERVALL', '20', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('MAN_PW_SERIENBRIEF_ADRESS_SCHEMA', '<FIRMA_NAME> <FIRMA_NAMENSERGAENZUNG> <FIRMA_KURZTEXT><br><ADRESSE_ABTEILUNG><br><ANFRAGE_ANSPRECHPARTNER><br><ADRESSE_STRASSE> <ADRESSE_HAUSNUMMER><br><br><ADRESSE_POSTFACH><br><ADRESSE_POSTLEITZAHL> <ADRESSE_ORT><br><ADRESSE_POSTFACH_PLZ> <ADRESSE_POSTFACH_ORT><br><ADRESSE_LAND>', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('MAN_PW_TAGE_WARNHINWEIS', '30', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('pattern_berichtsempfaenger', '', NULL, NULL, NULL, 'EXTERN', 'AKTIV', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00', '2021-02-10 08:11:27'),
	('SICHERUNGSDIALOG_UPDATE_INTERVALL', '60', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_ACCOUNT', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_FORM', 'EINZEL', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_HOST', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_MAIL_ABSENDER', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_MAIL_BETREFF', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_MAIL_BLOCKSIZE', '10', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_MAIL_EMPFAENGER', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_MAIL_TEXT', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_MODUS', 'ASCII', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_PASSWORT', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_PLATTFORM', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_USER', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19'),
	('TRANSFER_ZIEL_VERZEICHNIS', '', NULL, NULL, NULL, 'INTERN', 'AKTIV', 1, NULL, NULL, '2021-02-12 11:08:19');


DROP TABLE IF EXISTS `lauf`;
CREATE TABLE IF NOT EXISTS `lauf` (
  `LAUF_ID` int(11) NOT NULL AUTO_INCREMENT,
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TABELLE` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `TYP` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `ART` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATUS` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`LAUF_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `laufinfo`;
CREATE TABLE IF NOT EXISTS `laufinfo` (
  `LAUF_INFO_ID` int(11) NOT NULL AUTO_INCREMENT,
  `LAUF_ID` int(11) NOT NULL DEFAULT '0',
  `TYP` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `SATZ` mediumblob,
  `INFO` mediumtext COLLATE utf8mb4_unicode_ci,
  `KORREKTUR_MOEGLICH` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `ANZ_PARAMS_KORREKTUR` int(11) DEFAULT NULL,
  `KORREKTUR_INFO` mediumtext COLLATE utf8mb4_unicode_ci,
  `STATUS` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`LAUF_INFO_ID`),
  KEY `lauf_id_index` (`LAUF_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `log_melder_aktion`;
CREATE TABLE IF NOT EXISTS `log_melder_aktion` (
  `ZEITPUNKT` bigint(20) NOT NULL,
  `SESSION_ID` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `KENNUNG` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `AKTION` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DAUER` bigint(20) DEFAULT NULL,
  `STATUS` int(11) DEFAULT NULL,
  `ZUSATZINFO` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ZEITPUNKT`,`SESSION_ID`,`KENNUNG`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `log_server`;
CREATE TABLE IF NOT EXISTS `log_server` (
  `TYPE` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ZEITPUNKT` bigint(20) NOT NULL,
  `INFO` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`TYPE`,`ZEITPUNKT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `log_session`;
CREATE TABLE IF NOT EXISTS `log_session` (
  `SESSION_ID` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `KENNUNG` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ZEITPUNKT_START` bigint(20) DEFAULT NULL,
  `ZEITPUNKT_ENDE` bigint(20) DEFAULT NULL,
  `LETZTE_AKTION` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `STATUS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ZEITPUNKT_LOESCH` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`SESSION_ID`),
  KEY `zeitpunkt_ende_index` (`ZEITPUNKT_ENDE`),
  KEY `zeitpunkt_loesch_index` (`ZEITPUNKT_LOESCH`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `meldeberechtigte`;
CREATE TABLE IF NOT EXISTS `meldeberechtigte` (
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATUM` date NOT NULL DEFAULT '2000-01-01',
  `ANZAHL_MELDER` int(11) NOT NULL DEFAULT '0',
  `ANZAHL_VB_MELDER` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AMT`,`STATISTIK_ID`,`BZR`,`DATUM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melder`;
CREATE TABLE IF NOT EXISTS `melder` (
  `MELDER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `ADRESSEN_ID` int(11) NOT NULL DEFAULT '0',
  `ANSPRECHPARTNER_ID` int(11) NOT NULL DEFAULT '0',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `KENNUNG` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SYSTEM_PASSWORT` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PASSWORT` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PW_RUECKSETZUNG_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PW_RUECKSETZUNG_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PW_RUECKSETZUNG_FRAGE` smallint(6) DEFAULT NULL,
  `PW_RUECKSETZUNG_ANTWORT` blob,
  `PRIVATER_SCHLUESSEL` blob,
  `OEFFENTLICHER_SCHLUESSEL` blob,
  `PRIVATER_SCHLUESSEL_GESCHUETZT` blob,
  `OEFFENTLICHER_SCHLUESSEL_GESCHUETZT` blob,
  `PASSWORT_AENDERBAR` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `PASSWORT_AENDERUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ZUSAMMENFUEHRBAR` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `BELEG_NR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('NEU','AEND','SPERRE','EXPORT','LOESCH','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `STATUS_VB` enum('','NEU','AEND','EXPORT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS_MK` enum('','NEU','AEND','EXPORT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS_WARTUNG` enum('INAKTIV','AKTIV') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INAKTIV',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_REGISTRIERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_VB` datetime DEFAULT NULL,
  `ZEITPUNKT_MK` datetime DEFAULT NULL,
  `ZEITPUNKT_ANSCHREIBEN` datetime DEFAULT NULL,
  `ZEITPUNKT_FREMDEXPORT` datetime DEFAULT NULL,
  PRIMARY KEY (`MELDER_ID`),
  KEY `firmen_index` (`FIRMEN_ID`),
  KEY `adressen_index` (`ADRESSEN_ID`),
  KEY `ansprechpartner_index` (`ANSPRECHPARTNER_ID`),
  KEY `kennung_index` (`KENNUNG`),
  KEY `beleg_index` (`BELEG_NR`(30)),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melderkonto`;
CREATE TABLE IF NOT EXISTS `melderkonto` (
  `MKTO_ID` int(11) NOT NULL AUTO_INCREMENT,
  `MELDUNG_ID` int(11) NOT NULL DEFAULT '0',
  `DATUM` datetime DEFAULT NULL,
  `MELDEART` enum('FORMULAR','DATEI','CSVSERVICE','CORE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FORMULAR',
  `DATEI` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_INT` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_TICKET` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF8` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF9` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MKF10` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `PROTOKOLL` mediumblob,
  `PROTOKOLL_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ARCHIV` mediumblob,
  `ARCHIV_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ARCHIV_STATUS` enum('NICHT_VORHANDEN','VORHANDEN','ANGEFORDERT','NICHT_MOEGLICH') COLLATE utf8mb4_unicode_ci DEFAULT 'NICHT_VORHANDEN',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','AEND','LOESCH','AKTIV','FEHLER','LOESCHKANDIDAT','STORNO_BEANTRAGT','STORNO_ABGEHOLT','STORNIERT','WIRD_ERSETZT','ERSETZT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_EXPORT` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`MKTO_ID`),
  UNIQUE KEY `transfer_ticket_index` (`TRANSFER_TICKET`),
  KEY `meldung_index` (`MELDUNG_ID`),
  KEY `melder_index` (`MELDER_ID`),
  KEY `firmen_index` (`FIRMEN_ID`),
  KEY `statistik_index` (`STATISTIK_ID`),
  KEY `amt_index` (`AMT`),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melderkonto_archiv_zeitpunkt`;
CREATE TABLE IF NOT EXISTS `melderkonto_archiv_zeitpunkt` (
  `MKTO_ID` int(11) NOT NULL,
  `ZEITPUNKT_ARCHIV` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melderkonto_layout`;
CREATE TABLE IF NOT EXISTS `melderkonto_layout` (
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATUM_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Meldezeitpunkt',
  `DATUM_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `DATUM_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATUM_RHF` tinyint(3) unsigned NOT NULL DEFAULT '1',
  `DATEI_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Originaldatei',
  `DATEI_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `DATEI_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEI_RHF` tinyint(3) unsigned NOT NULL DEFAULT '2',
  `FIRMA_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Unternehmen',
  `FIRMA_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `FIRMA_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FIRMA_RHF` tinyint(3) unsigned NOT NULL DEFAULT '3',
  `QR_OF_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Ordnungsnr.',
  `QR_OF_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `QR_OF_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QR_OF_RHF` tinyint(3) unsigned NOT NULL DEFAULT '4',
  `QR_INT_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QR_INT_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `QR_INT_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QR_INT_RHF` tinyint(3) unsigned NOT NULL DEFAULT '5',
  `BZR_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Berichtszeitraum',
  `BZR_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `BZR_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR_RHF` tinyint(3) unsigned NOT NULL DEFAULT '6',
  `TRTKT_UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRTKT_LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `TRTKT_AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRTKT_RHF` tinyint(3) unsigned NOT NULL DEFAULT '7',
  `L1UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L1LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L1AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L1RHF` tinyint(3) unsigned NOT NULL DEFAULT '8',
  `L2UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L2LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L2AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L2RHF` tinyint(3) unsigned NOT NULL DEFAULT '9',
  `L3UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L3LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L3AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L3RHF` tinyint(3) unsigned NOT NULL DEFAULT '10',
  `L4UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L4LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L4AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L4RHF` tinyint(3) unsigned NOT NULL DEFAULT '11',
  `L5UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L5LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L5AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L5RHF` tinyint(3) unsigned NOT NULL DEFAULT '12',
  `L6UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L6LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L6AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L6RHF` tinyint(3) unsigned NOT NULL DEFAULT '13',
  `L7UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L7LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L7AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L7RHF` tinyint(3) unsigned NOT NULL DEFAULT '14',
  `L8UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L8LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L8AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L8RHF` tinyint(3) unsigned NOT NULL DEFAULT '15',
  `L9UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L9LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L9AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L9RHF` tinyint(3) unsigned NOT NULL DEFAULT '16',
  `L10UEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L10LEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `L10AUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `L10RHF` tinyint(3) unsigned NOT NULL DEFAULT '17',
  `ZSUEB` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZSLEN` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `ZSAUS` enum('','LINKS','RECHTS','ZENTRIERT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZSRHF` tinyint(3) unsigned NOT NULL DEFAULT '18',
  `DATEN` mediumblob,
  `DATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','AEND','LOESCH','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`STATISTIK_ID`,`AMT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melder_mapping`;
CREATE TABLE IF NOT EXISTS `melder_mapping` (
  `MELDER_MAPPING_ID` int(11) NOT NULL AUTO_INCREMENT,
  `PATTERN` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SERVER_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SEQUENZ` int(11) NOT NULL DEFAULT '0',
  `KOMMENTAR` mediumtext COLLATE utf8mb4_unicode_ci,
  `STATUS` enum('NEU','SPERRE','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`MELDER_MAPPING_ID`),
  UNIQUE KEY `pattern_index` (`PATTERN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melder_statistiken`;
CREATE TABLE IF NOT EXISTS `melder_statistiken` (
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `ROLLE` enum('MELDER','VERTRETER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MELDER',
  `MELDERECHT_BZR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('NEU','AEND','SPERRE','LOESCH','EXPORT','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ERINNERUNGSSERVICE` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `IS_ADRESSEN_ID` int(11) NOT NULL DEFAULT '0',
  `IS_GRUND` enum('','ERZEUGUNG','VORBELEGUNG','BENUTZER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `IS_ZUSATZ_INFO` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `IS_STATUS` enum('','NEU','EXPORT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_FREMDEXPORT` datetime DEFAULT NULL,
  `ZEITPUNKT_IS_EXPORT` datetime DEFAULT NULL,
  PRIMARY KEY (`MELDER_ID`,`STATISTIK_ID`,`AMT`,`FIRMEN_ID`),
  KEY `status_index` (`STATUS`),
  KEY `firmen_index` (`FIRMEN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `melder_zusammenfuehrung`;
CREATE TABLE IF NOT EXISTS `melder_zusammenfuehrung` (
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `IDENT_MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('AKTIV','INAKTIV') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_DEAKTIVIERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`MELDER_ID`,`IDENT_MELDER_ID`),
  KEY `ident_melder_index` (`IDENT_MELDER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `meldung`;
CREATE TABLE IF NOT EXISTS `meldung` (
  `MELDUNG_ID` int(11) NOT NULL AUTO_INCREMENT,
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_INT` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATUM` datetime DEFAULT NULL,
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AUF_BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_TICKET` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDEART` enum('FORMULAR','DATEI','CSVSERVICE','CORE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FORMULAR',
  `FORMULAR_ID` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FORMAT` enum('EDT','DSB','SDF') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DSB',
  `SPRACHE` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `BARRIEREFREI` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `BEMERKUNGEN` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FA_KOMMENTAR_EX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEI_MELDER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEI_SERVER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEI_EXPORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('NEU','EXPORT_V','EXPORT_W','EXPORT_B','ARCHIV','LOESCH','FEHLER','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  `ZEITPUNKT_VERARB_EXPORT` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`MELDUNG_ID`),
  UNIQUE KEY `transfer_ticket_index` (`TRANSFER_TICKET`),
  KEY `amt_index` (`AMT`),
  KEY `statistik_index` (`STATISTIK_ID`),
  KEY `firmen_index` (`FIRMEN_ID`),
  KEY `melder_index` (`MELDER_ID`),
  KEY `quell_of_index` (`QUELL_REFERENZ_OF`),
  KEY `bzr_index` (`BZR`),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `meldung` (`MELDUNG_ID`, `AMT`, `STATISTIK_ID`, `FIRMEN_ID`, `MELDER_ID`, `QUELL_REFERENZ_OF`, `QUELL_REFERENZ_INT`, `DATUM`, `BZR`, `AUF_BZR`, `TRANSFER_TICKET`, `MELDEART`, `FORMULAR_ID`, `FORMAT`, `SPRACHE`, `BARRIEREFREI`, `BEMERKUNGEN`, `FA_KOMMENTAR_EX`, `KOMMENTAR`, `DATEI_MELDER`, `DATEI_SERVER`, `DATEI_EXPORT`, `STATUS`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_WWW`, `ZEITPUNKT_VERARB_EXPORT`, `ZEITPUNKT_AENDERUNG`) VALUES
	(1, '00', 1, 1, 1, '10000000', '', '2021-02-23 08:04:51', '', '', '0815', 'FORMULAR', NULL, 'DSB', NULL, 'N', '', '', '', '', '', '', 'NEU', NULL, NULL, NULL, NULL);


DROP TABLE IF EXISTS `meldung_daten`;
CREATE TABLE IF NOT EXISTS `meldung_daten` (
  `MELDUNG_ID` int(11) NOT NULL DEFAULT '0',
  `FELD_NAME` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DATEN` mediumblob,
  `SEGMENT` int(11) NOT NULL DEFAULT '0',
  `DATEI_NAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATUS` enum('NEU','EXPORT_V','EXPORT_W','EXPORT_B','ARCHIV','LOESCH','FEHLER','ORIGINAL') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`MELDUNG_ID`,`FELD_NAME`,`SEGMENT`),
  KEY `status_index` (`STATUS`),
  KEY `meldung_id_index` (`MELDUNG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `meldung_historie`;
CREATE TABLE IF NOT EXISTS `meldung_historie` (
  `MELDUNG_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `MELDEART` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EINGANG_MELDUNG` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`MELDUNG_ID`),
  KEY `main_index` (`AMT`,`STATISTIK_ID`,`BZR`),
  KEY `eingang_index` (`EINGANG_MELDUNG`(9))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `originaladresse`;
CREATE TABLE IF NOT EXISTS `originaladresse` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AENDERUNGSAUFTRAG_ID` bigint(20) NOT NULL DEFAULT '0',
  `ADRESSEN_ID` int(11) NOT NULL DEFAULT '0',
  `ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STRASSE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `HAUSNUMMER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTLEITZAHL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH_PLZ` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `POSTFACH_ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `LAND` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ8` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ9` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZUSATZ10` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS3` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS4` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS5` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS6` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `URS7` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `FA_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FA_NAME_ERGAENZUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FA_KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ANSPRECHPARTNER_ID` int(11) NOT NULL DEFAULT '0',
  `AN_ANREDE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_VORNAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_MOBIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AN_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `pw_ruecksetzung`;
CREATE TABLE IF NOT EXISTS `pw_ruecksetzung` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `KENNUNG` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `BERICHTSEINHEIT_ID` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `AMT` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `STATID` varchar(4) COLLATE utf8mb4_unicode_ci NOT NULL,
  `FIRMA` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `STRASSE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `HAUSNUMMER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `POSTLEITZAHL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ANSP_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ANSP_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ANSPRECHPARTNER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `STATUS` enum('NEU','WARTEND','MELDER_NICHT_ERREICHT','MELDER_HAT_BESTAETIGT','POSTVERSAND_VERANLASSEN','ABGESCHLOSSEN_AUTOMATISIERT','ABGESCHLOSSEN_POST','VERWORFEN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `STATUS_MELDUNG` mediumtext COLLATE utf8mb4_unicode_ci,
  `MELDER_ID` bigint(20) DEFAULT NULL,
  `STATISTIK_ID` int(11) DEFAULT NULL,
  `SACHBEARBEITER_ID` int(11) DEFAULT NULL,
  `ZEITPUNKT_EINTRAG` datetime NOT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `kennung_index` (`KENNUNG`),
  KEY `amt_index` (`AMT`),
  KEY `melder_id_index` (`MELDER_ID`),
  KEY `status_index` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `quell_referenz_verwaltung`;
CREATE TABLE IF NOT EXISTS `quell_referenz_verwaltung` (
  `QUELL_REFERENZ_ID` int(11) NOT NULL AUTO_INCREMENT,
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_KUERZEL` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_OF_LAENGE` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_OF_TYP` enum('NUM','ALN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ALN',
  `SB` int(11) NOT NULL DEFAULT '0',
  `IMPORT_RECHT` int(11) NOT NULL DEFAULT '0',
  `SB_RECHT` int(11) NOT NULL DEFAULT '0',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ADRESS_FORM_KUERZEL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ADRESSEN_HERKUNFT` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ADRESSEN_BEARBEITUNG` enum('EDITIEREN','SPERREN') COLLATE utf8mb4_unicode_ci DEFAULT 'EDITIEREN',
  `STATUS` enum('NEU','AEND','LOESCH','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `AENDERUNGSWORKLOW` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`QUELL_REFERENZ_ID`),
  KEY `statistik_index` (`STATISTIK_ID`),
  KEY `amt_index` (`AMT`),
  KEY `quell_kuerzel_index` (`QUELL_REFERENZ_KUERZEL`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `quell_referenz_verwaltung` (`QUELL_REFERENZ_ID`, `STATISTIK_ID`, `AMT`, `QUELL_REFERENZ_KUERZEL`, `QUELL_REFERENZ_OF_LAENGE`, `QUELL_REFERENZ_OF_TYP`, `SB`, `IMPORT_RECHT`, `SB_RECHT`, `KOMMENTAR`, `ADRESS_FORM_KUERZEL`, `ADRESSEN_HERKUNFT`, `ADRESSEN_BEARBEITUNG`, `STATUS`, `AENDERUNGSWORKLOW`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_AENDERUNG`, `ZEITPUNKT_WWW`) VALUES
	(1, 0, '', 'Test', 8, 'NUM', 1, 0, 0, '', '', '', 'EDITIEREN', 'AEND', 'N', '2021-02-12 11:08:52', '2021-02-12 11:09:00', NULL);


DROP TABLE IF EXISTS `sachbearbeiter`;
CREATE TABLE IF NOT EXISTS `sachbearbeiter` (
  `SACHBEARBEITER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VORNAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ABTEILUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KENNUNG` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `PASSWORT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('AKTIV','SPERRE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  PRIMARY KEY (`SACHBEARBEITER_ID`),
  UNIQUE KEY `uk_kennung` (`KENNUNG`),
  KEY `name_index` (`NAME`(20)),
  KEY `amt_index` (`AMT`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `sachbearbeiter` (`SACHBEARBEITER_ID`, `NAME`, `VORNAME`, `AMT`, `ABTEILUNG`, `TELEFON`, `EMAIL`, `KENNUNG`, `PASSWORT`, `STATUS`, `ZEITPUNKT_EINTRAG`) VALUES
	(1, 'Test', 'Test', '00', '', '', '', 'Toengi-S', 'rO0ABXdUAAABlAAAAAEAAAPoAAAAID1d4KjLwJtAQ6s0Oz0jVj2a5ilTmUOaK7ZlYDJfvhdOAAAAINAE8YmfInRfM0ZdQ5RlsJUhy0tQu//sFnvohl8njjwa', 'AKTIV', '2003-12-11 15:45:31'),
	(2, 'pwtest', '', '00', '', '', '', 'pwtest', 'rO0ABXdUAAABlAAAAAEAAAPoAAAAIJ4ZaxZdOhfJ02yes6f2sNFheBucInlgzaRb3/K+ftNhAAAAIJdaH968xJGnqkHA/FSjYse86Uvv62mCJq/6U3SC4cOp', 'AKTIV', '2021-02-23 11:15:48');


DROP TABLE IF EXISTS `satzmuster`;
CREATE TABLE IF NOT EXISTS `satzmuster` (
  `MUSTER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `FIRMEN_ID` int(11) DEFAULT '0',
  `NAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` varchar(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SEQUENZ_KUERZEL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SATZ_TYP` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEN` mediumblob,
  `DATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `PRUEFERGEBNIS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATUS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`MUSTER_ID`),
  KEY `melder_index` (`MELDER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `sb_gruppen`;
CREATE TABLE IF NOT EXISTS `sb_gruppen` (
  `SB_GRUPPEN_ID` int(11) NOT NULL AUTO_INCREMENT,
  `SB_GRUPPEN_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ADMINGRUPPEN_ID` int(11) NOT NULL DEFAULT '0',
  `RECHTE` enum('ROOT','GADMIN','NONE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NONE',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  PRIMARY KEY (`SB_GRUPPEN_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `sb_gruppen` (`SB_GRUPPEN_ID`, `SB_GRUPPEN_NAME`, `ADMINGRUPPEN_ID`, `RECHTE`, `ZEITPUNKT_EINTRAG`) VALUES
	(1, 'ROOT', 1, 'ROOT', '2003-12-11 15:45:31'),
	(2, 'GADMIN', 1, 'GADMIN', '2021-02-12 11:08:40'),
	(3, 'SADMIN', 2, 'NONE', '2021-02-12 11:08:45');


DROP TABLE IF EXISTS `sb_gruppen_rechte_statistiken`;
CREATE TABLE IF NOT EXISTS `sb_gruppen_rechte_statistiken` (
  `SB_GRUPPEN_ID` int(11) NOT NULL DEFAULT '0',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `RECHTE` enum('SADMIN','SB','BROWSE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BROWSE',
  `ADMIN_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  PRIMARY KEY (`SB_GRUPPEN_ID`,`STATISTIK_ID`,`AMT`,`RECHTE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `sb_gruppen_rechte_statistiken` (`SB_GRUPPEN_ID`, `STATISTIK_ID`, `AMT`, `RECHTE`, `ADMIN_ID`, `ZEITPUNKT_EINTRAG`) VALUES
	(3, 1, '00', 'SADMIN', 2, '2021-02-12 11:10:03');


DROP TABLE IF EXISTS `sb_gruppen_zuordnung`;
CREATE TABLE IF NOT EXISTS `sb_gruppen_zuordnung` (
  `SB_GRUPPEN_ID` int(11) NOT NULL DEFAULT '0',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `ADMIN_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  PRIMARY KEY (`SB_GRUPPEN_ID`,`SACHBEARBEITER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `sb_gruppen_zuordnung` (`SB_GRUPPEN_ID`, `SACHBEARBEITER_ID`, `ADMIN_ID`, `ZEITPUNKT_EINTRAG`) VALUES
	(1, 1, 1, '2003-12-11 15:45:31'),
	(2, 1, 1, '2021-02-12 11:08:40'),
	(3, 1, 2, '2021-02-12 11:08:45'),
	(3, 2, 2, '2021-02-23 11:16:06');


DROP TABLE IF EXISTS `standardwerte`;
CREATE TABLE IF NOT EXISTS `standardwerte` (
  `KONFIGURATION_ID` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `WERT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `KEY1` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `KEY2` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `KEY3` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SB_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`KONFIGURATION_ID`,`KEY1`,`KEY2`,`KEY3`,`SB_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `statistiken`;
CREATE TABLE IF NOT EXISTS `statistiken` (
  `STATISTIK_ID` int(11) NOT NULL AUTO_INCREMENT,
  `BEZEICHNUNG` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ARBEITSGEBIET` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `EVAS` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATID` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SB_FA` int(11) NOT NULL DEFAULT '0',
  `SB_IT` int(11) NOT NULL DEFAULT '0',
  `ANGEBOTSART` enum('BEFRAGUNG_FORMULAR','BEFRAGUNG_DATEI','BEFRAGUNG_FORMULAR_DATEI','DATEISERVICE','REGISTRIERUNG','ANFRAGE','UMFRAGE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BEFRAGUNG_FORMULAR',
  `PERIODIZITAET` enum('MONAT','QUARTAL','HALBJAHR','JAHR','EINMALIG','UNREGELMAESSIG') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MONAT',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('AKTIV','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `SB_GRUPPEN_ID` int(11) NOT NULL DEFAULT '0',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`STATISTIK_ID`),
  KEY `arbeitsgebiet_index` (`ARBEITSGEBIET`),
  KEY `evas_index` (`EVAS`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `statistiken` (`STATISTIK_ID`, `BEZEICHNUNG`, `KURZTEXT`, `ARBEITSGEBIET`, `EVAS`, `STATID`, `SB_FA`, `SB_IT`, `ANGEBOTSART`, `PERIODIZITAET`, `KOMMENTAR`, `STATUS`, `SB_GRUPPEN_ID`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_AENDERUNG`, `ZEITPUNKT_WWW`) VALUES
	(1, 'Teststatistik', 'test', 'test', '', NULL, 1, 1, 'BEFRAGUNG_FORMULAR', 'JAHR', '', 'AKTIV', 1, '2003-12-17 12:51:34', '2003-12-19 16:58:07', '2003-12-20 10:34:04');


DROP TABLE IF EXISTS `statistiken_amt`;
CREATE TABLE IF NOT EXISTS `statistiken_amt` (
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AMT_INTERN` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STAT_ONLINE_KEY` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SB_GRUPPEN_ID` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_ID` int(11) NOT NULL DEFAULT '0',
  `SB_FA` int(11) NOT NULL DEFAULT '0',
  `SB_IT` int(11) NOT NULL DEFAULT '0',
  `ARCHIV_ZEIT` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDERKONTO` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ERINNERUNGSSERVICE` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ARCHIV_SERVICE` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `KORREKTUR` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `ERSATZMELDUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `ERSATZMELDUNG_MEHRFACH` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `STORNIERUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `STATUS` enum('AKTIV','INAKTIV','TEST','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `REGISTRIERUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `REG_ERWEIT_STATISTIK` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `REG_ERWEIT_FIRMA` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `ADRESSE_BESTAETIGEN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `MAX_ANZAHL_VORLAGEN` int(11) DEFAULT NULL,
  `SPEICHERN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `SPEICHERN_AUTOMATISCH` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `MELDUNG_AUFBEWAHREN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `SESSION_WIEDERHERSTELLEN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `DIREKT_IMPORT` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `REG_FORM_KUERZEL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `REG_URL_TEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KURZ_TEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `INFO_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `INFO_KUERZEL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `MELDE_HINWEIS` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'J',
  `INTERNE_MELDUNGS_BEARBEITUNG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `REFERER_VERWENDEN` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `REF_URL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_FACHABT_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_FACHABT_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_FACHABT_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_FACHABT_FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_FACHABT_INFOTEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_TECHNIK_NAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_TECHNIK_TELEFON` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_TECHNIK_EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ASP_TECHNIK_FAX` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DARSTELLUNG` enum('BARRIEREFREI','BEIDES','NICHT_BARRIEREFREI') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NICHT_BARRIEREFREI',
  `DIREKTZUGANG` enum('J','N') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N',
  `UMFRAGE_LINK_TEXT` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UMFRAGE_TEXT` mediumtext COLLATE utf8mb4_unicode_ci,
  `MELDERKONTO_ARCHIV_ZEIT` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PASSWORT_AENDERUNG` enum('HINWEIS','PFLICHT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'HINWEIS',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`STATISTIK_ID`,`AMT`),
  KEY `stat_online_index` (`STAT_ONLINE_KEY`),
  KEY `quell_referenz_index` (`QUELL_REFERENZ_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `statistiken_amt` (`STATISTIK_ID`, `AMT`, `AMT_INTERN`, `STAT_ONLINE_KEY`, `SB_GRUPPEN_ID`, `QUELL_REFERENZ_ID`, `SB_FA`, `SB_IT`, `ARCHIV_ZEIT`, `MELDERKONTO`, `ERINNERUNGSSERVICE`, `ARCHIV_SERVICE`, `KORREKTUR`, `ERSATZMELDUNG`, `ERSATZMELDUNG_MEHRFACH`, `STORNIERUNG`, `KOMMENTAR`, `STATUS`, `REGISTRIERUNG`, `REG_ERWEIT_STATISTIK`, `REG_ERWEIT_FIRMA`, `ADRESSE_BESTAETIGEN`, `MAX_ANZAHL_VORLAGEN`, `SPEICHERN`, `SPEICHERN_AUTOMATISCH`, `MELDUNG_AUFBEWAHREN`, `SESSION_WIEDERHERSTELLEN`, `DIREKT_IMPORT`, `REG_FORM_KUERZEL`, `REG_URL_TEXT`, `KURZ_TEXT`, `INFO_URL`, `INFO_KUERZEL`, `MELDE_HINWEIS`, `INTERNE_MELDUNGS_BEARBEITUNG`, `REFERER_VERWENDEN`, `REF_URL`, `ASP_FACHABT_NAME`, `ASP_FACHABT_TELEFON`, `ASP_FACHABT_EMAIL`, `ASP_FACHABT_FAX`, `ASP_FACHABT_INFOTEXT`, `ASP_TECHNIK_NAME`, `ASP_TECHNIK_TELEFON`, `ASP_TECHNIK_EMAIL`, `ASP_TECHNIK_FAX`, `DARSTELLUNG`, `DIREKTZUGANG`, `UMFRAGE_LINK_TEXT`, `UMFRAGE_TEXT`, `MELDERKONTO_ARCHIV_ZEIT`, `PASSWORT_AENDERUNG`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_AENDERUNG`, `ZEITPUNKT_WWW`) VALUES
	(1, '00', '', '42111001', 1, 1, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', NULL, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', NULL, NULL, NULL, 'HINWEIS', '2003-12-17 12:53:37', '2021-02-12 11:11:37', '2003-12-18 16:14:39');


DROP TABLE IF EXISTS `system_schluessel`;
CREATE TABLE IF NOT EXISTS `system_schluessel` (
  `SYSTEM_SCHLUESSEL_ID` int(11) NOT NULL AUTO_INCREMENT,
  `PRIVATER_SCHLUESSEL` blob NOT NULL,
  `OEFFENTLICHER_SCHLUESSEL` blob NOT NULL,
  `ZEITPUNKT_EINTRAG` datetime NOT NULL,
  PRIMARY KEY (`SYSTEM_SCHLUESSEL_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `system_schluessel` (`SYSTEM_SCHLUESSEL_ID`, `PRIVATER_SCHLUESSEL`, `OEFFENTLICHER_SCHLUESSEL`, `ZEITPUNKT_EINTRAG`) VALUES
	(1, _binary 0xACED00057A00000215000001940000000500000209308202050201003081EC06072A8648CE3D02013081E0020101302C06072A8648CE3D0101022100A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E5377304404207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9042026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B60441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F046997022100A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A70201010482010F3082010B020101042053D339EC1F5C6EE0DB513EA2A5921E2CF58EA22A9AAFBFC3D05B12388F885ADEA081E33081E0020101302C06072A8648CE3D0101022100A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E5377304404207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9042026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B60441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F046997022100A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7020101, _binary 0xACED00057A00000143000001940000000400000137308201333081EC06072A8648CE3D02013081E0020101302C06072A8648CE3D0101022100A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E5377304404207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9042026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B60441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F046997022100A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A702010103420004A0E3C56F8920C8D8EE014FA3A84C3E6E22F28095E7AC0DB99BD5D7844A63A5462CF0FF99CBC51EB0335CA17588ECD3BE6661C9E693F99FBEDBCDA30E6737023B, '2021-02-10 08:11:29');


DROP TABLE IF EXISTS `template`;
CREATE TABLE IF NOT EXISTS `template` (
  `TEMPLATE_ID` int(11) NOT NULL AUTO_INCREMENT,
  `TEMPLATE_NAME` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `STATISTIK_ID` int(11) DEFAULT '0',
  `AMT` varchar(2) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `FIRMEN_ID` int(11) DEFAULT '0',
  `MELDER_ID` int(11) DEFAULT '0',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `QUELL_REFERENZ_INT` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `FORMULARNAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `TYP` enum('SICHERUNG','VORLAGE','MELDUNG','SESSION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `STATUS` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `DATEN` mediumblob,
  `DATEN_PFAD` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`TEMPLATE_ID`),
  KEY `melder_index` (`MELDER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `transfer`;
CREATE TABLE IF NOT EXISTS `transfer` (
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AKTION` enum('MELDUNGS_TRANSFER','REGISTRIERUNG','AEND_MELDER_DATEIEXPORT','AEND_MELDER_DIREKTEINTRAG','AEND_MELDER_BEIDES','AEND_AUSKUNFTPFL_DATEIEXPORT','AEND_AUSKUNFTPFL_DIREKTEINTRAG','AEND_AUSKUNFTPFL_BEIDES','ERWEIT_STATISTIK','ERWEIT_FIRMA','LOESCH_STATISTIK','LOESCH_FIRMA','MAIL_DOWNLOAD') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'REGISTRIERUNG',
  `AENDERUNGSART` set('ADRESSE','FIRMA','ANSPRECHPARTNER','URS') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFERZIEL_ID` int(11) NOT NULL DEFAULT '0',
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','AEND','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`STATISTIK_ID`,`AMT`,`AKTION`,`AENDERUNGSART`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `transfer` (`STATISTIK_ID`, `AMT`, `AKTION`, `AENDERUNGSART`, `TRANSFERZIEL_ID`, `SACHBEARBEITER_ID`, `STATUS`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_AENDERUNG`) VALUES
	(1, '00', 'MAIL_DOWNLOAD', 'URS', 1, 1, 'AEND', '2021-02-18 09:05:28', '2021-02-18 09:07:31');


DROP TABLE IF EXISTS `transferziel`;
CREATE TABLE IF NOT EXISTS `transferziel` (
  `TRANSFERZIEL_ID` int(11) NOT NULL AUTO_INCREMENT,
  `KONVERTER` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KONVERTER_OPTIONEN` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_FORM` enum('','EINZEL','SAMMEL') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_PLATTFORM` enum('','LOKAL','UNIXFTP','UNIXSFTP','HOSTFTP','HOSTSFTP') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_HOST` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_USER` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_PASSWORT` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_ACCOUNT` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_ZIEL_VERZEICHNIS` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_MODUS` enum('','ASCII','BINARY') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_MAIL_EMPFAENGER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_MAIL_ABSENDER` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_MAIL_BETREFF` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `TRANSFER_MAIL_TEXT` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `KOMMENTAR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `AENDERUNGS_EXPORT_SPALTEN` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `SACHBEARBEITER_ID` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','AEND','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`TRANSFERZIEL_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `transferziel` (`TRANSFERZIEL_ID`, `KONVERTER`, `KONVERTER_OPTIONEN`, `TRANSFER_FORM`, `TRANSFER_PLATTFORM`, `TRANSFER_HOST`, `TRANSFER_USER`, `TRANSFER_PASSWORT`, `TRANSFER_ACCOUNT`, `TRANSFER_ZIEL_VERZEICHNIS`, `TRANSFER_MODUS`, `TRANSFER_MAIL_EMPFAENGER`, `TRANSFER_MAIL_ABSENDER`, `TRANSFER_MAIL_BETREFF`, `TRANSFER_MAIL_TEXT`, `KOMMENTAR`, `AENDERUNGS_EXPORT_SPALTEN`, `SACHBEARBEITER_ID`, `STATUS`, `ZEITPUNKT_EINTRAG`, `ZEITPUNKT_AENDERUNG`) VALUES
	(1, '', '', 'EINZEL', 'LOKAL', 'localhost', '', '', '', '', 'ASCII', '', 'stefan.toengi@destatis.de', ' für ßie', '<html>Hallo,<br>es gibt eine neue Vorbelegung für <b>dich</b>! äöü!</html>', '', '', 1, 'NEU', '2021-02-18 09:04:39', '2021-02-18 09:05:18');


DROP TABLE IF EXISTS `version`;
CREATE TABLE IF NOT EXISTS `version` (
  `VERSIONSNR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `SB` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('AKTIV','SPERRE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AKTIV',
  `SPERRE_INFO` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ZEITPUNKT_ERSTELLUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`VERSIONSNR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `version` (`VERSIONSNR`, `SB`, `STATUS`, `SPERRE_INFO`, `ZEITPUNKT_ERSTELLUNG`, `ZEITPUNKT_AENDERUNG`) VALUES
	('1.71', 1, 'AKTIV', '', '2021-02-10 08:11:27', '0000-00-00 00:00:00');


DROP TABLE IF EXISTS `vorbelegung_dateien`;
CREATE TABLE IF NOT EXISTS `vorbelegung_dateien` (
  `VORBELEGUNG_DATEIEN_ID` int(11) NOT NULL AUTO_INCREMENT,
  `VERSION` int(11) NOT NULL,
  `STATISTIK_ID` int(11) NOT NULL,
  `AMT` varchar(2) COLLATE utf8mb4_unicode_ci NOT NULL,
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DATEN_PFAD` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SCHLUESSEL` blob NOT NULL,
  `STATUS` enum('AKTIV','LOESCH') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ZEITPUNKT_EINTRAG` datetime NOT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  PRIMARY KEY (`VORBELEGUNG_DATEIEN_ID`),
  UNIQUE KEY `uk_stat_id_amt_bzr_daten_pfad` (`STATISTIK_ID`,`AMT`,`BZR`,`DATEN_PFAD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `vorbelegung_verwaltung`;
CREATE TABLE IF NOT EXISTS `vorbelegung_verwaltung` (
  `VORBELEGUNG_ID` int(11) NOT NULL AUTO_INCREMENT,
  `AMT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FIRMEN_ID` int(11) NOT NULL DEFAULT '0',
  `MELDER_ID` int(11) NOT NULL DEFAULT '0',
  `STATISTIK_ID` int(11) NOT NULL DEFAULT '0',
  `QUELL_REFERENZ_OF` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `QUELL_REFERENZ_INT` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `BZR` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FORMULARNAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `VB_WERTE_INDX` int(11) NOT NULL DEFAULT '0',
  `STATUS` enum('NEU','AEND','SPERRE','LOESCH','EXPORT','LOESCHKANDIDAT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEU',
  `ZEITPUNKT_EINTRAG` datetime DEFAULT NULL,
  `ZEITPUNKT_AENDERUNG` datetime DEFAULT NULL,
  `ZEITPUNKT_WWW` datetime DEFAULT NULL,
  PRIMARY KEY (`VORBELEGUNG_ID`),
  KEY `amt_index` (`AMT`),
  KEY `firmen_index` (`FIRMEN_ID`),
  KEY `melder_index` (`MELDER_ID`),
  KEY `statistik_index` (`STATISTIK_ID`),
  KEY `quell_of_index` (`QUELL_REFERENZ_OF`),
  KEY `vb_werte_index` (`VB_WERTE_INDX`),
  KEY `bzr_index` (`BZR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `vorbelegung_wert`;
CREATE TABLE IF NOT EXISTS `vorbelegung_wert` (
  `VB_WERTE_INDX` int(11) NOT NULL DEFAULT '0',
  `FELD_NAME` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `FELD_INHALT` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`VB_WERTE_INDX`,`FELD_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

