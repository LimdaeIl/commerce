package com.friday.commerce.core.web.swagger;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalHeaderConfig {

    @Bean
    public OpenApiCustomizer addGlobalHeaders() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    op.addParametersItem(new Parameter()
                            .in("header")
                            .name("X-User-Id")
                            .schema(new StringSchema())
                            .description("현재 사용자 ID")
                            .required(false));
                    op.addParametersItem(new Parameter()
                            .in("header")
                            .name("X-User-Role")
                            .schema(new StringSchema())
                            .description("권한 (e.g., USER, ADMIN)")
                            .required(false));
                })
        );
    }
}
