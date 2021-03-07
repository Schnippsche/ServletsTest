/*
 * @(#)RegDBSessionIdFactory.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */

package de.destatis.regdb.session;

import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import java.util.Arrays;
import java.util.Random;

public class RegDBSessionIdFactory
{

  private static RegDBSessionIdFactory sidFactory = null;

  private static final LoggerIfc log = Logger.getInstance().getLogger(RegDBSessionIdFactory.class);

  private final Random random;

  private static final char[] SESSION_ID_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

  private static final int RANDOM_CHAR_COUNT = 40;

  private final int maxRandomInt;

  private final int sessionIdLength;

  /**
   * Instantiates a new reg DB session id factory.
   */
  private RegDBSessionIdFactory()
  {
    this.random = new Random();
    this.maxRandomInt = ((int) Math.pow(SESSION_ID_CHARS.length, RANDOM_CHAR_COUNT)) - 1;
    this.sessionIdLength = RANDOM_CHAR_COUNT;
    log.debug("idLength: <" + this.sessionIdLength + "> maxRandomInt: <" + this.maxRandomInt + ">");
  }

  /**
   * Erzeuge session id.
   *
   * @return the string
   */
  public String erzeugeSessionId()
  {
    String sessionIdString;
    long timestamp = System.currentTimeMillis();
    this.random.setSeed(timestamp);
    int zufallszahl = this.random.nextInt(this.maxRandomInt);
    char[] sessionIdChars = new char[this.sessionIdLength];
    Arrays.fill(sessionIdChars, '0');
    int i = sessionIdChars.length - 1;
    int rest;
    int grenze = i - RANDOM_CHAR_COUNT;
    // log.test("Zufallszahl (neu): <"+zufallszahl+">");

    // Kodierung der Zufallszahl mit SESSION_ID_CHARS
    while (grenze < i)
    {
      if (zufallszahl <= 0)
      {
        zufallszahl = this.random.nextInt(this.maxRandomInt);
      }

      rest = zufallszahl % SESSION_ID_CHARS.length;
      zufallszahl /= SESSION_ID_CHARS.length;
      sessionIdChars[i--] = SESSION_ID_CHARS[rest];
    }
    sessionIdString = new String(sessionIdChars);
    return sessionIdString;
  }

  /**
   * Gets the single instance of RegDBSessionIdFactory.
   *
   * @return single instance of RegDBSessionIdFactory
   */
  public static synchronized RegDBSessionIdFactory getInstance()
  {
    if (sidFactory == null)
    {
      sidFactory = new RegDBSessionIdFactory();
    }
    return sidFactory;
  }

}
