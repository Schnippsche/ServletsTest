/*
 * @(#)ImportFormat.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport;

/**
 * The Enum ImportFormat.
 */
public enum ImportFormat
{

  UNBEKANNT,

  /**
   * The importohnezusatzfelder.
   */
  IMPORTOHNEZUSATZFELDER,

  /**
   * The importmitzusatzfelder.
   */
  IMPORTMITZUSATZFELDER,

  /**
   * The importmitansprechpartner.
   */
  IMPORTMITANSPRECHPARTNER,

  /**
   * The registerimport.
   */
  REGISTERIMPORT,

  /**
   * The vorbelegungsimport.
   */
  VORBELEGUNGSIMPORT,

  VORBELEGUNGDOWNLOADIMPORT,

  /**
   * The xmlimport.
   */
  XMLIMPORT,

  MELDERKONTOIMPORT
}
