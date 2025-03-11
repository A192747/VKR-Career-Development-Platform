package org.example.senderservice.mail;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(MailMessage message) throws MessagingException;
}
