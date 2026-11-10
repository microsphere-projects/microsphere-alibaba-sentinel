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
import io.microsphere.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.util.Collection;
import java.util.Set;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_PRIORITY;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
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

    private static final Logger logger = getLogger(JMXSentinelPluginRepository.class);

    /**
     * The characters that must be escaped in the {@link ObjectName} key property value
     *
     * @see ObjectName#quote(String)
     */
    private static final String OBJECT_NAME_SPECIAL_CHARS = ":=,\"*?";

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
            boolean installed = delegate.install(plugin);
            if (!installed) {
                // Rollback the MBean registration to keep the JMX registry and
                // the delegate repository consistent
                mBeanServer.unregisterMBean(objectName);
            }
            return installed;
        });
    }

    /**
     * Get the {@link SentinelPlugin} by its name.
     * <p>
     * NOTE : the JMX registry is the source of truth. If the MBean of the plugin has been
     * unregistered externally (e.g., via a JMX console), the plugin is also removed from
     * the delegate repository as a self-healing cleanup, and <code>null</code> is returned.
     *
     * @param pluginName the name of {@link SentinelPlugin}
     * @return the {@link SentinelPlugin} or <code>null</code> if not found
     */
    @Override
    public SentinelPlugin get(String pluginName) {
        return doInMBeanServer(pluginName, (mBeanServer, objectName) -> {
            if (mBeanServer.isRegistered(objectName)) {
                return delegate.get(pluginName);
            }
            // Self-healing : remove the stale plugin from the delegate repository
            if (delegate.uninstall(pluginName)) {
                logger.warn("The plugin[name : '{}'] MBean was unregistered externally, " +
                        "it has been removed from the delegate repository", pluginName);
            }
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
        MBeanServer mBeanServer = getPlatformMBeanServer();
        return execute(() -> {
            // The wildcard ObjectName pattern to query all the SentinelPlugin MBeans.
            // NOTE : it's a query pattern rather than a plugin name, so it must NOT be
            // escaped by escape(String) - a quoted "*" would match nothing
            ObjectName queryPattern = new ObjectName(JMX_DOMAIN + ":type=SentinelPlugin,name=*");
            synchronized (delegate) {
                Set<ObjectName> objectNames = mBeanServer.queryNames(queryPattern, null);
                if (objectNames.isEmpty()) {
                    return emptyList();
                }
                if (forRemoval) {
                    for (ObjectName name : objectNames) {
                        String pluginName = getPluginName(name);
                        uninstall(pluginName);
                    }
                }
                return delegate.getAll();
            }
        });
    }

    static ObjectName newObjectName(String plugName) throws Exception {
        String name = format(OBJECT_NAME_PATTERN, escape(plugName));
        return new ObjectName(name);
    }

    /**
     * Get the plugin name from the specified {@link ObjectName}, decoding the quoted
     * value if the plugin name was escaped by {@link #escape(String)}
     *
     * @param objectName the {@link ObjectName} of {@link SentinelPlugin}
     * @return the decoded plugin name
     * @see ObjectName#unquote(String)
     */
    static String getPluginName(ObjectName objectName) {
        String pluginName = objectName.getKeyProperty("name");
        if (pluginName != null && pluginName.length() > 1
                && pluginName.startsWith("\"") && pluginName.endsWith("\"")) {
            pluginName = ObjectName.unquote(pluginName);
        }
        return pluginName;
    }

    /**
     * Escape the plugin name if it contains any {@link ObjectName} special characters
     * (e.g., ':' , '=' , ',' , '"' , '*' , '?') , otherwise return it as-is
     *
     * @param pluginName the plugin name
     * @return the escaped (quoted) plugin name if needed, or the original one
     * @see ObjectName#quote(String)
     */
    static String escape(String pluginName) {
        for (int i = 0; i < pluginName.length(); i++) {
            if (OBJECT_NAME_SPECIAL_CHARS.indexOf(pluginName.charAt(i)) >= 0) {
                return ObjectName.quote(pluginName);
            }
        }
        return pluginName;
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