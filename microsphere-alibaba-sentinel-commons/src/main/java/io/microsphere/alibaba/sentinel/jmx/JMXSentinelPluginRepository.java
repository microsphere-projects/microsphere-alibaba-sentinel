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

package io.microsphere.alibaba.sentinel.jmx;

import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SentinelPluginRepository;
import io.microsphere.alibaba.sentinel.common.SimpleSentinelPluginRepository;
import io.microsphere.lang.Prioritized;
import io.microsphere.lang.function.ThrowableBiFunction;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.util.Collection;
import java.util.Set;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_PRIORITY;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.text.FormatUtils.format;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Collections.emptyList;

/**
 * {@link SentinelPluginRepository} class based on JMX
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelPluginRepository
 * @since 1.0.0
 */
public class JMXSentinelPluginRepository implements SentinelPluginRepository, Prioritized {

    /**
     * The JMX Domain of {@link SentinelPlugin Sentinel Plugin}
     */
    public static final String JMX_DOMAIN = "io.microsphere.sentinel";

    /**
     * The {@link ObjectName} pattern of {@link SentinelPlugin Sentinel Plugin}
     */
    public static final String OBJECT_NAME_PATTERN = JMX_DOMAIN + ":type=SentinelPlugin,name={}";

    private final SentinelPluginRepository delegate;

    public JMXSentinelPluginRepository() {
        this.delegate = new SimpleSentinelPluginRepository();
    }

    @Override
    public boolean isInstalled(String pluginName) {
        return doInMBeanServer(pluginName, (mBeanServer, objectName) -> mBeanServer.isRegistered(objectName));
    }

    @Override
    public boolean install(SentinelPlugin plugin) throws IllegalArgumentException, NullPointerException {
        return doInMBeanServer(plugin.getName(), (mBeanServer, objectName) -> {
            if (mBeanServer.isRegistered(objectName)) {
                return false;
            }
            StandardMBean mBean = new StandardMBean(plugin, SentinelPlugin.class);
            mBeanServer.registerMBean(mBean, objectName);
            return delegate.install(plugin);
        });
    }

    @Override
    public SentinelPlugin get(String pluginName) {
        return doInMBeanServer(pluginName, (mBeanServer, objectName) -> {
            if (mBeanServer.isRegistered(objectName)) {
                return delegate.get(pluginName);
            }
            delegate.uninstall(pluginName);
            return null;
        });
    }

    @Override
    public boolean uninstall(String pluginName) throws NullPointerException {
        if (pluginName == null) {
            throw new NullPointerException("The 'pluginName' must be not null");
        }
        return doInMBeanServer(pluginName, (mBeanServer, objectName) -> {
            if (mBeanServer.isRegistered(objectName)) {
                mBeanServer.unregisterMBean(objectName);
            }
            return delegate.uninstall(pluginName);
        });
    }

    @Override
    public void clear() {
        getAll(true);
    }

    @Override
    public Collection<SentinelPlugin> getAll() {
        return getAll(false);
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY;
    }

    protected Collection<SentinelPlugin> getAll(boolean forRemoval) {
        return doInMBeanServer("*", (mBeanServer, objectName) -> {
            Set<ObjectName> objectNames = mBeanServer.queryNames(objectName, null);
            if (objectNames.isEmpty()) {
                return emptyList();
            }
            if (forRemoval) {
                for (ObjectName name : objectNames) {
                    String pluginName = name.getKeyProperty("name");
                    uninstall(pluginName);
                }
            }
            return delegate.getAll();
        });
    }

    static ObjectName newObjectName(String plugName) throws Exception {
        String name = format(OBJECT_NAME_PATTERN, plugName);
        return new ObjectName(name);
    }

    protected <R> R doInMBeanServer(String plugName, ThrowableBiFunction<MBeanServer, ObjectName, R> consumer) {
        MBeanServer mBeanServer = getPlatformMBeanServer();
        return execute(() -> {
            ObjectName objectName = newObjectName(plugName);
            R result = null;
            synchronized (delegate) {
                result = consumer.apply(mBeanServer, objectName);
            }
            return result;
        });
    }
}