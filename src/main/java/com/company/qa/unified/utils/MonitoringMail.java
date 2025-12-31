package com.company.qa.unified.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Utility for sending monitoring / alert emails.
 * Used for:
 * - Test failure notifications
 * - Smoke test alerts
 * - CI/CD monitoring
 * - SLA breach notifications
 */
public class MonitoringMail {

    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;

    public MonitoringMail(
            String smtpHost,
            String smtpPort,
            String username,
            String password
    ) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
    }

    /**
     * Sends HTML email.
     */
    public void sendMail(
            String from,
            String[] to,
            String subject,
            String htmlBody
    ) {

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        // SSL (for Gmail / enterprise SMTP)
        props.put("mail.smtp.socketFactory.port", smtpPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(String.join(",", to))
            );
            message.setSubject(subject);

            // High priority
            message.addHeader("X-Priority", "1");

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(htmlBody, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("✅ Monitoring email sent successfully");

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send monitoring email", e);
        }
    }
}
