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

import com.alibaba.csp.sentinel.EntryType;
import io.microsphere.alibaba.sentinel.common.SentinelContext;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import io.microsphere.alibaba.sentinel.common.SimpleSentinelPlugin;
import io.microsphere.annotation.Nonnull;
import io.microsphere.hibernate.entity.EntityCallback;
import org.hibernate.type.Type;

import java.util.Optional;

import static com.alibaba.csp.sentinel.EntryType.IN;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_DB_SQL;
import static io.microsphere.alibaba.sentinel.common.SentinelContext.doInContext;
import static io.microsphere.alibaba.sentinel.common.SentinelPlugin.install;
import static io.microsphere.alibaba.sentinel.hibernate.SentinelHibernateConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.hibernate.SentinelHibernateConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.hibernate.SentinelHibernateConstants.PLUGIN_NAME;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Sentinel x Hibernate {@link EntityCallback}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EntityCallback
 * @since 1.0.0
 */
public class SentinelHibernateEntityCallback implements EntityCallback, SentinelPlugin {

    private final SentinelPlugin delegate;

    private final SentinelOperations sentinelOperations;

    public SentinelHibernateEntityCallback() {
        this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SentinelHibernateEntityCallback(@Nonnull String contextName, @Nonnull String origin) {
        this.delegate = new SimpleSentinelPlugin(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL, IN, false);
        this.sentinelOperations = new SentinelTemplate(getResourceType(), getTrafficType());
        install(this);
    }

    @Override
    public void onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        begin(entity, "INSERT").ifPresent(SentinelContext::withinContext);
    }

    @Override
    public void onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        end();
    }

    @Override
    public void onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        begin(entity, "UPDATE").ifPresent(SentinelContext::withinContext);
    }

    @Override
    public void onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        end();
    }

    @Override
    public void onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        begin(entity, "LOAD").ifPresent(SentinelContext::withinContext);
    }

    @Override
    public void onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types) {
        end();
    }

    @Override
    public void onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        begin(entity, "DELETE").ifPresent(SentinelContext::withinContext);
    }

    @Override
    public void onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        end();
    }

    @Override
    public boolean isAutoInstalled() {
        return this.delegate.isAutoInstalled();
    }

    @Override
    public boolean isEnabled() {
        return this.delegate.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.delegate.setEnabled(enabled);
    }

    @Nonnull
    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Nonnull
    @Override
    public String getContextName() {
        return this.delegate.getContextName();
    }

    @Nonnull
    @Override
    public String getOrigin() {
        return this.delegate.getOrigin();
    }

    @Override
    public int getResourceType() {
        return this.delegate.getResourceType();
    }

    @Nonnull
    @Override
    public EntryType getTrafficType() {
        return this.delegate.getTrafficType();
    }

    protected Optional<SentinelContext> begin(Object entity, String action) {
        if (isEnabled()) {
            String resourceName = getSentinelResourceName(entity, action);
            String contextName = this.delegate.getContextName();
            String origin = this.delegate.getOrigin();
            return of(execute(() -> this.sentinelOperations.begin(resourceName, contextName, origin)));
        }
        return empty();
    }

    protected void end() {
        if (isEnabled()) {
            doInContext(this.sentinelOperations::end, true);
        }
    }

    protected String getSentinelResourceName(Object entity, String action) {
        String className = entity.getClass().getName();
        return "Entity:" + action + ":" + className;
    }
}