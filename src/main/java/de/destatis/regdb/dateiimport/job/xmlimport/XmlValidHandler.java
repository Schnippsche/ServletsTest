package de.destatis.regdb.dateiimport.job.xmlimport;

import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.db.ResultRow;
import de.destatis.regdb.db.SqlUtil;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * The Class XmlValidHandler.
 *
 * @author Stefan
 */
public class XmlValidHandler extends DefaultHandler
{

  /**
   * The log.
   */
  protected final LoggerIfc log = Logger.getInstance()
    .getLogger(this.getClass());
  private final HashMap<String, Integer> amtStatOnlineKeys;
  private final SqlUtil sqlUtil;
  private final StringBuilder builder;
  /**
   * The parse stat online key.
   */
  boolean parseStatOnlineKey = false;
  /**
   * The parse amt.
   */
  boolean parseAmt = false;
  /**
   * The parse aktion.
   */
  boolean parseAktion = false;
  private String statOnlineKey;
  private String amt;
  private String aktion;
  private Locator locator;
  private Integer quellReferenzId;
  private String statistikId;
  private int anzahl;

  /**
   * Instantiates a new xml valid handler.
   *
   * @param sqlUtil the SqlUtil
   */
  public XmlValidHandler(SqlUtil sqlUtil)
  {
    this.amtStatOnlineKeys = new HashMap<>();
    this.sqlUtil = sqlUtil;
    this.quellReferenzId = null;
    this.builder = new StringBuilder();
    this.amt = "";
    this.aktion = "";
    this.statOnlineKey = "";
    this.anzahl = 0;
  }

  /**
   * Sets the document locator.
   *
   * @param locator the new document locator
   */
  @Override
  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  /**
   * Start element.
   *
   * @param uri        the uri
   * @param localName  the local name
   * @param qName      the q name
   * @param attributes the attributes
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
  {
    this.builder.setLength(0);
  }

  /**
   * End element.
   *
   * @param uri       the uri
   * @param localName the local name
   * @param qName     the q name
   * @throws SAXException the SAX exception
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    String value = this.builder.toString();
    this.builder.setLength(0);
    if ("stat_online_key".equals(qName))
    {
      this.statOnlineKey = value;
    } else if ("amt".equals(qName))
    {
      this.amt = value;
    } else if ("aktion".equals(qName))
    {
      this.aktion = value;
    } else if (XmlImportJob.XML_ADRESSE.equals(qName) || XmlImportJob.XML_FIRMA.equals(qName) || XmlImportJob.XML_MELDER.equals(qName) || XmlImportJob.XML_VORBELEGUNG
      .equals(qName) || XmlImportJob.XML_ERHEBUNG.equals(qName))
    {
      this.anzahl++;
      // Sind die wichtigen Elemente valide ?
      if (this.aktion.isEmpty())
      {
        throw new SAXException("Aktion ist leer in Abschnitt " + this.getLocation());
      }
      if (this.amt.isEmpty())
      {
        throw new SAXException("Amt ist leer in Abschnitt " + this.getLocation());
      }
      if (this.statOnlineKey.isEmpty())
      {
        throw new SAXException("Stat-Online-Key ist leer in Abschnitt " + this.getLocation());
      }

      if (!("NEU".equals(this.aktion) || "UPDATE".equals(this.aktion) || "LOESCH".equals(this.aktion) || "ANY".equals(this.aktion)))
      {
        throw new SAXException("Ungültige Aktion '" + this.aktion + "' des Abschnitts " + this.getLocation());
      }
      this.pruefeStatOnlineKey();
      this.amt = "";
      this.aktion = "";
      this.statOnlineKey = "";
      this.parseStatOnlineKey = false;
      this.parseAmt = false;
      this.parseAktion = false;
    }
  }

  /**
   * Characters.
   *
   * @param ch     the ch
   * @param start  the start
   * @param length the length
   */
  @Override
  public void characters(char[] ch, int start, int length)
  {
    // http://www.tutego.de/blog/javainsel/2007/01/character-%E2%80%9Echunking%E2%80%9C-bei-sax/
    this.builder.append(ch, start, length);
  }

  /**
   * Gets the location.
   *
   * @return the location
   */
  private String getLocation()
  {
    String location;
    if (this.locator != null)
    {
      location = " Zeile " + this.locator.getLineNumber();
      location += ", Spalte " + this.locator.getColumnNumber();
      location += ": ";
    } else
    {
      location = "unbekannt";
    }
    return location;
  }

  /**
   * Pruefe stat online key.
   *
   * @throws SAXException the SAX exception
   */
  private void pruefeStatOnlineKey() throws SAXException
  {
    // Pruefe auf passenden STAT-ONLINE-KEY und ermittle dabei die StatistikId und quellReferenzId
    String key = this.amt + "|" + this.statOnlineKey;
    Integer statistId = this.amtStatOnlineKeys.get(key);
    // Neu aufnehmen?
    if (statistId == null)
    {
      this.log.debug("pruefeStatOnlineKey , amt=" + this.amt + ", statonlinekey=" + this.statOnlineKey);
      Object ergebnis = this.checkAmtStatOnlineKey(this.amt, this.statOnlineKey);
      if (ergebnis instanceof Integer[])
      {
        Integer[] werte = (Integer[]) ergebnis;
        statistId = werte[0];
        this.amtStatOnlineKeys.put(key, statistId);
        if (this.quellReferenzId == null)
        {
          this.quellReferenzId = werte[1];
        } else if (!this.quellReferenzId.equals(werte[1]))
        {
          throw new SAXException("Die Importdatei enthält Daten zu unterschiedlichen Adressbeständen");
        }
      } else
      {
        throw new SAXException((String) ergebnis);
      }
    } else
    {
      this.statistikId = "" + statistId;
    }
  }

  /**
   * Gets the statistik id.
   *
   * @return the statistik id
   */
  public String getStatistikId()
  {
    return this.statistikId;
  }

  /**
   * Gets the quell referenz id.
   *
   * @return the quell referenz id
   */
  public Integer getQuellReferenzId()
  {
    return this.quellReferenzId;
  }

  /**
   * Gets the anzahl datensaetze.
   *
   * @return the anzahl datensaetze
   */
  public int getAnzahlDatensaetze()
  {
    return this.anzahl;
  }

  /**
   * Check amt stat online key.
   *
   * @param amt           the amt
   * @param statOnlineKey the stat online key
   * @return the object
   */
  private Object checkAmtStatOnlineKey(String amt, String statOnlineKey)
  {
    String sql = MessageFormat.format(RegisterImportJob.SQL_SELECT_STATONLINEKEY, amt, statOnlineKey);
    try
    {
      ResultRow row = sqlUtil.fetchOne(sql);
      if (row != null)
      {
        int statistId = row.getInt(1);
        int quellRefId = row.getInt(2);
        Integer[] ergebnis = new Integer[2];
        ergebnis[0] = statistId;
        ergebnis[1] = quellRefId;
        return ergebnis;
      }
      return "Kombination aus Amt (" + amt + ") und StatOnlineKey (" + statOnlineKey + ") existiert nicht!";
    } catch (JobException e)
    {
      return "Schlüssel aus Amt/StatOnlineKey konnte nicht geprüft werden:" + e.getMessage();
    }
  }

  /**
   * Gets the schema.
   *
   * @return the schema
   */
  public Schema getSchema()
  {
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = null;
    try
    {
      File file = new File(getClass().getResource("regdb.xsd")
        .toURI());
      this.log.debug("File:" + file.toString());
      schema = schemaFactory.newSchema(file);
      this.log.debug("Verwende XML Schema " + schema);
    } catch (SAXException | URISyntaxException exc)
    {
      log.error("Fehler bei XML DTD" + exc.getMessage(), exc);
    }
    return schema;
  }
}
