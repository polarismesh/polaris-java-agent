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
import com.tencent.cloud.common.async.PolarisAsyncPropertiesAutoConfiguration;
import com.tencent.cloud.common.metadata.config.MetadataAutoConfiguration;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.cloud.polaris.context.logging.PolarisLoggingApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommonBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonBeanInjector.class);

    private final AtomicBoolean bootstrapLoaded = new AtomicBoolean(false);

    @Override
    public String getModule() {
        return "spring-cloud-tencent-commons";
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        Object metadataAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, MetadataAutoConfiguration.class, "metadataAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, metadataAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("metadataAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                MetadataAutoConfiguration.class).getBeanDefinition());
        Object applicationContextAwareUtils = ReflectionUtils.invokeConstructor(configClassCreator, ApplicationContextAwareUtils.class, "applicationContextAwareUtils");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, applicationContextAwareUtils, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("applicationContextAwareUtils", BeanDefinitionBuilder.genericBeanDefinition(
                ApplicationContextAwareUtils.class).getBeanDefinition());
        Object polarisLoggingApplicationListener = ReflectionUtils.invokeConstructor(configClassCreator, PolarisLoggingApplicationListener.class, "polarisLoggingApplicationListener");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisLoggingApplicationListener, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisLoggingApplicationListener", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisLoggingApplicationListener.class).getBeanDefinition());
        Object polarisAsyncPropertiesAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, PolarisAsyncPropertiesAutoConfiguration.class, "polarisAsyncPropertiesAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, polarisAsyncPropertiesAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("polarisAsyncPropertiesAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                PolarisAsyncPropertiesAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }

}
