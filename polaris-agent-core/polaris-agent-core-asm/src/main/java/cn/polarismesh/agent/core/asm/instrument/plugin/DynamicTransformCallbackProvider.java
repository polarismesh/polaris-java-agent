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

import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.asm.instrument.InstrumentContext;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import java.lang.reflect.Constructor;
import java.util.Objects;

public class DynamicTransformCallbackProvider implements TransformCallbackProvider {

    private final String transformCallbackClassName;
    private final Object[] parameters;
    private final Class<?>[] parameterTypes;

    public DynamicTransformCallbackProvider(String transformCallbackClassName) {
        this.transformCallbackClassName = Objects
                .requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        this.parameters = null;
        this.parameterTypes = null;
    }

    public DynamicTransformCallbackProvider(String transformCallbackClassName, Object[] parameters,
            Class<?>[] parameterTypes) {
        this.transformCallbackClassName = Objects
                .requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public TransformCallback getTransformCallback(InstrumentContext instrumentContext, ClassLoader loader) {
        try {
            final Class<? extends TransformCallback> transformCallbackClass = instrumentContext
                    .injectClass(loader, transformCallbackClassName);
            Constructor<? extends TransformCallback> constructor = transformCallbackClass
                    .getConstructor(parameterTypes);
            return constructor.newInstance(parameters);
        } catch (Exception e) {
            throw new PolarisAgentException(transformCallbackClassName + " load fail Caused by:" + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "DynamicTransformCallbackProvider{" +
                "transformCallbackClassName='" + transformCallbackClassName + '\'' +
                '}';
    }
}
