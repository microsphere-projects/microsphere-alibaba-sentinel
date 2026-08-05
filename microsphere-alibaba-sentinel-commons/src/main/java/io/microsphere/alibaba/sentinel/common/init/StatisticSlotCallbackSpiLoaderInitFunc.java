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

package io.microsphere.alibaba.sentinel.common.init;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;
import com.alibaba.csp.sentinel.spi.SpiLoader;
import io.microsphere.alibaba.sentinel.common.callback.DefaultNodeEntryCallback;

import java.util.List;

import static com.alibaba.csp.sentinel.spi.SpiLoader.of;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.addEntryCallbacks;
import static io.microsphere.alibaba.sentinel.common.util.ProcessorSlotCallbackUtils.addExitCallbacks;

/**
 * {@link InitFunc} for StatisticSlotCallback's {@link SpiLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StatisticSlotCallbackRegistry
 * @see DefaultNodeEntryCallback
 * @see ProcessorSlotEntryCallback
 * @see ProcessorSlotExitCallback
 * @see SpiLoader
 * @since 1.0.0
 */
public class StatisticSlotCallbackSpiLoaderInitFunc implements InitFunc {

    @Override
    public void init() {
        loadDefaultNodeEntryCallbacks();
        loadProcessorSlotExitCallbacks();
    }

    private void loadDefaultNodeEntryCallbacks() {
        SpiLoader<DefaultNodeEntryCallback> defaultNodeEntryCallbackSpiLoader = of(DefaultNodeEntryCallback.class);
        List<DefaultNodeEntryCallback> defaultNodeEntryCallbacks = defaultNodeEntryCallbackSpiLoader.loadInstanceListSorted();
        addEntryCallbacks(defaultNodeEntryCallbacks);
    }

    private void loadProcessorSlotExitCallbacks() {
        SpiLoader<ProcessorSlotExitCallback> processorSlotExitCallbackSpiLoader = of(ProcessorSlotExitCallback.class);
        List<ProcessorSlotExitCallback> processorSlotExitCallbacks = processorSlotExitCallbackSpiLoader.loadInstanceListSorted();
        addExitCallbacks(processorSlotExitCallbacks);
    }
}