package org.example.senderservice.mail;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
