/*
 * @(#)AbstractBean.java 1.00.27.02.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport.job.adressimport;

import java.io.Serializable;

/**
 * The Class AbstractBean.
 */
public abstract class AbstractBean implements Serializable
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Not null.
   *
   * @param wert the wert
   * @return the string
   */
  protected String notNull(Object wert)
  {
    return wert == null ? "" : String.valueOf(wert);
  }

}
