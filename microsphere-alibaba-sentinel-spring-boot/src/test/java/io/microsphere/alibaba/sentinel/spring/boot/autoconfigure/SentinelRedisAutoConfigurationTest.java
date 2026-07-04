/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.alibaba.sentinel.spring.boot.autoconfigure;


import io.microsphere.alibaba.sentinel.mybatis.executor.SentinelMyBatisExecutorFilter;
import io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor;
import io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelRedisAutoConfiguration.Config;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SentinelRedisAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelRedisAutoConfiguration
 * @since 1.0.0
 */
class SentinelRedisAutoConfigurationTest extends AutoConfigurationTest<SentinelRedisAutoConfiguration> {

    @Test
    void testDefaults() {
        this.applicationContextRunner.run(context -> {
            assertThat(context).hasSingleBean(this.autoConfigurationClass)
                    .hasSingleBean(Config.class)
                    .hasSingleBean(SentinelRedisCommandInterceptor.class);
        });
    }

    @Test
    void tesOntDisabledProperty() {
        assertDisabledProperty("microsphere.sentinel.enabled=false",
                this.autoConfigurationClass, Config.class, SentinelMyBatisExecutorFilter.class);
        assertDisabledProperty("microsphere.redis.enabled=false",
                Config.class, SentinelRedisCommandInterceptor.class);
        assertDisabledProperty("microsphere.sentinel.redis.enabled=false",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertDisabledProperty("microsphere.redis.interceptor.enabled=false",
                Config.class, SentinelRedisCommandInterceptor.class);
    }

    @Test
    void testOnMissingClass() {
        assertFilteredClass("com.alibaba.csp.sentinel.SphU",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertFilteredClass("io.microsphere.alibaba.sentinel.common.SentinelPlugin",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertFilteredClass("org.springframework.data.redis.connection.RedisConnection",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertFilteredClass("io.microsphere.redis.metadata.RedisMetadataLoader",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertFilteredClass("io.microsphere.redis.spring.interceptor.RedisMethodInterceptor",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertFilteredClass("io.microsphere.redis.spring.annotation.EnableRedisInterceptor",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
        assertFilteredClass("io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor",
                this.autoConfigurationClass, Config.class, SentinelRedisCommandInterceptor.class);
    }
}