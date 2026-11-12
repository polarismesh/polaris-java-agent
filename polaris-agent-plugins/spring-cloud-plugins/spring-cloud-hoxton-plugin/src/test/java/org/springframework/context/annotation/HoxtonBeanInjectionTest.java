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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.polarismesh.agent.plugin.spring.cloud.interceptor.ConfigurationParserInterceptor;
import com.tencent.cloud.common.metadata.MetadataContextHolder;
import com.tencent.cloud.polaris.extend.nacos.NacosContextProperties;
import com.tencent.cloud.polaris.registry.PolarisRegistration;
import com.tencent.cloud.polaris.registry.nacos.NacosPolarisRegistrationCustomizer;
import com.tencent.cloud.polaris.router.instrument.feign.PolarisCachingSpringLoadBalanceFactory;
import com.tencent.cloud.polaris.router.instrument.resttemplate.RouterLabelRestTemplateInterceptor;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatProperties;
import com.tencent.polaris.metadata.core.MessageMetadataContainer;
import com.tencent.polaris.metadata.core.MetadataType;
import org.junit.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import static com.tencent.polaris.plugins.connector.common.constant.NacosConstant.MetadataMapKey.NACOS_CLUSTER_KEY;
import static com.tencent.polaris.plugins.connector.common.constant.NacosConstant.MetadataMapKey.NACOS_GROUP_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
		assertFalse(registry.containsBeanDefinition("feignEagerLoadSmartLifecycle"));
		assertFalse(registry.containsBeanDefinition("serviceEagerLoadSmartLifecycle"));
		assertFalse(registry.containsBeanDefinition("nacosPolarisRegistrationCustomizer"));
	}

	@Test
	public void registersEagerLoadingAndNacosCustomizationWithoutRpcEnhancement() {
		DefaultListableBeanFactory registry = inject(true, false, false);
		assertTrue(registry.containsBeanDefinition("feignEagerLoadSmartLifecycle"));
		assertTrue(registry.containsBeanDefinition("serviceEagerLoadSmartLifecycle"));
		assertTrue(registry.containsBeanDefinition("nacosPolarisRegistrationCustomizer"));
	}

	@Test
	public void honorsEagerLoadingDisabled() {
		DefaultListableBeanFactory registry = inject(true, false, false,
				Collections.singletonMap("spring.cloud.polaris.discovery.eager-load.enabled", false));
		assertFalse(registry.containsBeanDefinition("feignEagerLoadSmartLifecycle"));
		assertFalse(registry.containsBeanDefinition("serviceEagerLoadSmartLifecycle"));
		assertTrue(registry.containsBeanDefinition("nacosPolarisRegistrationCustomizer"));
	}

	@Test
	public void customizesNacosGroupAndClusterMetadata() {
		DefaultListableBeanFactory registry = inject(true, false, false);
		NacosContextProperties properties = registry.getBean("nacosContextProperties", NacosContextProperties.class);
		properties.setGroup("hoxton-group");
		properties.setClusterName("hoxton-cluster");
		NacosPolarisRegistrationCustomizer customizer = registry.getBean("nacosPolarisRegistrationCustomizer",
				NacosPolarisRegistrationCustomizer.class);
		Map<String, String> metadata = new HashMap<>();
		PolarisRegistration registration = mock(PolarisRegistration.class);
		when(registration.getMetadata()).thenReturn(metadata);
		customizer.customize(registration);
		assertEquals("hoxton-group", metadata.get(NACOS_GROUP_KEY));
		assertEquals("hoxton-cluster", metadata.get(NACOS_CLUSTER_KEY));
	}

	@Test
	public void registersRouterBeforeApplicationLoadBalancerConfiguration() {
		DefaultListableBeanFactory registry = inject(true, false, false,
				Collections.singletonMap("spring.cloud.polaris.router.enabled", true), RoutingApplication.class);
		List<String> beanNames = Arrays.asList(registry.getBeanDefinitionNames());
		assertTrue(beanNames.contains("addRouterLabelInterceptorForRestTemplate"));
		assertTrue(beanNames.contains("loadBalancedRestTemplateInitializerDeprecated"));
		assertTrue(beanNames.indexOf("addRouterLabelInterceptorForRestTemplate")
				< beanNames.indexOf("loadBalancedRestTemplateInitializerDeprecated"));
		assertEquals(1, registry.getBeanNamesForType(PolarisCachingSpringLoadBalanceFactory.class, true, false).length);
	}

	@Test
	public void skipsRouterWhenDisabledDuringFullParsingLifecycle() {
		DefaultListableBeanFactory registry = inject(true, false, false,
				Collections.singletonMap("spring.cloud.polaris.router.enabled", false), RoutingApplication.class);
		assertFalse(registry.containsBeanDefinition("routerAutoConfiguration"));
		assertFalse(registry.containsBeanDefinition("routerConfigModifierAutoConfiguration"));
		assertFalse(registry.containsBeanDefinition("routerLabelRestTemplateInterceptor"));
		assertTrue(registry.containsBeanDefinition("loadBalancedRestTemplateInitializerDeprecated"));
	}

	@Test
	public void skipsRouterWhenPolarisDisabledDuringFullParsingLifecycle() {
		DefaultListableBeanFactory registry = inject(false, false, false,
				Collections.singletonMap("spring.cloud.polaris.router.enabled", true), RoutingApplication.class);
		assertFalse(registry.containsBeanDefinition("routerAutoConfiguration"));
		assertFalse(registry.containsBeanDefinition("routerLabelRestTemplateInterceptor"));
	}

	@Test
	public void makesHeaderAvailableBeforeRestTemplateInstanceSelection() throws Exception {
		DefaultListableBeanFactory registry = inject(true, false, false,
				Collections.singletonMap("spring.cloud.polaris.router.enabled", true), RoutingApplication.class);
		RestTemplate template = new RestTemplate();
		AtomicBoolean selected = new AtomicBoolean();
		ClientHttpResponse response = new ClientHttpResponse() {
			public HttpStatus getStatusCode() { return HttpStatus.OK; }
			public int getRawStatusCode() { return 200; }
			public String getStatusText() { return "OK"; }
			public void close() { }
			public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
			public HttpHeaders getHeaders() { return new HttpHeaders(); }
		};
		LoadBalancerClient client = new LoadBalancerClient() {
			public ServiceInstance choose(String serviceId) { return null; }
			public URI reconstructURI(ServiceInstance instance, URI original) { return original; }
			@SuppressWarnings("unchecked")
			public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
				assertEquals("provider", serviceId);
				assertEquals("blue", MetadataContextHolder.get().getMetadataContainer(MetadataType.MESSAGE, false)
						.getRawMetadataMapValue(MessageMetadataContainer.LABEL_MAP_KEY_HEADER, "test"));
				selected.set(true);
				return (T) response;
			}
			public <T> T execute(String serviceId, ServiceInstance instance, LoadBalancerRequest<T> request) throws IOException {
				return execute(serviceId, request);
			}
		};

		Object routerConfig = configurationWithRestTemplates(
				"com.tencent.cloud.polaris.router.config.RouterAutoConfiguration$RouterLabelRestTemplateConfig", template);
		Method addRouter = routerConfig.getClass().getDeclaredMethod("addRouterLabelInterceptorForRestTemplate",
				RouterLabelRestTemplateInterceptor.class);
		addRouter.setAccessible(true);
		SmartInitializingSingleton routerInitializer = (SmartInitializingSingleton) addRouter.invoke(routerConfig,
				new RouterLabelRestTemplateInterceptor());

		LoadBalancerAutoConfiguration loadBalancerConfig = (LoadBalancerAutoConfiguration) configurationWithRestTemplates(
				LoadBalancerAutoConfiguration.class.getName(), template);
		Class<?> customizerConfig = Class.forName(
				"org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration$LoadBalancerInterceptorConfig");
		Constructor<?> constructor = customizerConfig.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method customize = customizerConfig.getDeclaredMethod("restTemplateCustomizer", LoadBalancerInterceptor.class);
		customize.setAccessible(true);
		RestTemplateCustomizer customizer = (RestTemplateCustomizer) customize.invoke(constructor.newInstance(),
				new LoadBalancerInterceptor(client));
		ObjectProvider<List<RestTemplateCustomizer>> customizers = new ObjectProvider<List<RestTemplateCustomizer>>() {
			public List<RestTemplateCustomizer> getObject(Object... args) { return getObject(); }
			public List<RestTemplateCustomizer> getObject() { return Collections.singletonList(customizer); }
			public List<RestTemplateCustomizer> getIfAvailable() { return getObject(); }
			public List<RestTemplateCustomizer> getIfUnique() { return getObject(); }
		};
		SmartInitializingSingleton loadBalancerInitializer = loadBalancerConfig
				.loadBalancedRestTemplateInitializerDeprecated(customizers);

		// Run the real Spring/SCT callbacks in the order produced by before -> parse -> after.
		DefaultListableBeanFactory callbackRegistry = new DefaultListableBeanFactory();
		for (String name : registry.getBeanDefinitionNames()) {
			if ("addRouterLabelInterceptorForRestTemplate".equals(name)) {
				callbackRegistry.registerBeanDefinition(name, BeanDefinitionBuilder
						.genericBeanDefinition(SmartInitializingSingleton.class, () -> routerInitializer).getBeanDefinition());
			}
			else if ("loadBalancedRestTemplateInitializerDeprecated".equals(name)) {
				callbackRegistry.registerBeanDefinition(name, BeanDefinitionBuilder
						.genericBeanDefinition(SmartInitializingSingleton.class, () -> loadBalancerInitializer).getBeanDefinition());
			}
		}
		callbackRegistry.preInstantiateSingletons();
		assertEquals(2, template.getInterceptors().size());
		assertTrue(template.getInterceptors().get(0) instanceof RouterLabelRestTemplateInterceptor);
		assertTrue(template.getInterceptors().get(1) instanceof LoadBalancerInterceptor);
		MetadataContextHolder.remove();
		try {
			template.execute("http://provider/echo", HttpMethod.GET,
					request -> request.getHeaders().set("test", "blue"), ignored -> null);
			assertTrue(selected.get());
		}
		finally {
			MetadataContextHolder.remove();
		}
	}

	private Object configurationWithRestTemplates(String className, RestTemplate template) throws Exception {
		Class<?> configurationClass = Class.forName(className);
		Constructor<?> constructor = configurationClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object configuration = constructor.newInstance();
		Field restTemplates = configurationClass.getDeclaredField("restTemplates");
		restTemplates.setAccessible(true);
		restTemplates.set(configuration, Collections.singletonList(template));
		return configuration;
	}

	private DefaultListableBeanFactory inject(boolean polarisEnabled, boolean rpcEnabled, boolean bootstrap) {
		return inject(polarisEnabled, rpcEnabled, bootstrap, Collections.emptyMap());
	}

	private DefaultListableBeanFactory inject(boolean polarisEnabled, boolean rpcEnabled, boolean bootstrap,
			Map<String, Object> overrides) {
		return inject(polarisEnabled, rpcEnabled, bootstrap, overrides,
				bootstrap ? BootstrapConfiguration.class : TestApplication.class);
	}

	private DefaultListableBeanFactory inject(boolean polarisEnabled, boolean rpcEnabled, boolean bootstrap,
			Map<String, Object> overrides, Class<?> applicationClass) {
		DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
		registry.setAllowBeanDefinitionOverriding(false);
		StandardEnvironment environment = new StandardEnvironment();
		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.application.name", "hoxton-injection-test");
		properties.put("spring.cloud.polaris.enabled", polarisEnabled);
		properties.put("spring.cloud.polaris.discovery.enabled", true);
		properties.put("spring.cloud.polaris.config.enabled", false);
		properties.put("spring.cloud.tencent.rpc-enhancement.enabled", rpcEnabled);
		properties.put("spring.cloud.polaris.stat.enabled", false);
		properties.put("spring.boot.enableautoconfiguration", false);
		properties.putAll(overrides);
		environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		ConfigurationClassParser parser = new ConfigurationClassParser(new CachingMetadataReaderFactory(),
				new FailFastProblemReporter(), environment, resourceLoader, new AnnotationBeanNameGenerator(), registry);
		BeanDefinitionHolder candidate = new BeanDefinitionHolder(new AnnotatedGenericBeanDefinition(
				applicationClass),
				bootstrap ? "bootstrapImportSelectorConfiguration" : "testApplication");
		registry.registerBeanDefinition("autoServiceRegistrationProperties",
				BeanDefinitionBuilder.genericBeanDefinition(AutoServiceRegistrationProperties.class).getBeanDefinition());
		registry.registerBeanDefinition("testLoadBalancerClient",
				BeanDefinitionBuilder.genericBeanDefinition(LoadBalancerClient.class).getBeanDefinition());
		ConfigurationParserInterceptor interceptor = new ConfigurationParserInterceptor();
		Object[] args = {Collections.singleton(candidate)};
		interceptor.before(parser, args);
		parser.parse(Collections.singleton(candidate));
		interceptor.after(parser, args, null, null);
		ConfigurationClassBeanDefinitionReader reader = new ConfigurationClassBeanDefinitionReader(registry,
				new PassThroughSourceExtractor(), resourceLoader, environment, new AnnotationBeanNameGenerator(),
				parser.getImportRegistry());
		reader.loadBeanDefinitions(parser.getConfigurationClasses());
		return registry;
	}

	@SpringBootApplication(scanBasePackages = "hoxton.injection.test.empty")
	static class TestApplication {
	}

	@SpringBootApplication(scanBasePackages = "hoxton.injection.test.empty")
	@Import({LoadBalancerAutoConfiguration.class, FeignRibbonClientAutoConfiguration.class})
	static class RoutingApplication {
	}

	@Configuration(proxyBeanMethods = false)
	static class BootstrapConfiguration {
	}
}
