package cn.polarismesh.agent.plugin.spring.cloud.inject;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.circuitbreaker.config.GatewayPolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerBootstrapConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerFeignClientAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.endpoint.PolarisCircuitBreakerEndpointAutoConfiguration;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.openfeign.PolarisFeignCircuitBreakerTargeterAutoConfiguration;
import org.springframework.core.env.Environment;

public class CircuitBreakerBeanInjector implements BeanInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerBeanInjector.class);

	private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

	@Override
	public String getModule() {
		return "spring-cloud-starter-tencent-polaris-circuitbreaker";
	}

	@Override
	public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.circuitbreaker.enabled"))) {
			LOGGER.warn("[PolarisJavaAgent] polaris circuitbreaker not enabled, skip inject bootstrap bean definitions for module {}", getModule());
			return;
		}
		bootstrapLoaded.set(true);
		Object polarisCircuitBreakerBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerBootstrapConfiguration.class, "polarisCircuitBreakerBootstrapConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisCircuitBreakerBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisCircuitBreakerBootstrapConfiguration.class).getBeanDefinition());
		if (null != ClassUtils.getClazz("org.springframework.cloud.openfeign.Targeter",
				Thread.currentThread().getContextClassLoader())) {
			String property = environment.getProperty("feign.hystrix.enabled");
			if (Boolean.parseBoolean(property)) {
				Object polarisFeignCircuitBreakerTargeterAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisFeignCircuitBreakerTargeterAutoConfiguration.class, "polarisFeignCircuitBreakerTargeterAutoConfiguration");
				ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisFeignCircuitBreakerTargeterAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
				registry.registerBeanDefinition("polarisFeignCircuitBreakerTargeterAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
						PolarisFeignCircuitBreakerTargeterAutoConfiguration.class).getBeanDefinition());
			}
		}
		LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
	}

	@Override
	public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
		if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.circuitbreaker.enabled"))) {
			LOGGER.warn("[PolarisJavaAgent] polaris not enabled, skip inject application bean definitions for module {}", getModule());
			return;
		}
		if (!bootstrapLoaded.get()) {
			onBootstrapStartup(configurationParser, configClassCreator, processConfigurationClass, registry, environment);
		}
		Object polarisCircuitBreakerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerAutoConfiguration.class, "polarisCircuitBreakerAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisCircuitBreakerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisCircuitBreakerAutoConfiguration.class).getBeanDefinition());
		if (null != ClassUtils.getClazz("feign.Feign", Thread.currentThread().getContextClassLoader())
				&& null != ClassUtils.getClazz("org.springframework.cloud.openfeign.FeignClientFactoryBean", Thread.currentThread()
				.getContextClassLoader())) {
			Object polarisCircuitBreakerFeignClientAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerFeignClientAutoConfiguration.class, "polarisCircuitBreakerFeignClientAutoConfiguration");
			ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerFeignClientAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
			registry.registerBeanDefinition("polarisCircuitBreakerFeignClientAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
					PolarisCircuitBreakerFeignClientAutoConfiguration.class).getBeanDefinition());
		}
		if (null != ClassUtils.getClazz("org.springframework.web.reactive.DispatcherHandler",
				Thread.currentThread().getContextClassLoader()) && null != ClassUtils.getClazz(
				"org.springframework.cloud.gateway.config.GatewayAutoConfiguration",
				Thread.currentThread().getContextClassLoader())) {
			String property = environment.getProperty("spring.cloud.gateway.enabled");
			if (Boolean.parseBoolean(property)) {
				Object gatewayPolarisCircuitBreakerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, GatewayPolarisCircuitBreakerAutoConfiguration.class, "gatewayPolarisCircuitBreakerAutoConfiguration");
				ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, gatewayPolarisCircuitBreakerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
				registry.registerBeanDefinition("gatewayPolarisCircuitBreakerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
						GatewayPolarisCircuitBreakerAutoConfiguration.class).getBeanDefinition());
			}
		}
		Object polarisCircuitBreakerEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerEndpointAutoConfiguration.class, "polarisCircuitBreakerEndpointAutoConfiguration");
		ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
		registry.registerBeanDefinition("polarisCircuitBreakerEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
				PolarisCircuitBreakerEndpointAutoConfiguration.class).getBeanDefinition());
		LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
	}
}

