package com.friday.commerce.core.mail.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class MailTemplateConfig {

    @Bean
    public ClassLoaderTemplateResolver emailTemplateResolver() {
        var r = new ClassLoaderTemplateResolver();
        r.setPrefix("templates/mail/"); // src/main/resources/templates/mail/
        r.setSuffix(".html");
        r.setTemplateMode("HTML");
        r.setCharacterEncoding("UTF-8");
        r.setCacheable(true);
        r.setOrder(1);
        return r;
    }
}
