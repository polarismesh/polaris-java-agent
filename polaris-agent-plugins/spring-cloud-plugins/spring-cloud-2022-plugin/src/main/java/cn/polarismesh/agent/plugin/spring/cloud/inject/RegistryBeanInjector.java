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

package cn.polarismesh.agent.plugin.spring.cloud.inject;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.DiscoveryPropertiesAutoConfiguration;
import com.tencent.cloud.polaris.DiscoveryPropertiesBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.discovery.PolarisDiscoveryAutoConfiguration;
import com.tencent.cloud.polaris.discovery.PolarisDiscoveryClientConfiguration;
import com.tencent.cloud.polaris.discovery.reactive.PolarisReactiveDiscoveryClientConfiguration;
import com.tencent.cloud.polaris.discovery.refresh.PolarisRefreshConfiguration;
import com.tencent.cloud.polaris.endpoint.PolarisDiscoveryEndpointAutoConfiguration;
import com.tencent.cloud.polaris.registry.PolarisServiceRegistryAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegistryBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

    @Override
    public String getModule() {
        return "spring-cloud-starter-tencent-polaris-discovery";
    }

    @Override
    public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.discovery.enabled"))) {
            LOGGER.warn("[PolarisJavaAgent] polaris discovery not enabled, skip inject bootstrap bean definitions for module {}", getModule());
            return;
        }
        bootstrapLoaded.set(true);
        Object discoveryPropertiesBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, DiscoveryPropertiesBootstrapAutoConfiguration.class, "discoveryPropertiesBootstrapAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, discoveryPropertiesBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("discoveryPropertiesBootstrapAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                DiscoveryPropertiesBootstrapAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.discovery.enabled"))) {
            LOGGER.warn("[PolarisJavaAgent] polaris discovery not enabled, skip inject application bean definitions for module {}", getModule());
            return;
        }
        if (!bootstrapLoaded.get()) {
            onBootstrapStartup(configurationParser, configClassCreator, processConfigurationClass, registry, environment);
        }
        Object discoveryPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, DiscoveryPropertiesAutoConfiguration.class, "discoveryPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, discoveryPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("discoveryPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                DiscoveryPropertiesAutoConfiguration.class).getBeanDefinition());
        Object polarisDiscoveryAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisDiscoveryAutoConfiguration.class, "polarisDiscoveryAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisDiscoveryAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisDiscoveryAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisDiscoveryAutoConfiguration.class).getBeanDefinition());
        Object polarisDiscoveryClientConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisDiscoveryClientConfiguration.class, "polarisDiscoveryClientConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisDiscoveryClientConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisDiscoveryClientConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisDiscoveryClientConfiguration.class).getBeanDefinition());
        Object polarisReactiveDiscoveryClientConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisReactiveDiscoveryClientConfiguration.class, "polarisReactiveDiscoveryClientConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisReactiveDiscoveryClientConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisReactiveDiscoveryClientConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisReactiveDiscoveryClientConfiguration.class).getBeanDefinition());
        Object polarisRefreshConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisRefreshConfiguration.class, "polarisRefreshConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisRefreshConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisRefreshConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisRefreshConfiguration.class).getBeanDefinition());
        Object polarisServiceRegistryAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisServiceRegistryAutoConfiguration.class, "polarisServiceRegistryAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisServiceRegistryAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisServiceRegistryAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisServiceRegistryAutoConfiguration.class).getBeanDefinition());
        Object polarisDiscoveryEndpointAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisDiscoveryEndpointAutoConfiguration.class, "polarisDiscoveryEndpointAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisDiscoveryEndpointAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisDiscoveryEndpointAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisDiscoveryEndpointAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}

