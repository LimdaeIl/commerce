package com.friday.commerce.core.mail.domain;

public record EmailMessage(
        String from, String to, String subject, String htmlBody
) { }
