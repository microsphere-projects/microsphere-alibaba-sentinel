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


import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor;
import io.microsphere.mybatis.spring.test.config.MyBatisDataBaseTestConfiguration;
import io.microsphere.mybatis.test.mapper.UserMapper;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.test.config.RedisConfig;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;

import static com.alibaba.csp.sentinel.context.ContextUtil.enter;
import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.buildDefaultDruidDataSource;
import static io.microsphere.mybatis.test.AbstractMapperTest.assertUserMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * SentinelAutoConfiguration Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SentinelAlibabaDruidAutoConfiguration
 * @see SentinelMyBatisAutoConfiguration
 * @see SentinelRedisAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        MyBatisDataBaseTestConfiguration.class,
        RedisConfig.class,
        SentinelAutoConfigurationTest.TestConfig.class
},
        webEnvironment = RANDOM_PORT,
        properties = {
                "microsphere.redis.enabled=true",
                "spring.application.name=test-app",
                "mybatis.configLocation=classpath:/META-INF/mybatis/config.xml"
        })
@EnableAutoConfiguration
class SentinelAutoConfigurationTest {

    @Autowired
    private RedisContext redisContext;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SentinelRedisCommandInterceptor interceptor;

    @Autowired
    private SqlSessionFactory sessionFactory;

    @Test
    void test() {
        testAlibabaDruidAndMyBatisPlugin();
        testRedisPlugin();
    }

    private void testAlibabaDruidAndMyBatisPlugin() {
        Configuration configuration = this.sessionFactory.getConfiguration();
        SqlSession sqlSession = this.sessionFactory.openSession();
        UserMapper userMapper = configuration.getMapper(UserMapper.class, sqlSession);
        assertUserMapper(userMapper);

        Context context = enter("microsphere_sentinel_alibaba_druid_context", "Filter");
        assertNotNull(context);

        context = enter("microsphere_sentinel_mybatis_context", "Executor");
        assertNotNull(context);
    }

    void testRedisPlugin() {
        String key = "key";
        String value = "value";
        ValueOperations<String, String> valueOperations = this.stringRedisTemplate.opsForValue();
        valueOperations.set(key, value);
        assertEquals(value, valueOperations.get(key));

        Context context = enter("microsphere_sentinel_redis_context", "RedisConnection");
        assertNotNull(context);
    }

    static class TestConfig {

        @Bean(initMethod = "init", destroyMethod = "close")
        public DruidDataSource dataSource() throws IOException {
            return buildDefaultDruidDataSource();
        }
    }
}