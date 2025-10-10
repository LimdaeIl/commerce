package com.friday.commerce.core.mail.infrastructure;

import com.friday.commerce.core.mail.application.port.EmailTemplateRendererPort;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class ThymeleafEmailTemplateRenderer implements EmailTemplateRendererPort {
    private final SpringTemplateEngine engine;

    @Override
    public String render(String templateName, Map<String, Object> model) {
        var ctx = new org.thymeleaf.context.Context(Locale.KOREA);
        ctx.setVariables(model);
        return engine.process(templateName, ctx);
    }
}
