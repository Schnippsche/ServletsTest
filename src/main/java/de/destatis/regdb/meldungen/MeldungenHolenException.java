/*
 * @(#)MeldungenHolenException.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.meldungen;

public class MeldungenHolenException extends Exception
{

  private static final long serialVersionUID = 1174003615091286197L;

  /**
   * Instantiates a new meldungen holen exception.
   *
   * @param message the message
   */
  public MeldungenHolenException(String message)
  {
    super(message);
  }

}
