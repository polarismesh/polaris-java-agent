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
import java.util.concurrent.atomic.AtomicBoolean;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.config.PolarisConfigAutoConfiguration;
import com.tencent.cloud.polaris.config.PolarisConfigBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.config.endpoint.PolarisConfigEndpointAutoConfiguration;
import com.tencent.cloud.polaris.contract.config.PolarisContractPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.contract.config.PolarisContractProperties;
import com.tencent.cloud.polaris.contract.config.PolarisSwaggerAutoConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class ConfigBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBeanInjector.class);

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-starter-tencent-polaris-config";
	}

	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
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
		Object polarisContractProperties = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContractProperties.class, "polarisContractProperties");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContractProperties, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContractProperties", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContractProperties.class).getBeanDefinition());
		Object polarisConfigBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisConfigBootstrapAutoConfiguration.class, "polarisConfigBootstrapAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisConfigBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisConfigBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisConfigBootstrapAutoConfiguration.class).getBeanDefinition());
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}
}
