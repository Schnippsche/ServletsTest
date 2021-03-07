/*
 * @(#)WrongSessionException.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.db;

public class WrongSessionException extends Exception
{

  /**
   *
   */
  private static final long serialVersionUID = -4706613918512088059L;

  /**
   * Instantiates a new wrong session exception.
   */
  public WrongSessionException()
  {
    super("Session fehlerhaft oder nicht mehr gueltig");
  }

  /**
   * Instantiates a new wrong session exception.
   *
   * @param message the message
   * @param cause   the cause
   */
  public WrongSessionException(String message, Throwable cause)
  {
    super(message, cause);

  }

  /**
   * Instantiates a new wrong session exception.
   *
   * @param message the message
   */
  public WrongSessionException(String message)
  {
    super(message);
  }

  /**
   * Instantiates a new wrong session exception.
   *
   * @param cause the cause
   */
  public WrongSessionException(Throwable cause)
  {
    super(cause);
  }

}
