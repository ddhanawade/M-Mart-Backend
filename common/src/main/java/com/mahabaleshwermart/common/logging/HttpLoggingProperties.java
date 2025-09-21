package com.mahabaleshwermart.common.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "logging.http")
public class HttpLoggingProperties {
    private boolean enabled = true;
    private boolean logHeaders = true;
    private boolean logRequestBody = true;
    private boolean logResponseBody = false;
    private int maxPayloadLength = 4096;
    private boolean includeClientIp = true;

    private Set<String> sensitiveHeaders = new LinkedHashSet<>(Set.of(
            "authorization", "cookie", "set-cookie"
    ));

    private Set<String> sensitiveBodyFields = new LinkedHashSet<>(Set.of(
            "password", "confirmPassword", "token", "accessToken", "refreshToken"
    ));
}


