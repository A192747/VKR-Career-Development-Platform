package org.example.senderservice.sender;

import org.example.senderservice.model.EmailMessage;

public interface EmailService {
    void sendEmail(EmailMessage message);
}
