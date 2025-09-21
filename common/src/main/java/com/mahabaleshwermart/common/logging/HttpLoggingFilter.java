package com.mahabaleshwermart.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private final HttpLoggingProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !properties.isEnabled();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String correlationId = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY);

        logRequestLineAndHeaders(requestWrapper, correlationId);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponseLineAndHeaders(requestWrapper, responseWrapper, correlationId, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestLineAndHeaders(ContentCachingRequestWrapper request, String correlationId) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP Request: ").append(request.getMethod()).append(' ').append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append('?').append(request.getQueryString());
        }
        if (properties.isIncludeClientIp()) {
            sb.append(" from ").append(request.getRemoteAddr());
        }
        log.info(sb.toString());

        if (properties.isLogHeaders()) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String value = maskHeader(headerName, request.getHeader(headerName));
                log.info("Header: {}={}", headerName, value);
            }
        }

        if (properties.isLogRequestBody() && isTextLike(request.getContentType())) {
            String payload = readLimitedPayload(request.getContentAsByteArray());
            payload = maskSensitiveFields(payload);
            if (!payload.isBlank()) {
                log.info("RequestBody: {}", payload);
            }
        }
    }

    private void logResponseLineAndHeaders(ContentCachingRequestWrapper request,
                                           ContentCachingResponseWrapper response,
                                           String correlationId,
                                           long duration) throws IOException {
        log.info("HTTP Response: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(), duration);

        if (properties.isLogHeaders()) {
            for (String headerName : response.getHeaderNames()) {
                for (String value : response.getHeaders(headerName)) {
                    log.info("RespHeader: {}={}", headerName, maskHeader(headerName, value));
                }
            }
        }

        if (properties.isLogResponseBody() && isTextLike(response.getContentType())) {
            String payload = readLimitedPayload(response.getContentAsByteArray());
            payload = maskSensitiveFields(payload);
            if (!payload.isBlank()) {
                log.info("ResponseBody: {}", payload);
            }
        }
    }

    private boolean isTextLike(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith(MediaType.TEXT_PLAIN_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_XML_VALUE)
                || contentType.startsWith("application/x-www-form-urlencoded");
    }

    private String readLimitedPayload(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        int len = Math.min(bytes.length, properties.getMaxPayloadLength());
        return new String(bytes, 0, len, StandardCharsets.UTF_8);
    }

    private String maskHeader(String name, String value) {
        if (value == null) return null;
        if (name == null) return value;
        if (properties.getSensitiveHeaders().contains(name.toLowerCase())) {
            return maskValue(value);
        }
        return value;
    }

    private String maskSensitiveFields(String payload) {
        if (payload == null || payload.isBlank()) return payload;
        String masked = payload;
        for (String field : properties.getSensitiveBodyFields()) {
            // naive masking for common JSON shapes: "field":"value" or 'field':'value'
            masked = masked.replaceAll("(\\\"" + field + "\\\"\\s*:\\s*\\\")[^\\\"]+\\\"", "$1***\\\"")
                    .replaceAll("('" + field + "'\\s*:\\s*')([^']+)'", "$1***'");
        }
        return masked;
    }

    private String maskValue(String value) {
        if (value.length() <= 8) return "***";
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }
}


