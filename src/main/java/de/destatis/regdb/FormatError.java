package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import java.text.MessageFormat;
import java.util.Objects;

public class FormatError implements Comparable<FormatError>
{
  @XmlAttribute
  private final Integer rowNumber;
  private final String errorText;

  public FormatError(Integer rowNumber, String errorText)
  {
    this.rowNumber = rowNumber;
    this.errorText = errorText;
  }

  @Override
  public String toString()
  {
    if (rowNumber != null)
    {
      return MessageFormat.format("Zeile {0}: {1}", rowNumber, errorText);
    }
    return errorText;
  }

  @Override
  public int compareTo(FormatError that)
  {
    if (this.rowNumber != null || that.rowNumber != null)
    {
      if (this.rowNumber == null)
      {
        return -1;
      } else if (that.rowNumber == null)
      {
        return 1;
      } else
      {
        int rowNumberComparison = this.rowNumber.compareTo(that.rowNumber);
        if (rowNumberComparison != 0)
        {
          return rowNumberComparison < 0 ? -1 : 1;
        }
      }
    }

    return this.errorText.compareTo(that.errorText);
  }



  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FormatError that = (FormatError) o;
    return Objects.equals(rowNumber, that.rowNumber) && Objects.equals(errorText, that.errorText);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(rowNumber, errorText);
  }
}
