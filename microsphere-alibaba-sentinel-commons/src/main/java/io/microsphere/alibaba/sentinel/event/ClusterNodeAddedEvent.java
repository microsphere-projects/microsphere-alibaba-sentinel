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

package io.microsphere.alibaba.sentinel.event;

import com.alibaba.csp.sentinel.node.ClusterNode;
import io.microsphere.event.Event;

/**
 * The Event of Cluster Node Added
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClusterNode
 * @see Event
 * @since 1.0.0
 */
public class ClusterNodeAddedEvent extends Event {

    private final String contextName;

    private final String resourceName;

    public ClusterNodeAddedEvent(ClusterNode node, String contextName, String resourceName) {
        super(node);
        this.contextName = contextName;
        this.resourceName = resourceName;
    }

    public ClusterNode getClusterNode() {
        return (ClusterNode) getSource();
    }

    public String getContextName() {
        return contextName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
