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

package cn.polarismesh.agent.core.asm.instrument.classreading;

import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.util.Collections;
import java.util.List;

public class DefaultSimpleClassMetadata implements SimpleClassMetadata {

    private final int version;

    private final int accessFlag;

    private final String className;

    private final String superClassName;

    private final List<String> interfaceNames;

    private final byte[] classBinary;

    public DefaultSimpleClassMetadata(final int version, final int accessFlag, final String classInternalName,
            final String superClassInternalName, final List<String> interfaceInternalNames, final byte[] classBinary) {
        this.version = version;
        this.accessFlag = accessFlag;
        this.className = JavaAssistUtils.jvmNameToJavaName(classInternalName);

        this.superClassName = defaultSuperClassName(superClassInternalName);
        this.interfaceNames = defaultInterfaceName(interfaceInternalNames);

        this.classBinary = classBinary;
    }

    private String defaultSuperClassName(String superClassInternalName) {
        if (superClassInternalName == null) {
            return null;
        }
        return JavaAssistUtils.jvmNameToJavaName(superClassInternalName);
    }

    private List<String> defaultInterfaceName(List<String> interfaceInternalNames) {
        if (interfaceInternalNames == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(JavaAssistUtils.jvmNameToJavaName(interfaceInternalNames));
    }

    @Override
    public int getVersion() {
        return version;
    }

    public int getAccessFlag() {
        return accessFlag;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuperClassName() {
        return superClassName;
    }

    @Override
    public List<String> getInterfaceNames() {
        return interfaceNames;
    }

    @Override
    public byte[] getClassBinary() {
        return classBinary;
    }
}
