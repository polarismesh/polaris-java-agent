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

import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;
import java.lang.reflect.Constructor;

/**
 * @author Woonduk Kang(emeroad)
 */
final class DefineClassFactory {

    private static final DefineClass defineClass = newDefineClass();

    private static DefineClass newDefineClass() {
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            final ClassLoader agentClassLoader = DefineClassFactory.class.getClassLoader();
            final String name = "cn.polarismesh.agent.core.asm9.instrument.Java9DefineClass";
            try {
                Class<DefineClass> defineClassClazz = (Class<DefineClass>) agentClassLoader.loadClass(name);
                Constructor<DefineClass> constructor = defineClassClazz.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(name + " create fail Caused by:" + e.getMessage(), e);
            }
        }

        return new ReflectionDefineClass();
    }

    static DefineClass getDefineClass() {
        return defineClass;
    }

}
