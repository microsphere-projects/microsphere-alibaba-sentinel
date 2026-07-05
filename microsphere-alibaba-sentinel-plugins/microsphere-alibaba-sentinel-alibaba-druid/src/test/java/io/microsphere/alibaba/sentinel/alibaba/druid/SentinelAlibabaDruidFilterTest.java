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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidConstants.PLUGIN_NAME;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.resetContextMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SentinelAlibabaDruidFilter} Testt
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelAlibabaDruidFilter
 * @see AbstractAlibabaDruidTest
 * @since 1.0.0
 */
class SentinelAlibabaDruidFilterTest extends AbstractAlibabaDruidTest {

    private final SentinelAlibabaDruidFilter filter = new SentinelAlibabaDruidFilter();

    @BeforeEach
    void setUp() throws Throwable {
        resetContextMap();
    }

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(filter);
    }

    @Test
    void testEnable() throws Throwable {
        setEnable(true);
        super.test();

        Context context = enter(filter.getContextName(), filter.getOrigin());
        assertEquals("microsphere_sentinel_alibaba_druid_context", context.getName());
    }

    @Test
    void testDisable() throws Throwable {
        setEnable(false);
        super.test();

        Context context = enter(filter.getContextName(), filter.getOrigin());
        assertEquals("microsphere_sentinel_alibaba_druid_context", context.getName());
    }

    void setEnable(boolean enabled) {
        DruidDataSource dataSource = getDruidDataSource();
        List<Filter> proxyFilters = dataSource.getProxyFilters();
        for (Filter proxyFilter : proxyFilters) {
            if (proxyFilter instanceof SentinelAlibabaDruidFilter filter) {
                assertEquals(PLUGIN_NAME, filter.getName());
                filter.setEnabled(enabled);
            }
        }
    }
}