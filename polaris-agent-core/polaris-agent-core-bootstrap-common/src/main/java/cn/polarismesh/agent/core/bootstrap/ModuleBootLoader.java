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

package cn.polarismesh.agent.core.bootstrap;

import cn.polarismesh.agent.core.bootstrap.util.Java9ClazzFinder;
import cn.polarismesh.agent.core.common.starter.InstrumentationStarter;
import cn.polarismesh.agent.core.common.starter.ModuleSupport;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Objects;

public class ModuleBootLoader {

    private final Instrumentation instrumentation;
    // @Nullable
    private ModuleSupport moduleSupport;

    public ModuleBootLoader(Instrumentation instrumentation) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
    }

    public void loadModuleSupport(ClassLoader clazzLoader) {
        InstrumentationStarter java9InstrumentationStarter = Java9ClazzFinder.lookup(clazzLoader);
        this.moduleSupport = java9InstrumentationStarter.createModuleSupport(instrumentation);
        this.moduleSupport.setup();
    }

    public void defineAgentModule(ClassLoader classLoader) {
        if (moduleSupport == null) {
            throw new IllegalStateException("moduleSupport not loaded");
        }
        moduleSupport.defineAgentModule(classLoader);
    }

}
