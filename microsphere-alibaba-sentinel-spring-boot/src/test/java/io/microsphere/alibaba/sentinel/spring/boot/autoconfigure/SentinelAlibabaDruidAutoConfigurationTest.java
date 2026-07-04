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


import io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidFilter;
import io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAlibabaDruidAutoConfiguration.Config;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SentinelAlibabaDruidAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelAlibabaDruidAutoConfiguration
 * @since 1.0.0
 */
class SentinelAlibabaDruidAutoConfigurationTest extends AutoConfigurationTest<SentinelAlibabaDruidAutoConfiguration> {

    @Test
    void testDefaults() {
        this.applicationContextRunner.run(context -> {
            assertThat(context).hasSingleBean(this.autoConfigurationClass)
                    .hasSingleBean(Config.class)
                    .hasSingleBean(SentinelAlibabaDruidFilter.class);
        });
    }

    @Test
    void tesOntDisabledProperty() {
        assertDisabledProperty("microsphere.sentinel.enabled=false",
                this.autoConfigurationClass, Config.class, SentinelAlibabaDruidFilter.class);
        assertDisabledProperty("microsphere.sentinel.alibaba-druid.enabled=false",
                this.autoConfigurationClass, Config.class, SentinelAlibabaDruidFilter.class);
        assertDisabledProperty("microsphere.alibaba.druid.enabled=false",
                Config.class, SentinelAlibabaDruidFilter.class);
    }

    @Test
    void testOnMissingClass() {
        assertFilteredClass("com.alibaba.csp.sentinel.SphU",
                this.autoConfigurationClass, Config.class, SentinelAlibabaDruidFilter.class);
        assertFilteredClass("io.microsphere.alibaba.sentinel.common.SentinelPlugin",
                this.autoConfigurationClass, Config.class, SentinelAlibabaDruidFilter.class);
        assertFilteredClass("io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable",
                this.autoConfigurationClass, Config.class, SentinelAlibabaDruidFilter.class);
        assertFilteredClass("io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidFilter",
                this.autoConfigurationClass, Config.class, SentinelAlibabaDruidFilter.class);
        assertFilteredClass("com.alibaba.druid.pool.DruidDataSource",
                Config.class, SentinelAlibabaDruidFilter.class);
    }
}