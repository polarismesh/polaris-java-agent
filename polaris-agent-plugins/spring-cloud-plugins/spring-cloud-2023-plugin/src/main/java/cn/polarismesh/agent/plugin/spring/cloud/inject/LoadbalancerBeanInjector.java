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

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.con.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.con.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.con.Utils;
import com.tencent.cloud.polaris.loadbalancer.PolarisLoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

public class LoadbalancerBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalancerBeanInjector.class);
	@Override
	public String getModule() {
		return "spring-cloud-tencent-polaris-loadbalancer";
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
//		if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.loadbalancer.enabled"))) {
//			LOGGER.warn("[PolarisJavaAgent] polaris loadbalancer not enabled, skip inject application bean definitions for module {}", getModule());
//			return;
//		}
		Object polarisLoadBalancerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisLoadBalancerAutoConfiguration.class, "polarisLoadBalancerAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisLoadBalancerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisLoadBalancerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisLoadBalancerAutoConfiguration.class).getBeanDefinition());

		// make LoadBalancerAutoConfiguration later
		Map<Object, Object> configurationClasses =  (Map<Object, Object>) ReflectionUtils.getObjectByFieldName(configurationParser, "configurationClasses");
		Object targetConfigClass = null;
		for (Object configClass : configurationClasses.keySet()) {
			Object resource = ReflectionUtils.getObjectByFieldName(configClass, "resource");
			if (resource instanceof ClassPathResource) {
				ClassPathResource classPathResource = (ClassPathResource) resource;
				if ("loadBalancerInterceptor".equals(classPathResource.getPath())) {
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
