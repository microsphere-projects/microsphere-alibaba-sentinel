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

import static com.alibaba.csp.sentinel.EntryType.IN;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON;
import static io.microsphere.alibaba.sentinel.common.SentinelPlugin.install;

/**
 * Abstract {@link SentinelPlugin Sentinel Plugin} class
 * <p>
 * NOTE : when {@code autoInstalled} is {@code true}, the constructor publishes {@code this}
 * to the {@link SentinelPluginRepository} before the subclass constructor completes (and thus
 * before the subclass fields are assigned). Subclasses should either keep their state in the
 * constructor arguments, or make their extra fields safely visible (e.g., {@code final} or
 * {@code volatile}) so that the published instance is not observed in a partially-constructed
 * state by other threads (e.g., the JMX repository iteration).
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelPlugin
 * @since 1.0.0
 */
public abstract class AbstractSentinelPlugin implements SentinelPlugin {

    protected final String name;

    protected final String contextName;

    protected final String origin;

    protected final int resourceType;

    protected final EntryType trafficType;

    protected final boolean autoInstalled;

    private volatile boolean enabled;

    protected AbstractSentinelPlugin(String name, String contextName, String origin) {
        this(name, contextName, origin, COMMON);
    }

    protected AbstractSentinelPlugin(String name, String contextName, String origin, int resourceType) {
        this(name, contextName, origin, resourceType, IN);
    }

    protected AbstractSentinelPlugin(String name, String contextName, String origin, int resourceType, EntryType trafficType) {
        this(name, contextName, origin, resourceType, trafficType, true);
    }

    protected AbstractSentinelPlugin(String name, String contextName, String origin, int resourceType, EntryType trafficType, boolean autoInstalled) {
        this.name = name;
        this.contextName = contextName;
        this.origin = origin;
        this.resourceType = resourceType;
        this.trafficType = trafficType;
        this.autoInstalled = autoInstalled;
        this.enabled = SentinelPlugin.super.isEnabled();
        if (autoInstalled) {
            install(this);
        }
    }

    /**
     * Enable this {@link SentinelPlugin Sentinel Plugin}
     */
    public void enable() {
        setEnabled(true);
    }

    /**
     * Disable this {@link SentinelPlugin Sentinel Plugin}
     */
    public void disable() {
        setEnabled(false);
    }

    /**
     * Is this {@link SentinelPlugin Sentinel Plugin} enabled ?
     *
     * @return If enabled, return <code>true</code>
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set whether this {@link SentinelPlugin Sentinel Plugin} enabled or not
     *
     * @param enabled If enabled, set <code>true</code>
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the name of this {@link SentinelPlugin Sentinel Plugin}
     *
     * @return the name of this {@link SentinelPlugin Sentinel Plugin}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Get the context name of this {@link SentinelPlugin Sentinel Plugin}
     *
     * @return the context name of this {@link SentinelPlugin Sentinel Plugin}
     */
    @Override
    public String getContextName() {
        return this.contextName;
    }

    /**
     * Get the origin of this {@link SentinelPlugin Sentinel Plugin}
     *
     * @return the origin of this {@link SentinelPlugin Sentinel Plugin}
     */
    @Override
    public String getOrigin() {
        return this.origin;
    }

    /**
     * Get the resource type of this {@link SentinelPlugin Sentinel Plugin}
     *
     * @return the resource type of this {@link SentinelPlugin Sentinel Plugin}
     */
    public int getResourceType() {
        return this.resourceType;
    }

    /**
     * Get the traffic type of this {@link SentinelPlugin Sentinel Plugin}
     *
     * @return the traffic type of this {@link SentinelPlugin Sentinel Plugin}
     */
    public EntryType getTrafficType() {
        return this.trafficType;
    }

    /**
     * Is auto installed this {@link SentinelPlugin Sentinel Plugin}?
     *
     * @return If auto installed, return <code>true</code>
     */
    public boolean isAutoInstalled() {
        return this.autoInstalled;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", contextName='" + contextName + '\'' +
                ", origin='" + origin + '\'' +
                ", resourceType=" + resourceType +
                ", trafficType=" + trafficType +
                ", autoRegisterMBean=" + autoInstalled +
                ", enabled=" + enabled +
                '}';
    }

}