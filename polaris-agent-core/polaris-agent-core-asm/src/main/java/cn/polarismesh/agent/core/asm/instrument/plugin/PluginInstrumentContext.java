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

import cn.polarismesh.agent.core.asm.instrument.DynamicTransformTrigger;
import cn.polarismesh.agent.core.asm.instrument.InstrumentContext;
import cn.polarismesh.agent.core.asm.instrument.InstrumentEngine;
import cn.polarismesh.agent.core.asm.instrument.classloading.ClassInjector;
import cn.polarismesh.agent.core.asm.instrument.matcher.Matcher;
import cn.polarismesh.agent.core.asm.scanner.ClassScannerFactory;
import cn.polarismesh.agent.core.asm.scanner.Scanner;
import cn.polarismesh.agent.core.extension.instrument.exception.NotFoundInstrumentException;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.Objects;

public class PluginInstrumentContext implements InstrumentContext {

    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final ClassInjector classInjector;

    private final ClassFileTransformerLoader transformerRegistry;

    public PluginInstrumentContext(InstrumentEngine instrumentEngine,
            DynamicTransformTrigger dynamicTransformTrigger, ClassInjector classInjector,
            ClassFileTransformerLoader transformerRegistry) {
        this.instrumentEngine = Objects.requireNonNull(instrumentEngine, "instrumentEngine");
        this.dynamicTransformTrigger = Objects.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger");
        this.classInjector = Objects.requireNonNull(classInjector, "classInjector");
        this.transformerRegistry = Objects.requireNonNull(transformerRegistry, "transformerRegistry");
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className,
            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        Objects.requireNonNull(className, "className");

        try {
            final InstrumentEngine instrumentEngine = getInstrumentEngine();
            return instrumentEngine.getClass(this, classLoader, className, protectionDomain, classFileBuffer);
        } catch (NotFoundInstrumentException e) {
            return null;
        }
    }


    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        Objects.requireNonNull(className, "className");

        final String jvmClassName = JavaAssistUtils.javaClassNameToJvmResourceName(className);

        final Scanner scanner = ClassScannerFactory.newScanner(protectionDomain, classLoader);
        try {
            return scanner.exist(jvmClassName);
        } finally {
            scanner.close();
        }
    }

    private InstrumentEngine getInstrumentEngine() {
        return this.instrumentEngine;
    }

    @Override
    public void addClassFileTransformer(final Matcher matcher, final TransformCallback transformCallback) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(transformCallback, "transformCallback");
        final TransformCallbackProvider transformCallbackProvider = new InstanceTransformCallbackProvider(
                transformCallback);
        transformerRegistry.addClassFileTransformer(this, matcher, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(final Matcher matcher, final String transformCallbackClassName) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        final TransformCallbackProvider transformCallbackProvider = new DynamicTransformCallbackProvider(
                transformCallbackClassName);
        transformerRegistry.addClassFileTransformer(this, matcher, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(final Matcher matcher, final String transformCallbackClassName,
            Object[] parameters, Class<?>[] parameterTypes) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        final TransformCallbackProvider transformCallbackProvider = new DynamicTransformCallbackProvider(
                transformCallbackClassName, parameters, parameterTypes);
        transformerRegistry.addClassFileTransformer(this, matcher, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName,
            final TransformCallback transformCallback) {
        Objects.requireNonNull(targetClassName, "targetClassName");
        Objects.requireNonNull(transformCallback, "transformCallback");
        final TransformCallbackProvider transformCallbackProvider = new InstanceTransformCallbackProvider(
                transformCallback);
        this.transformerRegistry.addClassFileTransformer(this, classLoader, targetClassName, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName,
            final String transformCallbackClassName) {
        Objects.requireNonNull(targetClassName, "targetClassName");
        Objects.requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        final TransformCallbackProvider transformCallbackProvider = new DynamicTransformCallbackProvider(
                transformCallbackClassName);
        this.transformerRegistry.addClassFileTransformer(this, classLoader, targetClassName, transformCallbackProvider);
    }


    @Override
    public void retransform(Class<?> target, final TransformCallback transformCallback) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(transformCallback, "transformCallback");

        final InstanceTransformCallbackProvider transformCallbackProvider = new InstanceTransformCallbackProvider(
                transformCallback);
        final ClassFileTransformerDelegate classFileTransformerGuardDelegate = new ClassFileTransformerDelegate(this,
                transformCallbackProvider);

        this.dynamicTransformTrigger.retransform(target, classFileTransformerGuardDelegate);
    }


    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        Objects.requireNonNull(className, "className");

        return classInjector.injectClass(targetClassLoader, className);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        if (classPath == null) {
            return null;
        }

        return classInjector.getResourceAsStream(targetClassLoader, classPath);
    }


}
