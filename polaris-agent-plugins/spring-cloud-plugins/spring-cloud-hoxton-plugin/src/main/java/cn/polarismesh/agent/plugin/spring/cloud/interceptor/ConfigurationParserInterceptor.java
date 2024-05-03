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
import java.util.Set;
import java.util.function.Predicate;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import com.tencent.cloud.common.metadata.config.MetadataAutoConfiguration;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.cloud.plugin.lossless.config.LosslessAutoConfiguration;
import com.tencent.cloud.plugin.lossless.config.LosslessPropertiesAutoConfiguration;
import com.tencent.cloud.plugin.lossless.config.LosslessPropertiesBootstrapConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextPostConfiguration;
import com.tencent.cloud.polaris.loadbalancer.config.PolarisLoadBalancerAutoConfiguration;
import com.tencent.cloud.polaris.router.config.FeignAutoConfiguration;
import com.tencent.cloud.polaris.router.config.RouterAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementBootstrapConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatPropertiesBootstrapConfiguration;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class ConfigurationParserInterceptor implements Interceptor {

	private static final Predicate<String> DEFAULT_EXCLUSION_FILTER = className ->
			(className.startsWith("java.lang.annotation.") || className.startsWith("org.springframework.stereotype."));

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

			Object polarisContextBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisContextBootstrapAutoConfiguration.class, "polarisContextBootstrapAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisContextBootstrapAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisContextBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisContextBootstrapAutoConfiguration.class).getBeanDefinition());

			// rpc
			Object polarisStatPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisStatPropertiesBootstrapConfiguration.class, "polarisStatPropertiesBootstrapConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisStatPropertiesBootstrapConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisStatPropertiesBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisStatPropertiesAutoConfiguration.class).getBeanDefinition());
			Object rpcEnhancementBootstrapConfiguration = ReflectionUtils.invokeConstructor(constructor, RpcEnhancementBootstrapConfiguration.class, "rpcEnhancementBootstrapConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, rpcEnhancementBootstrapConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("rpcEnhancementBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					RpcEnhancementBootstrapConfiguration.class).getBeanDefinition());

			// lossless
			Object losslessPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, LosslessPropertiesAutoConfiguration.class, "losslessPropertiesAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, losslessPropertiesAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("losslessPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					LosslessPropertiesAutoConfiguration.class).getBeanDefinition());
			Object losslessPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(constructor, LosslessPropertiesBootstrapConfiguration.class, "losslessPropertiesBootstrapConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, losslessPropertiesBootstrapConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("losslessPropertiesBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					LosslessPropertiesBootstrapConfiguration.class).getBeanDefinition());

		} else if (isMainBeanDefinition(beanDefinitionHolder)) {
			Class<?> clazz = ClassUtils.getClazz("org.springframework.context.annotation.ConfigurationClass", null);
			Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz, Class.class, String.class);
			Method processConfigurationClass = ReflectionUtils.findMethod(target.getClass(), "processConfigurationClass", clazz, Predicate.class);
			ReflectionUtils.makeAccessible(processConfigurationClass);

			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ReflectionUtils.getObjectByFieldName(target, "registry");

			// sct common
			Object applicationContextAwareUtils = ReflectionUtils.invokeConstructor(constructor, ApplicationContextAwareUtils.class, "applicationContextAwareUtils");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, applicationContextAwareUtils, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("applicationContextAwareUtils", BeanDefinitionBuilder.genericBeanDefinition(
					ApplicationContextAwareUtils.class).getBeanDefinition());
			Object metadataAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, MetadataAutoConfiguration.class, "metadataAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, metadataAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("metadataAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					MetadataAutoConfiguration.class).getBeanDefinition());

			// polaris-context
			Object polarisContextAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisContextAutoConfiguration.class, "polarisContextAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisContextAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisContextAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisContextAutoConfiguration.class).getBeanDefinition());
			Object polarisContextPostConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisContextPostConfiguration.class, "polarisContextPostConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisContextPostConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisContextPostConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisContextPostConfiguration.class).getBeanDefinition());

			// rpc
			Object polarisStatPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisStatPropertiesAutoConfiguration.class, "polarisStatPropertiesAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisStatPropertiesAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisStatPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisStatPropertiesBootstrapConfiguration.class).getBeanDefinition());
			Object rpcEnhancementAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, RpcEnhancementAutoConfiguration.class, "rpcEnhancementAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, rpcEnhancementAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("rpcEnhancementAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					RpcEnhancementAutoConfiguration.class).getBeanDefinition());

			// lossless
			Object losslessAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, LosslessAutoConfiguration.class, "losslessAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, losslessAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("losslessAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					LosslessAutoConfiguration.class).getBeanDefinition());

			// loadbalancer
			Object polarisLoadBalancerAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, PolarisLoadBalancerAutoConfiguration.class, "polarisLoadBalancerAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, polarisLoadBalancerAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisLoadBalancerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisLoadBalancerAutoConfiguration.class).getBeanDefinition());

			// router
			Object routerAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, RouterAutoConfiguration.class, "routerAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, target, routerAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("routerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					RouterAutoConfiguration.class).getBeanDefinition());
			if (null != ClassUtils.getClazz("feign.RequestInterceptor",
					Thread.currentThread().getContextClassLoader())) {
				Object feignAutoConfiguration = ReflectionUtils.invokeConstructor(constructor, FeignAutoConfiguration.class, "feignAutoConfiguration");
				ReflectionUtils.invokeMethod(processConfigurationClass, target, feignAutoConfiguration, DEFAULT_EXCLUSION_FILTER);
				registry.registerBeanDefinition("feignAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
						FeignAutoConfiguration.class).getBeanDefinition());
			}

		}
	}
}


