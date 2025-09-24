package com.friday.commerce.core.utils.snowflake;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
class SnowflakeConfig {

    @Bean
    public Snowflake snowflake(SnowflakeProperties props) {
        long node = (props.getNodeId() != null)
                ? props.getNodeId()
                : (props.isAutoDetectNodeId() ? Snowflake.inferNodeId() : 0L);

        return new Snowflake(
                props.getEpochMillis(),
                node,
                props.getClockSkewToleranceMillis()
        );
    }
}
