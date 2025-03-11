package org.example.mainservice.mail.service.internal.template;

import org.example.mainservice.mail.TemplateType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface TemplateHandler {
    TemplateType templateName();
}
