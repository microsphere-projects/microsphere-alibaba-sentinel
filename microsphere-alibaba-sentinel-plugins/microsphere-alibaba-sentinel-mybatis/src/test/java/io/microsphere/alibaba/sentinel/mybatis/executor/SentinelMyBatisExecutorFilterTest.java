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

package io.microsphere.alibaba.sentinel.mybatis.executor;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.mybatis.test.AbstractMapperTest;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static com.alibaba.csp.sentinel.context.ContextUtil.exit;
import static io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants.DEFAULT_ORIGIN;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link SentinelMyBatisExecutorFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelMyBatisExecutorFilter
 * @since 1.0.0
 */
class SentinelMyBatisExecutorFilterTest extends AbstractMapperTest {

    private SentinelMyBatisExecutorFilter filter;

    @Override
    protected void customize(Configuration configuration) {
        this.filter = new SentinelMyBatisExecutorFilter();
        InterceptingExecutorInterceptor interceptor = new InterceptingExecutorInterceptor(ofArray(this.filter));
        configuration.addInterceptor(interceptor);
    }

    @Test
    void testDisabled() throws Throwable {
        this.filter.setEnabled(false);
        super.testMapper();
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