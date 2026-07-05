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

package io.microsphere.alibaba.sentinel.hibernate.entity;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import io.microsphere.hibernate.test.AbstractHibernateH2Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Set;

import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static com.alibaba.csp.sentinel.context.ContextUtil.exit;
import static io.microsphere.alibaba.sentinel.hibernate.SentinelHibernateConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.hibernate.SentinelHibernateConstants.DEFAULT_ORIGIN;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link SentinelHibernateEntityCallback} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelHibernateEntityCallback
 * @since 1.0.0
 */
class SentinelHibernateEntityCallbackTest extends AbstractHibernateH2Test {

    @AfterEach
    void postTest() {
        Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
        DefaultNode entranceNode = context.getEntranceNode();
        Set<Node> childList = entranceNode.getChildList();
        assertFalse(childList.isEmpty());
        exit();
    }
}