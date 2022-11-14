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

import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinitionFactory;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;
import cn.polarismesh.agent.core.extension.instrument.ClassFilter;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.MethodFilter;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.tree.ClassNode;

public class ASMClass implements InstrumentClass {

    private static final String FIELD_PREFIX = "_$PINPOINT$_";

    private final InstrumentContext pluginContext;

    private final ASMClassNodeAdapter classNode;

    private final InterceptorDefinitionFactory factory;
    private boolean modified = false;
    private String name;

    public ASMClass(InstrumentContext pluginContext, ClassLoader classLoader, ProtectionDomain protectionDomain,
            ClassNode classNode, InterceptorDefinitionFactory factory) {
        this(pluginContext, new ASMClassNodeAdapter(pluginContext, classLoader, protectionDomain, classNode), factory);
    }

    public ASMClass(InstrumentContext pluginContext, ASMClassNodeAdapter classNode,
            InterceptorDefinitionFactory factory) {
        this.pluginContext = pluginContext;
        this.classNode = Objects.requireNonNull(classNode, "classNode");
        this.factory = factory;
    }

    public ClassLoader getClassLoader() {
        return this.classNode.getClassLoader();
    }

    @Override
    public boolean isInterceptable() {
        if (isAnnotation() || isModified()) {
            return false;
        }
        // interface static method or default method is java 1.8 or later
        if (isInterface() && (this.classNode.getMajorVersion() < 52 || !JvmUtils.getVersion()
                .onOrAfter(JvmVersion.JAVA_8))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isInterface() {
        return this.classNode.isInterface();
    }

    private boolean isAnnotation() {
        return this.classNode.isAnnotation();
    }

    @Override
    public String getName() {
        // for performance.
        if (this.name == null) {
            this.name = classNode.getName();
        }
        return this.name;
    }

    @Override
    public String getSuperClass() {
        return this.classNode.getSuperClassName();
    }

    @Override
    public String[] getInterfaces() {
        return this.classNode.getInterfaceNames();
    }

    @Override
    public InstrumentMethod getDeclaredMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "name");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        final ASMMethodNodeAdapter methodNode = this.classNode.getDeclaredMethod(methodName, desc);
        if (methodNode == null) {
            return null;
        }

        return new ASMMethod(this.pluginContext, this, methodNode, factory);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return getDeclaredMethods(MethodFilters.ACCEPT_ALL);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(final MethodFilter methodFilter) {
        Objects.requireNonNull(methodFilter, "methodFilter");

        final List<InstrumentMethod> candidateList = new ArrayList<>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredMethods()) {
            final InstrumentMethod method = new ASMMethod(this.pluginContext, this, methodNode, factory);
            if (methodFilter.accept(method)) {
                candidateList.add(method);
            }
        }

        return candidateList;
    }

    @Override
    public InstrumentMethod getConstructor(final String... parameterTypes) {
        return getDeclaredMethod("<init>", parameterTypes);
    }

    @Override
    public List<InstrumentMethod> getDeclaredConstructors() {
        final List<InstrumentMethod> candidateList = new ArrayList<>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredConstructors()) {
            final InstrumentMethod method = new ASMMethod(this.pluginContext, this, methodNode, factory);
            candidateList.add(method);
        }
        return candidateList;
    }

    @Override
    public boolean hasDeclaredMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "methodName");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasDeclaredMethod(methodName, desc);
    }

    @Override
    public boolean hasMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "methodName");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasMethod(methodName, desc);
    }

    @Override
    public boolean hasEnclosingMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "methodName");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasOutClass(methodName, desc);
    }

    @Override
    public boolean hasConstructor(final String... parameterTypeArray) {
        return getConstructor(parameterTypeArray) != null;
    }

    @Override
    public boolean hasField(String fieldName, String type) {
        Objects.requireNonNull(fieldName, "name");

        final String desc = type == null ? null : JavaAssistUtils.toJvmSignature(type);
        return this.classNode.getField(fieldName, desc) != null;
    }

    @Override
    public boolean hasField(String name) {
        return hasField(name, null);
    }

    @Override
    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        Objects.requireNonNull(filter, "filter");

        final List<InstrumentClass> nestedClasses = new ArrayList<>();
        for (ASMClassNodeAdapter innerClassNode : this.classNode.getInnerClasses()) {
            final ASMNestedClass nestedClass = new ASMNestedClass(this.pluginContext, innerClassNode, factory);
            if (filter.accept(nestedClass)) {
                nestedClasses.add(nestedClass);
            }
        }

        return nestedClasses;
    }

    public boolean isModified() {
        return modified;
    }

    void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public byte[] toBytecode() {
        return classNode.toByteArray();
    }
}