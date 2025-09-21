package com.mahabaleshwermart.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class ServiceLoggingAspect {

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        log.debug("Service start: {}", method);
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.debug("Service end: {} durationMs={}", method, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("Service error: {} durationMs={} message={}", method, duration, ex.getMessage(), ex);
            throw ex;
        }
    }
}


