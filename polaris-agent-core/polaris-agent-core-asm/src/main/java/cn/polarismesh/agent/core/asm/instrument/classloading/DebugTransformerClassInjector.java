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

package cn.polarismesh.agent.core.asm.instrument.classloading;

import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.asm.instrument.BootstrapPackage;
import java.io.InputStream;

public class DebugTransformerClassInjector implements ClassInjector {

    private static final BootstrapPackage bootstrapPackage = new BootstrapPackage();

    public DebugTransformerClassInjector() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        ClassLoader targetClassLoader = classLoader;
        if (bootstrapPackage.isBootstrapPackage(className)) {
            targetClassLoader = Object.class.getClassLoader();
        }

        try {
            return (Class<? extends T>) Class.forName(className, false, targetClassLoader);
        } catch (ClassNotFoundException e) {
            throw new PolarisAgentException("ClassNo class " + className + " with classLoader " + classLoader, e);
        }
    }


    @Override
    public InputStream getResourceAsStream(ClassLoader classLoader, String internalName) {
        ClassLoader targetClassLoader = getClassLoader(classLoader);

        targetClassLoader = filterBootstrapPackage(targetClassLoader, internalName);

        return targetClassLoader.getResourceAsStream(internalName);
    }

    @Override
    public boolean match(ClassLoader classLoader) {
        return true;
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }


    private ClassLoader filterBootstrapPackage(ClassLoader classLoader, String classPath) {
        if (bootstrapPackage.isBootstrapPackageByInternalName(classPath)) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }
}
