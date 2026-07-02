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

package io.microsphere.alibaba.sentinel.common.constants;

import io.microsphere.alibaba.sentinel.common.SentinelContext;
import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.constants.PropertyConstants;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;

/**
 * The constants of Sentinel
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface SentinelConstants {

    /***
     * The Flow Data ID pattern
     */
    String FLOW_DATA_ID_PATTERN = "{}-flow-rules";

    /**
     * The default Context name pattern
     */
    String DEFAULT_CONTEXT_NAME_PATTERN = "microsphere_sentinel_{}_context";

    /**
     * The default Context name : "microsphere_sentinel_default_context"
     */
    String DEFAULT_CONTEXT_NAME = "microsphere_sentinel_default_context";

    /**
     * The default origin : ""
     */
    String DEFAULT_ORIGIN = "";

    /**
     * The Property Name Prefix of Sentinel : "microsphere.sentinel."
     */
    String PROPERTY_NAME_PREFIX = "microsphere.sentinel.";

    /**
     * The default value of enabled : "true"
     */
    String ENABLED_PROPERTY_VALUE = "true";

    /**
     * The Property Name of enabled : "microsphere.sentinel.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = ENABLED_PROPERTY_VALUE,
            source = {
                    SYSTEM_PROPERTIES_SOURCE,
                    APPLICATION_SOURCE
            }
    )
    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The attribute name of {@link SentinelContext}
     *
     * @see SentinelContext
     */
    String SENTINEL_CONTEXT_ATTRIBUTE_NAME = "sentinel-context";

    /**
     * The default priority for Sentinel Plugins
     */
    int DEFAULT_PRIORITY = 9;
}