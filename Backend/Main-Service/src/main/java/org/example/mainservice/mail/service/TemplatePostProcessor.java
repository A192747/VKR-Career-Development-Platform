package org.example.mainservice.mail.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.mail.service.formers.FormMessageService;
import org.example.mainservice.mail.service.internal.template.TemplateHandler;
import org.example.mainservice.mail.TemplateType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TemplatePostProcessor implements BeanPostProcessor {
    protected final static Map<TemplateType, FormMessageService> TEMPLATE_TO_SERVICE = new HashMap<>();
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(TemplateHandler.class)) {
            Method method = bean.getClass().getMethod("getTemplateName");
            method.setAccessible(true);
            TemplateType type = (TemplateType) method.invoke(bean);
            TEMPLATE_TO_SERVICE.put(type, (FormMessageService) bean);
            log.info("Added " + beanName + " bean " + bean.getClass().getName());
        }
        return bean;
    }
}
