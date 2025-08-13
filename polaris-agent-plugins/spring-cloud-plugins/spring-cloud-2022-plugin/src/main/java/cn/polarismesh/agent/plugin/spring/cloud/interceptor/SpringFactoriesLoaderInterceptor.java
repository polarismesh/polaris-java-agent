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

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.*;
import com.tencent.polaris.api.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpringFactoriesLoaderInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringFactoriesLoaderInterceptor.class);

    private final List<BeanInjector> beanInjectors = new ArrayList<>();

    private final Map<ClassLoader, Boolean> parsedClasses = new ConcurrentHashMap<>();

    public SpringFactoriesLoaderInterceptor() {
        beanInjectors.add(new CommonBeanInjector());
        beanInjectors.add(new PolarisContextBeanInjector());
        beanInjectors.add(new MetadataTransferBeanInjector());
        beanInjectors.add(new RegistryBeanInjector());
        beanInjectors.add(new ConfigBeanInjector());
        beanInjectors.add(new RpcEnhancementBeanInjector());
        beanInjectors.add(new LosslessBeanInjector());
        beanInjectors.add(new LoadbalancerBeanInjector());
        beanInjectors.add(new RouterBeanInjector());
        beanInjectors.add(new CircuitBreakerBeanInjector());
        beanInjectors.add(new RateLimitBeanInjector());
    }


    @SuppressWarnings("unchecked")
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Map<String, List<String>> oldFactories = (Map<String, List<String>>) ReflectionUtils.getObjectByFieldName(
                target, "factories");
        if (CollectionUtils.isEmpty(oldFactories)) {
            oldFactories = new HashMap<>();
        }
        Map<String, List<String>> newFactories = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : oldFactories.entrySet()) {
            newFactories.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        for (BeanInjector beanInjector : beanInjectors) {
            String message = String.format("[PolarisJavaAgent] start to inject JNI definition in module %s",
                    beanInjector.getModule());
            // 静默阶段, 需要手动输出日志
            System.out.println(message);
            LOGGER.info(message);
            Map<String, List<String>> classNames = beanInjector.getClassNameForType();
            if (CollectionUtils.isEmpty(classNames)) {
                continue;
            }
            for (Map.Entry<String, List<String>> entry : classNames.entrySet()) {
                List<String> existsValues = newFactories.get(entry.getKey());
                List<String> toAddValues = entry.getValue();
                if (null != existsValues) {
                    for (String toAddValue : toAddValues) {
                        if (existsValues.contains(toAddValue)) {
                            continue;
                        }
                        existsValues.add(toAddValue);
                    }
                } else {
                    classNames.put(entry.getKey(), toAddValues);
                }
            }
        }
        ReflectionUtils.setValueByFieldName(target, "factories", Collections.unmodifiableMap(newFactories));
    }
}
