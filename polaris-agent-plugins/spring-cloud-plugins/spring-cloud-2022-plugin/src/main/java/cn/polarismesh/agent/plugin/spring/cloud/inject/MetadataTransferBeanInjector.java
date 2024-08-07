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
import com.tencent.cloud.metadata.config.MetadataTransferAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MetadataTransferBeanInjector implements BeanInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataTransferBeanInjector.class);

    @Override
    public String getModule() {
        return "spring-cloud-starter-tencent-metadata-transfer";
    }

    @Override
    public void onApplicationStartup(Object configurationParser, Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {
        if (!(Utils.checkPolarisEnabled(environment))) {
            LOGGER.warn("[PolarisJavaAgent] polaris not enabled, skip inject application bean definitions for module {}", getModule());
            return;
        }
        Object metadataTransferAutoConfiguration = ReflectionUtils.invokeConstructor(configClassCreator, MetadataTransferAutoConfiguration.class, "metadataTransferAutoConfiguration");
        ReflectionUtils.invokeMethod(processConfigurationClass, configurationParser, metadataTransferAutoConfiguration, Constant.DEFAULT_EXCLUSION_FILTER);
        registry.registerBeanDefinition("metadataTransferAutoConfiguration", BeanDefinitionBuilder.genericBeanDefinition(
                MetadataTransferAutoConfiguration.class).getBeanDefinition());
        LOGGER.info("[PolarisJavaAgent] success to inject application bean definitions for module {}", getModule());
    }
}
