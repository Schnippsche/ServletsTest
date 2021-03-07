package de.destatis.regdb;

import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

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
  /**
   * The log.
   */
  protected final LoggerIfc log = Logger.getInstance().getLogger(this.getClass());

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
      SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
      this.ecipher = Cipher.getInstance(key.getAlgorithm());
      this.dcipher = Cipher.getInstance(key.getAlgorithm());

      // Prepare the parameter to the ciphers
      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

      // Create the ciphers
      this.ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
      this.dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
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
      byte[] enc = this.ecipher.doFinal(utf8);

      return Base64.getEncoder().encodeToString(enc);

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
      byte[] dec = Base64.getDecoder().decode(str);
      // Decrypt
      byte[] utf8 = this.dcipher.doFinal(dec);

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