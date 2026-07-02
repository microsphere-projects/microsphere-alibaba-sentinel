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
import com.alibaba.csp.sentinel.EntryType;
import io.microsphere.logging.Logger;

import static com.alibaba.csp.sentinel.EntryType.IN;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON;
import static com.alibaba.csp.sentinel.SphU.entry;
import static com.alibaba.csp.sentinel.Tracer.traceEntry;
import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static com.alibaba.csp.sentinel.context.ContextUtil.exit;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_ORIGIN;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.StringUtils.isBlank;

/**
 * The default class of {@link SentinelOperations}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelOperations
 * @since 1.0.0
 */
public class SentinelTemplate implements SentinelOperations {

    private static final Logger logger = getLogger(SentinelTemplate.class);

    private final int resourceType;

    private final EntryType trafficType;

    public SentinelTemplate() {
        this(COMMON);
    }

    public SentinelTemplate(int resourceType) {
        this(resourceType, IN);
    }

    public SentinelTemplate(int resourceType, EntryType trafficType) {
        this.resourceType = resourceType;
        this.trafficType = trafficType;
    }

    @Override
    public SentinelContext begin(String resourceName, String contextName, String origin) throws Exception {
        String actualContextName = isBlank(contextName) ? DEFAULT_CONTEXT_NAME : contextName;
        String actualOrigin = isBlank(origin) ? DEFAULT_ORIGIN : origin;
        if (logger.isTraceEnabled()) {
            logger.trace("The operation of Sentinel[context name : '{}' -> '{}' , origin : '{}' -> '{}' , resource name : '{}', type : {} , entry type : {}] is beginning",
                    contextName, actualContextName, origin, actualOrigin, resourceName, this.resourceType, this.trafficType);
        }
        enter(actualContextName, actualOrigin);
        Entry entry = entry(resourceName, this.resourceType, this.trafficType);
        return new SentinelContext(resourceName, actualContextName, actualOrigin, entry);
    }

    @Override
    public void end(SentinelContext context) {
        Entry entry = context.getEntry();
        Throwable failure = context.getFailure();
        if (failure != null) {
            traceEntry(failure, entry);
        }
        entry.exit();
        exit();
        if (logger.isTraceEnabled()) {
            logger.trace("The operation of Sentinel[{}] is ended", context);
        }
    }
}