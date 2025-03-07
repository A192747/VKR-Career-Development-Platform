package org.example.senderservice.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    void save(Event event);
    void delete(UUID event);
    Event findById(UUID id);
    Page<Event> findAll(Pageable pageable);
    Page<Event> findMailingWaitingEvents(Pageable pageable);
}
