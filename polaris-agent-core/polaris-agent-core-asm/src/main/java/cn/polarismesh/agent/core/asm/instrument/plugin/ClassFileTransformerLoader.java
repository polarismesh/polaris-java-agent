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
import cn.polarismesh.agent.core.asm.instrument.matcher.Matcher;
import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassFileTransformerLoader {

    private final DynamicTransformTrigger dynamicTransformTrigger;

    private final List<ClassFileTransformer> classTransformers = new ArrayList<>();

    public ClassFileTransformerLoader(DynamicTransformTrigger dynamicTransformTrigger) {
        this.dynamicTransformTrigger = Objects.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger");
    }

    public void addClassFileTransformer(InstrumentContext instrumentContext, final Matcher matcher,
            final TransformCallbackProvider transformCallbackProvider) {
        Objects.requireNonNull(instrumentContext, "instrumentContext");
        Objects.requireNonNull(transformCallbackProvider, "transformCallbackProvider");

        final MatchableClassFileTransformer guard = new MatchableClassFileTransformerDelegate(instrumentContext,
                matcher, transformCallbackProvider);
        classTransformers.add(guard);
    }


    public void addClassFileTransformer(InstrumentContext instrumentContext, ClassLoader classLoader,
            String targetClassName, TransformCallbackProvider transformCallbackProvider) {
        Objects.requireNonNull(targetClassName, "targetClassName");
        Objects.requireNonNull(transformCallbackProvider, "transformCallbackProvider");

        final ClassFileTransformerDelegate classFileTransformerGuardDelegate = new ClassFileTransformerDelegate(
                instrumentContext, transformCallbackProvider);

        this.dynamicTransformTrigger
                .addClassFileTransformer(classLoader, targetClassName, classFileTransformerGuardDelegate);
    }

    public List<ClassFileTransformer> getClassTransformerList() {
        return classTransformers;
    }
}
