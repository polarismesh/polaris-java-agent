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

package cn.polarismesh.agent.core.asm.instrument.transform;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.CodeSourceUtils;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.lang.instrument.ClassFileTransformer;
import java.net.URL;
import java.security.ProtectionDomain;

public class BaseClassFileTransformer {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(BaseClassFileTransformer.class.getCanonicalName());

    private final ClassLoader agentClassLoader;

    public BaseClassFileTransformer(ClassLoader agentClassLoader) {
        this.agentClassLoader = agentClassLoader;
    }

    public byte[] transform(ClassLoader classLoader, String classInternalName, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classFileBuffer, ClassFileTransformer transformer) {
        final String className = JavaAssistUtils.jvmNameToJavaName(classInternalName);

        final URL codeLocation = CodeSourceUtils.getCodeLocation(protectionDomain);
        final String transform = getTransformState(classBeingRedefined);
        logger.info(String.format("[%s] classLoader:%s className:%s transformer:%s codeSource:%s",
                transform, classLoader, className, transformer.getClass().getName(), codeLocation));

        try {
            final Thread thread = Thread.currentThread();
            final ClassLoader before = getContextClassLoader(thread);
            thread.setContextClassLoader(this.agentClassLoader);
            try {
                return transformer
                        .transform(classLoader, className, classBeingRedefined, protectionDomain, classFileBuffer);
            } finally {
                // The context class loader have to be recovered even if it was null.
                thread.setContextClassLoader(before);
            }
        } catch (Throwable e) {
            logger.warn(String.format(
                    "Transformer:%s threw an exception. codeLocation:%s cl:%s ctxCl:%s agentCl:%s Cause:%s",
                    transformer.getClass().getName(), codeLocation, classLoader,
                    Thread.currentThread().getContextClassLoader(), agentClassLoader, e.getMessage()), e);
            return null;
        }
    }

    private String getTransformState(Class<?> classBeingRedefined) {
        if (classBeingRedefined == null) {
            return "transform";
        }
        return "retransform";
    }

    private ClassLoader getContextClassLoader(Thread thread) throws Throwable {
        try {
            return thread.getContextClassLoader();
        } catch (SecurityException se) {
            throw se;
        } catch (Throwable th) {
            logger.warn(String.format("getContextClassLoader %s. Caused:%s", th.getMessage(), th.getMessage()));
            throw th;
        }
    }

}
