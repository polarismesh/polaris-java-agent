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
import cn.polarismesh.agent.core.asm9.module.Providers;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.module.ModuleDescriptor;
import java.util.*;

public class Java9Module implements JavaModule {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(Java9Module.class.getCanonicalName());

    private final Instrumentation instrumentation;
    private final Module module;

    public Java9Module(Instrumentation instrumentation, Module module) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.module = module;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean isNamed() {
        return this.module.isNamed();
    }

    @Override
    public String getName() {
        return this.module.getName();

    }

    @Override
    public List<Providers> getProviders() {
        List<Providers> result = new ArrayList<>();
        Set<ModuleDescriptor.Provides> providesSet = this.module.getDescriptor().provides();
        for (ModuleDescriptor.Provides provides : providesSet) {
            String service = provides.service();
            List<String> providers = provides.providers();
            Providers newProviders = new Providers(service, providers);
            result.add(newProviders);
        }
        return result;
    }

    @Override
    public void addReads(JavaModule targetJavaModule) {
        final Java9Module target = checkJavaModule(targetJavaModule);

        logger.info("addReads module:" + module.getName() +" target:" + target);
        // for debug
        final Set<Module> readModules = Set.of(target.module);
        RedefineModuleUtils.addReads(instrumentation, module, readModules);
    }

    @Override
    public void addExports(String packageName, JavaModule targetJavaModule) {
        Objects.requireNonNull(packageName, "packageName");

        final Java9Module target = checkJavaModule(targetJavaModule);

        logger.info("addExports module:" + module.getName() + " pkg:" + packageName + " target:" + target);
        final Map<String, Set<Module>> extraModules = Map.of(packageName, Set.of(target.module));
        RedefineModuleUtils.addExports(instrumentation, module, extraModules);
    }

    private Java9Module checkJavaModule(JavaModule targetJavaModule) {
        Objects.requireNonNull(targetJavaModule, "targetJavaModule");

        if (targetJavaModule instanceof Java9Module) {
            return (Java9Module) targetJavaModule;
        }
        throw new RuntimeException("invalid JavaModule: " + targetJavaModule.getClass());
    }

    @Override
    public void addOpens(String packageName, JavaModule javaModule) {
        Objects.requireNonNull(packageName, "packageName");

        final Java9Module target = checkJavaModule(javaModule);

        logger.info("addExports module:" + module.getName() + " pkg:" + packageName + " target:" + target);

        final Map<String, Set<Module>> extraOpens = Map.of(packageName, Set.of(target.module));
        RedefineModuleUtils.addOpens(instrumentation, module, extraOpens);
    }


    @Override
    public void addUses(Class<?> target) {
        Objects.requireNonNull(target, "target");

        logger.info("addUses module:" + module.getName() +" target:" + target);

        final Set<Class<?>> extraUses = Set.of(target);
        RedefineModuleUtils.addUses(instrumentation, module, extraUses);
    }

    @Override
    public void addProvides(Class<?> service, List<Class<?>> providerList) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(providerList, "providerList");

       logger.info("addProvides module:" + module.getName() +" service:" + service + " providerList:" + providerList);

        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of(service, providerList);
        RedefineModuleUtils.addProvides(instrumentation, module, extraProvides);
    }

    @Override
    public boolean isExported(String packageName, JavaModule targetJavaModule) {
        Objects.requireNonNull(packageName, "packageName");

        final Java9Module target = checkJavaModule(targetJavaModule);
        return module.isExported(packageName, target.module);
    }

    @Override
    public boolean isOpen(String packageName, JavaModule targetJavaModule) {
        Objects.requireNonNull(packageName, "packageName");

        final Java9Module target = checkJavaModule(targetJavaModule);
        return module.isOpen(packageName, target.module);
    }

    @Override
    public boolean canRead(JavaModule targetJavaModule) {
        final Java9Module target = checkJavaModule(targetJavaModule);
        return this.module.canRead(target.module);
    }

    @Override
    public boolean canRead(Class<?> targetClazz) {
        return this.module.canUse(targetClazz);
    }


    @Override
    public ClassLoader getClassLoader() {
        return module.getClassLoader();
    }

    @Override
    public Set<String> getPackages() {
        return module.getPackages();
    }

    @Override
    public String toString() {
        return module.toString();
    }
}
