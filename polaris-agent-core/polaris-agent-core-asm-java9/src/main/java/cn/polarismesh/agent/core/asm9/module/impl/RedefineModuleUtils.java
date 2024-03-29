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

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RedefineModuleUtils {

    public static void addUses(Instrumentation instrumentation, Module module, Set<Class<?>> extraUses) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraUses, "extraUses");

        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addReads(Instrumentation instrumentation, Module module, Set<Module> extraReads) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraReads, "extraReads");

        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addExports(Instrumentation instrumentation, Module module, Map<String, Set<Module>> extraExports) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraExports, "extraExports");

        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addOpens(Instrumentation instrumentation, Module module, Map<String, Set<Module>> extraOpens) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraOpens, "extraOpens");

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    public static void addProvides(Instrumentation instrumentation, Module module, Map<Class<?>, List<Class<?>>> extraProvides) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraProvides, "extraProvides");

        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }
}
