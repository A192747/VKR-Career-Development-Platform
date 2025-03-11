package org.example.mainservice.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.event.EventService;
import org.example.mainservice.mail.service.TemplateServiceRouter;
import org.example.mainservice.mail.service.internal.MailMessage;
import org.example.mainservice.event.Event;
import org.example.mainservice.event.EventType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DelayedSenderServiceImpl implements DelayedSenderService {
    private final TemplateServiceRouter templateServiceRouter;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    @Override
    public void sendMessage(Map<String, String> args, TemplateType templateType) throws JsonProcessingException {
        log.info("Message preparing");
        log.info("Arguments: {}", args);
        MailMessage mailMessage = templateServiceRouter.getService(templateType).formMessage(args);
        log.info("Arguments: {}", args);
        log.info("Message formed");
        log.info(mailMessage.toString());

        Event event = new Event();
        event.setCreatedAt(Instant.now());
        event.setBody(objectMapper.writeValueAsString(mailMessage));
        event.setEventType(EventType.MAILING);
        event.setTriggeredAt(Instant.parse(args.get("sendAt")));
        log.info("Event created");

        eventService.save(event);
        log.info("Event saved");
    }
}
