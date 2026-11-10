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

import io.microsphere.alibaba.sentinel.spring.boot.condition.ConditionalOnSentinelAvailable;
import io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;


import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.ENABLED_PROPERTY_NAME;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.ANY;

/**
 * The Spring Boot Auto-Configuration class of Alibaba Sentinel x Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration
 * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * @see org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
 * @see io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
 * @see org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
 * @see org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration
 * @see io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnWebApplication(type = ANY)
@ConditionalOnSentinelAvailable
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
@ConditionalOnClass(name = {
        "org.springframework.web.method.HandlerMethod",                                   // Spring Web
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor",              // Microsphere Spring Web
        "io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor"     // Microsphere Alibaba Sentinel x Spring Web
})
@AutoConfigureAfter(name = {
        "com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration",                    // Spring Cloud Alibaba Sentinel
        "io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration",        // Microsphere Spring Boot WebMVC
        "io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration"       // Microsphere Spring Boot WebFlux
})
public class SentinelSpringWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentinelHandlerMethodInterceptor sentinelHandlerMethodInterceptor() {
        return new SentinelHandlerMethodInterceptor();
    }
}