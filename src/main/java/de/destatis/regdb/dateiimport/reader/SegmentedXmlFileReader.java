package de.destatis.regdb.dateiimport.reader;

import de.destatis.regdb.dateiimport.job.xmlimport.XmlBean;
import de.destatis.regdb.dateiimport.job.xmlimport.XmlDefaultHandler;
import de.werum.sis.idev.res.job.JobException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * The type Segmented xml file reader.
 */
public class SegmentedXmlFileReader implements SegmentedFileReader<XmlBean>
{
  @Override
  public ArrayList<XmlBean> readSegment(Path path, Charset charset, int offset, int len) throws JobException
  {
    XmlDefaultHandler handler = new XmlDefaultHandler(offset, len);
    try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ))
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(is, handler);
    }
    catch (SAXException e)
    {
      if (!"LIMIT".equals(e.getMessage()))
      {
        throw new JobException(e.getMessage(), e);
      }
    }
    catch (IOException | ParserConfigurationException e)
    {
      throw new JobException(e.getMessage(), e);
    }
    return handler.getXmlBeans();
  }
}
