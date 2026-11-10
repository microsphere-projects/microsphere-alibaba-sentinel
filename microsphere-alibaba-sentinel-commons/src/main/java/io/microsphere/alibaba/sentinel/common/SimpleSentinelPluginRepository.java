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

import io.microsphere.logging.Logger;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * Simple {@link SentinelPluginRepository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelPluginRepository
 * @since 1.0.0
 */
public class SimpleSentinelPluginRepository implements SentinelPluginRepository {

    private static final Logger logger = getLogger(SimpleSentinelPluginRepository.class);

    private final ConcurrentHashMap<String, SentinelPlugin> pluginsRepository = newConcurrentHashMap(8);

    @Override
    public boolean isInstalled(String pluginName) {
        return this.pluginsRepository.containsKey(pluginName);
    }

    @Override
    public boolean install(SentinelPlugin plugin) throws IllegalArgumentException, NullPointerException {
        if (plugin == null) {
            throw new NullPointerException("The plugin must not be null");
        }
        String name = plugin.getName();
        if (this.pluginsRepository.putIfAbsent(name, plugin) == null) {
            return true;
        }
        logger.warn("The plugin [{}] with name ['{}'] already exists", plugin, name);
        return false;
    }

    @Override
    public SentinelPlugin get(String pluginName) {
        return this.pluginsRepository.get(pluginName);
    }

    @Override
    public boolean uninstall(String pluginName) throws NullPointerException {
        return this.pluginsRepository.remove(pluginName) != null;
    }

    @Override
    public void clear() {
        this.pluginsRepository.clear();
    }

    @Override
    public Collection<SentinelPlugin> getAll() {
        return this.pluginsRepository.values();
    }
}