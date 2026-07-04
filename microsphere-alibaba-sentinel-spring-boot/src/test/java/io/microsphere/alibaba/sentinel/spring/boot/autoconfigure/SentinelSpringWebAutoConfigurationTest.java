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


import io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SentinelSpringWebAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelSpringWebAutoConfiguration
 * @since 1.0.0
 */
class SentinelSpringWebAutoConfigurationTest extends AutoConfigurationTest<SentinelSpringWebAutoConfiguration> {

    @Test
    void testAutoConfiguredClasses() {
        this.applicationContextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(this.autoConfigurationClass)
                    .doesNotHaveBean(SentinelHandlerMethodInterceptor.class);
        });

        this.webApplicationContextRunner.run(context -> {
            assertThat(context).hasSingleBean(this.autoConfigurationClass)
                    .hasSingleBean(SentinelHandlerMethodInterceptor.class);
        });
    }

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(SentinelHandlerMethodInterceptor.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.sentinel.spring-web.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(HandlerMethod.class);
        globalMissingClasses.add(HandlerMethodInterceptor.class);
        globalMissingClasses.add(SentinelHandlerMethodInterceptor.class);
    }
}