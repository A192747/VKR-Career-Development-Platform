package org.example.senderservice.sender;

import org.example.senderservice.model.EmailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Override
    public void sendEmail(EmailMessage message) {
        System.out.println("Send email");
    }
}
