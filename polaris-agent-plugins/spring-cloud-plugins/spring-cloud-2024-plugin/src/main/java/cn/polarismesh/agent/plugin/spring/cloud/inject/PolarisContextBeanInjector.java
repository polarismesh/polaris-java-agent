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

package cn.polarismesh.agent.plugin.spring.cloud.inject;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextPropertiesBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextPostConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PolarisContextBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisContextBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

    @Override
    public String getModule() {
        return "spring-cloud-tencent-polaris-context";
    }

    @Override
    public Map<String, List<String>> getClassNameForType() {
        Map<String, List<String>> values = new HashMap<>();
        values.put("org.springframework.context.ApplicationListener", Arrays.asList(
                "com.tencent.cloud.polaris.context.logging.PolarisLoggingApplicationListener",
                "com.tencent.cloud.polaris.context.listener.FailedEventApplicationListener"));
        values.put("org.springframework.boot.env.EnvironmentPostProcessor", Collections.singletonList(
                "com.tencent.cloud.polaris.context.config.PolarisContextEnvironmentPostProcessor"));
        return values;
    }

    @Override
    public void onBootstrapStartup(Object configurationParser,
                                   Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment))) {
            LOGGER.warn("[PolarisJavaAgent] polaris not enabled, skip inject bootstrap bean definitions for module {}", getModule());
            return;
        }
        bootstrapLoaded.set(true);
        Object polarisContextPropertiesBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextPropertiesBootstrapAutoConfiguration.class, "polarisContextPropertiesBootstrapAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextPropertiesBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisContextPropertiesBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisContextPropertiesBootstrapAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
    }

    @Override
    public void onApplicationStartup(Object configurationParser,
                                     Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment))) {
            LOGGER.warn("[PolarisJavaAgent] polaris not enabled, skip inject application bean definitions for module {}", getModule());
            return;
        }
        if (!bootstrapLoaded.get()) {
            onBootstrapStartup(configurationParser, configClassCreator, processConfigurationClass, registry, environment);
        }
        Object polarisContextAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextAutoConfiguration.class, "polarisContextAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisContextAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisContextAutoConfiguration.class).getBeanDefinition());
        Object polarisContextPostConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisContextPostConfiguration.class, "polarisContextPostConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisContextPostConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisContextPostConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisContextPostConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}
