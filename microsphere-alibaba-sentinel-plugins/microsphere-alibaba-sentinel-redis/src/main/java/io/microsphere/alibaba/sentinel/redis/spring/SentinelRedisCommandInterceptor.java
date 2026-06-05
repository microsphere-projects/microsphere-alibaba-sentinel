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

import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor;
import io.microsphere.redis.spring.interceptor.RedisMethodContext;
import io.microsphere.alibaba.sentinel.common.AbstractSentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelContext;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.Map;

import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_PRIORITY;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.buildResourceName;
import static io.microsphere.alibaba.sentinel.redis.SentinelRedisConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.redis.SentinelRedisConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.redis.SentinelRedisConstants.PLUGIN_NAME;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;

/**
 * {@link RedisConnectionInterceptor} for Sentinel
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SentinelRedisCommandInterceptor extends AbstractSentinelPlugin implements RedisConnectionInterceptor,
        InitializingBean, BeanClassLoaderAware {

    private static final Logger logger = getLogger(SentinelRedisCommandInterceptor.class);

    private final Map<Method, String> methodResourceNamesCache = newFixedHashMap(512);

    private ClassLoader classLoader;

    private final SentinelOperations sentinelOperations;

    public SentinelRedisCommandInterceptor() {
        this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SentinelRedisCommandInterceptor(String contextName, String origin) {
        super(PLUGIN_NAME, contextName, origin);
        this.sentinelOperations = new SentinelTemplate(getResourceType(), getTrafficType());
    }

    @Override
    public void beforeExecute(RedisMethodContext<RedisConnection> redisMethodContext) throws Throwable {
        if (isEnabled()) {
            Method method = redisMethodContext.getMethod();

            String resourceName = getResourceName(method);

            if (resourceName == null) {
                logger.trace("The RedisConnection method['{}'] should not be intercepted in the {}", method, redisMethodContext);
                return;
            }

            SentinelContext context = this.sentinelOperations.begin(resourceName, getContextName(), getOrigin());
            setContext(redisMethodContext, context);
        }
    }

    @Override
    public void afterExecute(RedisMethodContext<RedisConnection> redisMethodContext, Object result, Throwable failure) {
        if (isEnabled()) {
            SentinelContext sentinelContext = getSentinelContext(redisMethodContext);

            if (sentinelContext == null) {
                logger.trace("The SentinelContext can't be found in the {}", redisMethodContext);
                return;
            }

            sentinelContext.setResult(result);
            sentinelContext.setFailure(failure);
            this.sentinelOperations.end(sentinelContext);
        }
    }

    private String getResourceName(Method method) {
        return methodResourceNamesCache.get(method);
    }

    public static void setContext(RedisMethodContext context, SentinelContext sentinelContext) {
        context.setAttribute(SENTINEL_CONTEXT_ATTRIBUTE_NAME, sentinelContext);
    }

    public static SentinelContext getSentinelContext(RedisMethodContext context) {
        return (SentinelContext) context.getAttribute(SENTINEL_CONTEXT_ATTRIBUTE_NAME);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void afterPropertiesSet() {
        initMethodResourceNamesCache();
    }

    private void initMethodResourceNamesCache() {
        Class[] allInterfaceClasses = getAllInterfacesForClass(RedisClusterConnection.class, classLoader);
        for (Class interfaceClass : allInterfaceClasses) {
            Method[] methods = interfaceClass.getMethods();
            for (Method method : methods) {
                String resourceName = buildResourceName(method);
                methodResourceNamesCache.put(method, resourceName);
            }
        }
    }

    @Override
    public int getOrder() {
        return DEFAULT_PRIORITY;
    }
}