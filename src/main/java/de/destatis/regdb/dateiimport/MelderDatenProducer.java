/*
 * @(#)MelderDatenProducer.java 1.00.12.01.2020
 * Copyright 2020 Statistisches Bundesamt
 * @author Stefan Toengi (Destatis)
 */
package de.destatis.regdb.dateiimport;

import java.security.GeneralSecurityException;
import java.util.concurrent.BlockingQueue;

import de.werum.sis.crypt.PackedKeyPair;
import de.werum.sis.idev.intern.actions.util.MelderDaten;
import de.werum.sis.idev.res.job.JobException;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;
import de.werum.sis.idev.res.secure.PasswordAlgorithm;
import de.werum.sis.idev.res.secure.UserDataEncryptionAlgorithm;
import de.werum.sis.idev.res.secure.UserKeyEncryptionAlgorithm;
import de.werum.sis.idev.res.util.PasswordGenerator;

/**
 * The Class MelderDatenProducer.
 */
public class MelderDatenProducer implements Runnable
{

  /** The log. */
  protected final LoggerIfc log = Logger.getInstance()
      .getLogger(this.getClass());
  /** The blocking queue. */
  private final BlockingQueue<MelderDaten> blockingQueue;
  /** The quit. */
  private boolean quit = false;

  /**
   * Instantiates a new melder daten producer.
   *
   * @param blockingQueue the blocking queue
   */
  MelderDatenProducer(BlockingQueue<MelderDaten> blockingQueue)
  {
    this.blockingQueue = blockingQueue;
  }

  /**
   * Run.
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    try
    {
      while (!Thread.currentThread()
          .isInterrupted() && !this.quit)
      {
        this.blockingQueue.put(this.passwortUndSchluesselGenerieren());
      }
    }
    catch (InterruptedException e)
    {
      this.quit = true;
      this.log.info("Beende MelderdatenProducer");
      Thread.currentThread()
          .interrupt();
    }
    catch (JobException e)
    {
      this.log.error(e.getMessage(), e);
      this.quit = true;
    }
  }

  /**
   * Schluessel generieren.
   *
   * @param systemPasswort the system passwort
   * @return the melder daten
   * @throws JobException the job exception
   */
  private MelderDaten schluesselGenerieren(String systemPasswort) throws JobException
  {
    try
    {
      byte[] privaterSchluessel;
      byte[] oeffentlicherSchluessel;
      PackedKeyPair keyPair = UserDataEncryptionAlgorithm.generateUserDataKeyPair();
      privaterSchluessel = keyPair.getPackedPrivateKey();
      oeffentlicherSchluessel = keyPair.getPackedPublicKey();
      return this.datenVerschluesseln(systemPasswort, privaterSchluessel, oeffentlicherSchluessel);
    }
    catch (GeneralSecurityException e)
    {
      throw new JobException(e.getMessage(), e);
    }
  }

  /**
   * Daten verschluesseln.
   *
   * @param systemPasswort the system passwort
   * @param privaterSchluessel the privater schluessel
   * @param oeffentlicherSchluessel the oeffentlicher schluessel
   * @return the melder daten
   * @throws JobException the job exception
   */
  private MelderDaten datenVerschluesseln(String systemPasswort, byte[] privaterSchluessel, byte[] oeffentlicherSchluessel) throws JobException
  {
    try
    {
      String passwort = PasswordAlgorithm.createExternalPassword(systemPasswort);
      byte[] privaterSchluesselGeschuetzt;
      byte[] oeffentlicherSchluesselGeschuetzt;
      privaterSchluesselGeschuetzt = UserKeyEncryptionAlgorithm.encryptUserKey(privaterSchluessel, systemPasswort);
      oeffentlicherSchluesselGeschuetzt = UserKeyEncryptionAlgorithm.encryptUserKey(oeffentlicherSchluessel, systemPasswort);
      return new MelderDaten(systemPasswort, passwort, privaterSchluessel, oeffentlicherSchluessel, privaterSchluesselGeschuetzt, oeffentlicherSchluesselGeschuetzt);
    }
    catch (GeneralSecurityException e)
    {
      throw new JobException(e.getMessage(), e);
    }
  }

  /**
   * Passwort und schluessel generieren.
   *
   * @return the melder daten
   * @throws JobException Exception
   */
  private MelderDaten passwortUndSchluesselGenerieren() throws JobException
  {
    String systemPasswort = PasswordGenerator.generatePassword();
    return this.schluesselGenerieren(systemPasswort);
  }
}
