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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.DiscoveryPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.contract.config.PolarisContractPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.router.config.properties.PolarisNearByRouterProperties;
import com.tencent.cloud.polaris.DiscoveryPropertiesBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.router.config.properties.PolarisRuleBasedRouterProperties;
import com.tencent.cloud.polaris.router.config.FeignAutoConfiguration;
import com.tencent.cloud.polaris.router.config.RouterAutoConfiguration;
import com.tencent.cloud.polaris.router.endpoint.PolarisRouterEndpointAutoConfiguration;
import com.tencent.cloud.polaris.router.config.properties.PolarisMetadataRouterProperties;
import com.tencent.cloud.polaris.router.config.RouterConfigModifierAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.webclient.PolarisLoadBalancerClientRequestTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

public class RouterBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(RouterBeanInjector.class);
	@Override
	public String getModule() {
		return "spring-cloud-starter-tencent-polaris-router";
	}

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
	}


	@Override
	@SuppressWarnings("unchecked")
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
		if (null != ClassUtils.getClazz("feign.RequestInterceptor",
				Thread.currentThread().getContextClassLoader())) {
			Object feignAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, FeignAutoConfiguration.class, "feignAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, feignAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("feignAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					FeignAutoConfiguration.class).getBeanDefinition());
		}
		Object routerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, RouterAutoConfiguration.class, "routerAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, routerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("routerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				RouterAutoConfiguration.class).getBeanDefinition());
		Object polarisNearByRouterProperties = ReflectionUtils.invokeConstructor(configClassCreator, PolarisNearByRouterProperties.class, "polarisNearByRouterProperties");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisNearByRouterProperties, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisNearByRouterProperties", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisNearByRouterProperties.class).getBeanDefinition());
		Object polarisMetadataRouterProperties = ReflectionUtils.invokeConstructor(configClassCreator, PolarisMetadataRouterProperties.class, "polarisMetadataRouterProperties");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisMetadataRouterProperties, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisMetadataRouterProperties", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisMetadataRouterProperties.class).getBeanDefinition());
		Object polarisRuleBasedRouterProperties = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRuleBasedRouterProperties.class, "polarisRuleBasedRouterProperties");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRuleBasedRouterProperties, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisRuleBasedRouterProperties", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisRuleBasedRouterProperties.class).getBeanDefinition());
		Object routerConfigModifierAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, RouterConfigModifierAutoConfiguration.class, "routerConfigModifierAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, routerConfigModifierAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("routerConfigModifierAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				RouterConfigModifierAutoConfiguration.class).getBeanDefinition());
		Object polarisContractPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContractPropertiesAutoConfiguration.class, "polarisContractPropertiesAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContractPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContractPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContractPropertiesAutoConfiguration.class).getBeanDefinition());
	}
}
