package org.example.senderservice.mail;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMailMessage {
    @NotNull
    private UUID eventId;
    @NotNull
    private MailMessage message;
}
