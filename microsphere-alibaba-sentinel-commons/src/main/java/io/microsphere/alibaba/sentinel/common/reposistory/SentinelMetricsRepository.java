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

package io.microsphere.alibaba.sentinel.common.reposistory;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.spi.Spi;
import io.microsphere.alibaba.sentinel.common.callback.DefaultNodeEntryCallback;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.logging.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static com.alibaba.csp.sentinel.Constants.CONTEXT_DEFAULT_NAME;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.getEntryCallback;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getMetricSearcher;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.MapUtils.newTreeMap;
import static io.microsphere.lang.function.ThrowableFunction.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ObjectUtils.defaultIfNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The Repository of Alibaba Sentinel Metrics
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultNodeEntryCallback
 * @see DefaultNode
 * @since 1.0.0
 */
@Spi
public class SentinelMetricsRepository implements DefaultNodeEntryCallback {

    private static final Logger logger = getLogger(SentinelMetricsRepository.class);

    private final ConcurrentMap<String, String> resourceToContextMapping = newConcurrentHashMap(256);

    private final MetricSearcher metricSearcher;

    public SentinelMetricsRepository() {
        this.metricSearcher = getMetricSearcher();
    }

    @Override
    public void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args) throws Exception {
        updateResourceToContextMapping(context, node);
    }

    @Override
    public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args) {
        updateResourceToContextMapping(context, node);
    }

    /**
     * Find {@link MetricNode} Map by Context and Time Range
     *
     * @param beginTimeMs the beginning time in milliseconds
     * @param endTimeMs   the ending time in milliseconds
     * @return a map of context names to lists of {@link MetricNode} objects
     */
    @Nonnull
    public Map<String, List<MetricNode>> findContextMetricNodesMap(long beginTimeMs, long endTimeMs) {
        List<MetricNode> metricNodes = findMetricNodes(beginTimeMs, endTimeMs);

        if (isEmpty(metricNodes)) {
            return emptyMap();
        }

        Map<String, List<MetricNode>> contextMetricsNodesMap = newTreeMap();

        for (MetricNode metricNode : metricNodes) {
            String resource = metricNode.getResource();
            String context = getContext(resource);
            List<MetricNode> resourceMetricNodes = contextMetricsNodesMap.computeIfAbsent(context, r -> new LinkedList<>());
            resourceMetricNodes.add(metricNode);
        }

        return unmodifiableMap(contextMetricsNodesMap);
    }

    /**
     * Find {@link MetricNode} List by Begin Time and Recommend Lines
     *
     * @param beginTimeMs    the beginning time in milliseconds
     * @param recommendLines the recommended number of lines to retrieve
     * @return a list of {@link MetricNode} objects
     */
    @Nonnull
    public List<MetricNode> findMetricNodes(long beginTimeMs, int recommendLines) {
        return findMetricNodes(metricSearcher -> metricSearcher.find(beginTimeMs, recommendLines),
                Collections::emptyList);
    }

    /**
     * Find {@link MetricNode} List by Time Range
     *
     * @param beginTimeMs the beginning time in milliseconds
     * @param endTimeMs   the ending time in milliseconds
     * @return a list of {@link MetricNode} objects
     */
    @Nonnull
    public List<MetricNode> findMetricNodes(long beginTimeMs, long endTimeMs) {
        return findMetricNodes(beginTimeMs, endTimeMs, null);
    }

    /**
     * Find {@link MetricNode} List by Time Range and Resource
     *
     * @param beginTimeMs the beginning time in milliseconds
     * @param endTimeMs   the ending time in milliseconds
     * @param resource    the resource name
     * @return a list of {@link MetricNode} objects
     */
    @Nonnull
    public List<MetricNode> findMetricNodes(long beginTimeMs, long endTimeMs, String resource) {
        return findMetricNodes(metricSearcher ->
                metricSearcher.findByTimeAndResource(beginTimeMs, endTimeMs, resource), Collections::emptyList);
    }

    /**
     * Get the resource name from the given {@link DefaultNode}
     *
     * @param node the {@link DefaultNode} object
     * @return the resource name
     */
    public String getResource(DefaultNode node) {
        return node.getId().getName();
    }

    /**
     * Get the context name associated with the given resource name
     *
     * @param resource the resource name
     * @return the context name
     */
    public String getContext(String resource) {
        return resourceToContextMapping.getOrDefault(resource, CONTEXT_DEFAULT_NAME);
    }

    protected List<MetricNode> findMetricNodes(ThrowableFunction<MetricSearcher, List<MetricNode>> callback,
                                               Supplier<List<MetricNode>> defaultValueSupplier) {
        List<MetricNode> metricNodes = execute(this.metricSearcher, callback, (s, e) -> {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to execute the callback in MetricSearcher", e);
            }
            return null;
        });
        return defaultIfNull(metricNodes, defaultValueSupplier);
    }

    private void updateResourceToContextMapping(Context context, DefaultNode node) {
        updateResourceToContextMapping(context.getName(), node);
    }

    private void updateResourceToContextMapping(String context, DefaultNode node) {
        String resource = getResource(node);
        resourceToContextMapping.putIfAbsent(resource, context);
    }

    /**
     * Get the singleton instance of {@link SentinelMetricsRepository}
     *
     * @return the singleton instance of {@link SentinelMetricsRepository}
     */
    @Nullable
    public static SentinelMetricsRepository getSentinelMetricsRepository() {
        return getEntryCallback(SentinelMetricsRepository.class);
    }
}