package org.example.mainservice.mail.service.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage {
    @NotNull
    private String title;
    @NotNull
    private String body;
    @NotNull
    private String sendTo;
}
