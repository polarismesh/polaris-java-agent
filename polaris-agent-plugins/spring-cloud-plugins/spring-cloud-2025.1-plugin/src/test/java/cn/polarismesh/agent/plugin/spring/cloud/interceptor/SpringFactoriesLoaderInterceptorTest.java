/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import org.junit.Test;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpringFactoriesLoaderInterceptorTest {

    private static final String CONTEXT_PROCESSOR =
            "com.tencent.cloud.polaris.context.config.PolarisContextEnvironmentPostProcessor";

    @Test
    public void injectsBoot4FactoriesWhenNoEntriesExist() throws Exception {
        Map<String, List<String>> factories = Collections.emptyMap();
        SpringFactoriesLoader loader = createLoader(factories);

        new SpringFactoriesLoaderInterceptor().after(loader, new Object[0], null, null);

        Map<String, List<String>> result = getFactories(loader);
        assertTrue(result.get(EnvironmentPostProcessor.class.getName()).contains(CONTEXT_PROCESSOR));
        assertTrue(result.get(ConfigDataLocationResolver.class.getName()).contains(
                "com.tencent.cloud.polaris.config.configdata.PolarisConfigDataLocationResolver"));
        assertTrue(result.get(ConfigDataLoader.class.getName()).contains(
                "com.tencent.cloud.polaris.config.configdata.PolarisConfigDataLoader"));
        assertFalse(result.containsKey("org.springframework.boot.env.EnvironmentPostProcessor"));
        assertTrue(factories.isEmpty());
    }

    @Test
    public void mergesFactoriesWithoutDuplicatesOrMutatingOriginalMap() throws Exception {
        Map<String, List<String>> factories = new HashMap<>();
        factories.put(EnvironmentPostProcessor.class.getName(),
                Collections.singletonList(CONTEXT_PROCESSOR));
        factories.put("example.Factory", Collections.singletonList("example.Implementation"));
        SpringFactoriesLoader loader = createLoader(factories);
        SpringFactoriesLoaderInterceptor interceptor = new SpringFactoriesLoaderInterceptor();

        interceptor.after(loader, new Object[0], null, null);
        interceptor.after(loader, new Object[0], null, null);

        Map<String, List<String>> result = getFactories(loader);
        assertEquals(1, Collections.frequency(result.get(EnvironmentPostProcessor.class.getName()),
                CONTEXT_PROCESSOR));
        assertEquals(Collections.singletonList("example.Implementation"), result.get("example.Factory"));
        assertEquals(2, factories.size());
    }

    private static SpringFactoriesLoader createLoader(Map<String, List<String>> factories) throws Exception {
        Constructor<SpringFactoriesLoader> constructor = SpringFactoriesLoader.class
                .getDeclaredConstructor(ClassLoader.class, Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(SpringFactoriesLoaderInterceptorTest.class.getClassLoader(), factories);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> getFactories(SpringFactoriesLoader loader) {
        return (Map<String, List<String>>) ReflectionUtils.getObjectByFieldName(loader, "factories");
    }
}
