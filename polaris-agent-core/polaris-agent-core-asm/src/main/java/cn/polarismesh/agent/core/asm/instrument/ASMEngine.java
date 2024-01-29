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
import cn.polarismesh.agent.core.extension.instrument.exception.NotFoundInstrumentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class ASMEngine implements InstrumentEngine {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ASMEngine.class.getCanonicalName());

    private final Instrumentation instrumentation;

    private final InterceptorDefinitionFactory factory;

    public ASMEngine(Instrumentation instrumentation, InterceptorDefinitionFactory factory) {
        this.instrumentation = instrumentation;
        this.factory = factory;
    }

    @Override
    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader,
            String className, ProtectionDomain protectionDomain, byte[] classFileBuffer)
            throws NotFoundInstrumentException {
        Objects.requireNonNull(className, "className");

        try {
            if (classFileBuffer == null) {
                final ASMClassNodeAdapter classNode = ASMClassNodeAdapter
                        .get(instrumentContext, classLoader, protectionDomain,
                                JavaAssistUtils.javaNameToJvmName(className));
                if (classNode == null) {
                    return null;
                }
                return new ASMClass(instrumentContext, classNode, factory);
            }

            // Use ASM tree api.
            final ClassReader classReader = new ClassReader(classFileBuffer);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            return new ASMClass(instrumentContext, classLoader, protectionDomain, classNode, factory);
        } catch (Exception e) {
            throw new NotFoundInstrumentException(e);
        }
    }

    @Override
    public void appendToBootstrapClassPath(JarFile jarFile) {
        Objects.requireNonNull(jarFile, "jarFile");
        logger.info(String.format("appendToBootstrapClassPath:%s", jarFile.getName()));
        instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
    }
}
