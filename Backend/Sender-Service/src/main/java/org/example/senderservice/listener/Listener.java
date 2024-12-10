package org.example.senderservice.listener;

import org.example.senderservice.model.EmailMessage;
import org.springframework.kafka.support.Acknowledgment;

public interface Listener {
    void listen(EmailMessage message, Acknowledgment acknowledgment);
}
