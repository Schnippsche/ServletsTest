package de.destatis.regdb;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

/**
 * Die Klasse <code>DesEncrypter</code> liefert Methoden zur
 * Verschlüsselung/Entschlüsselung von Objekten(Strings)
 * <p>
 *
 * @author Toengi
 * @version 1 vom 16.03.2020
 **/
public class DesEncrypter
{
  private Cipher ecipher;
  private Cipher dcipher;
  /** The log. */
  protected final LoggerIfc log = Logger.getInstance()
      .getLogger(this.getClass());

  /**
   * Instantiates a new des encrypter.
   *
   * @param passPhrase the pass phrase
   */
  public DesEncrypter(String passPhrase)
  {
    try
    {
      // Create the key
      // Iteration count
      int iterationCount = 19;// 8-byte Salt
      byte[] salt = {(byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03};
      KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
      SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
          .generateSecret(keySpec);
      ecipher = Cipher.getInstance(key.getAlgorithm());
      dcipher = Cipher.getInstance(key.getAlgorithm());

      // Prepare the parameter to the ciphers
      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

      // Create the ciphers
      ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
      dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
    }
    catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e)
    {
      this.log.error(e.getMessage(), e);
    }
  }

  /**
   * Encrypt.
   *
   * @param str the str
   * @return the string
   */
  public String encrypt(String str)
  {
    try
    {
      // Encode the string into bytes using utf-8
      byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);

      // Encrypt
      byte[] enc = ecipher.doFinal(utf8);

      return Base64.getEncoder()
          .encodeToString(enc);

    }
    catch (BadPaddingException | IllegalBlockSizeException e)
    {
      this.log.error(e.getMessage(), e);
    }

    return null;
  }

  /**
   * Decrypt.
   *
   * @param str the str
   * @return the string
   */
  public String decrypt(String str)
  {
    try
    {
      // Decode base64 to get bytes
      byte[] dec = Base64.getDecoder()
          .decode(str);
      // Decrypt
      byte[] utf8 = dcipher.doFinal(dec);

      // Decode using utf-8
      return new String(utf8, StandardCharsets.UTF_8);
    }
    catch (BadPaddingException | IllegalBlockSizeException e)
    {
      this.log.error(e.getMessage(), e);
    }

    return null;
  }
}