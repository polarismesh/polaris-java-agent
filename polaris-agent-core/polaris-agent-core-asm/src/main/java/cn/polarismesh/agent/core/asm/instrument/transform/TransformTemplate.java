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

import cn.polarismesh.agent.core.asm.instrument.InstrumentContext;
import cn.polarismesh.agent.core.asm.instrument.matcher.Matcher;
import cn.polarismesh.agent.core.asm.instrument.matcher.Matchers;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import java.util.Objects;

public class TransformTemplate implements TransformOperations {

    private final InstrumentContext instrumentContext;

    public TransformTemplate(InstrumentContext instrumentContext) {
        this.instrumentContext = Objects.requireNonNull(instrumentContext, "instrumentContext");
    }

    protected InstrumentContext getInstrumentContext() {
        return instrumentContext;
    }

    @Override
    public void transform(String className, Class<? extends TransformCallback> transformCallbackClass) {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(transformCallbackClass, "transformCallbackClass");

        final Matcher matcher = Matchers.newClassNameMatcher(className);

        TransformCallbackChecker.validate(transformCallbackClass);

        // release class reference
        final String transformCallbackName = transformCallbackClass.getName();
        this.instrumentContext.addClassFileTransformer(matcher, transformCallbackName);
    }

    @Override
    public void transform(String className, Class<? extends TransformCallback> transformCallbackClass,
            Object[] parameters, Class<?>[] parameterTypes) {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(transformCallbackClass, "transformCallbackClass");

        TransformCallbackChecker.validate(transformCallbackClass, parameterTypes);
        if (ParameterUtils.hasNull(parameterTypes)) {
            throw new IllegalArgumentException("null parameterType not supported");
        }
        ParameterUtils.checkParameterType(parameterTypes);

        final Matcher matcher = Matchers.newClassNameMatcher(className);

        // release class reference
        final String transformCallbackName = transformCallbackClass.getName();
        this.instrumentContext.addClassFileTransformer(matcher, transformCallbackName, parameters, parameterTypes);
    }
}
