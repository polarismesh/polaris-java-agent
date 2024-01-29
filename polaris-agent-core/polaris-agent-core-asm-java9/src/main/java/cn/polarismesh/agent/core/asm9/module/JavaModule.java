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

package cn.polarismesh.agent.core.asm9.module;

import java.util.List;
import java.util.Set;

public interface JavaModule {
    boolean isSupported();

    boolean isNamed();

    String getName();

    List<Providers> getProviders();

    void addReads(JavaModule target);

    void addExports(String packageName, JavaModule target);

    void addOpens(String packageName, JavaModule target);

    void addUses(Class<?> target);

    void addProvides(Class<?> service, List<Class<?>> providerList);

    boolean isExported(String packageName, JavaModule targetJavaModule);

    boolean isOpen(String packageName, JavaModule targetJavaModule);

    boolean canRead(JavaModule targetJavaModule);

    boolean canRead(Class<?> targetClazz);

    ClassLoader getClassLoader();

    Set<String> getPackages();
}
