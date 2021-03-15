package de.destatis.regdb;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class Email.
 */
public class Email
{
  private Set<String> empfaenger;
  private String absender;
  private String betreff;
  private String text;

  /**
   * Instantiates a new email.
   */
  public Email()
  {
    super();
  }

  /**
   * Instantiates a new email.
   *
   * @param empfaenger the list of empfaenger
   * @param absender   the absender
   * @param betreff    the betreff
   * @param text       the text
   */
  public Email(Set<String> empfaenger, String absender, String betreff, String text)
  {
    super();
    this.empfaenger = empfaenger;
    this.absender = absender;
    this.betreff = betreff;
    this.text = text;
  }

  /**
   * Instantiates a new Email.
   *
   * @param empfaenger the empfaenger
   * @param absender   the absender
   * @param betreff    the betreff
   * @param text       the text
   */
  public Email(String empfaenger, String absender, String betreff, String text)
  {
    this(new HashSet<>(), absender, betreff, text);
    addEmpfaenger(empfaenger);
  }

  /**
   * Instantiates a new Email.
   *
   * @param absender the absender
   * @param betreff  the betreff
   * @param text     the text
   */
  public Email(String absender, String betreff, String text)
  {
    this((String) null, absender, betreff, text);

  }

  /**
   * Gets the empfaenger.
   *
   * @return the empfaenger
   */
  public Set<String> getEmpfaenger()
  {
    if (this.empfaenger == null)
    {
      this.empfaenger = new HashSet<>();
    }
    return this.empfaenger;
  }

  /**
   * Sets the empfaenger.
   *
   * @param empfaenger the new empfaenger
   */
  public void setEmpfaenger(Set<String> empfaenger)
  {
    this.empfaenger = empfaenger;
  }

  /**
   * Add empfaenger.
   *
   * @param empfaenger the empfaenger
   */
  public void addEmpfaenger(String empfaenger)
  {
    if (empfaenger != null)
      this.getEmpfaenger().add(empfaenger);
  }

  /**
   * Gets the absender.
   *
   * @return the absender
   */
  public String getAbsender()
  {
    return this.absender;
  }

  /**
   * Sets the absender.
   *
   * @param absender the new absender
   */
  public void setAbsender(String absender)
  {
    this.absender = absender;
  }

  /**
   * Gets the betreff.
   *
   * @return the betreff
   */
  public String getBetreff()
  {
    return this.betreff;
  }

  /**
   * Sets the betreff.
   *
   * @param betreff the new betreff
   */
  public void setBetreff(String betreff)
  {
    this.betreff = betreff;
  }

  /**
   * Gets the text.
   *
   * @return the text
   */
  public String getText()
  {
    return this.text;
  }

  /**
   * Sets the text.
   *
   * @param text the new text
   */
  public void setText(String text)
  {
    this.text = text;
  }

}
