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
import com.tencent.cloud.polaris.DiscoveryPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.DiscoveryPropertiesBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.discovery.PolarisDiscoveryAutoConfiguration;
import com.tencent.cloud.polaris.registry.PolarisServiceRegistryAutoConfiguration;
import com.tencent.cloud.polaris.ribbon.PolarisDiscoveryRibbonAutoConfiguration;
import   com.tencent.cloud.polaris.endpoint.PolarisDiscoveryEndpointAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesBootstrapConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class RegistryBeanInjector implements BeanInjector {
	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
		Object discoveryPropertiesBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, DiscoveryPropertiesBootstrapAutoConfiguration.class, "discoveryPropertiesBootstrapAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, discoveryPropertiesBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("discoveryPropertiesBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				DiscoveryPropertiesBootstrapAutoConfiguration.class).getBeanDefinition());
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
		Object discoveryPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, DiscoveryPropertiesAutoConfiguration.class, "discoveryPropertiesAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, discoveryPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("discoveryPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				DiscoveryPropertiesAutoConfiguration.class).getBeanDefinition());
		Object polarisDiscoveryAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisDiscoveryAutoConfiguration.class, "polarisDiscoveryAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisDiscoveryAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisDiscoveryAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisDiscoveryAutoConfiguration.class).getBeanDefinition());
		Object polarisDiscoveryRibbonAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisDiscoveryRibbonAutoConfiguration.class, "polarisDiscoveryRibbonAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisDiscoveryRibbonAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisDiscoveryRibbonAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisDiscoveryRibbonAutoConfiguration.class).getBeanDefinition());
		Object polarisServiceRegistryAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisServiceRegistryAutoConfiguration.class, "polarisServiceRegistryAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisServiceRegistryAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisServiceRegistryAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisServiceRegistryAutoConfiguration.class).getBeanDefinition());
		Object polarisDiscoveryEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisDiscoveryEndpointAutoConfiguration.class, "polarisDiscoveryEndpointAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisDiscoveryEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisDiscoveryEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisDiscoveryEndpointAutoConfiguration.class).getBeanDefinition());
//		Object polarisFeignBeanPostProcessor = ReflectionUtils.invokeConstructor(configClassCreator, PolarisFeignBeanPostProcessor.class, "polarisFeignBeanPostProcessor");
//		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisFeignBeanPostProcessor, Constant.DEFAULT_EXCLUSION_FILTER);
//		registry.registerBeanDefinition("polarisFeignBeanPostProcessor", BeanDefinitionBuilder.genericBeanDefinition(
//				PolarisFeignBeanPostProcessor.class).getBeanDefinition());
	}
}