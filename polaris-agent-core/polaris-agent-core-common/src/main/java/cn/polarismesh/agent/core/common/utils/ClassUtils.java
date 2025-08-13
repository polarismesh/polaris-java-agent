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

package cn.polarismesh.agent.core.common.utils;

import java.util.Objects;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;

public class ClassUtils {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ClassUtils.class.getCanonicalName());

    public static String getPackageName(String fqcn, char packageSeparator, String defaultValue) {
        Objects.requireNonNull(fqcn, "fqcn");

        final int lastPackageSeparatorIndex = fqcn.lastIndexOf(packageSeparator);
        if (lastPackageSeparatorIndex == -1) {
            return defaultValue;
        }
        return fqcn.substring(0, lastPackageSeparatorIndex);
    }

    public static <T> Class<T> getClazz(String clazzName, ClassLoader classLoader) {
        if (null == classLoader) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            return (Class<T>) Class.forName(clazzName, false, classLoader);
        } catch (ClassNotFoundException e) {
            logger.info(String.format("class %s not found", clazzName));
        }
        return null;
    }

}
