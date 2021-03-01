/*
 * @(#)DBLockException.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.db;

public class DBLockException extends Exception
{

  /**
   *
   */
  private static final long serialVersionUID = 9089012232275129338L;

  /**
   * Instantiates a new DB lock exception.
   */
  public DBLockException()
  {
    super();
  }

  /**
   * Instantiates a new DB lock exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public DBLockException(String message, Throwable cause)
  {
    super(message, cause);

  }

  /**
   * Instantiates a new DB lock exception.
   *
   * @param message the message
   */
  public DBLockException(String message)
  {
    super(message);
  }

  /**
   * Instantiates a new DB lock exception.
   *
   * @param cause the cause
   */
  public DBLockException(Throwable cause)
  {
    super(cause);
  }

}
