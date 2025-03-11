package org.example.mainservice.mail.service.formers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.mail.service.internal.MailMessage;
import org.example.mainservice.mail.service.internal.template.MailTemplateRepository;
import org.example.mainservice.mail.service.internal.template.TemplateHandler;
import org.example.mainservice.mail.TemplateType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
@TemplateHandler(templateName = TemplateType.MEETING_SOON)
@RequiredArgsConstructor
public class MsgSoonFormMessageServiceImpl implements FormMessageService {
    private final MailTemplateRepository mailTemplateRepository;
    @Override
    public MailMessage formMessage(Map<String, String> args) {
        if (args.get("sendAt") == null) {
            args.put("sendAt", String.valueOf(Instant.now()));
        } else {
            Instant sendAt = Instant.parse(args.get("sendAt"));
            Duration fifteenMinutes = Duration.ofMinutes(15);
            Instant newTime = sendAt.minus(fifteenMinutes);
            args.put("sendAt", String.valueOf(newTime));
        }
        return formDefaultMailMessage(args, mailTemplateRepository);
    }
}
