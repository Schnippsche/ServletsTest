/*
 * @(#)InhaltsverzeichnisXMLParser.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.meldungen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Die Klasse <code>InhaltsverzeichnisXMLParser</code> parst
 * das Inhaltsverzeichnis im Kontext Meldungen holen
 * <p>
 *
 * @version 1 vom 15.04.2003 18:07
 * @author Schindler
 **/
public class InhaltsverzeichnisXMLParser
{

  private HashMap<String, List<String>> mapFormularDateien;
  private HashMap<String, List<String>> mapUploadDateien;
  final boolean ok = true;

  String fehlertext;

  /**
   * Instantiates a new inhaltsverzeichnis XM L parser.
   */
  public InhaltsverzeichnisXMLParser()
  {
    //
  }

  /**
   * Parses the datei.
   *
   * @param xmldatei the xmldatei
   * @throws MeldungenHolenException the meldungen holen exception
   */
  public void parseDatei(File xmldatei) throws MeldungenHolenException
  {

    String key;
    String wert;
    List<String> listNamenForm;
    List<String> listNamenUpload;
    try
    {
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(xmldatei);
      Element root = doc.getRootElement();

      List listMeldung = root.getChild("meldungen")
          .getChildren("meldung");
      this.mapFormularDateien = new HashMap<>();
      this.mapUploadDateien = new HashMap<>();
      /* Schleife ueber alle Elemente <meldung> */

      for (int i = 0; i < listMeldung.size(); i++)
      {
        Element meldung = (Element) listMeldung.get(i);
        /* Id im Element <meldung> */
        key = meldung.getAttributeValue("meldung_id");

        /* Schleife ueber alle Elemente <formular_datei_name> */
        List listFormular = meldung.getChildren("formular_datei_name");
        listNamenForm = new ArrayList<>();
        for (int j = 0; j < listFormular.size(); j++)
        {
          Element formular = (Element) listFormular.get(j);
          /* Name im Element <formular> */
          wert = formular.getText();
          listNamenForm.add(wert);
        }
        /* Ausgabe ID und Dateinamen */
        this.mapFormularDateien.put(key, listNamenForm);

        /* Schleife ueber alle Elemente <upload_datei_name> */
        List listUpload = meldung.getChildren("upload_datei_name");
        listNamenUpload = new ArrayList<>();
        for (int k = 0; k < listUpload.size(); k++)
        {
          Element up = (Element) listUpload.get(k);
          /* Name im Element <upload> */
          wert = up.getText();
          listNamenUpload.add(wert);
        }
        /* Ausgabe ID und Dateinamen */
        this.mapUploadDateien.put(key, listNamenUpload);
      }
    }
    catch (Exception e)
    {
      throw new MeldungenHolenException(e.getMessage());
    }
  }

  /**
   * Liefert dateinamen formular.
   *
   * @param id the id
   * @return dateinamen formular
   */

  public Vector<String> getDateinamenFormular(String id)
  {
    Vector<String> namen = new Vector<>();

    if (!this.mapFormularDateien.containsKey(id))
    {
      return namen;
    }

    List<String> listNamen = this.mapFormularDateien.get(id);
    for (int i = 0; i < listNamen.size(); i++)
    {
      namen.addElement(listNamen.get(i));
    }
    return namen;
  }

  /**
   * Liefert dateinamen upload.
   *
   * @param id the id
   * @return dateinamen upload
   */

  public Vector<String> getDateinamenUpload(String id)
  {
    Vector<String> namen = new Vector<>();

    if (!this.mapUploadDateien.containsKey(id))
    {
      return namen;
    }

    List<String> listNamen = this.mapUploadDateien.get(id);
    for (int i = 0; i < listNamen.size(); i++)
    {
      namen.addElement(listNamen.get(i));
    }
    return namen;
  }

  /**
   * Liefert status.
   *
   * @return status
   */

  public boolean getStatus()
  {
    return this.ok;
  }

  /**
   * Liefert fehler text.
   *
   * @return fehler text
   */

  public String getFehlerText()
  {
    return this.fehlertext;
  }

}