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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlot;
import com.alibaba.csp.sentinel.spi.Spi;
import io.microsphere.alibaba.sentinel.common.callback.DefaultNodeEntryCallback;
import io.microsphere.annotation.Nullable;
import io.microsphere.event.EventDispatcher;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.getEntryCallback;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.event.EventDispatcher.of;

/**
 * The Event Publisher of Alibaba Sentinel's {@link Node}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultNodeEntryCallback
 * @see DefaultNode
 * @see ClusterNodeAddedEvent
 * @see ClusterNodeAddedEventListener
 * @see NodeSelectorSlot
 * @see ClusterBuilderSlot
 * @see StatisticSlot
 * @since 1.0.0
 */
@Spi
public class SentinelNodeEventPublisher implements DefaultNodeEntryCallback {

    private final EventDispatcher eventDispatcher;

    /**
     * Processed the mapping between Sentinel resource name and {@link ClusterNode}
     */
    private final ConcurrentMap<String, ClusterNode> processedResourceClusterNodes = newConcurrentHashMap(256);

    public SentinelNodeEventPublisher() {
        this(null);
    }

    public SentinelNodeEventPublisher(@Nullable Executor eventDispatcherExecutor) {
        this.eventDispatcher = of(eventDispatcherExecutor);
    }

    public SentinelNodeEventPublisher addEventListener(ClusterNodeAddedEventListener eventListener) {
        this.eventDispatcher.addEventListener(eventListener);
        return this;
    }

    public SentinelNodeEventPublisher removeEventListener(ClusterNodeAddedEventListener eventListener) {
        this.eventDispatcher.removeEventListener(eventListener);
        return this;
    }

    @Override
    public void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args) {
        addNodeIfAbsent(context, resourceWrapper, node);
    }

    @Override
    public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args) {
        addNodeIfAbsent(context, resourceWrapper, node);
    }

    private void addNodeIfAbsent(Context context, ResourceWrapper resourceWrapper, DefaultNode node) {
        String contextName = context.getName();
        String resourceName = resourceWrapper.getName();
        addNodeIfAbsent(contextName, resourceName, node);
    }

    void addNodeIfAbsent(String contextName, String resourceName, DefaultNode node) {
        ClusterNode clusterNode = node.getClusterNode();
        this.processedResourceClusterNodes.computeIfAbsent(resourceName, key -> {
            onClusterNodeAdded(contextName, resourceName, clusterNode);
            return clusterNode;
        });
    }

    protected void onClusterNodeAdded(String contextName, String resourceName, ClusterNode clusterNode) {
        this.eventDispatcher.dispatch(new ClusterNodeAddedEvent(clusterNode, contextName, resourceName));
    }

    /**
     * Get the singleton instance of {@link SentinelNodeEventPublisher}
     *
     * @return the singleton instance of {@link SentinelNodeEventPublisher}
     */
    @Nullable
    public static SentinelNodeEventPublisher getSentinelNodeEventPublisher() {
        return getEntryCallback(SentinelNodeEventPublisher.class);
    }
}