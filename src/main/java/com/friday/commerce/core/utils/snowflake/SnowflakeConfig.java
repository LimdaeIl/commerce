package com.friday.commerce.core.utils.snowflake;

import com.friday.commerce.core.web.exception.AppErrorCode;
import com.friday.commerce.core.web.exception.AppException;
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
            throw new AppException(AppErrorCode.SNOWFLAKE_NODE_ID_REQUIRED);

        }

        return new Snowflake(
                props.getEpochMillis(),
                node,
                props.getClockSkewToleranceMillis()
        );
    }
}
