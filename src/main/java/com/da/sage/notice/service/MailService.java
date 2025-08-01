/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 14:37:45                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-14 18:50:29                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MailService {
  private static String HOST = "smtp.office365.com";
  private static String PORT = "587";
  private static String SENDER = "xxx@yourdomain.com";
  private static String PASSWORD = "your_password";

  public static void init(Vertx vertx) {
    JsonObject mailServer = vertx.getOrCreateContext().config().getJsonObject("mailServer");

    if (mailServer != null) {
      HOST = mailServer.getString("host", HOST);
      PORT = mailServer.getString("port", PORT);
      SENDER = mailServer.getString("sender", SENDER);
      PASSWORD = mailServer.getString("password", PASSWORD);
    }
  }

  public static void setSender(String sender) {
    SENDER = sender;
  }

  public static void setPassword(String password) {
    PASSWORD = password;
  }

  public static void sendEmail(String subject, String body, String mailTo, String mailCc) {

    Properties props = new Properties();
    props.put("mail.smtp.host", HOST);
    props.put("mail.smtp.port", PORT);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.ssl.trust", HOST);

    // Remove duplicates and trim whitespace from mailTo and mailCC
    Set<String> toSet = new HashSet<>();
    Set<String> ccSet = new HashSet<>();
    if (mailTo != null && !mailTo.isEmpty()) {
      for (String addr : mailTo.split(";")) {
        if (addr != null && !addr.trim().isEmpty()) {
          toSet.add(addr.trim());
        }
      }
    }
    if (mailCc != null && !mailCc.isEmpty()) {
      for (String addr : mailCc.split(";")) {
        if (addr != null && !addr.trim().isEmpty()) {
          ccSet.add(addr.trim());
        }
      }
    }

    // Ensure we have at least one recipient
    if (toSet.isEmpty()) {
      log.warn("Mail to is empty");
      return;
    }

    try {
      // login authentication
      Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(SENDER, PASSWORD);
        }
      });

      // build email content, multi recipients with ',' not ';'
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(SENDER));

      // TODO If you want to do test, change the TO and CC email address.
      // String to = "r.huang@dedienne-aero.com";
      // String cc = "";
      String to = String.join(",", toSet);
      String cc = String.join(",", ccSet);
      // log.debug("To recipients: {}", to);
      // log.debug("CC recipients: {}", cc);
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
      if (!ccSet.isEmpty()) {
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
      }
      message.setSubject(subject);
      message.setContent(body, "text/html; charset=UTF-8");

      // Add outlook specific headers
      message.setHeader("X-Message-Flag", "Follow up");
      message.setHeader("X-Priority", "1");
      message.setHeader("Importance", "High");

      // send mail
      Transport.send(message);

      log.info("Mail {} sent successfully!", subject);
    } catch (MessagingException e) {
      log.error("Failed to send mail {}: {}", subject, e.getMessage());
    }

  }
}
