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

import java.net.URL;
import java.util.Objects;
import java.util.jar.JarFile;

public class PluginConfig {

    private final Plugin<?> plugin;
    private final JarFile pluginJar;
    private final ClassNameFilter pluginPackageFilter;

    private String pluginJarURLExternalForm;

    public PluginConfig(Plugin<?> plugin, ClassNameFilter pluginPackageFilter) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.pluginPackageFilter = pluginPackageFilter;
        this.pluginJar = getJarFile(plugin);
    }

    private JarFile getJarFile(Plugin<?> plugin) {
        if (plugin instanceof JarPlugin) {
            return ((JarPlugin<?>) plugin).getJarFile();
        }
        throw new IllegalArgumentException("unsupported plugin " + plugin);
    }

    public URL getPluginUrl() {
        return plugin.getURL();
    }

    public JarFile getPluginJarFile() {
        return pluginJar;
    }

    public String getPluginJarURLExternalForm() {
        if (this.pluginJarURLExternalForm == null) {
            this.pluginJarURLExternalForm = plugin.getURL().toExternalForm();
        }
        return this.pluginJarURLExternalForm;
    }

    public Plugin<?> getPlugin() {
        return plugin;
    }

    public ClassNameFilter getPluginPackageFilter() {
        return pluginPackageFilter;
    }

    @Override
    public String toString() {
        return "PluginConfig{" +
                "pluginJar=" + plugin.getURL() +
                ", pluginJarURLExternalForm='" + pluginJarURLExternalForm + '\'' +
                ", pluginPackageFilter=" + pluginPackageFilter +
                '}';
    }
}