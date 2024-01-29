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

package cn.polarismesh.agent.core.asm.instrument.interceptor;

import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.registry.InterceptorRegistry;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class InvokeCodeGenerator {

    protected final InterceptorDefinition interceptorDefinition;
    protected final InstrumentMethod targetMethod;
    protected final int interceptorId;

    public InvokeCodeGenerator(int interceptorId, InterceptorDefinition interceptorDefinition,
            InstrumentMethod targetMethod) {
        this.interceptorId = interceptorId;
        this.interceptorDefinition = Objects.requireNonNull(interceptorDefinition, "interceptorDefinition");
        this.targetMethod = Objects.requireNonNull(targetMethod, "targetMethod");

    }

    protected String getInterceptorType() {
        return interceptorDefinition.getInterceptorBaseClass().getName();
    }

    protected String getParameterTypes() {
        String[] parameterTypes = targetMethod.getParameterTypes();
        return JavaAssistUtils.getParameterDescription(parameterTypes);
    }

    protected String getTarget() {
        return Modifier.isStatic(targetMethod.getModifiers()) ? "null" : "this";
    }

    protected String getArguments() {
        if (targetMethod.getParameterTypes().length == 0) {
            return "null";
        }

        return "$args";
    }

    protected String getInterceptorRegistryClassName() {
        return InterceptorRegistry.class.getName();
    }

    protected String getInterceptorVar() {
        return getInterceptorVar(interceptorId);
    }

    public static String getInterceptorVar(int interceptorId) {
        return "_$PINPOINT$_interceptor" + interceptorId;
    }
}