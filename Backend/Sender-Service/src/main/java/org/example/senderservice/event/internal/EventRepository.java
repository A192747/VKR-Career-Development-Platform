package org.example.senderservice.event.internal;

import org.example.senderservice.event.Event;
import org.example.senderservice.event.EventStatus;
import org.example.senderservice.event.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE e.eventType = :eventType AND e.status = :status AND e.triggeredAt < :date")
    Page<Event> findByEventTypeAndStatusAndEventDateAfter(@Param("eventType") EventType eventType,
                                                          @Param("status") EventStatus status,
                                                          @Param("date") Instant date,
                                                          Pageable pageable);
}
