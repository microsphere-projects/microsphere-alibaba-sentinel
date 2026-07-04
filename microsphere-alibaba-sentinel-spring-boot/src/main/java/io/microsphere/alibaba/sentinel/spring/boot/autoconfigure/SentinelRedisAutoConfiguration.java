package io.microsphere.alibaba.sentinel.spring.boot.autoconfigure;

import io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor;
import io.microsphere.alibaba.sentinel.spring.boot.condition.ConditionalOnSentinelAvailable;
import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import io.microsphere.redis.spring.boot.autoconfigure.condition.ConditionalOnRedisAvailable;
import io.microsphere.redis.spring.boot.autoconfigure.condition.ConditionalOnRedisInterceptorEnabled;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static io.microsphere.alibaba.sentinel.redis.SentinelRedisConstants.ENABLED_PROPERTY_NAME;

/**
 * Microsphere Sentinel Spring Boot Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration
 * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnSentinelAvailable
@ConditionalOnRedisAvailable
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
@ConditionalOnClass(name = {
        "io.microsphere.redis.spring.annotation.EnableRedisInterceptor",                  // Microsphere Redis Spring
        "io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor"    // Microsphere Alibaba Sentinel x Redis
})
@AutoConfigureAfter(name = {
        "com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration",                    // Spring Cloud Alibaba Sentinel
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",       // Spring Boot Redis
})
@Import(value = {
        SentinelRedisAutoConfiguration.Config.class,
})
public class SentinelRedisAutoConfiguration {

    @ConditionalOnRedisInterceptorEnabled
    @EnableRedisInterceptor
    static class Config {

        @Bean
        @ConditionalOnMissingBean
        public SentinelRedisCommandInterceptor sentinelRedisCommandInterceptor() {
            return new SentinelRedisCommandInterceptor();
        }
    }

}