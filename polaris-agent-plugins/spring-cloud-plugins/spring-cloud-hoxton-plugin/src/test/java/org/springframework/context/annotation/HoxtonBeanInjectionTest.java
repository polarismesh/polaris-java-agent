/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package org.springframework.context.annotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.polarismesh.agent.plugin.spring.cloud.interceptor.ConfigurationParserInterceptor;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatProperties;
import org.junit.Test;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Exercises injection with the actual Spring 5.2 parser and bean definition reader.
 */
public class HoxtonBeanInjectionTest {

	@Test
	public void registersStatisticsWithoutRpcEnhancement() {
		DefaultListableBeanFactory registry = inject(true, false, false);
		assertTrue(registry.containsBeanDefinition("polarisServiceRegistry"));
		assertTrue(registry.containsBeanDefinition("spring.cloud.polaris.stat-" + PolarisStatProperties.class.getName()));
		assertFalse(registry.containsBeanDefinition("rpcEnhancementAutoConfiguration"));
	}

	@Test
	public void registersStatisticsInBootstrapWithoutRpcEnhancement() {
		DefaultListableBeanFactory registry = inject(true, false, true);
		assertTrue(registry.containsBeanDefinition("polarisStatPropertiesBootstrapConfiguration"));
		assertTrue(registry.containsBeanDefinition("spring.cloud.polaris.stat-" + PolarisStatProperties.class.getName()));
		assertFalse(registry.containsBeanDefinition("rpcEnhancementBootstrapConfiguration"));
	}

	@Test
	public void stillRegistersRpcEnhancementWhenEnabled() {
		DefaultListableBeanFactory registry = inject(true, true, false);
		assertTrue(registry.containsBeanDefinition("rpcEnhancementAutoConfiguration"));
		assertTrue(registry.containsBeanDefinition("spring.cloud.polaris.stat-" + PolarisStatProperties.class.getName()));
	}

	@Test
	public void skipsStatisticsWhenPolarisDisabled() {
		DefaultListableBeanFactory registry = inject(false, false, false);
		assertFalse(registry.containsBeanDefinition("polarisStatPropertiesAutoConfiguration"));
		assertFalse(registry.containsBeanDefinition("spring.cloud.polaris.stat-" + PolarisStatProperties.class.getName()));
	}

	private DefaultListableBeanFactory inject(boolean polarisEnabled, boolean rpcEnabled, boolean bootstrap) {
		DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
		StandardEnvironment environment = new StandardEnvironment();
		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.application.name", "hoxton-injection-test");
		properties.put("spring.cloud.polaris.enabled", polarisEnabled);
		properties.put("spring.cloud.polaris.discovery.enabled", true);
		properties.put("spring.cloud.polaris.config.enabled", false);
		properties.put("spring.cloud.tencent.rpc-enhancement.enabled", rpcEnabled);
		properties.put("spring.cloud.polaris.stat.enabled", false);
		environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		ConfigurationClassParser parser = new ConfigurationClassParser(new CachingMetadataReaderFactory(),
				new FailFastProblemReporter(), environment, resourceLoader, new AnnotationBeanNameGenerator(), registry);
		BeanDefinitionHolder candidate = new BeanDefinitionHolder(new AnnotatedGenericBeanDefinition(
				bootstrap ? BootstrapConfiguration.class : TestApplication.class),
				bootstrap ? "bootstrapImportSelectorConfiguration" : "testApplication");
		registry.registerBeanDefinition("autoServiceRegistrationProperties",
				BeanDefinitionBuilder.genericBeanDefinition(AutoServiceRegistrationProperties.class).getBeanDefinition());
		new ConfigurationParserInterceptor().after(parser, new Object[] {Collections.singleton(candidate)}, null, null);
		ConfigurationClassBeanDefinitionReader reader = new ConfigurationClassBeanDefinitionReader(registry,
				new PassThroughSourceExtractor(), resourceLoader, environment, new AnnotationBeanNameGenerator(),
				parser.getImportRegistry());
		reader.loadBeanDefinitions(parser.getConfigurationClasses());
		return registry;
	}

	@SpringBootApplication(scanBasePackages = "hoxton.injection.test.empty")
	static class TestApplication {
	}

	@Configuration(proxyBeanMethods = false)
	static class BootstrapConfiguration {
	}
}
