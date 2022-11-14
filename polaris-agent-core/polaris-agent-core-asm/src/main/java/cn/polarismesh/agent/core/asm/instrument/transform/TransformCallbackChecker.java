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
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import java.lang.reflect.Modifier;
import java.util.Objects;

public final class TransformCallbackChecker {

    private TransformCallbackChecker() {
    }

    public static void validate(Class<? extends TransformCallback> transformCallbackClass) {
        validate(transformCallbackClass, null);
    }

    public static void validate(Class<? extends TransformCallback> transformCallbackClass, Class<?>[] parameterTypes) {
        Objects.requireNonNull(transformCallbackClass, "transformCallbackClass");

        // check inner class
        final Class<?> enclosingClass = transformCallbackClass.getEnclosingClass();
        if (enclosingClass != null) {
            // inner class state

            // check static class
            int modifiers = transformCallbackClass.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                throw new PolarisAgentException(
                        "transformCallbackClass must be static inner class. class:" + transformCallbackClass.getName());
            }
        }

        try {
            // check default constructor
            transformCallbackClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new PolarisAgentException("constructor not found " + transformCallbackClass.getName(), e);
        }
    }
}
