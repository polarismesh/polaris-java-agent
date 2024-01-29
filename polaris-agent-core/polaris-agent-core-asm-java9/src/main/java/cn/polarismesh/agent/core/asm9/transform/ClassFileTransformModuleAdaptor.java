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

package cn.polarismesh.agent.core.asm9.transform;

import cn.polarismesh.agent.core.asm9.module.JavaModule;
import cn.polarismesh.agent.core.asm9.module.JavaModuleFactory;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Objects;

public class ClassFileTransformModuleAdaptor implements ClassFileTransformer {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ClassFileTransformModuleAdaptor.class.getCanonicalName());

    private final ClassFileTransformer delegate;
    private final JavaModuleFactory javaModuleFactory;
    private final JavaModule bootstrapModule;

    public ClassFileTransformModuleAdaptor(Instrumentation instrumentation, ClassFileTransformer delegate, JavaModuleFactory javaModuleFactory) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.javaModuleFactory = Objects.requireNonNull(javaModuleFactory, "javaModuleFactory");
        this.bootstrapModule = javaModuleFactory.wrapFromClass(JavaModuleFactory.class);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return internalTransform(null, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return internalTransform(module, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }


    private byte[] internalTransform(Object transformedModuleObject, ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        final byte[] transform = delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transformedModuleObject == null) {
            return transform;
        }
        if (transform != null && transform != classfileBuffer) {
            if (!javaModuleFactory.isNamedModule(transformedModuleObject)) {
                return transform;
            }
            // bootstrap-core permission
            final JavaModule transformedModule = javaModuleFactory.wrapFromModule(transformedModuleObject);
            addModulePermission(transformedModule, className, bootstrapModule);


            if (loader != Object.class.getClassLoader()) {
                // plugin permission
                final Object pluginModuleObject = getPluginModule(loader);
                final JavaModule pluginModule = javaModuleFactory.wrapFromModule(pluginModuleObject);

                addModulePermission(transformedModule, className, pluginModule);
            }
        }
        return transform;
    }

    private Object getPluginModule(ClassLoader loader) {
        // current internal implementation
        // The plugin.jar is loaded into the unnamed module
        return javaModuleFactory.getUnnamedModule(loader);
    }

    private void addModulePermission(JavaModule transformedModule, String className, JavaModule targetModule) {
        if (!transformedModule.canRead(targetModule)) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("addReads module:%s target:%s", transformedModule, targetModule));
            }
            transformedModule.addReads(targetModule);
        }

        final String packageName = getPackageName(className);
        if (packageName != null) {
            if (!transformedModule.isExported(packageName, targetModule)) {
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("addExports module:%s pkg:%s target:%s", transformedModule, packageName, targetModule));
                }
                transformedModule.addExports(packageName, targetModule);
            }
            // need open?
        }
    }

    private String getPackageName(String className) {
        final String packageName = getPackageName(className, null);
        if (packageName == null) {
            return null;
        }
        return getPackageNameFromInternalName(className);
    }

    private static String getPackageName(String fqcn, String defaultValue) {
        Objects.requireNonNull(fqcn, "fqcn");

        final int lastPackageSeparatorIndex = fqcn.lastIndexOf('/');
        if (lastPackageSeparatorIndex == -1) {
            return null;
        }
        return fqcn.substring(0, lastPackageSeparatorIndex);
    }

    private static String getPackageNameFromInternalName(String className) {
        Objects.requireNonNull(className, "className");

        final String jvmPackageName = getPackageName(className, null);
        if (jvmPackageName == null) {
            return null;
        }

        return JavaAssistUtils.jvmNameToJavaName(jvmPackageName);
    }
}
