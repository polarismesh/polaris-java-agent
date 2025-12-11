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

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import com.tencent.cloud.polaris.circuitbreaker.config.GatewayPolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerFeignClientAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class CircuitBreakerBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerBeanInjector.class);


    @Override
    public String getModule() {
        return "spring-cloud-starter-tencent-polaris-circuitbreaker";
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.circuitbreaker.enabled"))) {
            LOGGER.warn("[PolarisJavaAgent] polaris not enabled, skip inject application bean definitions for module {}", getModule());
            return;
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
        if (null != ClassUtils.getClazz("org.springframework.web.reactive.DispatcherHandler", Thread.currentThread()
                .getContextClassLoader())
                && null != ClassUtils.getClazz("org.springframework.cloud.gateway.config.GatewayAutoConfiguration", Thread.currentThread()
                .getContextClassLoader())) {
            String property = environment.getProperty("spring.cloud.gateway.enabled");
            if (Boolean.parseBoolean(property)) {
                Object gatewayPolarisCircuitBreakerAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, GatewayPolarisCircuitBreakerAutoConfiguration.class, "gatewayPolarisCircuitBreakerAutoConfiguration");
                ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, gatewayPolarisCircuitBreakerAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
                registry.registerBeanDefinition("gatewayPolarisCircuitBreakerAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                        GatewayPolarisCircuitBreakerAutoConfiguration.class).getBeanDefinition());
            }
        }
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}

