package com.friday.commerce.core.mail.infrastructure;

import com.friday.commerce.core.mail.application.port.MailSenderPort;
import com.friday.commerce.core.mail.domain.EmailMessage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaMailSenderAdapter implements MailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String defaultFrom;

    @Override
    public void send(EmailMessage msg) {
        mailSender.send(mime -> {
            var helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(msg.to());
            helper.setSubject(msg.subject());
            helper.setFrom(Optional.ofNullable(msg.from()).orElse(defaultFrom));
            helper.setText(msg.htmlBody(), true);
        });
    }
}

