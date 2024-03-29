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

import cn.polarismesh.agent.core.asm.instrument.matcher.Matcher;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.Objects;

public class GuardInstrumentContext implements InstrumentContext {

    private final InstrumentContext instrumentContext;
    private boolean closed = false;

    public GuardInstrumentContext(InstrumentContext instrumentContext) {
        this.instrumentContext = Objects.requireNonNull(instrumentContext, "instrumentContext");
    }


    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        checkOpen();
        return instrumentContext.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        checkOpen();
        return instrumentContext.exist(classLoader, className, protectionDomain);
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        checkOpen();
        return instrumentContext.injectClass(targetClassLoader, className);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        checkOpen();
        return instrumentContext.getResourceAsStream(targetClassLoader, classPath);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName,
            TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallback);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName,
            String transformCallbackClass) {
        checkOpen();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallbackClass);
    }

    @Override
    public void addClassFileTransformer(Matcher matcher, TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }

    @Override
    public void addClassFileTransformer(Matcher matcher, String transformCallbackClassName) {
        checkOpen();
        instrumentContext.addClassFileTransformer(matcher, transformCallbackClassName);
    }

    @Override
    public void addClassFileTransformer(Matcher matcher, String transformCallbackClassName, Object[] parameters,
            Class<?>[] parameterType) {
        checkOpen();
        instrumentContext.addClassFileTransformer(matcher, transformCallbackClassName, parameters, parameterType);
    }


    @Override
    public void retransform(Class<?> target, TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.retransform(target, transformCallback);
    }

    public void close() {
        this.closed = true;
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("instrumentContext already closed");
        }
    }
}
