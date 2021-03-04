package de.destatis.regdb.dateiimport.job.xmlimport;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * @author Stefan
 */
public class
XmlDefaultHandler extends DefaultHandler
{

  private static final String KEY_OF = "quell_referenz_of";
  private static final String KEY_STATSPEZ = "statspez_key";
  private static final String XML_PARAM = "param";
  protected final LoggerIfc log = Logger.getInstance()
    .getLogger(this.getClass());
  private final StringBuilder builder;
  private final HashMap<String, String> values;
  private final ArrayList<XmlBean> beans;
  private int rows;
  private final int offset;
  private final int maxSize;
  private String attributeName;
  private Locator locator;
  private int lineNumber;

  /**
   * Instantiates a new xml default handler.
   *
   * @param offset  the offset
   * @param maxSize the max size
   */
  public XmlDefaultHandler(int offset, int maxSize)
  {
    this.rows = 0;
    this.offset = offset;
    this.maxSize = maxSize;
    this.builder = new StringBuilder();
    this.values = new HashMap<>();
    this.beans = new ArrayList<>();

  }

  /**
   * Gets the xml beans.
   *
   * @return the xml beans
   */
  public ArrayList<XmlBean> getXmlBeans()
  {
    return this.beans;
  }

  @Override
  public void setDocumentLocator(final Locator locator)
  {
    this.locator = locator; // Save the locator, so that it can be used later for line tracking when traversing nodes.
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
    qName = qName.trim()
      .toLowerCase();
    switch (qName)
    {
      case XmlImportJob.XML_ERHEBUNG:
      case XmlImportJob.XML_ADRESSE:
      case XmlImportJob.XML_FIRMA:
      case XmlImportJob.XML_MELDER:
      case XmlImportJob.XML_VORBELEGUNG:
        lineNumber = locator != null ? locator.getLineNumber() : 0;
        break;
      case XML_PARAM:
        lineNumber = locator != null ? locator.getLineNumber() : 0;
        int attributeLength = attributes.getLength();
        for (int i = 0; i < attributeLength; i++)
        {
          String attrName = attributes.getQName(i);
          if ("feldname".equals(attrName))
          {
            this.attributeName = attrName + "." + attributes.getValue(i).trim();
          }
          if ("feldwert".equals(attrName))
          {
            this.builder.append(attributes.getValue(i));
          }
        }
    }
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
    qName = qName.trim()
      .toLowerCase();
    switch (qName)
    {
      case XmlImportJob.XML_ERHEBUNG:
        this.addElement(KEY_STATSPEZ, XmlImportJob.XML_ERHEBUNG);
        break;
      case XmlImportJob.XML_ADRESSE:
        this.addElement(KEY_OF, XmlImportJob.XML_ADRESSE);
        break;
      case XmlImportJob.XML_FIRMA:
        this.addElement(KEY_OF, XmlImportJob.XML_FIRMA);
        break;
      case XmlImportJob.XML_MELDER:
        this.addElement(KEY_OF, XmlImportJob.XML_MELDER);
        break;
      case XmlImportJob.XML_VORBELEGUNG:
        this.addElement(KEY_OF, XmlImportJob.XML_VORBELEGUNG);
        break;
      case XML_PARAM:
        this.values.put(this.attributeName, value);
        break;
      default:
        this.values.put(qName, value);
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
   * Adds the element.
   *
   * @param identifier  the identifier
   * @param elementName the element name
   * @throws SAXException the SAX exception
   */
  private void addElement(String identifier, String elementName) throws SAXException
  {
    this.rows++;
    if (this.rows > this.offset)
    {
      this.values.put(XmlBean.ELEMENT_NAME, elementName);
      XmlBean bean = new XmlBean(identifier, this.values);
      bean.setRowNumber(lineNumber);
      log.debug(bean.toString());
      this.beans.add(bean);
    }
    this.values.clear();
    if (this.beans.size() >= this.maxSize)
    {
      // Sonst gibt es keine Möglichkeit, frühzeitig abzubrechen!
      throw new SAXException("LIMIT");
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
