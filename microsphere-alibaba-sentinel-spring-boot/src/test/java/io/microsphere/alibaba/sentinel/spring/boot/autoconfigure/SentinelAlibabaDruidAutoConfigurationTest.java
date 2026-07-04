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


import io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable;
import io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidFilter;
import io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAlibabaDruidAutoConfiguration.Config;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * {@link SentinelAlibabaDruidAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelAlibabaDruidAutoConfiguration
 * @since 1.0.0
 */
class SentinelAlibabaDruidAutoConfigurationTest extends AutoConfigurationTest<SentinelAlibabaDruidAutoConfiguration> {

    @Test
    void tesOnDisabledProperty() {
        assertDisabledProperty("microsphere.alibaba.druid.enabled=false",
                Config.class, SentinelAlibabaDruidFilter.class);
    }

    @Test
    void testOnMissingClass() {
        assertFilteredClass("com.alibaba.druid.pool.DruidDataSource",
                Config.class, SentinelAlibabaDruidFilter.class);
    }

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(Config.class);
        autoConfiguredClasses.add(SentinelAlibabaDruidFilter.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.sentinel.alibaba-druid.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(ConditionalOnAlibabaDruidAvailable.class);
        globalMissingClasses.add(SentinelAlibabaDruidFilter.class);
    }
}