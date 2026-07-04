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

package io.microsphere.alibaba.sentinel.spring.boot.autoconfigure;


import io.microsphere.alibaba.sentinel.mybatis.executor.SentinelMyBatisExecutorFilter;
import io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelMyBatisAutoConfiguration.Config;
import io.microsphere.mybatis.spring.annotation.EnableMyBatisExtension;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Set;

/**
 * {@link SentinelMyBatisAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelMyBatisAutoConfiguration
 * @since 1.0.0
 */
class SentinelMyBatisAutoConfigurationTest extends AutoConfigurationTest<SentinelMyBatisAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(Config.class);
        autoConfiguredClasses.add(SentinelMyBatisExecutorFilter.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.mybatis.enabled=false");
        globalDisabledPropertyValues.add("microsphere.sentinel.mybatis.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(SqlSessionFactory.class);
        globalMissingClasses.add(EnableMyBatisExtension.class);
        globalMissingClasses.add(SentinelMyBatisExecutorFilter.class);
    }
}