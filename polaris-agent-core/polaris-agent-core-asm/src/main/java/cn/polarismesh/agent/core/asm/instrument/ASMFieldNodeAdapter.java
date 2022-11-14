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

package cn.polarismesh.agent.core.asm.instrument;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

public class ASMFieldNodeAdapter {

    private final FieldNode fieldNode;

    public ASMFieldNodeAdapter(final FieldNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    public String getName() {
        return this.fieldNode.name;
    }

    public String getClassName() {
        Type type = Type.getType(this.fieldNode.desc);
        return type.getClassName();
    }

    public String getDesc() {
        return this.fieldNode.desc;
    }

    public boolean isStatic() {
        return (this.fieldNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isFinal() {
        return (this.fieldNode.access & Opcodes.ACC_FINAL) != 0;
    }

    public void setAccess(final int access) {
        this.fieldNode.access = access;
    }

    public int getAccess() {
        return this.fieldNode.access;
    }
}
