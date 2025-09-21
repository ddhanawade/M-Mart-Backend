package com.mahabaleshwermart.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

@Aspect
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class RestControllerLoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerClass() {}

    @Pointcut("execution(* *(..))")
    public void anyMethod() {}

    @Around("restControllerClass() && anyMethod()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String methodSignature = pjp.getSignature().toShortString();
        Object[] args = pjp.getArgs();
        log.info("Controller start: {} args={}", methodSignature, Arrays.toString(args));
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            if (result instanceof ResponseEntity<?> responseEntity) {
                log.info("Controller end: {} status={} durationMs={}"
                        , methodSignature, responseEntity.getStatusCode().value(), duration);
            } else {
                log.info("Controller end: {} durationMs={}", methodSignature, duration);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("Controller error: {} durationMs={} message={}", methodSignature, duration, ex.getMessage(), ex);
            throw ex;
        }
    }
}


