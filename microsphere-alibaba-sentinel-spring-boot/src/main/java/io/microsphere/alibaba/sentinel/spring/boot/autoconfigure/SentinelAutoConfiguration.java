package io.microsphere.alibaba.sentinel.spring.boot.autoconfigure;

import io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants;
import io.microsphere.alibaba.sentinel.redis.SentinelRedisConstants;
import io.microsphere.alibaba.sentinel.spring.boot.condition.ConditionalOnSentinelAvailiable;
import io.microsphere.mybatis.spring.annotation.EnableMyBatis;
import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * Microsphere Sentinel Spring Boot Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnSentinelAvailiable
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
})
@Import(value = {
        SentinelAutoConfiguration.RedisConfiguration.class,
        SentinelAutoConfiguration.MyBatisConfiguration.class
})
public class SentinelAutoConfiguration {

    @ConditionalOnProperty(
            name = SentinelRedisConstants.ENABLED_PROPERTY_NAME,
            matchIfMissing = true
    )
    @ConditionalOnClass(name = {
            "org.springframework.data.redis.connection.RedisConnection",
            "io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor"
    })
    @EnableRedisInterceptor
    static class RedisConfiguration {
    }

    @ConditionalOnProperty(
            name = SentinelMyBatisConstants.ENABLED_PROPERTY_NAME,
            matchIfMissing = true
    )
    @ConditionalOnClass(name = {
            "org.apache.ibatis.executor.Executor"
    })
    @EnableMyBatis
    static class MyBatisConfiguration {
    }
}