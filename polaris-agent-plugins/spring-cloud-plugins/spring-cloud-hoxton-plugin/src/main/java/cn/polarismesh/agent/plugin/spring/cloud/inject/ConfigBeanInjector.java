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

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import com.tencent.cloud.polaris.config.PolarisConfigAutoConfiguration;
import com.tencent.cloud.polaris.config.PolarisConfigBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.config.endpoint.PolarisConfigEndpointAutoConfiguration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class ConfigBeanInjector implements BeanInjector {
	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		Object polarisConfigBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisConfigBootstrapAutoConfiguration.class, "polarisConfigBootstrapAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisConfigBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisConfigBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisConfigBootstrapAutoConfiguration.class).getBeanDefinition());
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		Object polarisConfigAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisConfigAutoConfiguration.class, "polarisConfigAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisConfigAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisConfigAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisConfigAutoConfiguration.class).getBeanDefinition());
		Object polarisConfigEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisConfigEndpointAutoConfiguration.class, "polarisConfigEndpointAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisConfigEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisConfigEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisConfigEndpointAutoConfiguration.class).getBeanDefinition());
	}
}
