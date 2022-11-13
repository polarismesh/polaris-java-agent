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

public enum SystemPropertyKey {

    JAVA_VERSION("java.version"),
    JAVA_RUNTIME_VERSION("java.runtime.version"),
    JAVA_RUNTIME_NAME("java.runtime.name"),
    JAVA_SPECIFICATION_VERSION("java.specification.version"),
    JAVA_CLASS_VERSION("java.class.version"),
    JAVA_VM_NAME("java.vm.name"),
    JAVA_VM_VERSION("java.vm.version"),
    JAVA_VM_INFO("java.vm.info"),
    JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version"),
    SUN_JAVA_COMMAND("sun.java.command"), // May be unsupported depending on the JVM.
    OS_NAME("os.name");

    private final String key;

    SystemPropertyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
