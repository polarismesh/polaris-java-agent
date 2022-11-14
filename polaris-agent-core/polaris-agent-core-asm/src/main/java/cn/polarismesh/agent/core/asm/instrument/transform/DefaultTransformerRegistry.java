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

import cn.polarismesh.agent.core.asm.instrument.matcher.ClassNameMatcher;
import cn.polarismesh.agent.core.asm.instrument.matcher.Matcher;
import cn.polarismesh.agent.core.asm.instrument.matcher.MultiClassNameMatcher;
import cn.polarismesh.agent.core.asm.instrument.plugin.MatchableClassFileTransformer;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.lang.instrument.ClassFileTransformer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultTransformerRegistry implements TransformerRegistry {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(DefaultTransformerRegistry.class.getCanonicalName());

    private final Map<String, ClassFileTransformer> registry;

    public DefaultTransformerRegistry(List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
        Objects.requireNonNull(matchableClassFileTransformerList, "matchableClassFileTransformerList");
        this.registry = newRegistry(matchableClassFileTransformerList);
    }

    private Map<String, ClassFileTransformer> newRegistry(
            List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
        final Map<String, ClassFileTransformer> registry = new HashMap<String, ClassFileTransformer>(512);
        for (MatchableClassFileTransformer transformer : matchableClassFileTransformerList) {
            try {
                addTransformer(registry, transformer.getMatcher(), transformer);
            } catch (Exception ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("failed to add transformer %s", transformer), ex);
                }
            }
        }
        return registry;
    }

    private void addTransformer(Map<String, ClassFileTransformer> registry, Matcher matcher,
            ClassFileTransformer transformer) {
        if (matcher instanceof ClassNameMatcher) {
            final ClassNameMatcher classNameMatcher = (ClassNameMatcher) matcher;
            String className = classNameMatcher.getClassName();
            addModifier0(registry, transformer, className);
        } else if (matcher instanceof MultiClassNameMatcher) {
            final MultiClassNameMatcher classNameMatcher = (MultiClassNameMatcher) matcher;
            List<String> classNameList = classNameMatcher.getClassNames();
            for (String className : classNameList) {
                addModifier0(registry, transformer, className);
            }
        } else {
            throw new IllegalArgumentException("unsupported matcher :" + matcher);
        }
    }

    private void addModifier0(Map<String, ClassFileTransformer> registry, ClassFileTransformer transformer,
            String className) {
        final String classInternalName = JavaAssistUtils.javaNameToJvmName(className);
        final ClassFileTransformer old = registry.put(classInternalName, transformer);
        if (old != null) {
            throw new IllegalStateException(
                    "Transformer already exist. className:" + classInternalName + " new:" + transformer.getClass()
                            + " old:" + old.getClass());
        }
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName,
            byte[] classFileBuffer) {
        return registry.get(classInternalName);
    }
}
