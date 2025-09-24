package cn.polarismesh.agent.plugin.spring.cloud.inject;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.auth.config.PolarisAuthAutoConfiguration;
import com.tencent.cloud.polaris.auth.config.PolarisAuthPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.auth.config.PolarisAuthPropertiesBootstrapConfiguration;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class AuthBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

    @Override
    public String getModule() {
        return "spring-cloud-starter-tencent-polaris-auth";
    }

    @Override
    public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator,
            Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment,
                "spring.cloud.polaris.auth.enabled"))) {
            LOGGER.warn(
                    "[PolarisJavaAgent] polaris auth not enabled, skip inject bootstrap bean definitions for module {}",
                    getModule());
            return;
        }
        bootstrapLoaded.set(true);
        Object polarisAuthPropertiesBootstrapConfiguration = ReflectionUtils.invokeConstructor(configClassCreator,
                PolarisAuthPropertiesBootstrapConfiguration.class, "polarisAuthPropertiesBootstrapConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser,
                polarisAuthPropertiesBootstrapConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisAuthPropertiesBootstrapConfiguration",
                BeanDefinitionBuilder.genericBeanDefinition(PolarisAuthPropertiesBootstrapConfiguration.class)
                        .getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator,
            Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment,
                "spring.cloud.polaris.auth.enabled"))) {
            LOGGER.warn(
                    "[PolarisJavaAgent] polaris not enabled, skip inject application bean definitions for module {}",
                    getModule());
            return;
        }
        if (!bootstrapLoaded.get()) {
            onBootstrapStartup(configurationParser, configClassCreator, processConfigurationClass, registry,
                    environment);
        }
        Object polarisAuthAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator,
                PolarisAuthAutoConfiguration.class, "polarisAuthAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser,
                polarisAuthAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisAuthAutoConfiguration",
                BeanDefinitionBuilder.genericBeanDefinition(PolarisAuthAutoConfiguration.class)
                        .getBeanDefinition());
        Object polarisAuthPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator,
                PolarisAuthPropertiesAutoConfiguration.class, "polarisAuthPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser,
                polarisAuthPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisAuthPropertiesAutoConfiguration",
                BeanDefinitionBuilder.genericBeanDefinition(PolarisAuthPropertiesAutoConfiguration.class)
                        .getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}
