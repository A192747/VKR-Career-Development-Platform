package org.example.mainservice.mail.service.formers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.mail.TemplateType;
import org.example.mainservice.mail.service.internal.MailMessage;
import org.example.mainservice.mail.service.internal.template.MailTemplateRepository;
import org.example.mainservice.mail.service.internal.template.TemplateHandler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
@TemplateHandler(templateName = TemplateType.FEEDBACK_CAME)
@RequiredArgsConstructor
public class FeedbackCameFormMessageServiceImpl implements FormMessageService {
    private final MailTemplateRepository mailTemplateRepository;

    @Override
    public MailMessage formMessage(Map<String, String> args) {
        args.put("sendAt", Instant.now().toString());
        return formDefaultMailMessage(args, mailTemplateRepository);
    }

}
