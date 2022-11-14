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

import cn.polarismesh.agent.core.asm.instrument.plugin.MatchableClassFileTransformer;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class DefaultClassFileTransformerDispatcher implements ClassFileTransformer {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(DefaultClassFileTransformerDispatcher.class.getCanonicalName());

    private final List<ClassFileFilter> classFileFilters = new ArrayList<>();

    private final TransformerRegistry transformerRegistry;

    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    private final BaseClassFileTransformer baseClassFileTransformer;

    public DefaultClassFileTransformerDispatcher(List<ClassFileTransformer> classFileTransformers,
            DynamicTransformerRegistry dynamicTransformerRegistry) {
        this.baseClassFileTransformer = new BaseClassFileTransformer(this.getClass().getClassLoader());
        //this.classFileFilters.add(new DefaultClassloaderFilter(this.getClass().getClassLoader()));
        this.classFileFilters.add(new DefaultClassFilter());
        this.classFileFilters.add(new UnmodifiableClassFilter());
        this.transformerRegistry = newTransformerRegistry(classFileTransformers);
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
    }

    private TransformerRegistry newTransformerRegistry(List<ClassFileTransformer> classFileTransformers) {
        return new DefaultTransformerRegistry(getMatchableTransformers(classFileTransformers));
    }

    private List<MatchableClassFileTransformer> getMatchableTransformers(
            List<ClassFileTransformer> classFileTransformers) {
        final List<MatchableClassFileTransformer> matcherList = new ArrayList<>();
        for (ClassFileTransformer transformer : classFileTransformers) {
            if (transformer instanceof MatchableClassFileTransformer) {
                final MatchableClassFileTransformer t = (MatchableClassFileTransformer) transformer;
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("registering class file transformer %s for %s ", t, t.getMatcher()));
                }
                matcherList.add(t);
            } else {
                logger.warn(String.format("ignore class file transformer %s", transformer));
            }
        }
        return matcherList;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        for (ClassFileFilter classFileFilter : classFileFilters) {
            if (!classFileFilter.accept(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)) {
                return null;
            }
        }
        ClassFileTransformer dynamicTransformer = dynamicTransformerRegistry.getTransformer(loader, className);
        if (dynamicTransformer != null) {
            return baseClassFileTransformer
                    .transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer,
                            dynamicTransformer);
        }
        ClassFileTransformer transformer = this.transformerRegistry
                .findTransformer(loader, className, classfileBuffer);
        if (transformer == null) {
            return null;
        }

        return baseClassFileTransformer
                .transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer,
                        transformer);

    }
}
