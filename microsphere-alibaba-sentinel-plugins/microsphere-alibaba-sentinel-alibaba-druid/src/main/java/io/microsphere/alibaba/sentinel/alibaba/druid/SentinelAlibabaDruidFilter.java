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
package io.microsphere.alibaba.sentinel.alibaba.druid;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import io.microsphere.alibaba.druid.filter.AbstractStatementFilter;
import io.microsphere.alibaba.sentinel.common.SentinelContext;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import io.microsphere.alibaba.sentinel.common.SimpleSentinelPlugin;

import static com.alibaba.csp.sentinel.EntryType.IN;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_DB_SQL;
import static io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidConstants.PLUGIN_NAME;
import static io.microsphere.alibaba.sentinel.common.SentinelContext.doInContext;
import static io.microsphere.alibaba.sentinel.common.SentinelPlugin.install;

/**
 * Sentinel x Alibaba Druid {@link Filter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Filter
 * @see FilterAdapter
 * @since 1.0.0
 */
public class SentinelAlibabaDruidFilter extends AbstractStatementFilter implements SentinelPlugin {

    private final SentinelPlugin delegate;

    private final SentinelOperations sentinelOperations;

    public SentinelAlibabaDruidFilter() {
        this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SentinelAlibabaDruidFilter(String contextName, String origin) {
        this.delegate = new SimpleSentinelPlugin(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL, IN, false);
        this.sentinelOperations = new SentinelTemplate(getResourceType(), getTrafficType());
        install(this);
    }

    @Override
    protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
        if (isEnabled()) {
            SentinelContext context = this.sentinelOperations.begin(resourceName, getContextName(), getOrigin());
            context.withinContext();
        }
    }

    @Override
    protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
        if (isEnabled()) {
            doInContext(context -> {
                context.setResult(result);
                context.setFailure(failure);
                this.sentinelOperations.end(context);
            }, true);
        }
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

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public String getContextName() {
        return this.delegate.getContextName();
    }

    @Override
    public String getOrigin() {
        return this.delegate.getOrigin();
    }

    @Override
    public int getResourceType() {
        return this.delegate.getResourceType();
    }

    @Override
    public EntryType getTrafficType() {
        return this.delegate.getTrafficType();
    }
}