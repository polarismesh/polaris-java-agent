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
import com.tencent.cloud.plugin.lossless.config.LosslessAutoConfiguration;
import com.tencent.cloud.plugin.lossless.config.LosslessPropertiesAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class LosslessBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LosslessBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

    @Override
    public String getModule() {
        return "spring-cloud-tencent-lossless-plugin";
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment) && Utils.checkKeyEnabled(environment, "spring.cloud.polaris.lossless.enabled"))) {
            LOGGER.warn("[PolarisJavaAgent] polaris lossless not enabled, skip inject application bean definitions for module {}", getModule());
            return;
        }
        Object losslessPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, LosslessPropertiesAutoConfiguration.class, "losslessPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, losslessPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("losslessPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                LosslessPropertiesAutoConfiguration.class).getBeanDefinition());
        Object losslessAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, LosslessAutoConfiguration.class, "losslessAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, losslessAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("losslessAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                LosslessAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}
