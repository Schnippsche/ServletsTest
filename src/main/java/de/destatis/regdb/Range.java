package de.destatis.regdb;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Stefan Toengi
 * Klasse zum Abbilden von Zahlenreihen als Range
 * z.B. wird aus dem Set 1000,1001,1002,1003,1004,1009
 * der String 1000-1004,1009
 */
public class Range
{

  private final Set<Integer> values;
  private static final Pattern TOKEN_PATTERN = Pattern.compile("([+-]?\\d+)-([+-]?\\d+)");
  private String strRange;
  private int anzahl;

  /**
   *
   */
  public Range()
  {
    this(new HashSet<>());
  }

  public Range(HashSet<Integer> newSet)
  {
    this.strRange = "";
    this.values = newSet;
    this.anzahl = -1;
  }

  @XmlAttribute
  public String getRange()
  {
    buildRange();
    return this.strRange;
  }

  @XmlAttribute
  public int getAnzahl()
  {
    if (this.anzahl == -1)
    {
      return (this.values != null) ? this.values.size() : 0;
    }
    return this.anzahl;
  }

  public void setAnzahl(int newAnzahl)
  {
    this.anzahl = newAnzahl;
  }

  public void setRange(String range)
  {
    this.strRange = range;
    buildSet();
  }

  private void buildRange()
  {
    int len = this.values.size();
    int idx1 = 0;
    int idx2 = 0;
    List<Integer> arr = this.values.stream().filter(i -> i > 0).sorted().collect(Collectors.toList());

    StringBuilder builder = new StringBuilder();
    boolean komma = false;
    while (idx1 < len)
    {
      while (++idx2 < len && arr.get(idx2) - arr.get(idx2 - 1) == 1)
      {
      }
      if (idx2 - idx1 > 2)
      {
        builder.append(komma ? ',' : "").append(arr.get(idx1)).append('-').append(arr.get(idx2 - 1));
        komma = true;
        idx1 = idx2;
      }
      else
      {
        for (; idx1 < idx2; idx1++)
        {
          builder.append(komma ? ',' : "").append(arr.get(idx1));
          komma = true;
        }
      }
    }

    this.strRange = builder.toString();
  }

  private void buildSet()
  {
    this.values.clear();
    if (this.strRange == null || this.strRange.isEmpty())
    {
      return;
    }
    String[] tokens = this.strRange.split("\\s*,\\s*");
    Matcher matcher;
    for (String token : tokens)
    {
      matcher = TOKEN_PATTERN.matcher(token);
      if (matcher.find())
      {
        int upper = Integer.parseInt(matcher.group(2));
        int lower = Integer.parseInt(matcher.group(1));
        this.values.addAll(IntStream.range(lower, upper + 1).boxed().collect(Collectors.toSet()));
      }
      else
      {
        this.values.add(Integer.parseInt(token));
      }
    }

  }

  /**
   * liefert die Werte im Set
   *
   * @return values
   */
  public Set<Integer> getValues()
  {
    return this.values;
  }

  @Override
  public String toString()
  {
    return "Range [range=" + this.strRange + "]";
  }

}
