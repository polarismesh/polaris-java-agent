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

public final class JvmUtils {

    private static final JvmVersion JVM_VERSION = getVersion0();
    private static final JvmType JVM_TYPE = getType0();

    private JvmUtils() {
    }

    public static JvmVersion getVersion() {
        return JVM_VERSION;
    }

    public static JvmType getType() {
        return JVM_TYPE;
    }

    public static boolean supportsVersion(JvmVersion other) {
        return JVM_VERSION.onOrAfter(other);
    }

    public static String getSystemProperty(SystemPropertyKey systemPropertyKey) {
        return System.getProperty(systemPropertyKey.getKey(), "");
    }

    private static JvmVersion getVersion0() {
        String javaVersion = getSystemProperty(SystemPropertyKey.JAVA_SPECIFICATION_VERSION);
        return JvmVersion.getFromVersion(javaVersion);
    }

    private static JvmType getType0() {
        String javaVmName = getSystemProperty(SystemPropertyKey.JAVA_VM_NAME);
        return JvmType.fromVmName(javaVmName);
    }
}
