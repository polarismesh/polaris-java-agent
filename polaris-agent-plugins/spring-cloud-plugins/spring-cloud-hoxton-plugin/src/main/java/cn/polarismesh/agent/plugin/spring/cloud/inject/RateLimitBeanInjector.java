package cn.polarismesh.agent.plugin.spring.cloud.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitAutoConfiguration;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitPropertiesBootstrapConfiguration;
import com.tencent.cloud.polaris.ratelimit.endpoint.PolarisRateLimitRuleEndpointAutoConfiguration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class RateLimitBeanInjector implements BeanInjector {
    @Override
    public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
        Object polarisRateLimitPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitPropertiesBootstrapConfiguration.class, "polarisRateLimitPropertiesBootstrapConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitPropertiesBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitPropertiesBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitPropertiesBootstrapConfiguration.class).getBeanDefinition());
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry) {
        Object polarisRateLimitAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitAutoConfiguration.class, "polarisRateLimitAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitAutoConfiguration.class).getBeanDefinition());
        Object polarisRateLimitPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitPropertiesAutoConfiguration.class, "polarisRateLimitPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitPropertiesAutoConfiguration.class).getBeanDefinition());
        Object polarisRateLimitRuleEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitRuleEndpointAutoConfiguration.class, "polarisRateLimitRuleEndpointAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitRuleEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitRuleEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitRuleEndpointAutoConfiguration.class).getBeanDefinition());
    }
}
