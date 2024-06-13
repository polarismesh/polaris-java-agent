package cn.polarismesh.agent.plugin.spring.cloud.inject;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitAutoConfiguration;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitPropertiesBootstrapConfiguration;
import com.tencent.cloud.polaris.ratelimit.endpoint.PolarisRateLimitRuleEndpointAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class RateLimitBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);
    @Override
    public String getModule() {
        return "spring-cloud-starter-tencent-polaris-ratelimit";
    }

    @Override
    public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        Object polarisRateLimitAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitAutoConfiguration.class, "polarisRateLimitAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitAutoConfiguration.class).getBeanDefinition());
        Object polarisRateLimitRuleEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitRuleEndpointAutoConfiguration.class, "polarisRateLimitRuleEndpointAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitRuleEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitRuleEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitRuleEndpointAutoConfiguration.class).getBeanDefinition());
        Object polarisRateLimitPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitPropertiesBootstrapConfiguration.class, "polarisRateLimitPropertiesBootstrapConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitPropertiesBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitPropertiesBootstrapConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitPropertiesBootstrapConfiguration.class).getBeanDefinition());
        Object polarisRateLimitPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRateLimitPropertiesAutoConfiguration.class, "polarisRateLimitPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRateLimitPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRateLimitPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRateLimitPropertiesAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}
