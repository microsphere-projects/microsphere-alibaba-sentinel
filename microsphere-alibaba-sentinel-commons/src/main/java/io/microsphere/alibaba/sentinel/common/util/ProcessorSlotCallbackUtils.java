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

package io.microsphere.alibaba.sentinel.common.util;

import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;
import io.microsphere.util.Utils;

import java.util.Collection;

import static com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry.getEntryCallbacks;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * Utility class for handling {@link ProcessorSlotEntryCallback} and {@link ProcessorSlotExitCallback} in Sentinel.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ProcessorSlotEntryCallback
 * @see ProcessorSlotExitCallback
 * @see StatisticSlotCallbackRegistry
 * @since 1.0.0
 */
public abstract class ProcessorSlotCallbackUtils implements Utils {

    public static void addEntryCallbacks(ProcessorSlotEntryCallback<DefaultNode>... callbacks) {
        addEntryCallbacks(ofList(callbacks));
    }

    public static void addExitCallbacks(ProcessorSlotExitCallback... callbacks) {
        addExitCallbacks(ofList(callbacks));
    }

    public static void addEntryCallbacks(Iterable<? extends ProcessorSlotEntryCallback<DefaultNode>> callbacks) {
        for (ProcessorSlotEntryCallback<DefaultNode> callback : callbacks) {
            addEntryCallback(callback);
        }
    }

    public static void addExitCallbacks(Iterable<? extends ProcessorSlotExitCallback> callbacks) {
        for (ProcessorSlotExitCallback callback : callbacks) {
            addExitCallback(callback);
        }
    }

    public static void addEntryCallback(ProcessorSlotEntryCallback<DefaultNode> callback) {
        StatisticSlotCallbackRegistry.addEntryCallback(key(callback), callback);
    }

    public static void addExitCallback(ProcessorSlotExitCallback callback) {
        StatisticSlotCallbackRegistry.addExitCallback(key(callback), callback);
    }

    public static <T extends ProcessorSlotEntryCallback<DefaultNode>> T getEntryCallback(Class<T> callbackClass) {
        Collection<ProcessorSlotEntryCallback<DefaultNode>> entryCallbacks = getEntryCallbacks();
        for (ProcessorSlotEntryCallback<DefaultNode> entryCallback : entryCallbacks) {
            if (callbackClass.isInstance(entryCallback)) {
                return (T) entryCallback;
            }
        }
        return null;
    }

    public static <T extends ProcessorSlotExitCallback> T getExitCallback(Class<T> callbackClass) {
        Collection<ProcessorSlotExitCallback> exitCallbacks = StatisticSlotCallbackRegistry.getExitCallbacks();
        for (ProcessorSlotExitCallback exitCallback : exitCallbacks) {
            if (callbackClass.isInstance(exitCallback)) {
                return (T) exitCallback;
            }
        }
        return null;
    }

    public static <T extends ProcessorSlotEntryCallback<DefaultNode>> T removeEntryCallback(Class<T> callbackClass) {
        return (T) StatisticSlotCallbackRegistry.removeEntryCallback(key(callbackClass));
    }

    public static <T extends ProcessorSlotExitCallback> T removeExitCallback(Class<T> callbackClass) {
        return (T) StatisticSlotCallbackRegistry.removeExitCallback(key(callbackClass));
    }

    static String key(Object callback) {
        return key(callback.getClass());
    }

    static String key(Class<?> callbackClass) {
        return getTypeName(callbackClass);
    }

    private ProcessorSlotCallbackUtils() {
    }
}