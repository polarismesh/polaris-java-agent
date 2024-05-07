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

import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextPostConfiguration;
import com.tencent.cloud.polaris.context.logging.PolarisLoggingApplicationListener;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class PolarisContextBeanInjector implements BeanInjector {
	@Override
	public void onBootstrapStartup(Object configurationParser,
			Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
		Object polarisContextBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextBootstrapAutoConfiguration.class, "polarisContextBootstrapAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContextBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContextBootstrapAutoConfiguration.class).getBeanDefinition());


	}

	@Override
	public void onApplicationStartup(Object configurationParser,
			Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
		Object polarisContextAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextAutoConfiguration.class, "polarisContextAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContextAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContextAutoConfiguration.class).getBeanDefinition());
		Object polarisContextPostConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextPostConfiguration.class, "polarisContextPostConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextPostConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisContextPostConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisContextPostConfiguration.class).getBeanDefinition());
		Object polarisLoggingApplicationListener = ReflectionUtils.invokeConstructor(configClassCreator, PolarisLoggingApplicationListener.class, "polarisLoggingApplicationListener");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisLoggingApplicationListener, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisLoggingApplicationListener", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisLoggingApplicationListener.class).getBeanDefinition());
//		Object polarisSDKContextManager = ReflectionUtils.invokeConstructor(configClassCreator, PolarisSDKContextManager.class, "polarisSDKContextManager");
//		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisSDKContextManager, Constant.DEFAULT_EXCLUSION_FILTER);
//		registry.registerBeanDefinition("polarisSDKContextManager", BeanDefinitionBuilder.genericBeanDefinition(
//				PolarisSDKContextManager.class).getBeanDefinition());
	}
}
