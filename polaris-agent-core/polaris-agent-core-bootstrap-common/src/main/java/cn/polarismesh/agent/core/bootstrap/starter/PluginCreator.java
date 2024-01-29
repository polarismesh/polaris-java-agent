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

package cn.polarismesh.agent.core.bootstrap.starter;

import cn.polarismesh.agent.core.asm.instrument.plugin.JavaVersionFilter;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginFilter;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginJar;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginNameFilter;
import cn.polarismesh.agent.core.bootstrap.BootLogger;
import cn.polarismesh.agent.core.common.utils.CollectionUtils;
import cn.polarismesh.agent.core.common.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginCreator {

    private static final BootLogger logger = BootLogger.getLogger(PluginCreator.class);

    private static final String PLUGINS_DIR = "plugins";

    public static List<PluginJar> createPluginJars(String agentDirPath) {
        List<String> pluginsFiles = resolvePluginFiles(agentDirPath);
        if (CollectionUtils.isEmpty(pluginsFiles)) {
            return Collections.emptyList();
        }
        List<PluginFilter> pluginFilters = new ArrayList<>();
        pluginFilters.add(new PluginNameFilter());
        pluginFilters.add(new JavaVersionFilter());
        List<PluginJar> pluginJars = new ArrayList<>();
        for (String pluginFile : pluginsFiles) {
            PluginJar pluginJar = PluginJar.fromFilePath(pluginFile);
            boolean passed = true;
            for (PluginFilter pluginFilter : pluginFilters) {
                if (!pluginFilter.accept(pluginJar)) {
                    passed = false;
                    break;
                }
            }
            if (!passed) {
                logger.info(String.format("[BootStrap] plugin %s has been skip loading", pluginJar.getPluginId()));
                continue;
            }
            logger.info(String.format("[BootStrap] plugin %s has been loading", pluginJar.getPluginId()));
            pluginJars.add(pluginJar);
        }
        return pluginJars;
    }

    private static List<String> resolvePluginFiles(String agentDirPath) {
        String pluginDirPath = agentDirPath + File.separator + PLUGINS_DIR;
        File[] jarFiles = FileUtils.listFiles(new File(pluginDirPath), new String[]{".jar"});
        List<String> fileNames = new ArrayList<>();
        if (FileUtils.isEmpty(jarFiles)) {
            logger.info(pluginDirPath + " is empty");
        } else {
            for (File file : jarFiles) {
                fileNames.add(FileUtils.toCanonicalPath(file));
            }
        }
        return fileNames;
    }

}
