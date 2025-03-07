package org.example.senderservice.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.senderservice.event.Event;
import org.example.senderservice.event.EventService;
import org.example.senderservice.event.EventStatus;
import org.example.senderservice.mail.KafkaMailMessage;
import org.example.senderservice.mail.MailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailDataBaseObserverImpl implements DataBaseObserver {
    private final EventService eventService;
    @Value("${spring.kafka.topics.mailing}")
    private String kafkaTopic;
    private final KafkaTemplate<String, KafkaMailMessage> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async
    @Scheduled(cron = "${database.observer.schedule:0 0/1 * * * *}")
    @Override
    public void observe() {
        int pageNumber = 0; // Начинаем с первой страницы
        int pageSize = 100; // Размер страницы (можно настроить)

        Page<Event> page;

        do {
            page = eventService.findMailingWaitingEvents(PageRequest.of(pageNumber, pageSize));
            page.getContent().forEach(event -> {
                event.setStatus(EventStatus.IN_PROGRESS);
                eventService.save(event);
                try {
                    KafkaMailMessage kafkaMailMessage = new KafkaMailMessage();
                    kafkaMailMessage.setEventId(event.getId());
                    MailMessage mailMessage = objectMapper.readValue(event.getBody(), MailMessage.class);
                    kafkaMailMessage.setMessage(mailMessage);
                    kafkaTemplate.send(kafkaTopic, kafkaMailMessage);
                } catch (Exception e) {
                    event.setStatus(EventStatus.FAILED);
                    eventService.save(event);
                    throw new RuntimeException(e);
                }
            });
            pageNumber++; // Переход к следующей странице
        } while (page.hasNext()); // Продолжаем, пока есть следующая страница
    }
}
