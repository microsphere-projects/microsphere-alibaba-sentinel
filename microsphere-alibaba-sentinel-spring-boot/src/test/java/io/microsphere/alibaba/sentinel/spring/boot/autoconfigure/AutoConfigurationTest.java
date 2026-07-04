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

import com.alibaba.csp.sentinel.SphU;
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
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

    WebApplicationContextRunner webApplicationContextRunner;

    final Class<A> autoConfigurationClass;

    AutoConfigurationTest() {
        this.autoConfigurationClass = (Class<A>) forClass(getClass())
                .as(AutoConfigurationTest.class).resolveGeneric(0);
    }

    @BeforeEach
    void setUp() {
        this.applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(of(autoConfigurationClass));
        this.webApplicationContextRunner = new WebApplicationContextRunner()
                .withConfiguration(of(this.autoConfigurationClass));
    }

    @Test
    void testAutoConfiguredClasses() {
        this.applicationContextRunner.run(context -> {
            for (Class<?> beanClass : getAutoConfiguredClasses()) {
                assertThat(context).hasSingleBean(beanClass);
            }
        });

        this.webApplicationContextRunner.run(context -> {
            for (Class<?> beanClass : getAutoConfiguredClasses()) {
                assertThat(context).hasSingleBean(beanClass);
            }
        });
    }

    @Test
    void testOnGlobalDisabledProperty() {
        for (String propertyValue : getGlobalDisabledPropertyValues()) {
            assertDisabledProperty(propertyValue, getAutoConfiguredClasses());
        }
    }

    @Test
    void testOnGlobalMissingClass() {
        for (Class<?> missingClass : getGlobalMissingClasses()) {
            assertFilteredClass(missingClass.getName(), getAutoConfiguredClasses());
        }
    }

    protected final Class<?>[] getAutoConfiguredClasses() {
        Set<Class<?>> autoConfiguredClasses = newLinkedHashSet();
        autoConfiguredClasses.add(this.autoConfigurationClass);
        configureAutoConfiguredClasses(autoConfiguredClasses);
        return autoConfiguredClasses.toArray(EMPTY_CLASS_ARRAY);
    }

    protected final Set<String> getGlobalDisabledPropertyValues() {
        Set<String> globalDisabledPropertyValues = newLinkedHashSet();
        globalDisabledPropertyValues.add("microsphere.sentinel.enabled=false");
        configureGlobalDisabledPropertyValues(globalDisabledPropertyValues);
        return globalDisabledPropertyValues;
    }

    protected final Set<Class<?>> getGlobalMissingClasses() {
        Set<Class<?>> globalMissingClasses = newLinkedHashSet();
        globalMissingClasses.add(SphU.class);
        globalMissingClasses.add(SentinelPlugin.class);
        configureGlobalMissingClasses(globalMissingClasses);
        return globalMissingClasses;
    }

    protected abstract void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses);

    protected abstract void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues);

    protected abstract void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses);

    void assertDisabledProperty(String propertyValue, Class<?>... beanClasses) {
        this.applicationContextRunner.withPropertyValues(propertyValue)
                .run(context -> {
                    for (Class<?> beanClass : beanClasses) {
                        assertThat(context).doesNotHaveBean(beanClass);
                    }
                });

        this.webApplicationContextRunner.withPropertyValues(propertyValue)
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

        this.webApplicationContextRunner.withClassLoader(new FilteredClassLoader(filteredClass))
                .run(context -> {
                    for (Class<?> beanClass : beanClasses) {
                        assertThat(context).doesNotHaveBean(beanClass);
                    }
                });
    }
}
