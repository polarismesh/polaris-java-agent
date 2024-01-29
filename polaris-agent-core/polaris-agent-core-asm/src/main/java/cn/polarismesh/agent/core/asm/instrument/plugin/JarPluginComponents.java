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

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.CodeSourceUtils;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JarPluginComponents {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(JarPluginComponents.class.getCanonicalName());

    private final Map<String, JarPluginComponent> componentMap;

    public JarPluginComponents(List<PluginJar> pluginJars) {
        this.componentMap = new LinkedHashMap<>(pluginJars.size());
        for (PluginJar pluginJar : pluginJars) {
            String key = generateKey(pluginJar.getUrl());
            componentMap.put(key, new JarPluginComponent(pluginJar));
        }
    }

    private String generateKey(URL url) {
        return url.toExternalForm();
    }

    public void addAgentPlugin(AgentPlugin AgentPlugin) {
        URL AgentPluginUrl = CodeSourceUtils.getCodeLocation(AgentPlugin.getClass());
        if (AgentPluginUrl == null) {
            logger.warn(String.format("unable to determine url for: %s", AgentPlugin.getClass()));
            return;
        }
        String key = generateKey(AgentPluginUrl);
        JarPluginComponent jarPluginComponent = componentMap.get(key);
        if (jarPluginComponent == null) {
            logger.warn(String.format("unexpected AgentPlugin: %s", AgentPlugin.getClass()));
            return;
        }
        jarPluginComponent.addAgentPlugin(AgentPlugin);
    }

    public Collection<JarPlugin<AgentPlugin>> buildJarPlugins() {
        List<JarPlugin<AgentPlugin>> jarPlugins = new ArrayList<>(componentMap.size());
        for (JarPluginComponent component : componentMap.values()) {
            jarPlugins.add(component.toJarPlugin());
        }
        return jarPlugins;
    }

    private static class JarPluginComponent {

        private final PluginJar pluginJar;
        private final List<AgentPlugin> AgentPlugins;

        private JarPluginComponent(PluginJar pluginJar) {
            this.pluginJar = Objects.requireNonNull(pluginJar, "pluginJar");
            this.AgentPlugins = new ArrayList<>();
        }

        private void addAgentPlugin(AgentPlugin AgentPlugin) {
            if (AgentPlugin != null) {
                AgentPlugins.add(AgentPlugin);
            }
        }

        private JarPlugin<AgentPlugin> toJarPlugin() {
            return new JarPlugin<>(pluginJar, AgentPlugins, pluginJar.getPluginPackages(), pluginJar.getPluginOpenModules());
        }
    }
}
