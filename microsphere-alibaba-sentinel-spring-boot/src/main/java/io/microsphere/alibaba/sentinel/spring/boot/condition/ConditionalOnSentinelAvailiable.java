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
package io.microsphere.alibaba.sentinel.spring.boot.condition;

import io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a component is eligible for registration when Alibaba Sentinel is available.
 * <p>
 * This annotation combines {@link ConditionalOnSentinelEnabled @ConditionalOnSentinelEnabled} and
 * {@link ConditionalOnClass @ConditionalOnClass(name = "com.alibaba.csp.sentinel.SphU")},
 * meaning the annotated component will be registered only if Sentinel is explicitly enabled
 * and the Sentinel core classes are present on the classpath.
 * </p>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @Configuration
 * @ConditionalOnSentinelAvailiable
 * public class MySentinelConfiguration {
 *     // This configuration will only be active if Sentinel is available
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SentinelAutoConfiguration
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Documented
@ConditionalOnSentinelEnabled
@ConditionalOnClass(name = {
        "com.alibaba.csp.sentinel.SphU"
})
public @interface ConditionalOnSentinelAvailiable {
}