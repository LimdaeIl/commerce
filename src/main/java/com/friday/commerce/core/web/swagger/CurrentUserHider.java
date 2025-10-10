package com.friday.commerce.core.web.swagger;
import com.friday.commerce.core.security.annotation.CurrentUser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class CurrentUserHider {
    @Bean
    public OperationCustomizer hideCurrentUserParams() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() == null || operation.getParameters().isEmpty()) return operation;

            Set<String> toHide = new HashSet<>();
            for (Parameter p : handlerMethod.getMethod().getParameters()) {
                if (p.isAnnotationPresent(CurrentUser.class)) {
                    toHide.add(p.getName());
                }
            }
            if (!toHide.isEmpty()) {
                operation.getParameters().removeIf(pp -> toHide.contains(pp.getName()));
            }
            return operation;
        };
    }
}
