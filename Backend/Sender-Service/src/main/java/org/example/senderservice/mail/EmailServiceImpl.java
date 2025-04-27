package org.example.senderservice.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@PropertySource("classpath:mail.properties")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.enabled}")
    private boolean mailEnabled;

    @Value("${spring.mail.username}")
    private String fromUser;

    @Value("${spring.mail.test.username}")
    private String testUser;

    @Override
    public void sendEmail(MailMessage messageObj) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(fromUser);
        String sendTo;
        if (testUser != null) {
            sendTo = testUser;
        } else {
            sendTo = messageObj.getSendTo();
        }
        helper.setTo(sendTo);
        helper.setText(messageObj.getBody(), true);
        helper.setSubject(messageObj.getTitle());

        if (mailEnabled) {
            mailSender.send(mimeMessage);
            log.info("Mail send");
        } else {
            log.info("Mail not send, because mail.enabled=false");
        }

    }
}
