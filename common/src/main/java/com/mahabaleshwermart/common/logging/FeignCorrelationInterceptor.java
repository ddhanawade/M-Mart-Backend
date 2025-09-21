package com.mahabaleshwermart.common.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign interceptor to propagate correlation and session/user headers to downstream services.
 */
public class FeignCorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            template.header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
        }

        // Propagate guest session and user headers if present on the incoming request
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            HttpServletRequest req = servletAttrs.getRequest();
            String guestSession = req.getHeader("X-Guest-Session");
            if (guestSession != null && !guestSession.isBlank()) {
                template.header("X-Guest-Session", guestSession);
            }
            String userId = req.getHeader("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }
        }
    }
}


