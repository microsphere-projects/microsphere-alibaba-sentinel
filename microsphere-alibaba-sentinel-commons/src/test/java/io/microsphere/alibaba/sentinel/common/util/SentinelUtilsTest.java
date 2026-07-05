package io.microsphere.alibaba.sentinel.common.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_API_GATEWAY;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_DB_SQL;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_RPC;
import static com.alibaba.csp.sentinel.ResourceTypeConstants.COMMON_WEB;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.buildResourceName;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.findSentinelMetricsTaskExecutor;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getDefaultContextName;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getFlowDataId;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getPluginEnabledPropertyName;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getResourceTypeAsString;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.getSentinelMetricsTaskExecutor;
import static io.microsphere.alibaba.sentinel.common.util.SentinelUtils.isPluginEnabled;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SentinelUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class SentinelUtilsTest {

    @Test
    void testBuildResourceName() {
        Method method = findMethod(SentinelUtils.class, "buildResourceName", Method.class);
        String resourceName = buildResourceName(method);
        assertEquals("SentinelUtils.buildResourceName(Method)", resourceName);
    }

    @Test
    void testGetPluginEnabledPropertyName() {
        assertEquals("microsphere.sentinel.default.enabled", getPluginEnabledPropertyName("default"));
    }

    @Test
    void testIsPluginEnabled() {
        assertTrue(isPluginEnabled("default"));
    }

    @Test
    void testGetDefaultContextName() {
        assertEquals(DEFAULT_CONTEXT_NAME, getDefaultContextName("default"));
    }

    @Test
    void testGetFlowDataId() {
        String flowDataId = getFlowDataId("test");
        assertEquals("test-flow-rules", flowDataId);
    }

    @Test
    void testGetResourceTypeAsString() {
        assertEquals("COMMON", getResourceTypeAsString(COMMON));
        assertEquals("COMMON_WEB", getResourceTypeAsString(COMMON_WEB));
        assertEquals("COMMON_RPC", getResourceTypeAsString(COMMON_RPC));
        assertEquals("COMMON_API_GATEWAY", getResourceTypeAsString(COMMON_API_GATEWAY));
        assertEquals("COMMON_DB_SQL", getResourceTypeAsString(COMMON_DB_SQL));
        assertEquals("UNKNOWN", getResourceTypeAsString(-1));
    }

    @Test
    void testFindSentinelMetricsTaskExecutor() {
        assertNotNull(findSentinelMetricsTaskExecutor());
    }

    @Test
    void testGetSentinelMetricsTaskExecutor() {
        assertSame(findSentinelMetricsTaskExecutor(), getSentinelMetricsTaskExecutor());
        assertSame(findSentinelMetricsTaskExecutor(), getSentinelMetricsTaskExecutor());
        assertNotEquals(findSentinelMetricsTaskExecutor(), getSentinelMetricsTaskExecutor(null));
    }

    @Test
    void testResetContextMap() {
        assertDoesNotThrow(SentinelUtils::resetContextMap);
    }
}