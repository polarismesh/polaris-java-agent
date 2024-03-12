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

package cn.polarismesh.agent.core.asm9.instrument;

import cn.polarismesh.agent.core.asm.instrument.classloading.ClassInjector;
import cn.polarismesh.agent.core.asm.instrument.classloading.PluginClassInjector;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import cn.polarismesh.agent.core.asm9.module.impl.DefaultModuleSupport;
import cn.polarismesh.agent.core.asm9.starter.ModuleSupportHolder;

import java.io.InputStream;

public class Java9ClassInjectorWrapper implements ClassInjector {

    private final PluginClassInjector pluginClassInjector;

    public Java9ClassInjectorWrapper(PluginClassInjector pluginClassInjector) {
        this.pluginClassInjector = pluginClassInjector;
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        // to support cn.polarismesh.agent.core.common.utils.ReflectionUtils.setSuperValueByFieldName usage
        PluginConfig pluginConfig = pluginClassInjector.getPluginConfig();
        if (!pluginConfig.getPlugin().getOpenModules().isEmpty()) {
            DefaultModuleSupport moduleSupport = ModuleSupportHolder.getInstance().getModuleSupport(null);
            moduleSupport.baseModuleAddOpens(pluginConfig.getPlugin().getOpenModules(),
                    moduleSupport.wrapJavaModule(classLoader.getUnnamedModule()));
        }
        return pluginClassInjector.injectClass(classLoader, className);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader classLoader, String internalName) {
        // to support cn.polarismesh.agent.core.common.utils.ReflectionUtils.setSuperValueByFieldName usage
        PluginConfig pluginConfig = pluginClassInjector.getPluginConfig();
        if (!pluginConfig.getPlugin().getOpenModules().isEmpty()) {
            DefaultModuleSupport moduleSupport = ModuleSupportHolder.getInstance().getModuleSupport(null);
            moduleSupport.baseModuleAddOpens(pluginConfig.getPlugin().getOpenModules(), moduleSupport.wrapJavaModule(classLoader.getUnnamedModule()));
        }
        return pluginClassInjector.getResourceAsStream(classLoader, internalName);
    }

    @Override
    public boolean match(ClassLoader classLoader) {
        return pluginClassInjector.match(classLoader);
    }
}
