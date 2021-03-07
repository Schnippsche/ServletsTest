package de.destatis.regdb;

import de.destatis.regdb.dateiimport.ImportFormat;

import javax.xml.bind.annotation.XmlAttribute;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Stefan
 */
public class Importdatei
{

  /**
   * Anzahl der Datensätze innerhalb der Importdatei
   */
  @XmlAttribute
  public int anzahlDatensaetze;
  /**
   * aktuelles Datensatz Offset
   */
  @XmlAttribute
  public int datensatzOffset;
  @XmlAttribute
  public String gezippterDateiname;
  @XmlAttribute
  public ImportFormat importFormat;
  @XmlAttribute
  public String importVerzeichnis;
  @XmlAttribute
  public String dateiName;
  @XmlAttribute
  public String originalDateiname;
  @XmlAttribute
  public String zeichensatz;

  /**
   *
   */
  public Importdatei()
  {
    this.zeichensatz = "UTF-8";
    this.importFormat = ImportFormat.UNBEKANNT;
  }

  public Charset getCharset()
  {
    return Charset.forName(this.zeichensatz);
  }

  public Path getPath()
  {
    return Paths.get(this.importVerzeichnis, this.dateiName);
  }

}
