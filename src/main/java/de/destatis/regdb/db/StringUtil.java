package de.destatis.regdb.db;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;

public class StringUtil
{

  /**
   * The empty String {@code ""}.
   *
   * @since 2.0
   */
  public static final String EMPTY = "";

  // Left/Right/Mid
  // -----------------------------------------------------------------------

  /**
   * <p>
   * Gets the leftmost {@code len} characters of a String.
   * </p>
   * <p>
   * If {@code len} characters are not available, or the
   * String is {@code null}, the String will be returned without
   * an exception. An empty String is returned if len is negative.
   * </p>
   *
   * <pre>
   * StringUtils.left(null, *)    = null
   * StringUtils.left(*, -ve)     = ""
   * StringUtils.left("", *)      = ""
   * StringUtils.left("abc", 0)   = ""
   * StringUtils.left("abc", 2)   = "ab"
   * StringUtils.left("abc", 4)   = "abc"
   * </pre>
   *
   * @param str the String to get the leftmost characters from, may be null
   * @param len the length of the required String
   * @return the leftmost characters, {@code null} if null String input
   */
  public static String left(final String str, final int len)
  {
    if (str == null)
    {
      return null;
    }
    if (len < 0)
    {
      return EMPTY;
    }
    if (str.length() <= len)
    {
      return str;
    }
    return str.substring(0, len);
  }

  /**
   * liefert den linken Teil eines getrimmten Strings
   *
   * @param str the String to get the leftmost characters from, may be null
   * @param len the length of the required String
   * @return the leftmost characters, {@code null} if null String input
   */
  public static String leftTrim(final String str, final int len)
  {
    return (str == null) ? null : left(str.trim(), len);
  }

  /**
   * <p>
   * Gets {@code len} characters from the middle of a String.
   * </p>
   * <p>
   * If {@code len} characters are not available, the remainder
   * of the String will be returned without an exception. If the
   * String is {@code null}, {@code null} will be returned.
   * An empty String is returned if len is negative or exceeds the
   * length of {@code str}.
   * </p>
   *
   * <pre>
   * StringUtils.mid(null, *, *)    = null
   * StringUtils.mid(*, *, -ve)     = ""
   * StringUtils.mid("", 0, *)      = ""
   * StringUtils.mid("abc", 0, 2)   = "ab"
   * StringUtils.mid("abc", 0, 4)   = "abc"
   * StringUtils.mid("abc", 2, 4)   = "c"
   * StringUtils.mid("abc", 4, 2)   = ""
   * StringUtils.mid("abc", -2, 2)  = "ab"
   * </pre>
   *
   * @param str the String to get the characters from, may be null
   * @param pos the position to start from, negative treated as zero
   * @param len the length of the required String
   * @return the middle characters, {@code null} if null String input
   */
  public static String mid(final String str, int pos, final int len)
  {
    if (str == null)
    {
      return null;
    }
    if (len < 0 || pos > str.length())
    {
      return EMPTY;
    }
    if (pos < 0)
    {
      pos = 0;
    }
    if (str.length() <= pos + len)
    {
      return str.substring(pos);
    }
    return str.substring(pos, pos + len);
  }

  /**
   * <p>
   * Gets the rightmost {@code len} characters of a String.
   * </p>
   * <p>
   * If {@code len} characters are not available, or the String
   * is {@code null}, the String will be returned without an
   * an exception. An empty String is returned if len is negative.
   * </p>
   *
   * <pre>
   * StringUtils.right(null, *)    = null
   * StringUtils.right(*, -ve)     = ""
   * StringUtils.right("", *)      = ""
   * StringUtils.right("abc", 0)   = ""
   * StringUtils.right("abc", 2)   = "bc"
   * StringUtils.right("abc", 4)   = "abc"
   * </pre>
   *
   * @param str the String to get the rightmost characters from, may be null
   * @param len the length of the required String
   * @return the rightmost characters, {@code null} if null String input
   */
  public static String right(final String str, final int len)
  {
    if (str == null)
    {
      return null;
    }
    if (len < 0)
    {
      return EMPTY;
    }
    if (str.length() <= len)
    {
      return str;
    }
    return str.substring(str.length() - len);
  }

  // Substring
  // -----------------------------------------------------------------------

  /**
   * <p>
   * Gets a substring from the specified String avoiding exceptions.
   * </p>
   * <p>
   * A negative start position can be used to start {@code n}
   * characters from the end of the String.
   * </p>
   * <p>
   * A {@code null} String will return {@code null}.
   * An empty ("") String will return "".
   * </p>
   *
   * <pre>
   * StringUtils.substring(null, *)   = null
   * StringUtils.substring("", *)     = ""
   * StringUtils.substring("abc", 0)  = "abc"
   * StringUtils.substring("abc", 2)  = "c"
   * StringUtils.substring("abc", 4)  = ""
   * StringUtils.substring("abc", -2) = "bc"
   * StringUtils.substring("abc", -4) = "abc"
   * </pre>
   *
   * @param str   the String to get the substring from, may be null
   * @param start the position to start from, negative means
   *              count back from the end of the String by this many characters
   * @return substring from start position, {@code null} if null String input
   */
  public static String substring(final String str, int start)
  {
    if (str == null)
    {
      return null;
    }

    // handle negatives, which means last n characters
    if (start < 0)
    {
      start = str.length() + start; // remember start is negative
    }

    if (start < 0)
    {
      start = 0;
    }
    if (start > str.length())
    {
      return EMPTY;
    }

    return str.substring(start);
  }

  /**
   * <p>
   * Gets a substring from the specified String avoiding exceptions.
   * </p>
   * <p>
   * A negative start position can be used to start/end {@code n}
   * characters from the end of the String.
   * </p>
   * <p>
   * The returned substring starts with the character in the {@code start}
   * position and ends before the {@code end} position. All position counting is
   * zero-based -- i.e., to start at the beginning of the string use
   * {@code start = 0}. Negative start and end positions can be used to
   * specify offsets relative to the end of the String.
   * </p>
   * <p>
   * If {@code start} is not strictly to the left of {@code end}, ""
   * is returned.
   * </p>
   *
   * <pre>
   * StringUtils.substring(null, *, *)    = null
   * StringUtils.substring("", * ,  *)    = "";
   * StringUtils.substring("abc", 0, 2)   = "ab"
   * StringUtils.substring("abc", 2, 0)   = ""
   * StringUtils.substring("abc", 2, 4)   = "c"
   * StringUtils.substring("abc", 4, 6)   = ""
   * StringUtils.substring("abc", 2, 2)   = ""
   * StringUtils.substring("abc", -2, -1) = "b"
   * StringUtils.substring("abc", -4, 2)  = "ab"
   * </pre>
   *
   * @param str   the String to get the substring from, may be null
   * @param start the position to start from, negative means
   *              count back from the end of the String by this many characters
   * @param end   the position to end at (exclusive), negative means
   *              count back from the end of the String by this many characters
   * @return substring from start position to end position,
   * {@code null} if null String input
   */
  public static String substring(final String str, int start, int end)
  {
    if (str == null)
    {
      return null;
    }

    // handle negatives
    if (end < 0)
    {
      end = str.length() + end; // remember end is negative
    }
    if (start < 0)
    {
      start = str.length() + start; // remember start is negative
    }

    // check length next
    if (end > str.length())
    {
      end = str.length();
    }

    // if start is greater than end, return ""
    if (start > end)
    {
      return EMPTY;
    }

    if (start < 0)
    {
      start = 0;
    }
    if (end < 0)
    {
      end = 0;
    }

    return str.substring(start, end);
  }

  public static int getInt(Object obj)
  {
    return getInt(obj, 0);
  }

  public static int getInt(Object obj, int defaultValue)
  {
    if (obj == null || obj.toString().isEmpty())
    {
      return defaultValue;
    }
    try
    {
      return Integer.parseInt(String.valueOf(obj));
    }
    catch (NumberFormatException e)
    {
      return defaultValue;
    }
  }

  public static LocalDateTime convertMySqlDate(Date date)
  {
    if (date == null)
    {
      return null;
    }
    //Converting Date to Timestamp
    Timestamp timestamp = new Timestamp(date.getTime());
    return timestamp.toLocalDateTime();
  }

  public static Date convertLocalDateTime(LocalDateTime ldt)
  {
    if (ldt == null)
    {
      return null;
    }
    Timestamp timestamp = Timestamp.valueOf(ldt);
    return new Date(timestamp.getTime());
  }

  /**
   * escapeSqlString liefert einen sicheren String für Datenbank-Operationen
   *
   * @param str der zu pruefende String
   * @return sicheren String für Datenbank
   */
  public static String escapeSqlString(String str)
  {
    if (str == null)
    {
      return "null";
    }
    StringBuilder buf = new StringBuilder((int) (str.length() * 1.1));
    // Dieser Code
    int stringLength = str.length();
    for (int i = 0; i < stringLength; ++i)
    {
      char c = str.charAt(i);
      switch (c)
      {
        case 0: /* Must be escaped for 'mysql' */
          buf.append('\\');
          buf.append('0');
          break;
        case '\n': /* Must be escaped for logs */
          buf.append('\\');
          buf.append('n');
          break;
        case '\r':
          buf.append('\\');
          buf.append('r');
          break;
        case '\\':
          buf.append('\\');
          buf.append('\\');
          break;
        case '\'':
          buf.append('\\');
          buf.append('\'');
          break;
        case '"': /* Better safe than sorry */
          buf.append('\\');
          buf.append('"');
          break;
        case '\032': /* This gives problems on Win32 */
          buf.append('\\');
          buf.append('Z');
          break;
        default:
          buf.append(c);
      }
    }
    return buf.toString();
  }

  /**
   * Not null.
   *
   * @param wert the wert
   * @return the string
   */
  public static String notNull(Object wert)
  {
    return wert == null ? "" : String.valueOf(wert);
  }

  public static String join(Collection<?> collection)
  {
    return join(collection, ",");
  }

  public static String join(Collection<?> collection, String delimiter)
  {
    StringBuilder buf = new StringBuilder();
    for (Object obj : collection)
    {
      if (obj != null)
      {
        buf.append(buf.length() == 0 ? "" : delimiter).append(obj);
      }
    }
    return buf.toString();
  }

}
