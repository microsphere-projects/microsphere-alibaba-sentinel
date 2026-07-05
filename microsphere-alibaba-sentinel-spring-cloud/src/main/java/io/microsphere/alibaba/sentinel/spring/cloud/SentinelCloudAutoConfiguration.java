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

package io.microsphere.alibaba.sentinel.spring.cloud;

import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelPluginRepository;
import io.microsphere.alibaba.sentinel.spring.boot.condition.ConditionalOnSentinelAvailable;
import io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesAvailable;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Collection;
import java.util.List;

import static io.microsphere.alibaba.sentinel.common.SentinelPluginRepository.INSTANCE;
import static io.microsphere.collection.ListUtils.newLinkedList;

/**
 * The Spring Cloud Auto-Configuration Class of Alibaba Sentinel
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration
 * @see io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration
 * @see io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration
 * @see io.microsphere.redis.spring.cloud.autoconfigure.RedisCloudAutoConfiguration
 * @see io.microsphere.spring.cloud.client.actuator.ConfigurationPropertyHasFeaturesAutoConfiguration
 * @see io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAlibabaDruidAutoConfiguration
 * @see io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelMyBatisAutoConfiguration
 * @see io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelRedisAutoConfiguration
 * @see io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelSpringWebAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnSentinelAvailable
@AutoConfigureAfter(name = {
        "com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration",
        "io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration",      // Microsphere Alibaba Druid Spring Cloud
        "io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration",                 // Microsphere MyBatis Spring Cloud
        "io.microsphere.redis.spring.cloud.autoconfigure.RedisCloudAutoConfiguration",                     // Microsphere Redis Spring Cloud
        "io.microsphere.spring.cloud.client.actuator.ConfigurationPropertyHasFeaturesAutoConfiguration",   // Microsphere Spring Cloud
        "io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAlibabaDruidAutoConfiguration", // Microsphere Sentinel Spring Boot x Alibaba Druid
        "io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelMyBatisAutoConfiguration",      // Microsphere Sentinel Spring Boot x MyBatis
        "io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelRedisAutoConfiguration",        // Microsphere Sentinel Spring Boot x Redis
        "io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelSpringWebAutoConfiguration"     // Microsphere Sentinel Spring Boot x Spring Web
})
@Import(value = {
        SentinelCloudAutoConfiguration.FeaturesConfig.class
})
public class SentinelCloudAutoConfiguration {

    @ConditionalOnFeaturesAvailable
    public static class FeaturesConfig {

        public static final String BEAN_NAME = "alibaba-sentinel.features";

        @Bean(name = BEAN_NAME)
        public HasFeatures features() {
            SentinelPluginRepository sentinelPluginRepository = INSTANCE;
            Collection<SentinelPlugin> sentinelPlugins = sentinelPluginRepository.getAll();
            List<Class<?>> abstractFeatures = newLinkedList();
            List<NamedFeature> namedFeatures = newLinkedList();
            for (SentinelPlugin sentinelPlugin : sentinelPlugins) {
                Class<?> type = sentinelPlugin.getClass();
                if (sentinelPlugin.isEnabled()) {
                    String name = sentinelPlugin.getName();
                    NamedFeature namedFeature = new NamedFeature(name, type);
                    namedFeatures.add(namedFeature);
                } else {
                    abstractFeatures.add(type);
                }
            }
            HasFeatures hasFeatures = new HasFeatures(abstractFeatures, namedFeatures);
            return hasFeatures;
        }
    }
}
