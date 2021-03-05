TRUNCATE TABLE adressen;
TRUNCATE TABLE ansprechpartner;
TRUNCATE TABLE adressen_melder_zp_www;
TRUNCATE TABLE ansprechpartner_melder_zp_www;
TRUNCATE TABLE erhebung;
TRUNCATE TABLE firmen;
TRUNCATE TABLE firmen_adressen;
TRUNCATE TABLE melder;
TRUNCATE TABLE melder_statistiken;
TRUNCATE TABLE job;
TRUNCATE TABLE vorbelegung_verwaltung;
TRUNCATE TABLE vorbelegung_wert;
TRUNCATE TABLE import_verwaltung;
TRUNCATE TABLE import_teil;
TRUNCATE TABLE statistiken;
TRUNCATE TABLE quell_referenz_verwaltung;
TRUNCATE TABLE statistiken_amt;

# Vorbereiten von 10 Statistiken
INSERT INTO statistiken (STATISTIK_ID, BEZEICHNUNG, KURZTEXT, ARBEITSGEBIET, EVAS, STATID, SB_FA, SB_IT, ANGEBOTSART, PERIODIZITAET, STATUS, SB_GRUPPEN_ID, ZEITPUNKT_EINTRAG) 
VALUES 
(1, 'Test1', 'Test1', '', 'EVAS1', '0001', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(2, 'Test2', 'Test2', '', 'EVAS2', '0002', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(3, 'Test3', 'Test3', '', 'EVAS3', '0003', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(4, 'Test4', 'Test4', '', 'EVAS4', '0004', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(5, 'Test5', 'Test5', '', 'EVAS5', '0005', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(6, 'Test6', 'Test6', '', 'EVAS6', '0006', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(7, 'Test7', 'Test7', '', 'EVAS7', '0007', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(8, 'Test8', 'Test8', '', 'EVAS8', '0008', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(9, 'Test9', 'Test9', '', 'EVAS9', '0009', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW()),
(10, 'Test10', 'Test10', '', 'EVAS10', '0010', 1, 1, 'BEFRAGUNG_FORMULAR', 'MONAT',  'AKTIV', 1, NOW());

INSERT INTO quell_referenz_verwaltung (QUELL_REFERENZ_ID, STATISTIK_ID, AMT, QUELL_REFERENZ_KUERZEL, QUELL_REFERENZ_OF_LAENGE, QUELL_REFERENZ_OF_TYP, SB, IMPORT_RECHT, SB_RECHT, ADRESSEN_HERKUNFT, ADRESSEN_BEARBEITUNG, STATUS, ZEITPUNKT_EINTRAG) 
VALUES 
(1, 1, '00', 'Bestand1', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(2, 2, '00', 'Bestand2', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(3, 3, '00', 'Bestand3', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(4, 4, '00', 'Bestand4', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(5, 5, '00', 'Bestand5', 9, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(6, 6, '00', 'Bestand6', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(7, 7, '00', 'Bestand7', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(8, 8, '00', 'Bestand8', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(9, 9, '00', 'Bestand9', 8, 'NUM', 1, 0, 0, 'IDEV', 'EDITIEREN', 'NEU', NOW()),
(10, 10, '00', 'Bestand10', 8, 'NUM', 1, 1, 1, 'IDEV', 'EDITIEREN', 'NEU', NOW());

REPLACE INTO statistiken_amt (STATISTIK_ID, AMT, STAT_ONLINE_KEY, SB_GRUPPEN_ID, QUELL_REFERENZ_ID, SB_FA, SB_IT, ARCHIV_ZEIT, MELDERKONTO, ERINNERUNGSSERVICE, ARCHIV_SERVICE, KORREKTUR, ERSATZMELDUNG, ERSATZMELDUNG_MEHRFACH, STORNIERUNG, KOMMENTAR, STATUS, REGISTRIERUNG, REG_ERWEIT_STATISTIK, REG_ERWEIT_FIRMA, ADRESSE_BESTAETIGEN, MAX_ANZAHL_VORLAGEN, SPEICHERN, SPEICHERN_AUTOMATISCH, MELDUNG_AUFBEWAHREN, SESSION_WIEDERHERSTELLEN, DIREKT_IMPORT, REG_FORM_KUERZEL, REG_URL_TEXT, KURZ_TEXT, INFO_URL, INFO_KUERZEL, MELDE_HINWEIS, INTERNE_MELDUNGS_BEARBEITUNG, REFERER_VERWENDEN, REF_URL, ASP_FACHABT_NAME, ASP_FACHABT_TELEFON, ASP_FACHABT_EMAIL, ASP_FACHABT_FAX, ASP_FACHABT_INFOTEXT, ASP_TECHNIK_NAME, ASP_TECHNIK_TELEFON, ASP_TECHNIK_EMAIL, ASP_TECHNIK_FAX, DARSTELLUNG, DIREKTZUGANG, UMFRAGE_LINK_TEXT, UMFRAGE_TEXT, MELDERKONTO_ARCHIV_ZEIT, PASSWORT_AENDERUNG, ZEITPUNKT_EINTRAG) 
VALUES
(1, '00', '42111001', 1, 1, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(2, '00', '42111002', 1, 2, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(3, '00', '42111003', 1, 3, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(4, '00', '42111004', 1, 4, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(5, '00', '42111005', 1, 5, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(6, '00', '42111006', 1, 6, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(7, '00', '42111007', 1, 7, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(8, '00', '42111008', 1, 8, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(9, '00', '42111009', 1, 9, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW()),
(10, '00', '42111010', 1, 10, 1, 1, '', 'J', 'J', 'N', 'N', 'N', 'N', 'N', '', 'AKTIV', 'J', 'J', 'J', 'N', 0, 'N', 'N', 'N', 'J', 'N', '', '', '', '', '', 'J', 'N', 'N', '', '', '', '', '', '', '', '', '', '', 'NICHT_BARRIEREFREI', 'N', null, null, null, 'HINWEIS', NOW());


REPLACE INTO sb_gruppen_zuordnung (SB_GRUPPEN_ID, SACHBEARBEITER_ID, ADMIN_ID, ZEITPUNKT_EINTRAG) 
VALUES 
(1, 1, 1, NOW()),
(2, 1, 1, NOW()),
(3, 1, 1, NOW());


REPLACE INTO sb_gruppen_rechte_statistiken (SB_GRUPPEN_ID, STATISTIK_ID, AMT, RECHTE, ADMIN_ID, ZEITPUNKT_EINTRAG)
VALUES 
(3, 1, '00', 'SADMIN', 2, NOW()),
(3, 2, '00', 'SADMIN', 2, NOW()),
(3, 3, '00', 'SADMIN', 2, NOW()),
(3, 4, '00', 'SADMIN', 2, NOW()),
(3, 5, '00', 'SADMIN', 2, NOW()),
(3, 6, '00', 'SADMIN', 2, NOW()),
(3, 7, '00', 'SADMIN', 2, NOW()),
(3, 8, '00', 'SADMIN', 2, NOW()),
(3, 9, '00', 'SADMIN', 2, NOW()),
(3, 10, '00', 'SADMIN', 2, NOW());

# Passwort ist test

REPLACE INTO sachbearbeiter (SACHBEARBEITER_ID, NAME, VORNAME, AMT, ABTEILUNG, TELEFON, EMAIL, KENNUNG, PASSWORT, STATUS, ZEITPUNKT_EINTRAG) 
VALUES 
(1, 'Tester', 'Stefan', '00', '', '', '', 'Stefan', 'rO0ABXdUAAABlAAAAAEAAAPoAAAAIB3oUuhWX62i8CbIbGs5+QfglP+RKULuYfFLQu0/xB+4AAAAIJ/u8NE1bav99htKDoKGnU8DZ0YIDY2zcMg2qJJJZ5R/', 'AKTIV', NOW());

REPLACE INTO konfiguration (KONFIGURATION_ID, WERT_STRING, TYP, STATUS, ZEITPUNKT_EINTRAG) 
VALUES 
('int_melderschluessel_poolsize', '4000', 'INTERN', 'AKTIV', NOW()),
('int_melderschluessel_threads', '4', 'INTERN', 'AKTIV', NOW());

REPLACE INTO erhebung (STATISTIK_ID, AMT, BZR, AUF_BZR, ERSTER_MELDUNGSTERMIN, LETZTER_MELDUNGSTERMIN, STATSPEZ_KEY, FORMULAR_ID, SB_GRUPPEN_ID, URL_PFAD, SERVLET_PFAD, JSP_PFAD, MELDEDATEN_PFAD, PASSWD_PFAD, VBDATEN_PFAD, VORBELEGUNGSABHAENGIG, SENDEN, ZURUECKSETZEN, PRUEFUNG, LOKALSICHERUNG, SERVERSICHERUNG, ARCHIVIERUNG, WEITERE_MELDUNG, EXPORT_CSV, UMFRAGE_URL, UMFRAGE_FELD_NAME_AMT, UMFRAGE_FELD_NAME_BZR, UMFRAGE_FELD_NAME_ERHEBUNGS_ID, UMFRAGE_FELD_NAME_QUELLREFERENZ_OF, BERICHTSPFLICHTIGE, STATUS, ZEITPUNKT_EINTRAG) 
VALUES (1, '00', '201501', '', '', '', '', 'formBXSAAAKBH', 1, '', '', '', '', '', '', 'N', 'J', 'J', 'J', 'J', 'J', 'J', 'J', 'N', NULL, NULL, NULL, NULL, NULL, 0, 'AKTIV', NOW());
