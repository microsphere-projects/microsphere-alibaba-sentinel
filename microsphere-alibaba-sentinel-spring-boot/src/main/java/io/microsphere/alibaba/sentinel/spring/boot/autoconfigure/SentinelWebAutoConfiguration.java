package io.microsphere.alibaba.sentinel.spring.boot.autoconfigure;

import io.microsphere.alibaba.sentinel.spring.boot.condition.ConditionalOnSentinelAvailable;
import io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import static io.microsphere.alibaba.sentinel.redis.SentinelRedisConstants.ENABLED_PROPERTY_NAME;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.ANY;

/**
 * Microsphere Sentinel Spring Boot Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration
 * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * @see org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
 * @see io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
 * @see org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
 * @see org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration
 * @see io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnWebApplication(type = ANY)
@ConditionalOnSentinelAvailable
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
@ConditionalOnClass(name = {
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor",                        // Microsphere Spring Web
        "io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor"               // Microsphere Alibaba Sentinel x Spring Web
})
@AutoConfigureAfter(name = {
        "com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration",                    // Spring Cloud Alibaba Sentinel
        "io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration",        // Microsphere Spring Boot WebMVC
        "io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration"       // Microsphere Spring Boot WebFlux
})
public class SentinelWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentinelHandlerMethodInterceptor sentinelHandlerMethodInterceptor() {
        return new SentinelHandlerMethodInterceptor();
    }
}