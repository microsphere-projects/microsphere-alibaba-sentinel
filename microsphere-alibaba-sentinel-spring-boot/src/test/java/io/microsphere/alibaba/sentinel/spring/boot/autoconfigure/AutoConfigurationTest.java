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

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;
import static org.springframework.core.ResolvableType.forClass;

/**
 * Abstract class for auto-configuration tests
 *
 * @param <A> the type of auto-configuration class
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationContextRunner
 * @since 1.0.0
 */
abstract class AutoConfigurationTest<A> {

    ApplicationContextRunner applicationContextRunner;

    final Class<A> autoConfigurationClass;

    AutoConfigurationTest() {
        this.autoConfigurationClass = (Class<A>) forClass(getClass())
                .as(AutoConfigurationTest.class).resolveGeneric(0);
    }

    @BeforeEach
    void setUp() {
        this.applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(of(autoConfigurationClass));
    }

    void assertDisabledProperty(String propertyValue, Class<?>... beanClasses) {
        this.applicationContextRunner.withPropertyValues(propertyValue)
                .run(context -> {
                    for (Class<?> beanClass : beanClasses) {
                        assertThat(context).doesNotHaveBean(beanClass);
                    }
                });
    }

    void assertFilteredClass(String filteredClass, Class<?>... beanClasses) {
        this.applicationContextRunner.withClassLoader(new FilteredClassLoader(filteredClass))
                .run(context -> {
                    for (Class<?> beanClass : beanClasses) {
                        assertThat(context).doesNotHaveBean(beanClass);
                    }
                });
    }
}
