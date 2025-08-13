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

package cn.polarismesh.agent.core.asm.instrument;

import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinition;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ASMMethodNodeAdapter {

    private final String declaringClassInternalName;
    private final MethodNode methodNode;
    private final ASMMethodVariables methodVariables;

    public ASMMethodNodeAdapter(final String declaringClassInternalName, final MethodNode methodNode) {
        if (declaringClassInternalName == null || methodNode == null) {
            throw new IllegalArgumentException(
                    "declaring class internal name and method annotation must not be null. class="
                            + declaringClassInternalName + ", methodNode=" + methodNode);
        }

        if (methodNode.instructions == null || methodNode.desc == null) {
            throw new IllegalArgumentException(
                    "method annotation's instructions or desc must not be null. class=" + declaringClassInternalName
                            + ", method=" + methodNode.name + methodNode.desc);
        }

        this.declaringClassInternalName = declaringClassInternalName;
        this.methodNode = methodNode;
        this.methodVariables = new ASMMethodVariables(declaringClassInternalName, methodNode);
    }

    public MethodNode getMethodNode() {
        return this.methodNode;
    }

    public String getDeclaringClassInternalName() {
        return this.declaringClassInternalName;
    }

    // find interceptor local variable.
    public boolean hasInterceptor() {
        return this.methodVariables.hasInterceptor();
    }

    public String getName() {
        if (isConstructor()) {
            // simple class name.
            int index = this.declaringClassInternalName.lastIndexOf('/');
            if (index < 0) {
                return this.declaringClassInternalName;
            } else {
                return this.declaringClassInternalName.substring(index + 1);
            }
        }

        return this.methodNode.name;
    }

    public void setName(final String name) {
        if (isConstructor()) {
            // skip.
            return;
        }
        this.methodNode.name = name;
    }

    public String[] getParameterTypes() {
        return this.methodVariables.getParameterTypes();
    }

    public String[] getParameterNames() {
        return this.methodVariables.getParameterNames();
    }

    public String getReturnType() {
        return this.methodVariables.getReturnType();
    }

    public int getAccess() {
        return this.methodNode.access;
    }

    public void setAccess(final int access) {
        this.methodNode.access = access;
    }

    public boolean isConstructor() {
        return this.methodNode.name.equals("<init>");
    }

    public String getDesc() {
        return this.methodNode.desc;
    }

    public int getLineNumber() {
        AbstractInsnNode node = this.methodNode.instructions.getFirst();
        while (node != null) {
            if (node.getType() == AbstractInsnNode.LINE) {
                return ((LineNumberNode) node).line;
            }
            node = node.getNext();
        }

        return 0;
    }

    public List<String> getExceptions() {
        return this.methodNode.exceptions;
    }

    public String getSignature() {
        return this.methodNode.signature;
    }

    public String getLongName() {
        return this.declaringClassInternalName + "/" + getName() + getDesc();
    }

    public boolean isStatic() {
        return (this.methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isAbstract() {
        return (this.methodNode.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public boolean isPrivate() {
        return (this.methodNode.access & Opcodes.ACC_PRIVATE) != 0;
    }

    public boolean isNative() {
        return (this.methodNode.access & Opcodes.ACC_NATIVE) != 0;
    }

    public void addDelegator(final String superClassInternalName) {
        Objects.requireNonNull(superClassInternalName, "superClassInternalName");

        final InsnList instructions = this.methodNode.instructions;
        if (isStatic()) {
            this.methodVariables.initLocalVariables(instructions);
            // load parameters
            this.methodVariables.loadArgs(instructions);
            // invoke static
            instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, superClassInternalName, this.methodNode.name,
                    this.methodNode.desc, false));
        } else {
            this.methodVariables.initLocalVariables(instructions);
            // load this
            this.methodVariables.loadVar(instructions, 0);
            // load parameters
            this.methodVariables.loadArgs(instructions);
            // invoke special
            instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, superClassInternalName, this.methodNode.name,
                    this.methodNode.desc, false));
        }
        // return
        this.methodVariables.returnValue(instructions);
    }

    private void initInterceptorLocalVariables(int interceptorId, InterceptorDefinition interceptorDefinition) {
        final InsnList instructions = new InsnList();
        if (this.methodVariables
                .initInterceptorLocalVariables(instructions, interceptorId, interceptorDefinition)) {
            // if first time.
            this.methodNode.instructions.insertBefore(this.methodVariables.getEnterInsnNode(), instructions);
        }
    }

    public void addBeforeInterceptor(final int interceptorId, final InterceptorDefinition interceptorDefinition) {
        initInterceptorLocalVariables(interceptorId, interceptorDefinition);

        final InsnList instructions = new InsnList();
        this.methodVariables.loadInterceptorLocalVariables(instructions, interceptorDefinition, false);

        final String description = Type.getMethodDescriptor(interceptorDefinition.getBeforeMethod());
        instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                Type.getInternalName(interceptorDefinition.getInterceptorBaseClass()), "before", description, true));
        this.methodNode.instructions.insertBefore(this.methodVariables.getEnterInsnNode(), instructions);
    }

    public void addAfterInterceptor(int interceptorId, InterceptorDefinition interceptorDefinition) {
        initInterceptorLocalVariables(interceptorId, interceptorDefinition);

        // add try catch block.
        final ASMTryCatch tryCatch = new ASMTryCatch(this.methodNode);
        this.methodNode.instructions
                .insertBefore(this.methodVariables.getEnterInsnNode(), tryCatch.getStartLabelNode());
        this.methodNode.instructions.insert(this.methodVariables.getExitInsnNode(), tryCatch.getEndLabelNode());

        // find return.
        AbstractInsnNode insnNode = this.methodNode.instructions.getFirst();
        while (insnNode != null) {
            final int opcode = insnNode.getOpcode();
            if (this.methodVariables.isReturnCode(opcode)) {
                final InsnList instructions = new InsnList();
                this.methodVariables.storeResultVar(instructions, opcode);
                invokeAfterInterceptor(instructions, interceptorDefinition, false);
                this.methodNode.instructions.insertBefore(insnNode, instructions);
            }
            insnNode = insnNode.getNext();
        }

        // try catch handler.
        InsnList instructions = new InsnList();
        this.methodVariables.storeThrowableVar(instructions);
        invokeAfterInterceptor(instructions, interceptorDefinition, true);
        // throw exception.
        this.methodVariables.loadInterceptorThrowVar(instructions);
        this.methodNode.instructions.insert(tryCatch.getEndLabelNode(), instructions);
        tryCatch.sort();
    }

    private void invokeAfterInterceptor(final InsnList instructions, final InterceptorDefinition interceptorDefinition,
            final boolean throwable) {
        this.methodVariables.loadInterceptorLocalVariables(instructions, interceptorDefinition, true);
        final String description = Type.getMethodDescriptor(interceptorDefinition.getAfterMethod());
        instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                Type.getInternalName(interceptorDefinition.getInterceptorBaseClass()), "after", description, true));
    }
}