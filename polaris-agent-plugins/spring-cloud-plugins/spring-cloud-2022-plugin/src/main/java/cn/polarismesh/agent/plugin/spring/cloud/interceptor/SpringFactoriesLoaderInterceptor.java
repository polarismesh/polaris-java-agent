/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.CommonBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.ConfigBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.LoadbalancerBeanInjector;
//import cn.polarismesh.agent.plugin.spring.cloud.inject.LosslessBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.MetadataTransferBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.PolarisContextBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RegistryBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RouterBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RpcEnhancementBeanInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringFactoriesLoaderInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringFactoriesLoaderInterceptor.class);

	private final List<BeanInjector> beanInjectors = new ArrayList<>();

	private final Map<ClassLoader, Boolean> parsedClasses = new ConcurrentHashMap<>();

	public SpringFactoriesLoaderInterceptor() {
		beanInjectors.add(new RegistryBeanInjector());
		beanInjectors.add(new RpcEnhancementBeanInjector());
		beanInjectors.add(new PolarisContextBeanInjector());
		beanInjectors.add(new ConfigBeanInjector());
		beanInjectors.add(new RouterBeanInjector());
		beanInjectors.add(new CommonBeanInjector());
		beanInjectors.add(new MetadataTransferBeanInjector());
		beanInjectors.add(new LoadbalancerBeanInjector());
	}


	@SuppressWarnings("unchecked")
	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (args[0] == null) {
			return;
		}
		ClassLoader classLoader = (ClassLoader)args[0];
		parsedClasses.computeIfAbsent(classLoader, new Function<ClassLoader, Boolean>() {
			@Override
			public Boolean apply(ClassLoader classLoader) {
				Map<String, List<String>> loadedClasses = (Map<String, List<String>>) result;

				for (BeanInjector beanInjector : beanInjectors) {
					LOGGER.info("[PolarisJavaAgent] start to inject JNI definition in module {}", beanInjector.getModule());
					Map<String, List<String>> classNames = beanInjector.getClassNameForType();
					if (classNames.isEmpty()) {
						continue;
					}
					for (Map.Entry<String, List<String>> entry : classNames.entrySet()) {
						List<String> existsValues = loadedClasses.get(entry.getKey());
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
				return true;
			}
		});
	}
}
