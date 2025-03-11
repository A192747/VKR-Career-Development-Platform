package org.example.senderservice.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.senderservice.event.internal.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    @Override
    public void save(Event event) {
        eventRepository.save(event);
    }

    @Override
    public void delete(UUID event) {
        eventRepository.deleteById(event);
    }

    @Override
    public Event findById(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No event found with id: " + id));
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Page<Event> findMailingWaitingEvents(Pageable pageable, Instant date) {
        Instant value = date.plus(3, ChronoUnit.HOURS);
        return eventRepository.findByEventTypeAndStatusAndEventDateAfter(EventType.MAILING, EventStatus.WAITING, value, pageable);
    }
}
