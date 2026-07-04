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
import io.microsphere.alibaba.sentinel.spring.boot.condition.ConditionalOnSentinelAvailable;
import io.microsphere.mybatis.spring.annotation.EnableMyBatisExtension;
import io.microsphere.mybatis.spring.boot.autoconfigure.condition.ConditionalOnMyBatisAvailable;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants.ENABLED_PROPERTY_NAME;


/**
 * The Spring Boot Auto-Configuration class of Alibaba Sentinel x MyBatis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration
 * @see org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
 * @see com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
 * @see io.microsphere.mybatis.spring.boot.autoconfigure.MyBatisAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnSentinelAvailable
@ConditionalOnMyBatisAvailable
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
@ConditionalOnClass(name = {
        "io.microsphere.mybatis.spring.annotation.EnableMyBatisExtension",                // Microsphere MyBatis Spring
        "io.microsphere.alibaba.sentinel.mybatis.executor.SentinelMyBatisExecutorFilter"  // Microsphere Alibaba Sentinel x Mybatis
})
@AutoConfigureAfter(name = {
        "com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration",                    // Spring Cloud Alibaba Sentinel
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",                 // MyBatis Spring Boot
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",            // MyBatis Plus Spring Boot
        "io.microsphere.mybatis.spring.boot.autoconfigure.MyBatisAutoConfiguration"       // Microsphere Alibaba Sentinel Spring Boot x Mybatis
})
@Import(value = {
        SentinelMyBatisAutoConfiguration.Config.class
})
public class SentinelMyBatisAutoConfiguration {

    @EnableMyBatisExtension
    static class Config {

        @Bean
        @ConditionalOnMissingBean
        public SentinelMyBatisExecutorFilter sentinelMyBatisExecutorFilter() {
            return new SentinelMyBatisExecutorFilter();
        }
    }
}
