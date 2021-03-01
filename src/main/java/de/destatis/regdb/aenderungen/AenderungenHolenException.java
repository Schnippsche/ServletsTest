/*
 * @(#)AenderungenHolenException.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.aenderungen;

public class AenderungenHolenException extends Exception
{

  private static final long serialVersionUID = 1174003615091286197L;

  /**
   * Instantiates a new aenderungen holen exception.
   *
   * @param message the message
   */
  public AenderungenHolenException(String message)
  {
    super(message);
  }

}
