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

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.router.config.FeignAutoConfiguration;
import com.tencent.cloud.polaris.router.config.RouterAutoConfiguration;
import com.tencent.cloud.polaris.router.endpoint.PolarisRouterEndpointAutoConfiguration;
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

	@Override
	@SuppressWarnings("unchecked")
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.router.enabled"))) {
			LOGGER.warn("[PolarisJavaAgent] polaris router not enabled, skip inject application bean definitions for module {}", getModule());
			return;
		}
		Object routerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, RouterAutoConfiguration.class, "routerAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, routerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("routerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				RouterAutoConfiguration.class).getBeanDefinition());
		if (null != ClassUtils.getClazz("feign.RequestInterceptor",
					Thread.currentThread().getContextClassLoader())) {
		Object feignAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, FeignAutoConfiguration.class, "feignAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, feignAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("feignAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				FeignAutoConfiguration.class).getBeanDefinition());
		}
		Object polarisRouterEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRouterEndpointAutoConfiguration.class, "polarisRouterEndpointAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRouterEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisRouterEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisRouterEndpointAutoConfiguration.class).getBeanDefinition());

		// make FeignRibbonClientAutoConfiguration later
		Map<Object, Object> configurationClasses =  (Map<Object, Object>) ReflectionUtils.getObjectByFieldName(configurationParser, "configurationClasses");
		Object targetConfigClass = null;
		for (Object configClass : configurationClasses.keySet()) {
			Object resource = ReflectionUtils.getObjectByFieldName(configClass, "resource");
			if (resource instanceof ClassPathResource) {
				ClassPathResource classPathResource = (ClassPathResource) resource;
				if ("org/springframework/cloud/openfeign/ribbon/FeignRibbonClientAutoConfiguration.class".equals(classPathResource.getPath())) {
					targetConfigClass = configurationClasses.remove(configClass);
					break;
				}
			}
		}
		if (null != targetConfigClass) {
			configurationClasses.put(targetConfigClass, targetConfigClass);
		}
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}
}
