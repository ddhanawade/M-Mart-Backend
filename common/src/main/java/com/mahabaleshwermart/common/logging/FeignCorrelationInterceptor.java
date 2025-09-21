package com.mahabaleshwermart.common.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

/**
 * Feign interceptor to propagate correlation id to downstream services.
 */
public class FeignCorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            template.header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
        }
    }
}


