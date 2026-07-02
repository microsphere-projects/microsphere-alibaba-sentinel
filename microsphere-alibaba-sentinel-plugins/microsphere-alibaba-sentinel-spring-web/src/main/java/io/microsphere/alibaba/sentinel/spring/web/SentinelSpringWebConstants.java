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

package io.microsphere.alibaba.sentinel.spring.web;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.constants.PropertyConstants;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.ENABLED_PROPERTY_VALUE;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.PROPERTY_NAME_PREFIX;
import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.constants.SymbolConstants.DOT;

/**
 * The interface to declare the constants of Sentinel x Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface SentinelSpringWebConstants {

    /**
     * The plugin name of Sentinel x Spring Web
     */
    String PLUGIN_NAME = "spring-web";

    /**
     * The default context name of Sentinel x Spring Web
     */
    String DEFAULT_CONTEXT_NAME = "microsphere_sentinel_spring_web_context";

    /**
     * The default origin of Sentinel x Spring Web
     */
    String DEFAULT_ORIGIN = "HandlerMethod";

    /**
     * The property name of the plugin of Sentinel x Spring Web enabled
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = ENABLED_PROPERTY_VALUE,
            source = {
                    SYSTEM_PROPERTIES_SOURCE,
                    APPLICATION_SOURCE
            }
    )
    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PLUGIN_NAME + DOT + PropertyConstants.ENABLED_PROPERTY_NAME;
}