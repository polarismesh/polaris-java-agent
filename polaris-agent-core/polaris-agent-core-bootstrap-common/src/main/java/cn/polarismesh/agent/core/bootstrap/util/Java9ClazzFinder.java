/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.core.bootstrap.util;

import cn.polarismesh.agent.core.common.starter.InstrumentationStarter;

import java.lang.reflect.Constructor;

public class Java9ClazzFinder {

    private static final String CLAZZ_NAME_JAVA9_STARTER = "cn.polarismesh.agent.core.asm9.starter.Java9InstrumentationStarter";

    private static InstrumentationStarter instrumentationStarter;


    public static InstrumentationStarter lookup(ClassLoader clazzLoader) {
        synchronized (Java9ClazzFinder.class) {
            if (instrumentationStarter != null) {
                return instrumentationStarter;
            } else {
                final Class<InstrumentationStarter> javaModuleFactory = getClazz(CLAZZ_NAME_JAVA9_STARTER, clazzLoader);
                try {
                    Constructor<InstrumentationStarter> constructor = javaModuleFactory.getDeclaredConstructor();
                    instrumentationStarter = constructor.newInstance();
                    return instrumentationStarter;
                } catch (Exception e) {
                    throw new IllegalStateException("JavaModuleFactory() invoke fail Caused by:" + e.getMessage(), e);
                }
            }
        }
    }

    private static <T> Class<T> getClazz(String clazzName, ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(clazzName, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(clazzName + " not found");
        }
    }
}
