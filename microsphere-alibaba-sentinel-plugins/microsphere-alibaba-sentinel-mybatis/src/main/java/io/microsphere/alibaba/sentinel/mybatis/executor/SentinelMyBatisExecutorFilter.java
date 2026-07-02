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

import io.microsphere.alibaba.sentinel.common.AbstractSentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;
import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorFilterChain;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_DB_SQL;
import static io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants.DEFAULT_ORIGIN;
import static io.microsphere.alibaba.sentinel.mybatis.SentinelMyBatisConstants.PLUGIN_NAME;
import static io.microsphere.util.ExceptionUtils.throwTarget;

/**
 * Sentinel x MyBatis {@link ExecutorFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ExecutorFilter
 * @since 1.0.0
 */
public class SentinelMyBatisExecutorFilter extends AbstractSentinelPlugin implements ExecutorFilter {

    private final SentinelOperations sentinelOperations;

    public SentinelMyBatisExecutorFilter() {
        this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SentinelMyBatisExecutorFilter(String contextName, String origin) {
        super(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL);
        this.sentinelOperations = new SentinelTemplate(getResourceType(), getTrafficType());
    }

    @Override
    public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException {
        return doInSentinel(ms, () -> ExecutorFilter.super.update(ms, parameter, chain));
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql, ExecutorFilterChain chain) throws SQLException {
        return doInSentinel(ms, () -> ExecutorFilter.super.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, chain));
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, ExecutorFilterChain chain) throws SQLException {
        return doInSentinel(ms, () -> ExecutorFilter.super.query(ms, parameter, rowBounds, resultHandler, chain));
    }

    protected <T> T doInSentinel(MappedStatement ms, Callable<T> callable) throws SQLException {
        if (isEnabled()) {
            String resourceName = getSentinelResourceName(ms);
            return sentinelOperations.call(resourceName, getContextName(), getOrigin(),
                    context -> callable.call(), SQLException.class);
        } else {
            try {
                return callable.call();
            } catch (Exception e) {
                throw throwTarget(e, SQLException.class);
            }
        }
    }

    protected String getSentinelResourceName(MappedStatement ms) {
        return ms.getId();
    }
}