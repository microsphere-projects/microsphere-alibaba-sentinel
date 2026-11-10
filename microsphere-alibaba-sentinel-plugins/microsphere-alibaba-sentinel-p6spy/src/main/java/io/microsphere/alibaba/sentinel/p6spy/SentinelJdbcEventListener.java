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

import com.alibaba.csp.sentinel.EntryType;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import io.microsphere.alibaba.sentinel.common.SentinelContext;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import io.microsphere.alibaba.sentinel.common.SimpleSentinelPlugin;
import io.microsphere.logging.Logger;

import java.sql.SQLException;
import java.util.regex.Pattern;

import static com.alibaba.csp.sentinel.EntryType.IN;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_DB_SQL;
import static io.microsphere.alibaba.sentinel.common.SentinelContext.doInContext;
import static io.microsphere.alibaba.sentinel.common.SentinelPlugin.install;
import static io.microsphere.alibaba.sentinel.p6spy.SentinelP6SpyConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.p6spy.SentinelP6SpyConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.p6spy.SentinelP6SpyConstants.PLUGIN_NAME;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * P6Spy {@link JdbcEventListener} based on Alibaba Sentinel
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SentinelJdbcEventListener extends SimpleJdbcEventListener implements SentinelPlugin {

    /**
     * The resource name prefix for JDBC statements : "jdbc:"
     */
    public static final String RESOURCE_NAME_PREFIX = "jdbc:";

    /**
     * The maximum length of the normalized SQL resource name, which is used to bound
     * the resource cardinality so that the Sentinel statistics structures (e.g., ClusterNode)
     * will not grow unboundedly
     */
    public static final int MAX_RESOURCE_NAME_LENGTH = 256;

    /**
     * The pattern of the single-quoted string literals (e.g., 'value', 'O''Brien')
     */
    private static final Pattern SINGLE_QUOTED_LITERAL_PATTERN = Pattern.compile("'(?:''|[^'])*'");

    /**
     * The pattern of the double-quoted identifiers/literals (e.g., "name")
     */
    private static final Pattern DOUBLE_QUOTED_LITERAL_PATTERN = Pattern.compile("\"[^\"]*\"");

    /**
     * The pattern of the numeric literals (e.g., 123, 1.5)
     */
    private static final Pattern NUMERIC_LITERAL_PATTERN = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");

    /**
     * The pattern of the consecutive whitespaces
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private static final Logger logger = getLogger(SentinelJdbcEventListener.class);

    private final SentinelPlugin delegate;

    private final SentinelOperations sentinelOperations;

    public SentinelJdbcEventListener() {
        this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SentinelJdbcEventListener(String contextName, String origin) {
        this.delegate = new SimpleSentinelPlugin(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL, IN, false);
        this.sentinelOperations = new SentinelTemplate(getResourceType(), getTrafficType());
        install(this);
    }

    @Override
    public void onBeforeAnyExecute(StatementInformation statementInformation) {
        if (isEnabled()) {
            if (isEligibleStatement(statementInformation)) {
                execute(() -> {
                    String resourceName = getResourceName(statementInformation);
                    SentinelContext context = this.sentinelOperations.begin(resourceName, getContextName(), getOrigin());
                    context.withinContext();
                });
            }
        }
    }

    @Override
    public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
        // The cleanup path must be unconditional : if the plugin is disabled between
        // onBeforeAnyExecute and onAfterAnyExecute, the SentinelContext still needs to
        // be ended to exit the Entry and clean up the ThreadLocal
        if (isEligibleStatement(statementInformation)) {
            doInContext(context -> {
                context.setFailure(e);
                this.sentinelOperations.end(context);
            }, true);
        }
    }

    /**
     * Get the Sentinel resource name for the specified {@link StatementInformation}.
     * <p>
     * The raw SQL is normalized (literals are replaced with {@code ?} placeholders,
     * whitespaces are collapsed and the length is truncated) to bound the resource
     * cardinality, so that the Sentinel statistics structures (e.g., {@code ClusterNode})
     * will not grow unboundedly, and the sensitive data carried in the SQL literals
     * will not be exposed to the monitoring surfaces.
     *
     * @param statementInformation the {@link StatementInformation}
     * @return the normalized resource name, e.g., "jdbc:SELECT * FROM users WHERE id = ?"
     */
    protected String getResourceName(StatementInformation statementInformation) {
        String sql = statementInformation.getSql();
        String resourceName = sql == null || sql.isEmpty() ? RESOURCE_NAME_PREFIX + "unknown" : RESOURCE_NAME_PREFIX + normalizeSql(sql);
        logger.trace("Sentinel JDBC StatementInformation resource[name : '{}']", resourceName);
        return resourceName;
    }

    /**
     * Normalize the SQL to bound the resource cardinality :
     * <ul>
     *     <li>The single/double-quoted literals are replaced with {@code ?}</li>
     *     <li>The numeric literals are replaced with {@code ?}</li>
     *     <li>The consecutive whitespaces are collapsed into a single space</li>
     *     <li>The result is truncated if its length exceeds {@link #MAX_RESOURCE_NAME_LENGTH}</li>
     * </ul>
     *
     * @param sql the raw SQL
     * @return the normalized SQL
     */
    protected String normalizeSql(String sql) {
        String normalized = SINGLE_QUOTED_LITERAL_PATTERN.matcher(sql).replaceAll("?");
        normalized = DOUBLE_QUOTED_LITERAL_PATTERN.matcher(normalized).replaceAll("?");
        normalized = NUMERIC_LITERAL_PATTERN.matcher(normalized).replaceAll("?");
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ").trim();
        if (normalized.length() > MAX_RESOURCE_NAME_LENGTH) {
            normalized = normalized.substring(0, MAX_RESOURCE_NAME_LENGTH) + "...";
        }
        return normalized;
    }

    /**
     * Determine whether the specified {@link StatementInformation} is eligible
     *
     * @param statementInformation
     * @return
     */
    protected boolean isEligibleStatement(StatementInformation statementInformation) {
        return statementInformation instanceof PreparedStatementInformation;
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
        return COMMON_DB_SQL;
    }

    @Override
    public EntryType getTrafficType() {
        return IN;
    }
}