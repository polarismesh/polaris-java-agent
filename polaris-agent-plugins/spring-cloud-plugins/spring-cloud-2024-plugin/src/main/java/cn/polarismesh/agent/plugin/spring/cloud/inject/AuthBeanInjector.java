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
import com.tencent.cloud.polaris.auth.config.PolarisAuthAutoConfiguration;
import com.tencent.cloud.polaris.auth.config.PolarisAuthPropertiesAutoConfiguration;
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
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator,
            Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment,
                "spring.cloud.polaris.auth.enabled"))) {
            LOGGER.warn(
                    "[PolarisJavaAgent] polaris not enabled, skip inject application bean definitions for module {}",
                    getModule());
            return;
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
