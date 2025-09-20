package com.mahabaleshwermart.cartservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign client configuration for debugging and error handling
 */
@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            String responseBody = "";
            try {
                if (response.body() != null) {
                    responseBody = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.error("Error reading response body", e);
            }

            log.error("Feign client error - Method: {}, Status: {}, URL: {}, Response Body: {}", 
                methodKey, response.status(), response.request().url(), responseBody);

            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
