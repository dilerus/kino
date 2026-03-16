package com.pagechange.notification;

import com.pagechange.config.AppConstants;
import com.pagechange.config.MonitoringConfig;
import com.pagechange.util.Sleeper;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

public class EmailNotifier implements Notifier {
    private final List<String> recipients;
    private final Sleeper sleeper;
    private final SoundNotifier soundNotifier;

    public EmailNotifier(List<String> recipients, Sleeper sleeper, SoundNotifier soundNotifier) {
        this.recipients = recipients;
        this.sleeper = sleeper;
        this.soundNotifier = soundNotifier;
    }

    @Override
    public void notify(NotificationContext context) {
        for (String email : recipients) {
            sendMail(email, context, 1);
        }
    }

    private void sendMail(String email, NotificationContext context, int retries) {
        if (soundNotifier != null) {
            soundNotifier.notify(context);
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", AppConstants.SMTP_HOST);
        properties.put("mail.smtp.port", AppConstants.SMTP_PORT);

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(
                                AppConstants.SMTP_USERNAME,
                                AppConstants.SMTP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConstants.SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Sukces!");
            message.setText(buildEmailText(context));

            Transport.send(message);
            System.out.println("Email na adres " + email + " zostal wyslany pomyslnie.");

        } catch (Exception e) {
            if (retries <= AppConstants.EMAIL_RETRY_LIMIT) {
                System.out.println("Wystapil blad podczas wysylania emaila: '" + e.getMessage() +
                        "', ponawiam probe wyslania maila, proba nr " + retries + "/" +
                        AppConstants.EMAIL_RETRY_LIMIT);
                sleeper.sleep(AppConstants.EMAIL_RETRY_DELAY_MS);
                sendMail(email, context, retries + 1);
            }
        }
    }

    private String buildEmailText(NotificationContext context) {
        MonitoringConfig config = context.getConfig();
        StringBuilder txt = new StringBuilder("Zmiana strony!!!\n");
        txt.append("Strona: ").append(config.getUrl()).append("\n");

        if (context.getPhrase() != null) {
            if (config.isNegation()) {
                txt.append("Nie znaleziono textu: ").append(context.getPhrase());
            } else {
                txt.append("Znaleziono text: ").append(context.getPhrase());
            }
        }

        return txt.toString();
    }
}
