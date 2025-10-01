package com.friday.commerce.catalog.domain.policy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "commerce.catalog.category")
public class CategoryProperties {

    private int maxDepth;
    private int hardLimit;
}
