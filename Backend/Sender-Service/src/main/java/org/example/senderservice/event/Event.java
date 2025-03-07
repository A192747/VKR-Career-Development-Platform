package org.example.senderservice.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private UUID id;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;
    @Column(name = "body", nullable = false)
    private String body;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;
}
