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

import cn.polarismesh.agent.core.extension.AgentPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JarPluginComponent {

    private final PluginJar pluginJar;
    private final List<AgentPlugin> plugins;

    private JarPluginComponent(PluginJar pluginJar) {
        this.pluginJar = Objects.requireNonNull(pluginJar, "pluginJar");
        this.plugins = new ArrayList<>();
    }

    private void addProfilerPlugin(AgentPlugin profilerPlugin) {
        if (profilerPlugin != null) {
            plugins.add(profilerPlugin);
        }
    }

    private JarPlugin<AgentPlugin> toJarPlugin() {
        return new JarPlugin<AgentPlugin>(pluginJar, plugins, pluginJar.getPluginPackages(), pluginJar.getPluginOpenModules());
    }
}
