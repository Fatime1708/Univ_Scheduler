package com.univscheduler.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String EMAIL_EXPEDITEUR = "nnogoye.faye@univ-thies.sn";
    private static final String MOT_DE_PASSE     = "kjyjyynzxcfzbszg";

    public static boolean envoyerEmail(String destinataire, 
                                        String sujet, 
                                        String contenu) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destinataire));
            msg.setSubject(sujet);
            msg.setText(contenu);
            Transport.send(msg);
            System.out.println("✓ Email envoyé à : " + destinataire);
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            return false;
        }
    }
}