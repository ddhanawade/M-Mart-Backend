package com.mahabaleshwermart.common.logging;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(HttpLoggingProperties.class)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistrationBean() {
        FilterRegistrationBean<CorrelationIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorrelationIdFilter());
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestControllerLoggingAspect restControllerLoggingAspect() {
        return new RestControllerLoggingAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceLoggingAspect serviceLoggingAspect() {
        return new ServiceLoggingAspect();
    }

    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    @ConditionalOnMissingBean
    public RequestInterceptor feignCorrelationInterceptor() {
        return new FeignCorrelationInterceptor();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilterRegistrationBean(HttpLoggingProperties properties) {
        FilterRegistrationBean<HttpLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpLoggingFilter(properties));
        registrationBean.setOrder(Integer.MIN_VALUE + 1);
        return registrationBean;
    }
}


