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

package cn.polarismesh.agent.core.common.utils;

import java.util.Objects;

public final class ReflectionUtils {

    public static final String ARRAY_POSTFIX = "[]";

    private ReflectionUtils() {
    }

    public static String getParameterTypeName(Class<?> parameterType) {
        Objects.requireNonNull(parameterType, "parameterType");

        if (!parameterType.isArray()) {
            return parameterType.getName();
        }

        int arrayDepth = 0;
        while (parameterType.isArray()) {
            parameterType = parameterType.getComponentType();
            arrayDepth++;
        }

        final int bufferSize = getBufferSize(parameterType.getName(), arrayDepth);
        final StringBuilder buffer = new StringBuilder(bufferSize);

        buffer.append(parameterType.getName());
        for (int i = 0; i < arrayDepth; i++) {
            buffer.append(ARRAY_POSTFIX);
        }
        return buffer.toString();
    }

    private static int getBufferSize(String paramTypeName, int arrayDepth) {
        return paramTypeName.length() + (ARRAY_POSTFIX.length() * arrayDepth);
    }
}
