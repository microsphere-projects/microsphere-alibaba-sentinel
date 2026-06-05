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

import org.junit.jupiter.api.Test;

import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getPluginEnabledPropertyName;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.PLUGIN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SentinelSpringWebConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelSpringWebConstants
 * @since 1.0.0
 */
class SentinelSpringWebConstantsTest {

    @Test
    void testConstants() {
        assertEquals("spring-web", PLUGIN_NAME);
        assertEquals("microsphere_sentinel_spring_web_context", DEFAULT_CONTEXT_NAME);
        assertEquals("HandlerMethod", DEFAULT_ORIGIN);
        assertEquals("microsphere.sentinel.spring-web.enabled", ENABLED_PROPERTY_NAME);
        assertEquals(getPluginEnabledPropertyName(PLUGIN_NAME), ENABLED_PROPERTY_NAME);
    }
}