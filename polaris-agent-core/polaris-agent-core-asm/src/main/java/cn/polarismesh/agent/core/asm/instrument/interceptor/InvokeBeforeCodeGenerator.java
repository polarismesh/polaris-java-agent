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

public class InvokeBeforeCodeGenerator extends InvokeCodeGenerator {

    private final int interceptorId;
    private final InstrumentClass targetClass;

    public InvokeBeforeCodeGenerator(int interceptorId, InterceptorDefinition interceptorDefinition,
            InstrumentClass targetClass, InstrumentMethod targetMethod) {
        super(interceptorId, interceptorDefinition, targetMethod);
        this.interceptorId = interceptorId;
        this.targetClass = targetClass;
    }

    public String generate() {
        final CodeBuilder builder = new CodeBuilder();

        builder.begin();

        builder.format("%1$s = %2$s.getInterceptor(%3$d); ", getInterceptorVar(), getInterceptorRegistryClassName(),
                interceptorId);

        final Method beforeMethod = interceptorDefinition.getBeforeMethod();
        if (beforeMethod != null) {
            builder.format("((%1$s)%2$s).before(", getInterceptorType(), getInterceptorVar());
            appendArguments(builder);
            builder.format(");");
        }

        builder.end();

        return builder.toString();
    }

    private void appendArguments(CodeBuilder builder) {
        final InterceptorType type = interceptorDefinition.getInterceptorType();
        if (type == InterceptorType.ARRAY_ARGS) {
            appendSimpleBeforeArguments(builder);
        }
    }

    private void appendSimpleBeforeArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$s", getTarget(), getArguments());
    }

}
