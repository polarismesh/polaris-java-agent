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

package cn.polarismesh.agent.core.asm.instrument.classloading;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
final class ReflectionDefineClass implements DefineClass {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ReflectionDefineClass.class.getCanonicalName());

    private static final Method DEFINE_CLASS;

    static {
        try {
            DEFINE_CLASS = ClassLoader.class
                    .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_CLASS.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access ClassLoader.defineClass(String, byte[], int, int)", e);
        }
    }

    @Override
    public final Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        try {
            return (Class<?>) DEFINE_CLASS.invoke(classLoader, name, bytes, 0, bytes.length);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw handleDefineClassFail(classLoader, name, e);
        }
    }

    private RuntimeException handleDefineClassFail(ClassLoader classLoader, String className, Exception e) {
        logger.warn(String.format("%s define fail cl:%s Caused by:%s", className, classLoader, e.getMessage()), e);
        return new RuntimeException(className + " define fail Caused by:" + e.getMessage(), e);
    }


}
