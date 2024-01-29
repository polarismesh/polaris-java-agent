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

package cn.polarismesh.agent.core.asm9.module.impl;

import cn.polarismesh.agent.core.asm9.module.JavaModule;
import cn.polarismesh.agent.core.asm9.module.JavaModuleFactory;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

public class Java9ModuleFactory implements JavaModuleFactory {

    private final Instrumentation instrumentation;

    public Java9ModuleFactory(Instrumentation instrumentation) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
    }

    @Override
    public JavaModule wrapFromClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return new Java9Module(instrumentation, clazz.getModule());
    }

    @Override
    public JavaModule wrapFromModule(Object module) {
        if (!(module instanceof Module)) {
            throw new IllegalArgumentException("module not java.lang.module");
        }
        return new Java9Module(instrumentation, (Module) module);
    }

    @Override
    public boolean isNamedModule(Object module) {
        if (!(module instanceof Module)) {
            throw new IllegalArgumentException("module not java.lang.module");
        }
        return ((Module) module).isNamed();
    }

    @Override
    public Module getUnnamedModule(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");

        return classLoader.getUnnamedModule();
    }

    @Override
    public Module getModule(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        return clazz.getModule();
    }
}
