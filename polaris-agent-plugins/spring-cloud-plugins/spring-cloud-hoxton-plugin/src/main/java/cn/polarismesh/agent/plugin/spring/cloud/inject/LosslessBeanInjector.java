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
import com.tencent.cloud.plugin.lossless.config.LosslessAutoConfiguration;
import com.tencent.cloud.plugin.lossless.config.LosslessPropertiesAutoConfiguration;
import com.tencent.cloud.plugin.lossless.config.LosslessPropertiesBootstrapConfiguration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class LosslessBeanInjector implements BeanInjector {
	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
		Object losslessPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, LosslessPropertiesBootstrapConfiguration.class, "losslessPropertiesBootstrapConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, losslessPropertiesBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("losslessPropertiesBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				LosslessPropertiesBootstrapConfiguration.class).getBeanDefinition());
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
		Object losslessPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, LosslessPropertiesAutoConfiguration.class, "losslessPropertiesAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, losslessPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("losslessPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				LosslessPropertiesAutoConfiguration.class).getBeanDefinition());
		Object losslessAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, LosslessAutoConfiguration.class, "losslessAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, losslessAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("losslessAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				LosslessAutoConfiguration.class).getBeanDefinition());
	}
}
