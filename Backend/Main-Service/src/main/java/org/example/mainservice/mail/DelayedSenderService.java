package org.example.mainservice.mail;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface DelayedSenderService {
    void sendMessage(Map<String, String> args, TemplateType templateType) throws JsonProcessingException;
}
