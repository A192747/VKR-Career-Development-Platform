package org.example.senderservice.listener;

import org.example.senderservice.mail.KafkaMailMessage;
import org.springframework.kafka.support.Acknowledgment;

public interface Listener {
    void listen(KafkaMailMessage message, Acknowledgment acknowledgment);
}
