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

import io.microsphere.alibaba.sentinel.common.AbstractSentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelContext;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.spring.web.annotation.EnableWebExtension;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_WEB;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_PRIORITY;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants.PLUGIN_NAME;
import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.web.util.WebScope.REQUEST;

/**
 * The {@link HandlerMethodInterceptor} class for Sentinel x Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelPlugin
 * @see HandlerMethodInterceptor
 * @see EnableWebExtension
 * @since 1.0.0
 */
public class SentinelHandlerMethodInterceptor extends AbstractSentinelPlugin implements HandlerMethodInterceptor,
        ApplicationListener<WebEndpointMappingsReadyEvent>, Ordered {

    public static final String BEAN_NAME = "sentinelHandlerMethodInterceptor";

    private static final Logger logger = getLogger(SentinelHandlerMethodInterceptor.class);

    private final SentinelOperations sentinelOperations;

    private int order;

    private Map<Method, String> methodResourceNamesCache;

    public SentinelHandlerMethodInterceptor() {
        this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SentinelHandlerMethodInterceptor(String contextName, String origin) {
        super(PLUGIN_NAME, contextName, origin, COMMON_WEB);
        this.sentinelOperations = new SentinelTemplate(getResourceType(), getTrafficType());
        this.setOrder(DEFAULT_PRIORITY);
    }

    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
        if (isEnabled()) {
            String resourceName = getResourceName(handlerMethod);
            SentinelContext context = this.sentinelOperations.begin(resourceName, getContextName(), getOrigin());
            setSentinelContext(context, request);
        }
    }

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) {
        if (isEnabled()) {
            SentinelContext context = getSentinelContext(request);
            if (context == null) {
                logger.trace("The SentinelContext is not found in the request : {}", request);
                return;
            }
            context.setResult(returnValue)
                    .setFailure(error);
            this.sentinelOperations.end(context);
        }
    }

    @Override
    public void onApplicationEvent(WebEndpointMappingsReadyEvent event) {
        initEntryCache(event);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Set the {@link SentinelContext} to {@link NativeWebRequest}
     *
     * @param context {@link SentinelContext}
     * @param request {@link NativeWebRequest}
     */
    public static void setSentinelContext(@Nonnull SentinelContext context, @Nonnull NativeWebRequest request) {
        REQUEST.setAttribute(request, SENTINEL_CONTEXT_ATTRIBUTE_NAME, context);
    }

    /**
     * Get the {@link SentinelContext} from {@link NativeWebRequest}
     *
     * @param request {@link NativeWebRequest}
     * @return the {@link SentinelContext} if found
     */
    @Nonnull
    public static SentinelContext getSentinelContext(NativeWebRequest request) {
        return REQUEST.getAttribute(request, SENTINEL_CONTEXT_ATTRIBUTE_NAME);
    }

    protected void initEntryCache(WebEndpointMappingsReadyEvent event) {
        Collection<WebEndpointMapping> webEndpointMappings = event.getMappings();
        int size = webEndpointMappings.size();

        Map<Method, String> methodEntryNamesCache = newFixedHashMap(size);
        this.methodResourceNamesCache = methodEntryNamesCache;

        Iterator<WebEndpointMapping> iterator = webEndpointMappings.iterator();
        while (iterator.hasNext()) {
            WebEndpointMapping webEndpointMapping = iterator.next();
            Object endpoint = webEndpointMapping.getEndpoint();
            if (endpoint instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) endpoint;
                String resourceName = buildResourceName(handlerMethod, webEndpointMapping);
                Method method = handlerMethod.getMethod();
                methodEntryNamesCache.put(method, resourceName);
                logger.trace("Create the resourceName : '{}' for HandlerMethod : {}", resourceName, handlerMethod);
            }
        }
    }

    protected String buildResourceName(HandlerMethod handlerMethod, WebEndpointMapping webEndpointMapping) {
        return webEndpointMapping.toExpression() + "#" + handlerMethod;
    }

    /**
     * Get the resource name of {@link HandlerMethod}
     *
     * @param handlerMethod Spring Web {@link HandlerMethod Handler Method}
     * @return non-null
     */
    public String getResourceName(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        return this.methodResourceNamesCache.computeIfAbsent(method, m -> PLUGIN_NAME + ":" + handlerMethod);
    }
}