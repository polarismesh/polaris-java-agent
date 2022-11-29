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
import cn.polarismesh.agent.core.common.utils.StringUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PluginNameFilter implements PluginFilter {

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
}
