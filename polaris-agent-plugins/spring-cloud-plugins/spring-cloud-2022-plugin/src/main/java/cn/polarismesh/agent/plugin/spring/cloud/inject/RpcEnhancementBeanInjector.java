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
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementBootstrapConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesBootstrapConfiguration;
import com.tencent.cloud.rpc.enhancement.transformer.InstanceTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class RpcEnhancementBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcEnhancementBeanInjector.class);

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-tencent-rpc-enhancement";
	}

	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		Object polarisStatPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisStatPropertiesBootstrapConfiguration.class, "polarisStatPropertiesBootstrapConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisStatPropertiesBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisStatPropertiesBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisStatPropertiesBootstrapConfiguration.class).getBeanDefinition());
		Object rpcEnhancementBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, RpcEnhancementBootstrapConfiguration.class, "rpcEnhancementBootstrapConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, rpcEnhancementBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("rpcEnhancementBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				RpcEnhancementBootstrapConfiguration.class).getBeanDefinition());
		Object polarisStatPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisStatPropertiesAutoConfiguration.class, "polarisStatPropertiesAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisStatPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisStatPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisStatPropertiesAutoConfiguration.class).getBeanDefinition());
		Object rpcEnhancementAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, RpcEnhancementAutoConfiguration.class, "rpcEnhancementAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, rpcEnhancementAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("rpcEnhancementAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				RpcEnhancementAutoConfiguration.class).getBeanDefinition());
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}
}
