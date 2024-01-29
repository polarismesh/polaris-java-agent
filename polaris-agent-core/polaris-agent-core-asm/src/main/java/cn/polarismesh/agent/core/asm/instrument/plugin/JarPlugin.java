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
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;

public class JarPlugin<T> implements Plugin<T> {

    private final PluginJar pluginJar;

    private final List<T> instanceList;
    private final List<String> packageList;

    private final List<String> openModules;

    public JarPlugin(PluginJar pluginJar, List<T> instanceList, List<String> packageList, List<String> openModules) {
        this.pluginJar = Objects.requireNonNull(pluginJar, "pluginJar");
        this.instanceList = Objects.requireNonNull(instanceList, "instanceList");
        this.packageList = Objects.requireNonNull(packageList, "packageList");
        this.openModules = Objects.requireNonNull(openModules, "openModules");
    }

    @Override
    public URL getURL() {
        return pluginJar.getUrl();
    }

    @Override
    public List<T> getInstanceList() {
        return instanceList;
    }

    @Override
    public List<String> getPackageList() {
        return packageList;
    }


    public JarFile getJarFile() {
        return pluginJar.getJarFile();
    }

    public List<String> getOpenModules() {
        return openModules;
    }

    @Override
    public String toString() {
        return "JarPlugin{" +
                "pluginJar=" + pluginJar +
                ", instanceList=" + instanceList +
                ", packageList=" + packageList +
                ", openModules=" + openModules +
                '}';
    }
}
