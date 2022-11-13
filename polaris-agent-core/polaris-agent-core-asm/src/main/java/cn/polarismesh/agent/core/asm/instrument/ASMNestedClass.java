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

package cn.polarismesh.agent.core.asm.instrument;

import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinitionFactory;
import cn.polarismesh.agent.core.extension.instrument.ClassFilter;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.MethodFilter;
import java.util.Collections;
import java.util.List;

public class ASMNestedClass implements InstrumentClass {

    private final ASMClass aClass;

    public ASMNestedClass(InstrumentContext pluginContext, ASMClassNodeAdapter classNodeAdapter,
            InterceptorDefinitionFactory factory) {
        this.aClass = new ASMClass(pluginContext, classNodeAdapter, factory);
    }

    public ClassLoader getClassLoader() {
        return this.aClass.getClassLoader();
    }

    @Override
    public boolean isInterceptable() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return this.aClass.isInterface();
    }

    @Override
    public String getName() {
        return this.aClass.getName();
    }

    @Override
    public String getSuperClass() {
        return this.aClass.getSuperClass();
    }

    @Override
    public String[] getInterfaces() {
        return this.aClass.getInterfaces();
    }

    @Override
    public InstrumentMethod getDeclaredMethod(String name, String... parameterTypes) {
        return null;
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return Collections.emptyList();
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(MethodFilter methodFilter) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasDeclaredMethod(String methodName, String... args) {
        return this.aClass.hasDeclaredMethod(methodName, args);
    }

    @Override
    public boolean hasMethod(String methodName, String... parameterTypes) {
        return this.aClass.hasMethod(methodName, parameterTypes);
    }

    @Override
    public boolean hasEnclosingMethod(String methodName, String... parameterTypes) {
        return this.aClass.hasEnclosingMethod(methodName, parameterTypes);
    }

    @Override
    public InstrumentMethod getConstructor(String... parameterTypes) {
        return null;
    }

    @Override
    public List<InstrumentMethod> getDeclaredConstructors() {
        return null;
    }

    @Override
    public boolean hasConstructor(String... parameterTypeArray) {
        return this.aClass.hasConstructor(parameterTypeArray);
    }

    @Override
    public boolean hasField(String name, String type) {
        return this.aClass.hasField(name, type);
    }

    @Override
    public boolean hasField(String name) {
        return this.aClass.hasField(name);
    }

    @Override
    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        return this.aClass.getNestedClasses(filter);
    }

    @Override
    public byte[] toBytecode() {
        return null;
    }
}