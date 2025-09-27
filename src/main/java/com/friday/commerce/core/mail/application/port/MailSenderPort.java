package com.friday.commerce.core.mail.application.port;

import com.friday.commerce.core.mail.domain.EmailMessage;

public interface MailSenderPort {
    void send(EmailMessage message);
}
