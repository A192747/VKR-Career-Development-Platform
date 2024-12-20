package org.example.senderservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.senderservice.model.EmailTemplate;
import org.example.senderservice.model.TemplateType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EmailTemplateConfig {

    private static final Log log = LogFactory.getLog(EmailTemplateConfig.class);
    @Getter
    private Map<TemplateType, EmailTemplate> templates = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            templates = loadTemplates();
            System.out.println(templates);
            log.info("Loaded " + templates.size() + " templates.");
        } catch (IOException e) {
            log.error("Error loading email templates", e);
        }
    }

    @Value("${email-templates.path:classpath:/templates/*.yml}")
    private String templatesPath;

    public Map<TemplateType, EmailTemplate> loadTemplates() throws IOException {
        Map<TemplateType, EmailTemplate> templates = new HashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(templatesPath);

        log.info("Resource size " + resources.length);
        for (Resource resource : resources) {
            // Загрузка и обработка каждого файла YAML
            String fileName = resource.getFilename();
            if (fileName != null && fileName.endsWith(".yml")) {
                // Преобразование YAML в объект EmailTemplate
                EmailTemplate template = loadTemplateFromYaml(resource);
                String name = fileName.substring(0, fileName.lastIndexOf("."));
                templates.put(TemplateType.valueOf(name.toUpperCase()), template);
            }
        }

        return templates;
    }

    private EmailTemplate loadTemplateFromYaml(Resource resource) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = resource.getInputStream()) {
            log.info("Loading template from: " + resource.getFilename());
            EmailTemplate template = yaml.loadAs(inputStream, EmailTemplate.class);
            log.info("Loaded template: " + template);
            return template;
        } catch (IOException e) {
            log.error("Failed to load template from file: " + resource.getFilename(), e);
            throw new RuntimeException("Failed to load email template from file", e);
        }
    }
}
