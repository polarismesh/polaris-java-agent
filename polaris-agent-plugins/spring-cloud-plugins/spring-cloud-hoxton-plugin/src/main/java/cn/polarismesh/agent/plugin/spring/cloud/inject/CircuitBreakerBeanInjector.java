package cn.polarismesh.agent.plugin.spring.cloud.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerBootstrapConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.ReactivePolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.endpoint.PolarisCircuitBreakerEndpointAutoConfiguration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class CircuitBreakerBeanInjector  implements BeanInjector {
    @Override
    public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {

        Object polarisCircuitBreakerBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerBootstrapConfiguration.class, "polarisCircuitBreakerBootstrapConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisCircuitBreakerBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisCircuitBreakerBootstrapConfiguration.class).getBeanDefinition());
//        Object polarisFeignCircuitBreakerTargeterAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisFeignCircuitBreakerTargeterAutoConfiguration.class, "polarisFeignCircuitBreakerTargeterAutoConfiguration");
//        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisFeignCircuitBreakerTargeterAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//        registry.registerBeanDefinition("polarisFeignCircuitBreakerTargeterAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//                PolarisFeignCircuitBreakerTargeterAutoConfiguration.class).getBeanDefinition());
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        Object polarisCircuitBreakerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerAutoConfiguration.class, "polarisCircuitBreakerAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisCircuitBreakerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisCircuitBreakerAutoConfiguration.class).getBeanDefinition());
        Object reactivePolarisCircuitBreakerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, ReactivePolarisCircuitBreakerAutoConfiguration.class, "reactivePolarisCircuitBreakerAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, reactivePolarisCircuitBreakerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("reactivePolarisCircuitBreakerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                ReactivePolarisCircuitBreakerAutoConfiguration.class).getBeanDefinition());
//        Object polarisCircuitBreakerFeignClientAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerFeignClientAutoConfiguration.class, "polarisCircuitBreakerFeignClientAutoConfiguration");
//        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerFeignClientAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//        registry.registerBeanDefinition("polarisCircuitBreakerFeignClientAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//                PolarisCircuitBreakerFeignClientAutoConfiguration.class).getBeanDefinition());
//        Object gatewayPolarisCircuitBreakerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, GatewayPolarisCircuitBreakerAutoConfiguration.class, "gatewayPolarisCircuitBreakerAutoConfiguration");
//        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, gatewayPolarisCircuitBreakerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//        registry.registerBeanDefinition("gatewayPolarisCircuitBreakerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//                GatewayPolarisCircuitBreakerAutoConfiguration.class).getBeanDefinition());
//        Object polarisFeignCircuitBreakerTargeterAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisFeignCircuitBreakerTargeterAutoConfiguration.class, "polarisFeignCircuitBreakerTargeterAutoConfiguration");
//        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisFeignCircuitBreakerTargeterAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
//        registry.registerBeanDefinition("polarisFeignCircuitBreakerTargeterAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
//                PolarisFeignCircuitBreakerTargeterAutoConfiguration.class).getBeanDefinition());
        Object polarisCircuitBreakerEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisCircuitBreakerEndpointAutoConfiguration.class, "polarisCircuitBreakerEndpointAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisCircuitBreakerEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisCircuitBreakerEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisCircuitBreakerEndpointAutoConfiguration.class).getBeanDefinition());
    }
}

