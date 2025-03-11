package org.example.mainservice.mail.service.internal.template;

import org.example.mainservice.mail.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {
    @Query("SELECT e FROM MailTemplate e WHERE e.name = :name")
    MailTemplate findByName(@Param("name") TemplateType name);
}
