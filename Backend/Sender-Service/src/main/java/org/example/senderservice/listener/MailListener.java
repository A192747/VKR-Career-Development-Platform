package org.example.senderservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.senderservice.event.Event;
import org.example.senderservice.event.EventService;
import org.example.senderservice.event.EventStatus;
import org.example.senderservice.mail.KafkaMailMessage;
import org.example.senderservice.mail.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailListener implements Listener {
    private final EmailService emailService;
    private final EventService eventService;

    @KafkaListener(topics = "${spring.kafka.topics.mailing}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Override
    public void listen(KafkaMailMessage kafkaMailMessage, Acknowledgment acknowledgment) {
        log.info("Recived message" + kafkaMailMessage);
        Event event = eventService.findById(kafkaMailMessage.getEventId());

        try {
            emailService.sendEmail(kafkaMailMessage.getMessage());
            log.info("Message sent successfully");

            event.setStatus(EventStatus.COMPLETED);

            System.out.println("Message sent successfully");
            acknowledgment.acknowledge();  // Коммит в Kafka после успешной рассылки
        } catch (Exception e) {
            log.error("Failed to send message: " + e.getMessage());
            event.setStatus(EventStatus.FAILED);
            throw new RuntimeException(e);  // Исключение для повторной попытки
        } finally {
            eventService.save(event);
        }
    }
}
