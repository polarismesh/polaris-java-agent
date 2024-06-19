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

package cn.polarismesh.agent.core.asm.instrument.plugin;

import cn.polarismesh.agent.core.common.conf.ConfigManager;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.common.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PluginNameFilter implements PluginFilter {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(PluginNameFilter.class.getCanonicalName());

    private final Set<String> pluginNames;

    public PluginNameFilter() {
        pluginNames = getLoadablePluginNames();
    }

    @Override
    public boolean accept(PluginJar pluginJar) {
        return pluginNames.contains(pluginJar.getPluginId());
    }

    private static Set<String> getLoadablePluginNames() {
        String enablePlugins = ConfigManager.INSTANCE.getConfigValue(ConfigManager.KEY_PLUGIN_ENABLE);
        enablePlugins = appendSpringCloudPluginNameIfNeeded(enablePlugins);
        if (StringUtils.isEmpty(enablePlugins)) {
            return Collections.emptySet();
        }
        String[] names = enablePlugins.split(",");
        Set<String> values = new HashSet<>();
        for (String name : names) {
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            values.add(name);
        }
        return values;
    }

    private static String appendSpringCloudPluginNameIfNeeded(String enablePlugins) {
        if (StringUtils.hasText(enablePlugins)) {
            String[] names = enablePlugins.split(",");
            for (String name : names) {
                if (StringUtils.hasText(name) && name.contains("spring-cloud-")) {
                    return enablePlugins;
                }
            }
        }

        // check if spring-cloud and spring-boot exist.
        if (ReflectionUtils.checkClassExists("org.springframework.cloud.configuration.SpringBootVersionVerifier")
                && ReflectionUtils.checkClassExists("org.springframework.boot.SpringBootVersion")) {
            Method method = ReflectionUtils.findMethod(ReflectionUtils.findClass("org.springframework.boot.SpringBootVersion"), "getVersion");
            Object version = ReflectionUtils.invokeMethod(method, null);

            if (version instanceof String) {
                String versionStr = (String) version;
                logger.info("Spring Boot Version: " + version);
                String springCloudPluginNamePattern = "spring-cloud-%s-plugin";
                String springCloudVersion = "";
                if (versionStr.startsWith("2.2") || versionStr.startsWith("2.3")) {
                    springCloudVersion = "hoxton";
                } else if (versionStr.startsWith("2.4") || versionStr.startsWith("2.5")) {
                    springCloudVersion = "2020";
                } else if (versionStr.startsWith("2.6") || versionStr.startsWith("2.7")) {
                    springCloudVersion = "2021";
                } else if (versionStr.startsWith("3.0") || versionStr.startsWith("3.1")) {
                    springCloudVersion = "2022";
                } else if (versionStr.startsWith("3.2") || versionStr.startsWith("3.3")) {
                    springCloudVersion = "2023";
                }
                if (StringUtils.hasText(springCloudVersion)) {
                    String springCloudPluginName = String.format(springCloudPluginNamePattern, springCloudVersion);
                    logger.info("Spring Cloud Version: " + springCloudVersion);
                    if (StringUtils.hasText(enablePlugins)) {
                        enablePlugins = enablePlugins + "," + springCloudPluginName;
                    } else {
                        enablePlugins = springCloudPluginName;
                    }
                }
            } else {
                logger.warn("Cannot get Spring Boot Version from MANIFEST.");
            }
        }
        return enablePlugins;
    }
}
