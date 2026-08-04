package io.microsphere.alibaba.sentinel.common.util;

import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import io.microsphere.annotation.Nonnull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static com.alibaba.csp.sentinel.config.SentinelConfig.getAppName;
import static com.alibaba.csp.sentinel.node.metric.MetricWriter.METRIC_BASE_DIR;
import static com.alibaba.csp.sentinel.node.metric.MetricWriter.formMetricFileName;
import static com.alibaba.csp.sentinel.util.PidUtil.getPid;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME_PATTERN;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.FLOW_DATA_ID_PATTERN;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.PROPERTY_NAME_PREFIX;
import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.invoke.MethodHandleUtils.findStatic;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ClassUtils.getSimpleName;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static io.microsphere.util.SystemUtils.getSystemProperty;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Alibaba Sentinel Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SentinelUtils {

    private static final Map<Integer, String> resourceTypeToLabelMapping = initResourceTypeToLabelMapping();

    /**
     * The {@link MethodHandle} of {@link ContextUtil#resetContextMap()}
     *
     * @see ContextUtil#resetContextMap()
     */
    private static final MethodHandle resetContextMapMethodHandle = findStatic(ContextUtil.class, "resetContextMap");

    private static volatile ScheduledExecutorService sentinelMetricsTaskExecutor;

    private static volatile MetricSearcher metricSearcher;

    static {
        shutdownOnExit(sentinelMetricsTaskExecutor);
        addShutdownHookCallback(() -> execute(SentinelUtils::resetContextMap));
    }

    private static Map<Integer, String> initResourceTypeToLabelMapping() {
        Field[] fields = ResourceTypeConstants.class.getFields();
        Map<Integer, String> resourceTypeToLabelMapping = newFixedHashMap(fields.length);
        for (Field field : fields) {
            if (isStatic(field.getModifiers())) {
                Integer value = getStaticFieldValue(true, field);
                resourceTypeToLabelMapping.put(value, field.getName());
            }
        }
        return resourceTypeToLabelMapping;
    }

    /**
     * The Resource name of the build execution method
     *
     * @param method {@link Method}
     * @return The method signature (simple class) serves as the Resource name
     */
    @Nonnull
    public static String buildResourceName(Method method) {
        String prefix = getSimpleName(method.getDeclaringClass()) + "." + method.getName();
        StringJoiner resourceNameBuilder = new StringJoiner(",", prefix + "(", ")");
        for (Class<?> parameterType : method.getParameterTypes()) {
            resourceNameBuilder.add(getSimpleName(parameterType));
        }
        return resourceNameBuilder.toString();
    }

    /**
     * Get the Flow Data ID
     *
     * @param appName the name of application
     * @return non-null
     */
    @Nonnull
    public static String getFlowDataId(String appName) {
        return format(FLOW_DATA_ID_PATTERN, appName);
    }

    /**
     * Get the Property Name of plugin enabled property : "microsphere.sentinel.${pluginName}.enabled"
     *
     * @param pluginName the name of plugin
     * @return non-null
     */
    @Nonnull
    public static final String getPluginEnabledPropertyName(String pluginName) {
        return PROPERTY_NAME_PREFIX + pluginName + DOT + ENABLED_PROPERTY_NAME;
    }

    /**
     * Is the plugin enabled ?
     *
     * @param pluginName the name of plugin
     * @return the <code>true</code> as default, otherwise set the Java System Property to be "false" , is like
     * "microsphere.sentinel.${pluginName}.enabled" = "false"
     */
    public static final boolean isPluginEnabled(String pluginName) {
        String propertyName = getPluginEnabledPropertyName(pluginName);
        String propertyValue = getSystemProperty(propertyName, "true");
        return !"false".equalsIgnoreCase(propertyValue);
    }

    /**
     * Get the default Context name
     *
     * @param pluginName the name of plugin
     * @return non-null
     */
    @Nonnull
    public static String getDefaultContextName(String pluginName) {
        return format(DEFAULT_CONTEXT_NAME_PATTERN, pluginName);
    }

    /**
     * Get the label string for resource type
     *
     * @param resourceType resource type
     * @return non-null
     */
    @Nonnull
    public static String getResourceTypeAsString(int resourceType) {
        return resourceTypeToLabelMapping.getOrDefault(resourceType, "UNKNOWN");
    }

    /**
     * Find the Sentinel Metrics Task {@link ScheduledExecutorService Executor} from {@link FlowRuleManager}
     *
     * @return <code>null</code> if can't be found
     * @see FlowRuleManager#SCHEDULER
     */
    public static ScheduledExecutorService findSentinelMetricsTaskExecutor() {
        return getStaticFieldValue(true, FlowRuleManager.class, "SCHEDULER");
    }

    /**
     * Get the Sentinel Metrics Task {@link ScheduledExecutorService Executor}
     *
     * @return {@link Executors#newSingleThreadScheduledExecutor(ThreadFactory) new a single ScheduledExecutorService}
     * if can't be {@link #findSentinelMetricsTaskExecutor() found}
     * @see #findSentinelMetricsTaskExecutor()
     */
    @Nonnull
    public static ScheduledExecutorService getSentinelMetricsTaskExecutor() {
        ScheduledExecutorService scheduledExecutorService = sentinelMetricsTaskExecutor;

        if (scheduledExecutorService == null) {
            scheduledExecutorService = getSentinelMetricsTaskExecutor(findSentinelMetricsTaskExecutor());
            sentinelMetricsTaskExecutor = scheduledExecutorService;
        }

        return scheduledExecutorService;
    }

    /**
     * Reset the {@link ContextUtil#resetContextMap() context map} of Sentinel
     *
     * @throws Throwable if any exception occurred
     * @see ContextUtil#resetContextMap()
     */
    public static void resetContextMap() throws Throwable {
        resetContextMapMethodHandle.invokeExact();
    }

    /**
     * Get the {@link MetricSearcher} of Sentinel Metrics
     *
     * @return non-null
     */
    @Nonnull
    public static MetricSearcher getMetricSearcher() {
        MetricSearcher metricSearcher = SentinelUtils.metricSearcher;
        if (metricSearcher == null) {
            synchronized (SentinelUtils.class) {
                metricSearcher = SentinelUtils.metricSearcher;
                if (metricSearcher == null) {
                    metricSearcher = newMetricSearcher();
                    SentinelUtils.metricSearcher = metricSearcher;
                }
            }
        }
        return metricSearcher;
    }

    /**
     * Create a new {@link MetricSearcher} instance
     *
     * @return non-null
     */
    @Nonnull
    public static MetricSearcher newMetricSearcher() {
        String appName = getAppName();
        int pid = getPid();
        return new MetricSearcher(METRIC_BASE_DIR, formMetricFileName(appName, pid));
    }

    static ScheduledExecutorService getSentinelMetricsTaskExecutor(ScheduledExecutorService defaultScheduledExecutorService) {
        return defaultScheduledExecutorService == null ?
                newSingleThreadScheduledExecutor(new NamedThreadFactory("sentinel-metrics-task", true)) :
                defaultScheduledExecutorService;
    }

    private SentinelUtils() {
    }
}