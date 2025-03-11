package org.example.mainservice.mail.service;

import lombok.RequiredArgsConstructor;
import org.example.mainservice.mail.TemplateType;
import org.example.mainservice.mail.service.formers.FormMessageService;
import org.springframework.stereotype.Component;
import static org.example.mainservice.mail.service.TemplatePostProcessor.TEMPLATE_TO_SERVICE;

@Component
@RequiredArgsConstructor
public class TemplateServiceRouter {

    public FormMessageService getService(TemplateType templateName) {
        FormMessageService service = TEMPLATE_TO_SERVICE.get(templateName);
        if (service == null) {
            throw new IllegalArgumentException("No service found for template: " + templateName);
        }
        return service;
    }
}
