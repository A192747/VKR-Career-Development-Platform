package org.example.senderservice.listener;

import org.example.senderservice.model.EmailMessage;
import org.example.senderservice.sender.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class ListenerImpl implements Listener {
    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "${spring.kafka.topics.mailing}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Override
    public void listen(EmailMessage message, Acknowledgment acknowledgment) {
        try {
            emailService.sendEmail(message);
            //log.info("Message sent successfully");
            System.out.println("Message sent successfully");
            acknowledgment.acknowledge();  // Коммит в Kafka после успешной рассылки
        } catch (Exception e) {
            System.out.println("Failed to send message: \" + e.getMessage()");
            //log.error("Failed to send message: " + e.getMessage());
            throw e;  // Исключение для повторной попытки
        }
    }
}
