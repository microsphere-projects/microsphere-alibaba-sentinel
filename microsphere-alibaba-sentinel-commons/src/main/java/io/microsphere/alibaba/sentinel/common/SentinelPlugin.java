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
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import io.microsphere.alibaba.sentinel.common.constants.SentinelConstants;
import io.microsphere.alibaba.sentinel.common.util.SentinelUtils;
import io.microsphere.annotation.Nonnull;

import static io.microsphere.alibaba.sentinel.common.SentinelPluginRepository.INSTANCE;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getDefaultContextName;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.isPluginEnabled;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;

/**
 * The Plugin interface for Sentinel
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface SentinelPlugin {

    /**
     * Is auto installed this {@link SentinelPlugin Sentinel Plugin}?
     *
     * @return If auto installed, return <code>true</code>
     */
    boolean isAutoInstalled();

    /**
     * Determine whether the plugin is enabled
     *
     * @return if enabled , return {@code true} , else return {@code false}
     */
    default boolean isEnabled() {
        return isPluginEnabled(getName());
    }

    /**
     * Set the plugin enabled or not
     *
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * The plugin name of Sentinel, the name must be unique in the application
     *
     * @return non-null
     */
    @Nonnull
    String getName();

    /**
     * The context name that Sentinel Plugin belongs to
     *
     * @return non-null
     * @see SentinelUtils#getDefaultContextName(String)
     */
    @Nonnull
    default String getContextName() {
        return getDefaultContextName(getName());
    }

    /**
     * The origin that Sentinel Plugin belongs to
     *
     * @return non-null
     * @see SentinelConstants#DEFAULT_ORIGIN
     */
    @Nonnull
    String getOrigin();

    /**
     * The resource that Sentinel Plugin belongs to
     *
     * @return the resource that Sentinel Plugin belongs to
     * @see ResourceTypeConstants
     */
    int getResourceType();

    /**
     * The traffic that Sentinel Plugin belongs to
     *
     * @return non-null
     */
    @Nonnull
    EntryType getTrafficType();

    /**
     * Install this {@link SentinelPlugin}
     *
     * @param plugin the {@link SentinelPlugin} to be installed
     * @throws NullPointerException if the {@link SentinelPlugin} is null
     */
    static void install(SentinelPlugin plugin) throws NullPointerException {
        SentinelPluginRepository repository = INSTANCE;
        // Install this SentinelPlugin
        repository.install(plugin);
        // Uninstall this SentinelPlugin when JVM shutdown
        addShutdownHookCallback(() -> repository.uninstall(plugin));
    }
}