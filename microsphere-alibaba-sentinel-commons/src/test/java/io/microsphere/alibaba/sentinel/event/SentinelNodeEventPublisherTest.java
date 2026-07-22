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


import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_THREAD;
import static com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.collection.ListUtils.newArrayList;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SentinelNodeEventPublisher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelNodeEventPublisher
 * @since 1.0.0
 */
class SentinelNodeEventPublisherTest {

    private SentinelNodeEventPublisher sentinelNodeEventPublisher;

    private SentinelTemplate sentinelTemplate;

    private String resourceNamePrefix = "test-resource-";

    private int times = 100;

    @BeforeEach
    void setUp() {
        this.sentinelNodeEventPublisher = new SentinelNodeEventPublisher(50);
        this.sentinelTemplate = new SentinelTemplate();
    }

    @Test
    void test() throws InterruptedException {

        initFlowRule(this.times);

        CountDownLatch countDownLatch = new CountDownLatch(this.times);

        ClusterNodeAddedEventListener listener = new ClusterNodeAddedEventListener() {
            @Override
            public void onEvent(ClusterNodeAddedEvent event) {
                assertEquals(DEFAULT_CONTEXT_NAME, event.getContextName());
                assertEquals(event.getResourceName(), event.getClusterNode().getName());
                countDownLatch.countDown();
            }
        };

        this.sentinelNodeEventPublisher.addEventListener(listener);

        ExecutorService executorService = newFixedThreadPool(2);
        for (int i = 0; i < 100; i++) {
            executorService.submit(this::execute);
        }

        countDownLatch.await();
        executorService.shutdown();

        this.sentinelNodeEventPublisher.removeEventListener(listener);
    }

    @Test
    void testAddNodeOnNulling() {
        this.sentinelNodeEventPublisher.addNode(null, null, null);
        this.sentinelNodeEventPublisher.addNode("contextName", null, null);
        this.sentinelNodeEventPublisher.addNode("contextName", "resourceName", null);
    }

    private void execute() {
        for (int i = 0; i < this.times; i++) {
            String resourceName = resourceName(i);
            sentinelTemplate.execute(resourceName, () -> {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private String resourceName(int index) {
        return resourceNamePrefix + (index + 1);
    }

    private void initFlowRule(int times) {
        List<FlowRule> rules = newArrayList(times);
        for (int i = 0; i < times; i++) {
            FlowRule rule = new FlowRule();
            rule.setResource(this.resourceName(i));
            rule.setCount(1);
            rule.setGrade(FLOW_GRADE_THREAD);
            rule.setLimitApp("default");
            rules.add(rule);
        }
        loadRules(rules);
    }
}