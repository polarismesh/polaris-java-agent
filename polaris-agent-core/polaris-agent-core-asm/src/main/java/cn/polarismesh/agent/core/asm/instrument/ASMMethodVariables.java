/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.asm.instrument;

import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinition;
import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorType;
import cn.polarismesh.agent.core.extension.registry.InterceptorRegistry;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author jaehong.kim
 */
public class ASMMethodVariables {

    private static final Type BYTE_TYPE = Type.getObjectType("java/lang/Byte");

    private static final Type BOOLEAN_TYPE = Type.getObjectType("java/lang/Boolean");

    private static final Type SHORT_TYPE = Type.getObjectType("java/lang/Short");

    private static final Type CHARACTER_TYPE = Type.getObjectType("java/lang/Character");

    private static final Type INTEGER_TYPE = Type.getObjectType("java/lang/Integer");

    private static final Type FLOAT_TYPE = Type.getObjectType("java/lang/Float");

    private static final Type LONG_TYPE = Type.getObjectType("java/lang/Long");

    private static final Type DOUBLE_TYPE = Type.getObjectType("java/lang/Double");

    private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

    private static final Comparator<LocalVariableNode> INDEX_COMPARATOR = new Comparator<LocalVariableNode>() {
        @Override
        public int compare(LocalVariableNode o1, LocalVariableNode o2) {
            return Integer.compare(o1.index, o2.index);
        }
    };

    private final LabelNode interceptorVariableStartLabelNode = new LabelNode();
    private final LabelNode interceptorVariableEndLabelNode = new LabelNode();

    private final String declaringClassInternalName;
    private final MethodNode methodNode;
    private final Type[] argumentTypes;
    private final Type returnType;

    private boolean initializedInterceptorLocalVariables = false;
    private AbstractInsnNode enterInsnNode;
    private AbstractInsnNode exitInsnNode;

    private int nextLocals;

    private int interceptorVarIndex;
    private int argsVarIndex;

    private int resultVarIndex;
    private int throwableVarIndex;

    public ASMMethodVariables(final String declaringClassInternalName, final MethodNode methodNode) {
        this.declaringClassInternalName = declaringClassInternalName;
        this.methodNode = methodNode;
        this.nextLocals = methodNode.maxLocals;
        this.argumentTypes = Type.getArgumentTypes(methodNode.desc);
        this.returnType = Type.getReturnType(methodNode.desc);
    }

    public AbstractInsnNode getEnterInsnNode() {
        return enterInsnNode;
    }

    public AbstractInsnNode getExitInsnNode() {
        return exitInsnNode;
    }

    public String[] getParameterTypes() {
        final String[] parameterTypes = new String[this.argumentTypes.length];
        for (int i = 0; i < this.argumentTypes.length; i++) {
            parameterTypes[i] = this.argumentTypes[i].getClassName();
        }

        return parameterTypes;
    }

    public String[] getParameterNames() {
        if (this.argumentTypes.length == 0) {
            return new String[0];
        }

        final List<LocalVariableNode> localVariableNodes = this.methodNode.localVariables;
        int localVariableStartIndex = 1;
        if (isStatic()) {
            // static method is none this.
            localVariableStartIndex = 0;
        }

        if (localVariableNodes == null || localVariableNodes.size() <= localVariableStartIndex
                || (this.argumentTypes.length + localVariableStartIndex) > localVariableNodes.size()) {
            // make simple argument names.
            final String[] names = new String[this.argumentTypes.length];
            for (int i = 0; i < this.argumentTypes.length; i++) {
                final String className = this.argumentTypes[i].getClassName();
                if (className != null) {
                    final int findIndex = className.lastIndexOf('.');
                    if (findIndex == -1) {
                        names[i] = className;
                    } else {
                        names[i] = className.substring(findIndex + 1);
                    }
                } else {
                    names[i] = this.argumentTypes[i].getDescriptor();
                }
            }
            return names;
        }

        // sort by index.
        Collections.sort(localVariableNodes, INDEX_COMPARATOR);
        String[] names = new String[this.argumentTypes.length];

        for (int i = 0; i < this.argumentTypes.length; i++) {
            final String name = localVariableNodes.get(localVariableStartIndex++).name;
            if (name != null) {
                names[i] = name;
            } else {
                names[i] = "";
            }
        }

        return names;
    }

    public String getReturnType() {
        return this.returnType.getClassName();
    }

    public boolean hasInterceptor() {
        final List<LocalVariableNode> localVariableNodes = this.methodNode.localVariables;
        if (localVariableNodes == null) {
            return false;
        }

        for (LocalVariableNode node : localVariableNodes) {
            if (node.name.equals("_$PINPOINT$_interceptor")) {
                return true;
            }
        }

        return false;
    }

    // new method only.
    public void initLocalVariables(final InsnList instructions) {
        // find enter & exit instruction.
        final LabelNode variableStartLabelNode = new LabelNode();
        final LabelNode variableEndLabelNode = new LabelNode();
        if (instructions.getFirst() != null) {
            instructions.insertBefore(instructions.getFirst(), variableStartLabelNode);
        } else {
            instructions.insert(variableStartLabelNode);
        }
        instructions.insert(instructions.getLast(), variableEndLabelNode);

        if (!isStatic()) {
            addLocalVariable("this", Type.getObjectType(this.declaringClassInternalName).getDescriptor(),
                    variableStartLabelNode, variableEndLabelNode);
        }

        for (Type type : this.argumentTypes) {
            addLocalVariable(JavaAssistUtils.javaClassNameToVariableName(type.getClassName()), type.getDescriptor(),
                    variableStartLabelNode, variableEndLabelNode);
        }
    }

    public boolean initInterceptorLocalVariables(final InsnList instructions, final int interceptorId,
            InterceptorDefinition interceptorDefinition) {
        if (this.initializedInterceptorLocalVariables) {
            return false;
        }
        this.initializedInterceptorLocalVariables = true;

        // find enter & exit instruction.
        if (isConstructor()) {
            this.enterInsnNode = findInitConstructorInstruction();
        } else {
            this.enterInsnNode = methodNode.instructions.getFirst();
        }

        if (this.enterInsnNode == null) {
            throw new IllegalStateException(
                    "not found enter code. " + declaringClassInternalName + "/" + methodNode.name + methodNode.desc);
        }

        this.exitInsnNode = methodNode.instructions.getLast();

        // setup interceptor variables start/end label.
        this.methodNode.instructions.insertBefore(this.enterInsnNode, this.interceptorVariableStartLabelNode);
        this.methodNode.instructions.insert(this.exitInsnNode, this.interceptorVariableEndLabelNode);

        // initialize interceptor variable.
        initInterceptorVar(instructions, interceptorId);

        // initialize argument variable.
        final InterceptorType interceptorType = interceptorDefinition.getInterceptorType();
        if (interceptorType == InterceptorType.ARRAY_ARGS) {
            // Object target, Object[] args
            initArgsVar(instructions);
        }

        return true;
    }

    AbstractInsnNode findInitConstructorInstruction() {
        int nested = 0;
        for (AbstractInsnNode insnNode = this.methodNode.instructions.getFirst(); insnNode != null;
                insnNode = insnNode.getNext()) {
            if (insnNode instanceof TypeInsnNode) {
                if (insnNode.getOpcode() == Opcodes.NEW) {
                    // new object().
                    nested++;
                }
            } else if (insnNode instanceof MethodInsnNode) {
                final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL && methodInsnNode.name.equals("<init>")) {
                    if (--nested < 0) {
                        // find this() or super().
                        return insnNode.getNext();
                    }
                }
            }
        }

        return null;
    }


    private void initInterceptorVar(final InsnList instructions, final int interceptorId) {
        assertInitializedInterceptorLocalVariables();
        this.interceptorVarIndex = addInterceptorLocalVariable("_$PINPOINT$_interceptor",
                "Lcom/navercorp/pinpoint/bootstrap/interceptor/Interceptor;");
        push(instructions, interceptorId);
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(InterceptorRegistry.class),
                "getInterceptor", "(I)" + Type.getDescriptor(Interceptor.class), false));
        storeVar(instructions, this.interceptorVarIndex);
        this.resultVarIndex = addInterceptorLocalVariable("_$PINPOINT$_result", "Ljava/lang/Object;");
        loadNull(instructions);
        storeVar(instructions, this.resultVarIndex);
        this.throwableVarIndex = addInterceptorLocalVariable("_$PINPOINT$_throwable", "Ljava/lang/Throwable;");
        loadNull(instructions);
        storeVar(instructions, this.throwableVarIndex);
    }

    private void initArgsVar(final InsnList instructions) {
        assertInitializedInterceptorLocalVariables();
        this.argsVarIndex = addInterceptorLocalVariable("_$PINPOINT$_args", "[Ljava/lang/Object;");
        loadArgsVar(instructions);
        storeVar(instructions, this.argsVarIndex);
    }


    int getInterceptorParameterCount(final InterceptorDefinition interceptorDefinition) {
        if (interceptorDefinition.getBeforeMethod() != null) {
            // skip this.
            return interceptorDefinition.getBeforeMethod().getParameterTypes().length - 1;
        } else if (interceptorDefinition.getAfterMethod() != null) {
            // skip this, result, throwable.
            return interceptorDefinition.getAfterMethod().getParameterTypes().length - 3;
        }

        return 0;
    }

    public void storeThrowableVar(final InsnList instructions) {
        assertInitializedInterceptorLocalVariables();
        storeVar(instructions, this.throwableVarIndex);
        loadNull(instructions);
        storeVar(instructions, this.resultVarIndex);
    }

    public void storeResultVar(final InsnList instructions, final int opcode) {
        assertInitializedInterceptorLocalVariables();
        if (opcode == Opcodes.RETURN) {
            // void.
            loadNull(instructions);
        } else if (opcode == Opcodes.ARETURN) {
            // object.
            dup(instructions);
        } else {
            if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
                // long or double.
                dup2(instructions);
            } else {
                dup(instructions);
            }
            final Type type = Type.getReturnType(this.methodNode.desc);
            box(instructions, type);
        }
        storeVar(instructions, this.resultVarIndex);
        loadNull(instructions);
        storeVar(instructions, this.throwableVarIndex);
    }

    public void loadInterceptorLocalVariables(final InsnList instructions,
            final InterceptorDefinition interceptorDefinition, final boolean after) {
        assertInitializedInterceptorLocalVariables();
        loadVar(instructions, this.interceptorVarIndex);
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST,
                Type.getInternalName(interceptorDefinition.getInterceptorBaseClass())));

        // target(this) object.
        loadThis(instructions);

        final InterceptorType interceptorType = interceptorDefinition.getInterceptorType();
        if (interceptorType == InterceptorType.ARRAY_ARGS) {
            // Object target, Object[] args
            loadVar(instructions, this.argsVarIndex);
        }

        if (after) {
            loadVar(instructions, this.resultVarIndex);
            loadVar(instructions, this.throwableVarIndex);
        }
    }

    public void loadInterceptorThrowVar(final InsnList instructions) {
        assertInitializedInterceptorLocalVariables();
        loadVar(instructions, this.throwableVarIndex);
        instructions.add(new InsnNode(Opcodes.ATHROW));
    }

    void loadThis(final InsnList instructions) {
        if (isConstructor()) {
            // load this.
            loadVar(instructions, 0);
        } else {
            if (isStatic()) {
                // load null.
                loadNull(instructions);
            } else {
                // load this.
                loadVar(instructions, 0);
            }
        }
    }

    void storeVar(final InsnList instructions, final int index) {
        instructions.add(new VarInsnNode(Opcodes.ASTORE, index));
    }

    void storeInt(final InsnList instructions, final int index) {
        instructions.add(new VarInsnNode(Opcodes.ISTORE, index));
    }

    void loadNull(final InsnList instructions) {
        instructions.add(new InsnNode(Opcodes.ACONST_NULL));
    }

    void loadVar(final InsnList instructions, final int index) {
        instructions.add(new VarInsnNode(Opcodes.ALOAD, index));
    }

    void loadInt(final InsnList instructions, final int index) {
        instructions.add(new VarInsnNode(Opcodes.ILOAD, index));
    }

    boolean isReturnCode(final int opcode) {
        return opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN || opcode == Opcodes.FRETURN
                || opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN;
    }

    Type getBoxedType(final Type type) {
        switch (type.getSort()) {
            case Type.BYTE:
                return BYTE_TYPE;
            case Type.BOOLEAN:
                return BOOLEAN_TYPE;
            case Type.SHORT:
                return SHORT_TYPE;
            case Type.CHAR:
                return CHARACTER_TYPE;
            case Type.INT:
                return INTEGER_TYPE;
            case Type.FLOAT:
                return FLOAT_TYPE;
            case Type.LONG:
                return LONG_TYPE;
            case Type.DOUBLE:
                return DOUBLE_TYPE;
        }
        return type;
    }

    void push(InsnList insnList, final int value) {
        if (value >= -1 && value <= 5) {
            insnList.add(new InsnNode(Opcodes.ICONST_0 + value));
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            insnList.add(new IntInsnNode(Opcodes.BIPUSH, value));
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            insnList.add(new IntInsnNode(Opcodes.SIPUSH, value));
        } else {
            insnList.add(new LdcInsnNode(value));
        }
    }

    void push(InsnList insnList, final String value) {
        if (value == null) {
            insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            insnList.add(new LdcInsnNode(value));
        }
    }


    void newArray(final InsnList insnList, final Type type) {
        insnList.add(new TypeInsnNode(Opcodes.ANEWARRAY, type.getInternalName()));
    }


    void dup(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP));
    }

    void dup2(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP2));
    }

    void dupX1(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP_X1));
    }

    void dupX2(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.DUP_X2));
    }

    void pop(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.POP));
    }

    void swap(final InsnList insnList) {
        insnList.add(new InsnNode(Opcodes.SWAP));
    }

    void loadArgsVar(final InsnList instructions) {
        if (this.argumentTypes.length == 0) {
            // null.
            loadNull(instructions);
            return;
        }

        push(instructions, this.argumentTypes.length);
        // new array
        newArray(instructions, OBJECT_TYPE);
        for (int i = 0; i < this.argumentTypes.length; i++) {
            Type type = this.argumentTypes[i];
            dup(instructions);
            push(instructions, i);
            // loadArg
            loadArg(instructions, this.argumentTypes, i);
            // box
            box(instructions, type);
            // arrayStore
            arrayStore(instructions, OBJECT_TYPE);
        }
    }

    void loadArgs(final InsnList instructions) {
        for (int i = 0; i < this.argumentTypes.length; i++) {
            loadArg(instructions, this.argumentTypes, i);
        }
    }

    void loadArg(final InsnList instructions, Type[] argumentTypes, int i) {
        final int index = getArgIndex(argumentTypes, i);
        final Type type = argumentTypes[i];
        instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index));
    }

    int getArgIndex(final Type[] argumentTypes, final int arg) {
        int index = isStatic() ? 0 : 1;
        for (int i = 0; i < arg; i++) {
            index += argumentTypes[i].getSize();
        }
        return index;
    }

    void box(final InsnList instructions, Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            return;
        }

        if (type == Type.VOID_TYPE) {
            // push null
            instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            Type boxed = getBoxedType(type);
            // new instance.
            newInstance(instructions, boxed);
            if (type.getSize() == 2) {
                // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                // dupX2
                dupX2(instructions);
                // dupX2
                dupX2(instructions);
                // pop
                pop(instructions);
            } else {
                // p -> po -> opo -> oop -> o
                // dupX1
                dupX1(instructions);
                // swap
                swap(instructions);
            }
            invokeConstructor(instructions, boxed, new Method("<init>", Type.VOID_TYPE, new Type[]{type}));
        }
    }

    void arrayStore(final InsnList instructions, final Type type) {
        instructions.add(new InsnNode(type.getOpcode(Opcodes.IASTORE)));
    }

    void newInstance(final InsnList instructions, final Type type) {
        instructions.add(new TypeInsnNode(Opcodes.NEW, type.getInternalName()));
    }

    void invokeConstructor(final InsnList instructions, final Type type, final Method method) {
        String owner = type.getSort() == Type.ARRAY ? type.getDescriptor() : type.getInternalName();
        instructions
                .add(new MethodInsnNode(Opcodes.INVOKESPECIAL, owner, method.getName(), method.getDescriptor(), false));
    }


    int addInterceptorLocalVariable(final String name, final String desc) {
        return addLocalVariable(name, desc, this.interceptorVariableStartLabelNode,
                this.interceptorVariableEndLabelNode);
    }

    int addLocalVariable(final String name, final String desc, final LabelNode start, final LabelNode end) {
        int index = this.nextLocals;
        this.nextLocals += 1;
        final LocalVariableNode node = new LocalVariableNode(name, desc, null, start, end, index);
        this.methodNode.localVariables.add(node);

        return index;
    }

    public void returnValue(final InsnList instructions) {
        instructions.add(new InsnNode(this.returnType.getOpcode(Opcodes.IRETURN)));
    }

    private boolean isStatic() {
        return (this.methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    private boolean isConstructor() {
        return this.methodNode.name != null && this.methodNode.name.equals("<init>");
    }

    private void assertInitializedInterceptorLocalVariables() {
        if (!this.initializedInterceptorLocalVariables) {
            throw new IllegalStateException("The interceptor local variables must be initialized.");
        }
    }

    // test only.
    List<LocalVariableNode> getLocalVariables() {
        return this.methodNode.localVariables;
    }
}