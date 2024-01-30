/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.asm9.instrument;

import cn.polarismesh.agent.core.asm.instrument.JavaLangAccess;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.SystemPropertyKey;
import cn.polarismesh.agent.core.optional9.instrument.JavaLangAccess9;

import java.lang.reflect.Constructor;

/**
 * @author jaehong.kim
 */
public class JavaLangAccessHelper {
    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(JavaLangAccessHelper.class.getCanonicalName());
    // Java 9 version over and after
    private static final String MISC_SHARED_SECRETS_CLASS_NAME = "jdk.internal.misc.SharedSecrets";
    private static final String MISC_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.misc.JavaLangAccess";
    // Java 12 version over and after
    private static final String ACCESS_SHARED_SECRETS_CLASS_NAME = "jdk.internal.access.SharedSecrets";
    private static final String ACCESS_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.access.JavaLangAccess";

    private static final String CLAZZ_NAME_JAVA_LANG_ACCESS17 = "cn.polarismesh.agent.core.optional17.instrument.JavaLangAccess17";

    private static final JavaLangAccess JAVA_LANG_ACCESS = newJavaLangAccessor();

    private JavaLangAccessHelper() {
    }

    public static JavaLangAccess getJavaLangAccess() {
        return JAVA_LANG_ACCESS;
    }

    // for debugging
    private static void dumpJdkInfo() {
        logger.warn(String.format("Dump JDK info java.vm.name:%s java.version:%s",
                JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VM_NAME), JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VM_VERSION)));
    }

    private static JavaLangAccess createJavaLangAccess17() throws Exception {
        Class<JavaLangAccess> clazz = getClazz(CLAZZ_NAME_JAVA_LANG_ACCESS17, JavaLangAccessHelper.class.getClassLoader());
        Constructor<JavaLangAccess> constructor = clazz.getDeclaredConstructor();
        return constructor.newInstance();
    }

    private static <T> Class<T> getClazz(String clazzName, ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(clazzName, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(clazzName + " not found");
        }
    }


    private static JavaLangAccess newJavaLangAccessor()  {
        try {
            Class.forName(MISC_JAVA_LANG_ACCESS_CLASS_NAME, false, JavaLangAccess.class.getClassLoader());
            return new JavaLangAccess9();
        } catch (ClassNotFoundException exception) {
            logger.error("fail to create JavaLangAccess9, error: " + exception.getMessage());
        }
        try {
            // https://github.com/naver/pinpoint/issues/6752
            // Oracle JDK11 : jdk.internal.access
            // openJDK11 =  jdk.internal.misc
            Class.forName(ACCESS_SHARED_SECRETS_CLASS_NAME, false, JavaLangAccess.class.getClassLoader());
            return createJavaLangAccess17();
        } catch (Exception exception) {
            logger.error("fail to create JavaLangAccess17, error: " + exception.getMessage());
        }

        dumpJdkInfo();
        throw new IllegalStateException("JavaLangAccess not found");
    }
}