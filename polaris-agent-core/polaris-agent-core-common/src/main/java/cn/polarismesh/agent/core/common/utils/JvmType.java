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

import java.util.EnumSet;

public enum JvmType {
    UNKNOWN(null),
    // ibm-j9 java.vm.name=IBM J9 VM;
    // openj9 java.vm.name=Eclipse OpenJ9 VM
    IBM("J9"),
    OPENJDK("OpenJDK"),
    ORACLE("HotSpot");

    private final String inclusiveString;

    private static final EnumSet<JvmType> JVM_TYPE = EnumSet.allOf(JvmType.class);

    JvmType(String inclusiveString) {
        this.inclusiveString = inclusiveString;
    }

    public static JvmType fromVendor(String vendorName) {
        if (vendorName == null) {
            return UNKNOWN;
        }
        final String vendorNameTrimmed = vendorName.trim();
        for (JvmType jvmType : JVM_TYPE) {
            if (jvmType.toString().equalsIgnoreCase(vendorNameTrimmed)) {
                return jvmType;
            }
        }
        return UNKNOWN;
    }

    public static JvmType fromVmName(String vmName) {
        if (vmName == null) {
            return UNKNOWN;
        }
        for (JvmType jvmType : JVM_TYPE) {
            if (jvmType.inclusiveString == null) {
                continue;
            }
            if (vmName.contains(jvmType.inclusiveString)) {
                return jvmType;
            }
        }
        return UNKNOWN;
    }
}
