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

package io.microsphere.alibaba.sentinel.redis.spring;

import io.microsphere.alibaba.sentinel.redis.spring.test.RedisContextConfig;
import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.interceptor.RedisMethodContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_PRIORITY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SentinelRedisCommandInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelRedisCommandInterceptor
 * @since 1.0.0
 */
@SpringJUnitConfig(classes = {
        RedisContextConfig.class,
        SentinelRedisCommandInterceptorTest.class
})
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true",
        "spring.application.name=test-app"
})
@EnableRedisInterceptor(wrapRedisTemplates = "stringRedisTemplate")
class SentinelRedisCommandInterceptorTest {

    @Autowired
    private RedisContext redisContext;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SentinelRedisCommandInterceptor interceptor;

    @Test
    void test() {
        String key = "key";
        String value = "value";
        ValueOperations<String, String> valueOperations = this.stringRedisTemplate.opsForValue();
        valueOperations.set(key, value);
        assertEquals(value, valueOperations.get(key));
        assertEquals(DEFAULT_PRIORITY, interceptor.getOrder());
    }

    @Test
    void testDisabled() {
        this.interceptor.setEnabled(false);
        assertFalse(this.interceptor.isEnabled());
        test();
        this.interceptor.setEnabled(true);
        assertTrue(this.interceptor.isEnabled());
    }

    @Test
    void testResourceNotFound() {
        Method method = getClass().getMethods()[0];
        RedisMethodContext context = new RedisMethodContext(this.stringRedisTemplate, method, ofArray(), this.redisContext);
        assertDoesNotThrow(() -> this.interceptor.beforeExecute(context));
        assertDoesNotThrow(() -> this.interceptor.afterExecute(context, null, null));
    }
}