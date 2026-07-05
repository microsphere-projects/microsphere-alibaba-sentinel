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

import com.alibaba.csp.sentinel.EntryType;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_ORIGIN;

/**
 * Simple {@link SentinelPlugin}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSentinelPlugin
 * @see SentinelPlugin
 * @since 1.0.0
 */
public class SimpleSentinelPlugin extends AbstractSentinelPlugin {

    public SimpleSentinelPlugin(String name) {
        super(name, DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    }

    public SimpleSentinelPlugin(String name, String contextName, String origin) {
        super(name, contextName, origin);
    }

    public SimpleSentinelPlugin(String name, String contextName, String origin, int resourceType) {
        super(name, contextName, origin, resourceType);
    }

    public SimpleSentinelPlugin(String name, String contextName, String origin, int resourceType, EntryType trafficType) {
        super(name, contextName, origin, resourceType, trafficType);
    }

    public SimpleSentinelPlugin(String name, String contextName, String origin, int resourceType, EntryType trafficType,
                                boolean autoInstalled) {
        super(name, contextName, origin, resourceType, trafficType, autoInstalled);
    }
}