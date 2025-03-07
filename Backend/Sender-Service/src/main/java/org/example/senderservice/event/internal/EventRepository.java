package org.example.senderservice.event.internal;

import org.example.senderservice.event.Event;
import org.example.senderservice.event.EventStatus;
import org.example.senderservice.event.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Page<Event> findByEventTypeAndStatus(EventType eventType, EventStatus status, Pageable pageable);
}
