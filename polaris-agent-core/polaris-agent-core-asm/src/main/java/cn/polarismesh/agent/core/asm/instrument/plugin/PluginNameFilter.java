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

package cn.polarismesh.agent.core.asm.instrument.plugin;

import cn.polarismesh.agent.core.common.conf.ConfigManager;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.StringUtils;

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
        logger.info("Enable plugins: " + enablePlugins);
        if (StringUtils.isEmpty(enablePlugins)) {
            logger.info("Enable plugins is empty, try to auto-detect");
            enablePlugins = appendSpringCloudPluginNameIfNeeded(enablePlugins);
            logger.info("Enable plugins after appendSpringCloudPluginNameIfNeeded: " + enablePlugins);
            enablePlugins = appendDubboPluginNameIfNeeded(enablePlugins);
            logger.info("Enable plugins after appendDubboPluginNameIfNeeded: " + enablePlugins);
        }
        logger.info("Final Enable plugins: " + enablePlugins);
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

    static String appendSpringCloudPluginNameIfNeeded(String enablePlugins) {
        if (StringUtils.hasText(enablePlugins)) {
            String[] names = enablePlugins.split(",");
            for (String name : names) {
                if (StringUtils.hasText(name) && name.contains("spring-cloud-")) {
                    return enablePlugins;
                }
            }
        }

        SpringBootVersionDetector detector = new SpringBootVersionDetector();
        String bootVersion = detector.detectVersion();
        if (bootVersion.isEmpty()) {
            logger.warn("No Spring Boot version detected from"
                    + " MANIFEST, classpath or fat JAR");
            return enablePlugins;
        }

        String pluginName = detector.getPluginName(bootVersion);
        if (pluginName.isEmpty()) {
            logger.warn("No compatible Spring Cloud plugin for"
                    + " Spring Boot version: " + bootVersion);
            return enablePlugins;
        }

        logger.info("Auto-detected Spring Boot version: "
                + bootVersion + ", plugin: " + pluginName);
        if (StringUtils.hasText(enablePlugins)) {
            return enablePlugins + "," + pluginName;
        }
        return pluginName;
    }

    /**
     * 自动检测 Dubbo 版本并追加对应插件名.
     * 如果 enablePlugins 中已包含 dubbo- 开头的插件，则跳过.
     */
    static String appendDubboPluginNameIfNeeded(
            String enablePlugins) {
        if (StringUtils.hasText(enablePlugins)) {
            String[] names = enablePlugins.split(",");
            for (String name : names) {
                if (StringUtils.hasText(name)
                        && name.contains("dubbo-")) {
                    return enablePlugins;
                }
            }
        }

        DubboVersionDetector detector = new DubboVersionDetector();
        String dubboVersion = detector.detectVersion();
        if (dubboVersion.isEmpty()) {
            return enablePlugins;
        }

        String pluginName = detector.getPluginName(dubboVersion);
        if (pluginName.isEmpty()) {
            logger.warn("Unsupported Dubbo version: "
                    + dubboVersion);
            return enablePlugins;
        }

        logger.info("Auto-detected Dubbo version: "
                + dubboVersion + ", plugin: " + pluginName);
        if (StringUtils.hasText(enablePlugins)) {
            return enablePlugins + "," + pluginName;
        }
        return pluginName;
    }

}
