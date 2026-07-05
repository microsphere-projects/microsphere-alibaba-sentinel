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

package io.microsphere.alibaba.sentinel.spring.cloud;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.actuator.FeaturesEndpoint;
import org.springframework.cloud.client.actuator.HasFeatures;

import java.util.Map;

import static io.microsphere.alibaba.sentinel.spring.cloud.SentinelCloudAutoConfiguration.FeaturesConfig.BEAN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link SentinelCloudAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                SentinelCloudAutoConfigurationTest.class
        },
        webEnvironment = NONE,
        properties = {
                "spring.application.name=test-app",
                "management.endpoints.web.exposure.include=features",
        }
)
@EnableAutoConfiguration
class SentinelCloudAutoConfigurationTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    @Test
    void test() {
        assertEquals("alibaba-sentinel.features", BEAN_NAME);
        HasFeatures hasFeatures = this.hasFeaturesMap.get(BEAN_NAME);
        assertNotNull(hasFeatures);
        assertNotNull(this.featuresEndpoint.features());
    }
}