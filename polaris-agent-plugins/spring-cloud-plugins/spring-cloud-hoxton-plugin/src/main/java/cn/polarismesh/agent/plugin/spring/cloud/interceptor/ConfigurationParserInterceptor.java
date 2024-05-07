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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.CircuitBreakerBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.CommonBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.ConfigBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.LoadbalancerBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.LosslessBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.PolarisContextBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RateLimitBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RegistryBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RouterBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RpcEnhancementBeanInjector;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class ConfigurationParserInterceptor implements Interceptor {

	private final List<BeanInjector> beanInjectors = new ArrayList<>();

	public ConfigurationParserInterceptor() {
		beanInjectors.add(new CommonBeanInjector());
		beanInjectors.add(new PolarisContextBeanInjector());
		beanInjectors.add(new RegistryBeanInjector());
		beanInjectors.add(new ConfigBeanInjector());
		beanInjectors.add(new RpcEnhancementBeanInjector());
		beanInjectors.add(new LosslessBeanInjector());
		beanInjectors.add(new LoadbalancerBeanInjector());
		beanInjectors.add(new RouterBeanInjector());
		beanInjectors.add(new CircuitBreakerBeanInjector());
		beanInjectors.add(new RateLimitBeanInjector());
	}


	private static boolean isMainBeanDefinition(BeanDefinitionHolder beanDefinitionHolder) {
		BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
		if (beanDefinition instanceof AnnotatedGenericBeanDefinition) {
			AnnotatedGenericBeanDefinition annotatedBeanDefinition = (AnnotatedGenericBeanDefinition)beanDefinition;
			Class<?> beanClass = annotatedBeanDefinition.getBeanClass();
			Annotation[] annotations = beanClass.getAnnotations();
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> aClass = annotation.annotationType();
				if ("org.springframework.boot.autoconfigure.SpringBootApplication".equals(aClass.getCanonicalName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		Set<?> candidates = (Set<?>) args[0];
		if (candidates.size() != 1) {
			return;
		}
		BeanDefinitionHolder beanDefinitionHolder = (BeanDefinitionHolder) candidates.iterator().next();
		if ("bootstrapImportSelectorConfiguration".equals(beanDefinitionHolder.getBeanName())) {
			// bootstrap
			Class<?> clazz = ClassUtils.getClazz("org.springframework.context.annotation.ConfigurationClass", null);
			Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz, Class.class, String.class);
			Method processConfigurationClass = ReflectionUtils.findMethod(target.getClass(), "processConfigurationClass", clazz, Predicate.class);
			ReflectionUtils.makeAccessible(processConfigurationClass);

			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ReflectionUtils.getObjectByFieldName(target, "registry");

			for (BeanInjector beanInjector : beanInjectors) {
				beanInjector.onBootstrapStartup(target, constructor, processConfigurationClass, registry);
			}

			// rpc


			// lossless

			// register
//			Object discoveryPropertiesBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, DiscoveryPropertiesBootstrapAutoConfiguration.class, "discoveryPropertiesBootstrapAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, discoveryPropertiesBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("discoveryPropertiesBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					DiscoveryPropertiesBootstrapAutoConfiguration.class).getBeanDefinition());

			// config
//			Object polarisConfigBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisConfigBootstrapAutoConfiguration.class, "polarisConfigBootstrapAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisConfigBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("polarisConfigBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					PolarisConfigBootstrapAutoConfiguration.class).getBeanDefinition());

		} else if (isMainBeanDefinition(beanDefinitionHolder)) {
			Class<?> clazz = ClassUtils.getClazz("org.springframework.context.annotation.ConfigurationClass", null);
			Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz, Class.class, String.class);
			Method processConfigurationClass = ReflectionUtils.findMethod(target.getClass(), "processConfigurationClass", clazz, Predicate.class);
			ReflectionUtils.makeAccessible(processConfigurationClass);

			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ReflectionUtils.getObjectByFieldName(target, "registry");

			for (BeanInjector beanInjector : beanInjectors) {
				beanInjector.onApplicationStartup(target, constructor, processConfigurationClass, registry);
			}


			// rpc


			// lossless

			// loadbalancer

			// router
//			Object routerAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, RouterAutoConfiguration.class, "routerAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, routerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("routerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					RouterAutoConfiguration.class).getBeanDefinition());
//			if (null != ClassUtils.getClazz("feign.RequestInterceptor",
//					Thread.currentThread().getContextClassLoader())) {
//				Object feignAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, FeignAutoConfiguration.class, "feignAutoConfiguration");
//				ReflectionUtils.invokeMethod(processConfigurationClass, target, feignAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//				registry.registerBeanDefinition("feignAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//						FeignAutoConfiguration.class).getBeanDefinition());
//			}

			// registry
//			Object discoveryPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, DiscoveryPropertiesAutoConfiguration.class, "discoveryPropertiesAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, discoveryPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("discoveryPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					DiscoveryPropertiesAutoConfiguration.class).getBeanDefinition());
//			Object polarisDiscoveryAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisDiscoveryAutoConfiguration.class, "polarisDiscoveryAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisDiscoveryAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("polarisDiscoveryAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					PolarisDiscoveryAutoConfiguration.class).getBeanDefinition());
//			Object polarisDiscoveryRibbonAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisDiscoveryRibbonAutoConfiguration.class, "polarisDiscoveryRibbonAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisDiscoveryRibbonAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("polarisDiscoveryRibbonAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					PolarisDiscoveryRibbonAutoConfiguration.class).getBeanDefinition());
//			Object polarisServiceRegistryAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisServiceRegistryAutoConfiguration.class, "polarisServiceRegistryAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisServiceRegistryAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("polarisServiceRegistryAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					PolarisServiceRegistryAutoConfiguration.class).getBeanDefinition());

			// config
//			Object polarisConfigAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisConfigAutoConfiguration.class, "polarisConfigAutoConfiguration");
//			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisConfigAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//			registry.registerBeanDefinition("polarisConfigAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//					PolarisConfigAutoConfiguration.class).getBeanDefinition());
		}
	}
}


