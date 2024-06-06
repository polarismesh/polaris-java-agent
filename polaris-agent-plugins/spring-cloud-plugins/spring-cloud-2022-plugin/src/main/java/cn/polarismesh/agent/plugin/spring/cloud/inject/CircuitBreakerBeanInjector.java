package cn.polarismesh.agent.plugin.spring.cloud.inject;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.circuitbreaker.config.*;
import com.tencent.cloud.polaris.circuitbreaker.endpoint.PolarisCircuitBreakerEndpointAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.cloud.openfeign.PolarisFeignCircuitBreakerTargeterAutoConfiguration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class CircuitBreakerBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerBeanInjector.class);

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-starter-tencent-polaris-circuitbreaker";
	}

	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		Object polarisCircuitBreakerEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerEndpointAutoConfiguration.class, "polarisCircuitBreakerEndpointAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisCircuitBreakerEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisCircuitBreakerEndpointAutoConfiguration.class).getBeanDefinition());
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}
}

