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

package cn.polarismesh.agent.plugin.spring.cloud.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesBootstrapConfiguration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

/**
 * Injects statistics configuration required by service registration even when
 * RPC enhancement and statistics reporting are disabled.
 */
public class StatBeanInjector implements BeanInjector {

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-tencent-rpc-enhancement-stat";
	}

	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator,
			Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		if (!Utils.checkPolarisEnabled(environment)) {
			return;
		}
		Object configuration = ReflectionUtils.invokeConstructor(configClassCreator,
				PolarisStatPropertiesBootstrapConfiguration.class, "polarisStatPropertiesBootstrapConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, configuration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisStatPropertiesBootstrapConfiguration",
				BeanDefinitionBuilder.genericBeanDefinition(PolarisStatPropertiesBootstrapConfiguration.class)
						.getBeanDefinition());
		bootstrapLoaded.set(true);
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator,
			Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		if (!Utils.checkPolarisEnabled(environment)) {
			return;
		}
		if (!bootstrapLoaded.get()) {
			onBootstrapStartup(configurationParser, configClassCreator, processConfigurationClass, registry, environment);
		}
		Object configuration = ReflectionUtils.invokeConstructor(configClassCreator,
				PolarisStatPropertiesAutoConfiguration.class, "polarisStatPropertiesAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, configuration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisStatPropertiesAutoConfiguration",
				BeanDefinitionBuilder.genericBeanDefinition(PolarisStatPropertiesAutoConfiguration.class)
						.getBeanDefinition());
	}
}
