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

import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import java.lang.reflect.Method;

public class InvokeAfterCodeGenerator extends InvokeCodeGenerator {

    private static final int THIS_RETURN_EXCEPTION_SIZE = 3;

    private final int interceptorId;
    private final InterceptorDefinition interceptorDefinition;
    private final InstrumentClass targetClass;
    private final boolean localVarsInitialized;
    private final boolean catchClause;

    public InvokeAfterCodeGenerator(int interceptorId,
            InterceptorDefinition interceptorDefinition,
            InstrumentClass targetClass, InstrumentMethod targetMethod,
            boolean localVarsInitialized, boolean catchClause) {
        super(interceptorId, interceptorDefinition, targetMethod);
        this.interceptorDefinition = interceptorDefinition;
        this.interceptorId = interceptorId;
        this.targetClass = targetClass;
        this.localVarsInitialized = localVarsInitialized;
        this.catchClause = catchClause;
    }

    public String generate() {
        final CodeBuilder builder = new CodeBuilder();

        builder.begin();

        if (!localVarsInitialized) {
            builder.format("%1$s = %2$s.getInterceptor(%3$d); ", getInterceptorVar(), getInterceptorRegistryClassName(),
                    interceptorId);
        }
        final Method afterMethod = interceptorDefinition.getAfterMethod();
        if (afterMethod != null) {
            builder.format("((%1$s)%2$s).after(", getInterceptorType(), getInterceptorVar());
            appendArguments(builder);
            builder.format(");");
        }

        if (catchClause) {
            builder.append(" throw $e;");
        }

        builder.end();

        return builder.toString();

    }

    private String getReturnValue() {
        if (catchClause) {
            return "null";
        }

        if (!targetMethod.isConstructor()) {
            if ("void".equals(targetMethod.getReturnType())) {
                return "null";
            }
        }

        return "($w)$_";
    }

    private String getException() {
        if (catchClause) {
            return "$e";
        }

        return "null";
    }

    private void appendArguments(CodeBuilder builder) {
        final InterceptorType type = interceptorDefinition.getInterceptorType();
        if (type == InterceptorType.ARRAY_ARGS) {
            appendSimpleAfterArguments(builder);
        }
    }

    private void appendSimpleAfterArguments(
            CodeBuilder builder) {
        builder.format("%1$s, %2$s, %3$s, %4$s", getTarget(), getArguments(), getReturnValue(), getException());
    }

}
