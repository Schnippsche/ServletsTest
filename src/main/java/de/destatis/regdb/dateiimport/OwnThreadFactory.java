/*
 * @(#)OwnThreadFactory.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class OwnThreadFactory implements ThreadFactory
{

  /**
   * New thread.
   *
   * @param runnable the runnable
   * @return the thread
   */
  @Override
  public Thread newThread(Runnable runnable)
  {
    Thread thread = Executors.defaultThreadFactory()
        .newThread(runnable);
    thread.setDaemon(true);
    return thread;
  }
}
