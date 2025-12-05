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
import com.tencent.cloud.polaris.loadbalancer.PolarisLoadBalancerAutoConfiguration;
import com.tencent.cloud.polaris.loadbalancer.PolarisLoadBalancerBootstrapAutoConfiguration;
import com.tencent.cloud.polaris.loadbalancer.PolarisLoadBalancerPropertiesAutoConfiguration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class LoadbalancerBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalancerBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

    @Override
    public String getModule() {
        return "spring-cloud-tencent-polaris-loadbalancer";
    }
    @Override
    public void onBootstrapStartup(Object configurationParser, Constructor<?> configClassCreator,
            Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        bootstrapLoaded.set(true);
        Object polarisLoadBalancerBootstrapAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator,
                PolarisLoadBalancerBootstrapAutoConfiguration.class, "polarisLoadBalancerBootstrapAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser,
                polarisLoadBalancerBootstrapAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisLoadBalancerBootstrapAutoConfiguration",
                BeanDefinitionBuilder.genericBeanDefinition(PolarisLoadBalancerBootstrapAutoConfiguration.class)
                        .getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject bootstrap bean definitions for module {}", getModule());
    }
    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.loadbalancer.enabled"))) {
            LOGGER.warn("[PolarisJavaAgent] polaris loadbalancer not enabled, skip inject application bean definitions for module {}", getModule());
            return;
        }
        if (!bootstrapLoaded.get()) {
            onBootstrapStartup(configurationParser, configClassCreator, processConfigurationClass, registry, environment);
        }
        Object polarisLoadBalancerPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisLoadBalancerPropertiesAutoConfiguration.class, "polarisLoadBalancerPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisLoadBalancerPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisLoadBalancerPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisLoadBalancerPropertiesAutoConfiguration.class).getBeanDefinition());
        Object polarisLoadBalancerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisLoadBalancerAutoConfiguration.class, "polarisLoadBalancerAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisLoadBalancerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisLoadBalancerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisLoadBalancerAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}
