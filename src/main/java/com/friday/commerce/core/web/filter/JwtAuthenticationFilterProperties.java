package com.friday.commerce.core.web.filter;

import com.friday.commerce.core.web.filter.JwtAuthenticationFilter.PathClass;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "security.jwt.filter")
@Component
public class JwtAuthenticationFilterProperties {

    private List<String> excludePathPatterns = new ArrayList<>();
    private List<String> optionalPathPatterns = new ArrayList<>();
    private List<String> includePathPatterns = new ArrayList<>();
    private PathClass defaultPathPattern = PathClass.REQUIRED;

    private List<String> excludeMethods = List.of();
    private boolean cookieFallback = false;
    private String atCookie = "Access-Token";
}
