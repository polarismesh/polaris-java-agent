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

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import com.tencent.cloud.common.metadata.config.MetadataAutoConfiguration;
import com.tencent.cloud.common.metadata.endpoint.PolarisMetadataEndpointAutoConfiguration;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.cloud.polaris.DiscoveryPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.DiscoveryPropertiesBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.config.PolarisConfigBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.contract.config.PolarisContractPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.router.config.FeignAutoConfiguration;
import com.tencent.cloud.polaris.router.config.RouterAutoConfiguration;
import com.tencent.cloud.polaris.router.config.RouterConfigModifierAutoConfiguration;
import com.tencent.cloud.polaris.router.config.properties.PolarisMetadataRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisNearByRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisRuleBasedRouterProperties;
import com.tencent.cloud.polaris.router.endpoint.PolarisRouterEndpointAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class CommonBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonBeanInjector.class);

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-tencent-commons";
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		Object metadataAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, MetadataAutoConfiguration.class, "metadataAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, metadataAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("metadataAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				MetadataAutoConfiguration.class).getBeanDefinition());
		Object polarisMetadataEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisMetadataEndpointAutoConfiguration.class, "polarisMetadataEndpointAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisMetadataEndpointAutoConfiguration,Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisMetadataEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisMetadataEndpointAutoConfiguration.class).getBeanDefinition());
		Object applicationContextAwareUtils = ReflectionUtils.invokeConstructor(configClassCreator, ApplicationContextAwareUtils.class, "applicationContextAwareUtils");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, applicationContextAwareUtils, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("applicationContextAwareUtils", BeanDefinitionBuilder.genericBeanDefinition(
				ApplicationContextAwareUtils.class).getBeanDefinition());
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}


	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
	}
}
