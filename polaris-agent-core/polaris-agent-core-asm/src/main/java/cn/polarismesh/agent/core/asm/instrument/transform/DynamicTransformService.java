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

import cn.polarismesh.agent.core.asm.instrument.DynamicTransformTrigger;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Objects;

public class DynamicTransformService implements DynamicTransformTrigger {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(DynamicTransformService.class.getCanonicalName());

    private final Instrumentation instrumentation;

    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    public DynamicTransformService(Instrumentation instrumentation,
            DynamicTransformerRegistry dynamicTransformerRegistry) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
    }

    @Override
    public void retransform(Class<?> target, ClassFileTransformer transformer) {
        logger.info(String.format("retransform request class: %s", target.getName()));
        assertClass(target);

        final RequestHandle requestHandle = this.dynamicTransformerRegistry.onRetransformRequest(target, transformer);
        boolean success = false;
        try {
            triggerRetransform(target);
            success = true;
        } finally {
            if (!success) {
                requestHandle.cancel();
            }
        }
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName,
            ClassFileTransformer transformer) {
        logger.info(
                String.format("Add dynamic transform. classLoader=%s, class=%s", classLoader, targetClassName));
        this.dynamicTransformerRegistry.onTransformRequest(classLoader, targetClassName, transformer);
    }

    private void assertClass(Class<?> target) {
        if (!instrumentation.isModifiableClass(target)) {
            throw new PolarisAgentException("Target class " + target + " is not modifiable");
        }
        final JvmVersion version = JvmUtils.getVersion();
        if (JvmVersion.JAVA_8.compareTo(version) == 0) {
            // If the version is java 8
            // Java 8 bug - NoClassDefFound error in transforming lambdas(https://bugs.openjdk.java.net/browse/JDK-8145964)
            final String className = target.getName();
            if (className.contains("$$Lambda$")) {
                throw new PolarisAgentException(
                        "Target class " + target + " is lambda class, Causes NoClassDefFound error in java 8.");
            }
        }
    }

    private void triggerRetransform(Class<?> target) {
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new PolarisAgentException(e);
        }
    }

}
