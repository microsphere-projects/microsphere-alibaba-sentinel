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

package io.microsphere.alibaba.sentinel.common.util;


import com.alibaba.csp.sentinel.metric.extension.callback.MetricExitCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import io.microsphere.alibaba.sentinel.common.reposistory.SentinelMetricsRepository;
import io.microsphere.alibaba.sentinel.event.SentinelNodeEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry.clearEntryCallback;
import static com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry.clearExitCallback;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.addEntryCallback;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.addExitCallback;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.getEntryCallback;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.getExitCallback;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.removeEntryCallback;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.removeExitCallback;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ProcessorSlotCallbackUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ProcessorSlotCallbackUtils
 * @since 1.0.0
 */
class ProcessorSlotCallbackUtilsTest {

    private SentinelMetricsRepository sentinelMetricsRepository;

    private SentinelNodeEventPublisher sentinelNodeEventPublisher;

    @BeforeEach
    void setUp() {
        clear();
        this.sentinelMetricsRepository = new SentinelMetricsRepository();
        this.sentinelNodeEventPublisher = new SentinelNodeEventPublisher();
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    void clear() {
        clearEntryCallback();
        clearExitCallback();
    }

    @Test
    void testEntryCallbackOps() {
        assertNull(getEntryCallback(SentinelMetricsRepository.class));
        assertNull(getEntryCallback(SentinelNodeEventPublisher.class));

        addEntryCallback(this.sentinelMetricsRepository);
        addEntryCallback(this.sentinelNodeEventPublisher);

        assertSame(this.sentinelMetricsRepository, getEntryCallback(SentinelMetricsRepository.class));
        assertSame(this.sentinelMetricsRepository, removeEntryCallback(SentinelMetricsRepository.class));
        assertNull(getEntryCallback(SentinelMetricsRepository.class));

        assertSame(this.sentinelNodeEventPublisher, getEntryCallback(SentinelNodeEventPublisher.class));
        assertSame(this.sentinelNodeEventPublisher, removeEntryCallback(SentinelNodeEventPublisher.class));
        assertNull(getEntryCallback(SentinelNodeEventPublisher.class));
    }

    @Test
    void testExitCallbackOps() {
        assertNull(getExitCallback(MetricExitCallback.class));

        MetricExitCallback metricExitCallback = new MetricExitCallback();
        addExitCallback(metricExitCallback);

        assertSame(metricExitCallback, getExitCallback(MetricExitCallback.class));
        assertSame(metricExitCallback, removeExitCallback(MetricExitCallback.class));

        ProcessorSlotExitCallback callback = (context, resourceWrapper, count, args) -> {
        };
        addExitCallback(callback);
        assertNull(getExitCallback(MetricExitCallback.class));
        assertSame(callback, getExitCallback(callback.getClass()));
        assertSame(callback, removeExitCallback(callback.getClass()));
    }
}