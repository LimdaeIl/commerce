package com.friday.commerce.core.utils.snowflake;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
class SnowflakeConfig {

    @Bean
    public Snowflake snowflake(SnowflakeProperties props) {
        Long configured = props.getNodeId();

        final long node;

        if (configured != null) {
            node = configured;
        } else if (props.isAutoDetectNodeId()) {
            node = Snowflake.inferNodeId();
        } else {
            throw new IllegalStateException(
                    "snowflake.node-id must be set when auto-detect-node-id=false");
        }

        return new Snowflake(
                props.getEpochMillis(),
                node,
                props.getClockSkewToleranceMillis()
        );
    }
}
