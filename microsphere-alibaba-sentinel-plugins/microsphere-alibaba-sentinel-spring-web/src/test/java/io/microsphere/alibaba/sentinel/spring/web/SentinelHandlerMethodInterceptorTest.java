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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import io.microsphere.spring.test.web.context.request.MockServletWebRequest;
import io.microsphere.spring.test.webmvc.AbstractWebMvcTest;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Set;

import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static com.alibaba.csp.sentinel.context.ContextUtil.exit;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor.BEAN_NAME;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor.getSentinelContext;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.DEFAULT_ORIGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SentinelHandlerMethodInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelHandlerMethodInterceptor
 * @since 1.0.0
 */
@WebAppConfiguration
@SpringJUnitConfig(classes = {
        SentinelHandlerMethodInterceptor.class,
        SentinelHandlerMethodInterceptorTest.class
})
@EnableWebMvc
@EnableWebMvcExtension(registerHandlerInterceptors = true)
class SentinelHandlerMethodInterceptorTest extends AbstractWebMvcTest {

    @Autowired
    private SentinelHandlerMethodInterceptor interceptor;

    @Test
    void testConstants() {
        assertEquals("sentinelHandlerMethodInterceptor", BEAN_NAME);
    }

    @Test
    void test() throws Exception {
        this.testHelloWorld();
        this.testGreeting();
        postTest();
    }

    @Test
    void testDisabled() throws Exception {
        this.interceptor.setEnabled(false);
        assertFalse(this.interceptor.isEnabled());
        test();
        this.interceptor.setEnabled(true);
        assertTrue(this.interceptor.isEnabled());
    }

    @Test
    void testAfterExecuteWithoutSentinelContext() {
        MockServletWebRequest request = new MockServletWebRequest();
        this.interceptor.afterExecute(null, null, null, null, request);
        assertNull(getSentinelContext(request));
    }

    void postTest() {
        Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
        DefaultNode entranceNode = context.getEntranceNode();
        Set<Node> childList = entranceNode.getChildList();
        assertFalse(childList.isEmpty());
        exit();
    }
}