package de.destatis.regdb.db;

import de.destatis.regdb.Email;
import de.werum.sis.idev.res.conf.db.DBConfig;
import de.werum.sis.idev.res.log.Logger;
import de.werum.sis.idev.res.log.LoggerIfc;
import de.werum.sis.idev.res.secure.ObfuscationAlgorithm;

import javax.mail.*;
import javax.mail.internet.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

/**
 * The Class MailVersandDaemon.
 */
public class MailVersandDaemon
{
  private static MailVersandDaemon instance = null;

  private final LoggerIfc log;
  private Session session;
  private String emailProtocol;
  private String emailUser;
  private String emailPassword;
  private String emailHost;
  private int emailPort;
  private int emailBlockSize;

  /**
   * Instantiates a new mail versand daemon.
   */
  private MailVersandDaemon()
  {
    super();
    this.log = Logger.getInstance().getLogger(this.getClass());
    this.log.info("initing " + this.getClass());
  }

  /**
   * Gets the single instance of MailVersandDaemon.
   *
   * @return single instance of MailVersandDaemon
   */
  public static synchronized MailVersandDaemon getInstance()
  {
    if (instance == null)
    {
      instance = new MailVersandDaemon();
      instance.checkSettings();
    }
    return instance;
  }

  /**
   * Check settings.
   */
  private void checkSettings()
  {
    Connection connection = ConnectionTool.getInstance().getConnection();
    if (connection == null)
    {
      throw new IllegalStateException("Connection konnte nicht erzeugt werden!");
    }
    try
    {
      DBConfig dbConfig = new DBConfig();
      boolean useAuth;

      this.emailProtocol = dbConfig.getParameter(connection, "int_email_protokoll");
      this.emailHost = dbConfig.getParameter(connection, "int_email_host");
      this.emailPort = Integer.parseInt(dbConfig.getParameter(connection, "int_email_port"));
      String obfuscatedEmailUser = dbConfig.getParameter(connection, "int_email_user");
      String obfuscatedEmailPassword = dbConfig.getParameter(connection, "int_email_passwort");
      String blockSize = dbConfig.getParameter(connection, "TRANSFER_MAIL_BLOCKSIZE");
      this.emailBlockSize = 10;
      if (blockSize != null && !blockSize.isEmpty())
      {
        this.emailBlockSize = Integer.parseInt(blockSize);
      }

      if (this.emailHost != null && !this.emailHost.isEmpty())
      {
        if (obfuscatedEmailUser != null && !obfuscatedEmailUser.isEmpty())
        {
          String deobfuscatedEmailUser = new String(ObfuscationAlgorithm.deobfuscate(obfuscatedEmailUser));
          if (!deobfuscatedEmailUser.isEmpty())
          {
            useAuth = true;
            this.emailUser = deobfuscatedEmailUser;
            if (obfuscatedEmailPassword != null && !obfuscatedEmailPassword.isEmpty())
            {
              this.emailPassword = new String(ObfuscationAlgorithm.deobfuscate(obfuscatedEmailPassword));
            } else
            {
              this.emailPassword = obfuscatedEmailPassword;
            }
          } else
          {
            useAuth = false;
            this.emailUser = null;
            this.emailPassword = null;
          }
        } else
        {
          useAuth = false;
          this.emailUser = null;
          this.emailPassword = null;
        }
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.timeout", 4000);
        javaMailProperties.put("mail.smtp.connectiontimeout", 4000);
        if (useAuth)
        {
          javaMailProperties.put("mail.smtp.auth", "true");
        }
        this.session = Session.getInstance(javaMailProperties);
      }
    } catch (Exception e)
    {
      this.log.error("Mail-Versand nicht moeglich!" + e.getMessage());
    } finally
    {
      ConnectionTool.getInstance().freeConnection(connection);
    }
  }

  /**
   * Send mail.
   *
   * @param email the email
   * @return boolean true wenn erfolgreich
   */
  public boolean sendMail(Email email)
  {
    // Absender
    InternetAddress absender = createValidAddress(email.getAbsender());
    if (absender == null)
    {
      this.log.error("Mail-Versand abgebrochen, da fehlerhafte Absender-Email!");
      return false;
    }
    try
    {
      LinkedList<InternetAddress> emailListe = sammleValideMailAddressen(email.getEmpfaenger());
      Date now = new Date();
      boolean result = true;
      for (int i = 0; i < emailListe.size(); i += this.emailBlockSize)
      {
        MimeMessage message = createMimeMessage();
        message.setFrom(absender);
        // Textteil
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(email.getText(), "UTF-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        for (int j = 0; j < this.emailBlockSize && !emailListe.isEmpty(); j++)
        {
          InternetAddress em = emailListe.poll();
          message.addRecipient(Message.RecipientType.BCC, em);
        }
        message.setSubject(email.getBetreff(), StandardCharsets.UTF_8.name());
        message.setContent(multipart);
        message.setSentDate(now);
        if (!send(message))
        {
          result = false;
        }
      }
      return result;
    } catch (MessagingException exc)
    {
      this.log.error("Mailversand fehlgeschlagen: " + exc.getMessage());
    }
    return false;
  }

  /**
   * Sammle valide mail addressen.
   *
   * @param emails the emails
   * @return the linked list
   */
  private LinkedList<InternetAddress> sammleValideMailAddressen(Set<String> emails)
  {
    LinkedList<InternetAddress> result = new LinkedList<>();
    for (String mail : emails)
    {
      InternetAddress ia = createValidAddress(mail);
      if (ia != null)
      {
        result.add(ia);
      }
    }
    this.log.debug("sammleValideEmails liefert " + result.size());
    return result;
  }

  /**
   * Creates the valid address.
   *
   * @param mail the mail
   * @return the internet address
   */
  public InternetAddress createValidAddress(String mail)
  {
    try
    {
      return new InternetAddress(mail);
    } catch (AddressException exc)
    {
      this.log.error("Fehlerhafte Mail-Adresse " + mail);
    }
    return null;
  }

  /**
   * Creates the mime message.
   *
   * @return the mime message
   */
  public MimeMessage createMimeMessage()
  {
    return new MimeMessage(this.session);
  }

  /**
   * Send.
   *
   * @param mimeMessage the mime message
   * @return the boolean
   */
  private boolean send(MimeMessage mimeMessage)
  {
    Transport transport = null;
    try
    {
      transport = this.session.getTransport(this.emailProtocol);
      transport.connect(this.emailHost, this.emailPort, this.emailUser, this.emailPassword);
      transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
      this.log.info("Mail verschickt an " + getAllEmpfaenger(mimeMessage));
      return true;
    } catch (AuthenticationFailedException ex)
    {
      this.log.error("Mail-Server-Authentifizierung fehlgeschlagen:" + ex.getMessage());
    } catch (MessagingException ex)
    {
      this.log.error("Mail-Server-Verbindung fehlgeschlagen:" + ex.getMessage());
    } finally
    {
      try
      {
        if (transport != null)
        {
          transport.close();
        }
      } catch (MessagingException e)
      {
        this.log.error("Fehler beim Schliessen der Mailverbindung", e);
      }
    }
    return false;
  }

  private String getAllEmpfaenger(MimeMessage mimeMessage) throws MessagingException
  {
    StringBuilder builder = new StringBuilder();
    Address[] addr = mimeMessage.getAllRecipients();
    for (Address address : addr)
    {
      if (address instanceof InternetAddress)
      {
        InternetAddress ia = (InternetAddress) address;
        if (builder.length() > 0)
        {
          builder.append(',');
        }
        builder.append(ia.getAddress());
      }
    }
    return builder.toString();
  }

}