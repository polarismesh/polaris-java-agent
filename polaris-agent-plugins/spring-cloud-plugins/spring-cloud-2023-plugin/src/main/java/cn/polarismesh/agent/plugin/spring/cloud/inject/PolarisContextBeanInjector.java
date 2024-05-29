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

package cn.polarismesh.agent.plugin.spring.cloud.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextPostConfiguration;
import com.tencent.cloud.polaris.context.logging.PolarisLoggingApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class PolarisContextBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(PolarisContextBeanInjector.class);

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-tencent-polaris-context";
	}

	@Override
	public Map<String, List<String>> getClassNameForType() {
		Map<String, List<String>> values = new HashMap<>();
		values.put("org.springframework.context.ApplicationListener", Collections.singletonList("com.tencent.cloud.polaris.context.logging.PolarisLoggingApplicationListener"));
		return values;
	}

	@Override
	public void onBootstrapStartup(Object configurationParser,
			Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		Object polarisContextAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextAutoConfiguration.class, "polarisContextAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContextAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContextAutoConfiguration.class).getBeanDefinition());
		Object polarisContextBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextBootstrapAutoConfiguration.class, "polarisContextBootstrapAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContextBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContextBootstrapAutoConfiguration.class).getBeanDefinition());

		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
	}

	@Override
	public void onApplicationStartup(Object configurationParser,
			Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {

		Object polarisContextPostConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextPostConfiguration.class, "polarisContextPostConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextPostConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContextPostConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContextPostConfiguration.class).getBeanDefinition());
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}
}
