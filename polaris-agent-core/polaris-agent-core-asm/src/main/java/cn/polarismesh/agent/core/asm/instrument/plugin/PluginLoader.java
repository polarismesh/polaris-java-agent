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

import cn.polarismesh.agent.core.extension.AgentPlugin;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginLoader {

    private static ClassLoader createPluginClassLoaderWithUrl(List<URL> pluginUrls, final ClassLoader parentClassLoader) {
        final URL[] urls = pluginUrls.toArray(new URL[0]);
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return new URLClassLoader(urls, parentClassLoader);
                }
            });
        }
        return new URLClassLoader(urls, parentClassLoader);
    }

    public static ClassLoader createPluginClassLoader(List<PluginJar> pluginJars, final ClassLoader parentClassLoade) {
        ClassLoader parentClassLoader = Object.class.getClassLoader();
        List<URL> pluginUrls = new ArrayList<URL>(pluginJars.size());
        for (PluginJar pluginJar : pluginJars) {
            pluginUrls.add(pluginJar.getUrl());
        }
        return createPluginClassLoaderWithUrl(pluginUrls, parentClassLoader);
    }

    public static List<AgentPlugin> loadPlugins(ClassLoader classLoader) {
        List<AgentPlugin> profilerPlugins = new ArrayList<>();
        ServiceLoader<AgentPlugin> serviceLoader = ServiceLoader.load(AgentPlugin.class, classLoader);
        for (AgentPlugin profilerPlugin : serviceLoader) {
            profilerPlugins.add(profilerPlugin);
        }
        return profilerPlugins;
    }

}
