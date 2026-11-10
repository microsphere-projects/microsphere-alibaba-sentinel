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
package io.microsphere.alibaba.sentinel.common;

import com.alibaba.csp.sentinel.Entry;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The context of Sentinel
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class SentinelContext {

    private static final ThreadLocal<SentinelContext> contextHolder = new ThreadLocal<>();

    @Nonnull
    private final String resourceName;

    @Nonnull
    private final String contextName;

    @Nonnull
    private final String origin;

    @Nonnull
    private final Entry entry;

    /**
     * The execution result, if <code>null</code>, it means the execution does not return any value.
     */
    @Nullable
    private Object result;

    /**
     * The optional {@link Throwable} instance, if <code>null</code>, it means the execution is successful.
     */
    @Nullable
    private Throwable failure;

    /**
     * The attributes
     */
    @Nullable
    private Map<String, Object> attributes;

    protected SentinelContext(@Nonnull String resourceName, @Nonnull String contextName, @Nonnull String origin,
                              @Nonnull Entry entry) {
        assertNotEmpty(resourceName, "The resource name must not be empty.");
        assertNotEmpty(contextName, "The context name must not be empty.");
        assertNotNull(origin, "The origin must not be null.");
        assertNotNull(entry, "The entry must not be null.");
        this.resourceName = resourceName;
        this.contextName = contextName;
        this.origin = origin;
        this.entry = entry;
    }

    /**
     * Get the resource name
     *
     * @return the resource name
     */
    @Nonnull
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Get the context name
     *
     * @return the context name
     */
    @Nonnull
    public String getContextName() {
        return contextName;
    }

    /**
     * Get the origin
     *
     * @return the origin
     */
    @Nonnull
    public String getOrigin() {
        return origin;
    }

    /**
     * Get the entry
     *
     * @return the entry
     */
    @Nonnull
    public Entry getEntry() {
        return entry;
    }

    /**
     * Set the result of the execution.
     *
     * @param result the result of the execution.
     * @return {@link SentinelContext}
     */
    public SentinelContext setResult(Object result) {
        this.result = result;
        return this;
    }

    /**
     * Set the result of the execution.
     *
     * @return <code>null</code> if the target callback does not return value or is failed
     */
    @Nullable
    public Object getResult() {
        return result;
    }

    /**
     * Set the failure of the execution.
     *
     * @param failure
     * @return {@link SentinelContext}
     */
    public SentinelContext setFailure(Throwable failure) {
        this.failure = failure;
        return this;
    }

    /**
     * Set the failure of the execution.
     *
     * @return <code>null</code> if the target callback executes successfully
     */
    @Nullable
    public Throwable getFailure() {
        return failure;
    }

    /**
     * Set the attribute name and value.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return {@link SentinelContext}
     */
    public SentinelContext setAttribute(String name, Object value) {
        Map<String, Object> attributes = getOrCreateAttributes();
        attributes.put(name, value);
        return this;
    }

    /**
     * Check whether the attribute exists by name.
     *
     * @param name the attribute name
     * @return <code>true</code> if exists, otherwise <code>false</code>
     */
    public boolean hasAttribute(String name) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return attributes.containsKey(name);
    }

    /**
     * Get the attribute value by name.
     *
     * @param name the attribute name
     * @return the attribute value if found, otherwise <code>null</code>
     */
    public <T> T getAttribute(String name) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return (T) attributes.get(name);
    }

    /**
     * Get the attribute value by name.
     *
     * @param name         the attribute name
     * @param defaultValue the default value of attribute
     * @return the attribute value if found, otherwise <code>defaultValue</code>
     */
    public <T> T getAttribute(String name, T defaultValue) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return (T) attributes.getOrDefault(name, defaultValue);
    }

    /**
     * Get the attribute value by name and type.
     *
     * @param name          the attribute name
     * @param attributeType the expected type of the attribute value
     * @param <T>           the type of the attribute value
     * @return the attribute value if found and assignable to the given type,
     * otherwise <code>null</code>
     */
    public <T> T getAttribute(String name, Class<T> attributeType) {
        Map<String, Object> attributes = getOrCreateAttributes();
        Object value = attributes.get(name);
        return attributeType.isInstance(value) ? attributeType.cast(value) : null;
    }

    /**
     * Remove the attribute by name.
     *
     * @param name the attribute name
     * @return the attribute value if removed, otherwise <code>null</code>
     */
    public <T> T removeAttribute(String name) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return (T) attributes.remove(name);
    }

    /**
     * Remove all attributes.
     *
     * @return {@link SentinelContext}
     */
    public SentinelContext removeAttributes() {
        Map<String, Object> attributes = this.attributes;
        if (attributes != null) {
            attributes.clear();
        }
        return this;
    }

    /**
     * Get the attributes.
     *
     * @return the read-only attributes
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = this.attributes;
        if (attributes == null) {
            return emptyMap();
        }
        return unmodifiableMap(attributes);
    }

    protected Map<String, Object> getOrCreateAttributes() {
        Map<String, Object> attributes = this.attributes;
        if (attributes == null) {
            attributes = new HashMap<>();
            this.attributes = attributes;
        }
        return attributes;
    }

    /**
     * Set the current {@link SentinelContext} into {@link ThreadLocal}.
     *
     * @return {@link SentinelContext} itself
     */
    public SentinelContext withinContext() {
        setContext(this);
        return this;
    }

    @Override
    public String toString() {
        return "SentinelContext{" +
                "resourceName='" + resourceName + '\'' +
                ", contextName='" + contextName + '\'' +
                ", origin='" + origin + '\'' +
                ", entry=" + entry +
                ", result=" + result +
                ", failure=" + failure +
                ", attributes=" + attributes +
                '}';
    }

    /**
     * Get the current {@link SentinelContext}
     *
     * @return <code>null</code> if there is no {@link SentinelContext} associated with the current thread
     */
    @Nullable
    public static SentinelContext getContext() {
        return contextHolder.get();
    }

    /**
     * Set the current {@link SentinelContext}
     *
     * @param context the {@link SentinelContext} , must not be <code>null</code>
     */
    public static void setContext(@Nullable SentinelContext context) {
        contextHolder.set(context);
    }

    /**
     * Remove and return the current {@link SentinelContext}
     *
     * @return <code>null</code> if there is no {@link SentinelContext} associated with the current thread
     */
    @Nullable
    public static SentinelContext removeContext() {
        SentinelContext context = getContext();
        contextHolder.remove();
        return context;
    }

    /**
     * Do something in the current {@link SentinelContext}
     *
     * @param contextConsumer the {@link Consumer} of {@link SentinelContext}, must not be <code>null</code>
     */
    public static void doInContext(@Nonnull Consumer<SentinelContext> contextConsumer) {
        doInContext(contextConsumer, false);
    }

    /**
     * Do something in the current {@link SentinelContext}
     *
     * @param contextConsumer the {@link Consumer} of {@link SentinelContext}, must not be <code>null</code>
     * @param forRemoval      if <code>true</code>, remove the current {@link SentinelContext} after the contextConsumer
     *                        execution.
     */
    public static void doInContext(@Nonnull Consumer<SentinelContext> contextConsumer, boolean forRemoval) {
        SentinelContext context = forRemoval ? removeContext() : getContext();
        if (context != null) {
            contextConsumer.accept(context);
        }
    }
}