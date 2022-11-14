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

import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.lang.instrument.ClassFileTransformer;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultDynamicTransformerRegistry implements DynamicTransformerRegistry {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(DefaultDynamicTransformerRegistry.class.getCanonicalName());
    private final ConcurrentMap<TransformerKey, ClassFileTransformer> transformerMap = new ConcurrentHashMap<TransformerKey, ClassFileTransformer>();

    public DefaultDynamicTransformerRegistry() {
    }

    @Override
    public RequestHandle onRetransformRequest(Class<?> target, final ClassFileTransformer transformer) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(transformer, "transformer");

        final TransformerKey key = createTransformKey(target);
        add(key, transformer);
        logger.info(String.format("added retransformer classLoader: %s, class: %s, registry size: %s",
                target.getClassLoader(), target.getName(), transformerMap.size()));
        return new DefaultRequestHandle(key);
    }

    @Override
    public void onTransformRequest(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {

        final TransformerKey transformKey = createTransformKey(classLoader, targetClassName);
        add(transformKey, transformer);

        logger.info(String.format("added dynamic transformer classLoader: %s, className: %s, registry size: %d",
                classLoader, targetClassName, transformerMap.size()));
    }

    private void add(TransformerKey key, ClassFileTransformer transformer) {
        final ClassFileTransformer prev = transformerMap.putIfAbsent(key, transformer);

        if (prev != null) {
            throw new PolarisAgentException(
                    "Transformer already exists. TransformKey: " + key + ", transformer: " + prev);
        }
    }

    private TransformerKey createTransformKey(ClassLoader classLoader, String targetClassName) {
        final String classInternName = JavaAssistUtils.javaNameToJvmName(targetClassName);
        return new TransformerKey(classLoader, classInternName);
    }

    private TransformerKey createTransformKey(Class<?> targetClass) {

        final ClassLoader classLoader = targetClass.getClassLoader();
        final String targetClassName = targetClass.getName();

        return createTransformKey(classLoader, targetClassName);
    }

    @Override
    public ClassFileTransformer getTransformer(ClassLoader classLoader, String targetClassName) {
        if (null == classLoader || transformerMap.isEmpty()) {
            return null;
        }

        final TransformerKey key = new TransformerKey(classLoader, targetClassName);
        final ClassFileTransformer transformer = transformerMap.remove(key);
        if (transformer != null) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        String.format("removed dynamic transformer classLoader: %s, className: %s, registry size: %d",
                                classLoader, targetClassName, transformerMap.size()));
            }
        }

        return transformer;
    }

    int size() {
        return transformerMap.size();
    }

    private static final class TransformerKey {

        // TODO defense classLoader memory leak
        private final ClassLoader classLoader;
        private final String targetClassInternalName;

        public TransformerKey(ClassLoader classLoader, String targetClassInternalName) {
            this.classLoader = classLoader;
            this.targetClassInternalName = Objects.requireNonNull(targetClassInternalName, "targetClassInternalName");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TransformerKey that = (TransformerKey) o;

            if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) {
                return false;
            }
            return targetClassInternalName.equals(that.targetClassInternalName);

        }

        @Override
        public int hashCode() {
            int result = classLoader != null ? classLoader.hashCode() : 0;
            result = 31 * result + targetClassInternalName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TransformerKey{" +
                    "classLoader=" + classLoader +
                    ", targetClassInternalName='" + targetClassInternalName + '\'' +
                    '}';
        }
    }

    private class DefaultRequestHandle implements RequestHandle {

        private final TransformerKey key;

        public DefaultRequestHandle(TransformerKey key) {
            this.key = Objects.requireNonNull(key, "key");
        }

        @Override
        public boolean cancel() {
            final ClassFileTransformer remove = transformerMap.remove(key);
            if (remove == null) {
                return false;
            }
            return true;
        }
    }

}
