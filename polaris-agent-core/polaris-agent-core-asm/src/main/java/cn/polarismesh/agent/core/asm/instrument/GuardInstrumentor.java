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


import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.asm.instrument.transform.TransformCallbackChecker;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import java.security.ProtectionDomain;
import java.util.Objects;

public class GuardInstrumentor implements Instrumentor {

    private final InstrumentContext instrumentContext;
    private boolean closed = false;

    public GuardInstrumentor(InstrumentContext instrumentContext) {
        this.instrumentContext = Objects.requireNonNull(instrumentContext, "instrumentContext");
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        checkOpen();
        return instrumentContext.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classfileBuffer) {
        checkOpen();
        return instrumentContext.getInstrumentClass(classLoader, className, null, classfileBuffer);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        checkOpen();
        return instrumentContext.exist(classLoader, className, protectionDomain);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className) {
        checkOpen();
        return instrumentContext.exist(classLoader, className, null);
    }


    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        checkOpen();
        return instrumentContext.injectClass(targetClassLoader, className);
    }

    @Override
    public void transform(ClassLoader classLoader, String targetClassName,
            Class<? extends TransformCallback> transformCallback) {
        checkOpen();
        Objects.requireNonNull(transformCallback, "transformCallback");
        TransformCallbackChecker.validate(transformCallback);

        final String transformCallbackClassName = transformCallback.getName();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallbackClassName);
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
            throw new IllegalStateException("Instrumentor already closed");
        }
    }
}
