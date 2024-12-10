package org.example.senderservice.model;

import lombok.Data;

import java.util.Map;


@Data
public class EmailMessage {
    private TemplateType templateType;
    private String recipientMail;
    private Map<String, String> placeholders;
}
