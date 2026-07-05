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

package io.microsphere.alibaba.sentinel.p6spy;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static com.alibaba.csp.sentinel.context.ContextUtil.exit;
import static io.microsphere.alibaba.sentinel.p6spy.SentinelP6SpyConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.p6spy.SentinelP6SpyConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.p6spy.SentinelP6SpyConstants.PLUGIN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SentinelJdbcEventListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelJdbcEventListener
 * @see AbstractAlibabaDruidTest
 * @since 1.0.0
 */
class SentinelJdbcEventListenerTest extends AbstractAlibabaDruidTest {

    @Override
    protected DruidDataSource buildDruidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        dataSource.setUrl("jdbc:p6spy:h2:mem:test_mem");
        dataSource.setUsername("sa");
        return dataSource;
    }

    @Test
    void testDefaults() {
        SentinelJdbcEventListener listener = new SentinelJdbcEventListener();
        assertTrue(listener.isEnabled());
        assertEquals(PLUGIN_NAME, listener.getName());
        assertEquals(DEFAULT_CONTEXT_NAME, listener.getContextName());
        assertEquals(DEFAULT_ORIGIN, listener.getOrigin());
    }

    @Test
    void testDisabled() {
        SentinelJdbcEventListener listener = new SentinelJdbcEventListener();
        listener.setEnabled(false);
        listener.onBeforeAnyExecute(null);
        listener.onAfterAnyExecute(null, 0L, null);
        assertFalse(listener.isEnabled());
    }

    @AfterEach
    void postTest() {
        Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
        DefaultNode entranceNode = context.getEntranceNode();
        Set<Node> childList = entranceNode.getChildList();
        assertFalse(childList.isEmpty());
        exit();
    }
}