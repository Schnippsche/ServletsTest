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
   *
   * @param message the message
   */
  public DBLockException(String message)
  {
    super(message);
  }

}
