package org.example.mainservice.mail.service.internal.template;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mainservice.mail.TemplateType;

@Entity
@Table(name = "mail_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private TemplateType name;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "body", nullable = false)
    private String body;
}
