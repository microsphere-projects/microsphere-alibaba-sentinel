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

package io.microsphere.alibaba.sentinel.event;


import io.microsphere.alibaba.sentinel.test.AbstractSentinelTemplateTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SentinelNodeEventPublisher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelNodeEventPublisher
 * @since 1.0.0
 */
class SentinelNodeEventPublisherTest extends AbstractSentinelTemplateTest {

    private SentinelNodeEventPublisher sentinelNodeEventPublisher;

    @BeforeEach
    void setUp() {
        this.sentinelNodeEventPublisher = new SentinelNodeEventPublisher();
    }

    @Test
    void test() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(this.times);

        ClusterNodeAddedEventListener listener = event -> {
            assertEquals(DEFAULT_CONTEXT_NAME, event.getContextName());
            assertEquals(event.getResourceName(), event.getClusterNode().getName());
            countDownLatch.countDown();
        };

        this.sentinelNodeEventPublisher.addEventListener(listener);

        super.executeInParallel(2, 10);

        countDownLatch.await();

        this.sentinelNodeEventPublisher.removeEventListener(listener);
    }
}