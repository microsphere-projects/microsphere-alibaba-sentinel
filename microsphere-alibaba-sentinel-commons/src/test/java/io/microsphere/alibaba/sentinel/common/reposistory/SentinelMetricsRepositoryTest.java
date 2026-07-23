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

package io.microsphere.alibaba.sentinel.common.reposistory;


import com.alibaba.csp.sentinel.node.metric.MetricNode;
import io.microsphere.alibaba.sentinel.test.AbstractSentinelTemplateTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SentinelMetricsRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelMetricsRepository
 * @since 1.0.0
 */
class SentinelMetricsRepositoryTest extends AbstractSentinelTemplateTest {

    private long startTime;

    private SentinelMetricsRepository sentinelMetricsRepository;

    @BeforeEach
    void setUp() {
        this.startTime = currentTimeMillis();
        this.sentinelMetricsRepository = new SentinelMetricsRepository();
    }

    @Test
    void test() {
        super.executeInParallel(2, 50);
        long endTime = currentTimeMillis();
        List<MetricNode> metricNodes = this.sentinelMetricsRepository.findMetricNodes(this.startTime, endTime);
        assertFalse(metricNodes.isEmpty());

        Map<String, List<MetricNode>> contextMetricNodesMap = this.sentinelMetricsRepository.findContextMetricNodesMap(this.startTime, endTime);
        assertFalse(contextMetricNodesMap.isEmpty());
        assertEquals(2, contextMetricNodesMap.size());
        metricNodes = contextMetricNodesMap.get(DEFAULT_CONTEXT_NAME);
        assertEquals(200, metricNodes.size());

        contextMetricNodesMap = this.sentinelMetricsRepository.findContextMetricNodesMap(endTime * 2, endTime * 2);
        assertTrue(contextMetricNodesMap.isEmpty());

        metricNodes = this.sentinelMetricsRepository.findMetricNodes(this.startTime, 10);
        assertFalse(metricNodes.isEmpty());
    }

    @Test
    void testOnFailed() {
        assertNull(this.sentinelMetricsRepository.findMetricNodes(metricSearcher -> {
            throw new RuntimeException("For testing");
        }, () -> null));
    }

}