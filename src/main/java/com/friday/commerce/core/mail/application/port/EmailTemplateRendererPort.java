package com.friday.commerce.core.mail.application.port;

import java.util.Map;

public interface EmailTemplateRendererPort {

    String render(String templateName, Map<String, Object> model);
}
