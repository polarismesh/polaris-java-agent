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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.tencent.cloud.polaris.config.adapter.PolarisConfigFileLocator;
import com.tencent.polaris.configuration.api.core.ConfigFileService;
import com.tencent.polaris.configuration.factory.ConfigFileServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * {@link org.springframework.boot.util.Instantiator#instantiate(Stream)}
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConfigurationInjectInterceptor extends BaseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationInjectInterceptor.class);

    @Override
    public void onBefore(Object target, Object[] args) {
        ApplicationEnvironmentPreparedEvent event = (ApplicationEnvironmentPreparedEvent) args[0];

        List<EnvironmentPostProcessor> external = new ArrayList<>();
        // 插入禁止 spring cloud alibaba 的一切能力
        external.add(new DisableSpringCloudAlibabaAbility());

        for (EnvironmentPostProcessor processor : external) {
            processor.postProcessEnvironment(event.getEnvironment(), event.getSpringApplication());
        }
    }

    private static class DisableSpringCloudAlibabaAbility implements EnvironmentPostProcessor {

        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
            disableSpringCloudAlibabaAbility(environment);
        }

        private void disableSpringCloudAlibabaAbility(ConfigurableEnvironment environment) {
//            String disableSCA = "__disable__sca__";
//
//            Properties properties = new Properties();
//            properties.setProperty("spring.cloud.sentinel.enabled", "false");
//            properties.setProperty("spring.cloud.nacos.discovery.watch.enabled", "false");
//            properties.setProperty("spring.cloud.loadbalancer.cache.enabled", "false");
//            properties.setProperty("spring.cloud.nacos.config.enabled", "false");

            String disableCheck = "__disable__check__";
            Properties properties = new Properties();
            properties.setProperty("spring.cloud.polaris.config.import-check.enabled", "false");
            environment.getPropertySources().addFirst(new PropertiesPropertySource(disableCheck, properties));

            LOGGER.info("[PolarisAgent] disable spring cloud alibaba all ability");
        }
    }

}