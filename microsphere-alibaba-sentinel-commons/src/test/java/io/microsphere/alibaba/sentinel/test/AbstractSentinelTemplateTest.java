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

package io.microsphere.alibaba.sentinel.test;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import io.microsphere.lang.function.ThrowableAction;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_THREAD;
import static com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules;
import static io.microsphere.collection.ListUtils.newArrayList;
import static java.lang.Math.max;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * The abstract test class for {@link SentinelTemplate}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelTemplate
 * @since 1.0.0
 */
public abstract class AbstractSentinelTemplateTest {

    protected SentinelTemplate sentinelTemplate;

    protected String resourceNamePrefix = "test-resource-";

    protected int times = 100;

    protected long defaultTimeElapsedInMillis = 1L;

    @BeforeEach
    void setUp() {
        this.sentinelTemplate = new SentinelTemplate();
        initFlowRule(this.times);
    }

    protected void executeInParallel(int threads, int tasks) {
        executeInParallel(threads, tasks, this.defaultTimeElapsedInMillis);
    }

    protected void executeInParallel(int threads, int tasks, long timeElapsedInMillis) {
        int times = max(threads, tasks);
        ExecutorService executorService = newFixedThreadPool(threads);
        for (int i = 0; i < times; i++) {
            executorService.execute(() -> execute(timeElapsedInMillis));
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            await(10L);
        }
    }

    protected void execute() {
        execute(this.defaultTimeElapsedInMillis);
    }

    protected void execute(long timeElapsedInMillis) {
        for (int i = 0; i < this.times; i++) {
            String resourceName = resourceName(i);
            sentinelTemplate.execute(resourceName, () -> {
                await(timeElapsedInMillis);
            });
        }
    }

    protected String resourceName(int index) {
        return resourceNamePrefix + (index + 1);
    }

    protected void initFlowRule(int times) {
        List<FlowRule> rules = newArrayList(times);
        for (int i = 0; i < times; i++) {
            FlowRule rule = createFlowRule(i);
            rules.add(rule);
        }
        loadRules(rules);
    }

    protected FlowRule createFlowRule(int index) {
        FlowRule rule = new FlowRule();
        rule.setResource(this.resourceName(index));
        rule.setCount(1);
        rule.setGrade(FLOW_GRADE_THREAD);
        rule.setLimitApp("default");
        return rule;
    }

    protected void await(long millis) {
        ThrowableAction.execute(() -> sleep(millis));
    }
}