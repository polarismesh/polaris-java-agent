/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.asm9.instrument;


import cn.polarismesh.agent.core.asm.instrument.JavaLangAccess;
import cn.polarismesh.agent.core.asm.instrument.classloading.DefineClass;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;

public class Java9DefineClass implements DefineClass {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(Java9DefineClass.class.getCanonicalName());

    @Override
    public final Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("define class:%s cl:%s", name, classLoader));
        }
        System.out.println(String.format("define class:%s cl:%s", name, classLoader));
        final JavaLangAccess javaLangAccess = JavaLangAccessHelper.getJavaLangAccess();
        try {
            return javaLangAccess.defineClass(classLoader, name, bytes, null, null);
        } catch (Throwable e) {
            logger.warn(String.format("{} define fail cl:%s Caused by:%s", name, classLoader, e.getMessage()), e);
            throw new RuntimeException(name + " define fail Caused by:" + e.getMessage(), e);
        }
    }
}
