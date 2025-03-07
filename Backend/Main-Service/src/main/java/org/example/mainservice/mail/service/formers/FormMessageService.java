package org.example.mainservice.mail.service.formers;

import org.example.mainservice.mail.service.internal.MailMessage;
import org.example.mainservice.mail.service.internal.template.MailTemplate;
import org.example.mainservice.mail.service.internal.template.MailTemplateRepository;
import org.example.mainservice.mail.service.internal.template.TemplateHandler;
import org.example.mainservice.mail.TemplateType;
import org.mapstruct.ap.internal.util.AnnotationProcessingException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

public interface FormMessageService {

    MailMessage formMessage(Map<String, String> args);

    default TemplateType getTemplateName() {
        TemplateHandler annotation = this.getClass().getAnnotation(TemplateHandler.class);
        if (annotation != null) {
            return annotation.templateName();
        }
        //Аннотация обязательна
        throw new AnnotationProcessingException("Необходимо указать аннотацию для сервиса");
    }

    default MailMessage formDefaultMailMessage(Map<String, String> args, MailTemplateRepository mailTemplateRepository) {
        MailMessage mailMessage = new MailMessage();

        // Логика формирования сообщения
        MailTemplate template = mailTemplateRepository.findByName(this.getTemplateName());
        if (template == null) {
            throw new NoSuchElementException(this.getTemplateName().name());
        }

        String str = template.getBody();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            str = str.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        mailMessage.setBody(str);
        mailMessage.setTitle(template.getTitle());
        mailMessage.setSendTo(args.get("sendTo"));

        return mailMessage;
    }
}
