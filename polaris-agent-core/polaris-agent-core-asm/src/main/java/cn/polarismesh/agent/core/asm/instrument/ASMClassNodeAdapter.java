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

import cn.polarismesh.agent.core.asm.scanner.ClassScannerFactory;
import cn.polarismesh.agent.core.asm.scanner.Scanner;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.CollectionUtils;
import cn.polarismesh.agent.core.common.utils.IOUtils;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import cn.polarismesh.agent.core.common.utils.StringMatchUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class ASMClassNodeAdapter {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ASMClassNodeAdapter.class.getCanonicalName());

    public static ASMClassNodeAdapter get(ClassInputStreamProvider pluginClassInputStreamProvider,
            final ClassLoader classLoader, ProtectionDomain protectionDomain, final String classInternalName) {
        return get(pluginClassInputStreamProvider, classLoader, protectionDomain, classInternalName, false);
    }

    public static ASMClassNodeAdapter get(ClassInputStreamProvider pluginClassInputStreamProvider,
            final ClassLoader classLoader, ProtectionDomain protectionDomain, final String classInternalName,
            final boolean skipCode) {
        Objects.requireNonNull(pluginClassInputStreamProvider, "pluginInputStreamProvider");
        Objects.requireNonNull(classInternalName, "classInternalName");

        final String classPath = classInternalName.concat(".class");
        final byte[] bytes = readStream(classPath, pluginClassInputStreamProvider, protectionDomain, classLoader);
        if (bytes == null) {
            return null;
        }
        final ClassReader classReader = new ClassReader(bytes);
        final ClassNode classNode = new ClassNode();

        final int parsingOptions = getParsingOption(skipCode);
        classReader.accept(classNode, parsingOptions);

        return new ASMClassNodeAdapter(pluginClassInputStreamProvider, classLoader, protectionDomain, classNode,
                skipCode);
    }

    private static int getParsingOption(boolean skipCode) {
        if (skipCode) {
            return ClassReader.SKIP_CODE;
        } else {
            return 0;
        }
    }

    private static byte[] readStream(String classPath, ClassInputStreamProvider pluginClassInputStreamProvider,
            ProtectionDomain protectionDomain, ClassLoader classLoader) {

        final Scanner scanner = ClassScannerFactory.newScanner(protectionDomain);
        if (scanner != null) {
            try {
                final InputStream stream = scanner.openStream(classPath);
                if (stream != null) {
                    try {
                        return IOUtils.toByteArray(stream);
                    } catch (IOException e) {
                        logger.warn(String.format("bytecode read fail scanner:%s path:%s", scanner, classPath));
                        return null;
                    }
                }
            } finally {
                scanner.close();
            }
        }

        final InputStream in = pluginClassInputStreamProvider.getResourceAsStream(classLoader, classPath);
        if (in != null) {
            try {
                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                logger.warn(String.format("bytecode read fail path:%s", classPath));
                return null;
            }
        }
        return null;
    }

    private final ClassInputStreamProvider pluginInputStreamProvider;
    private final ClassLoader classLoader;
    private final ProtectionDomain protectionDomain;
    private final ClassNode classNode;
    private final boolean skipCode;

    public ASMClassNodeAdapter(final ClassInputStreamProvider pluginInputStreamProvider, final ClassLoader classLoader,
            ProtectionDomain protectionDomain, final ClassNode classNode) {
        this(pluginInputStreamProvider, classLoader, protectionDomain, classNode, false);
    }

    public ASMClassNodeAdapter(final ClassInputStreamProvider pluginInputStreamProvider, final ClassLoader classLoader,
            ProtectionDomain protectionDomain, final ClassNode classNode, final boolean skipCode) {
        this.pluginInputStreamProvider = pluginInputStreamProvider;
        this.classLoader = classLoader;
        this.protectionDomain = protectionDomain;
        this.classNode = classNode;
        this.skipCode = skipCode;
    }

    public String getInternalName() {
        return this.classNode.name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

    public String getName() {
        return this.classNode.name == null ? null : JavaAssistUtils.jvmNameToJavaName(this.classNode.name);
    }

    public String getSuperClassInternalName() {
        return this.classNode.superName;
    }

    public String getSuperClassName() {
        return this.classNode.superName == null ? null : JavaAssistUtils.jvmNameToJavaName(this.classNode.superName);
    }

    public boolean isInterface() {
        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

    public boolean isAnnotation() {
        return (classNode.access & Opcodes.ACC_ANNOTATION) != 0;
    }

    public String[] getInterfaceNames() {
        final List<String> interfaces = this.classNode.interfaces;
        if (CollectionUtils.isEmpty(interfaces)) {
            return new String[0];
        }

        final List<String> list = new ArrayList<>(interfaces.size());
        for (String name : interfaces) {
            if (name != null) {
                list.add(JavaAssistUtils.jvmNameToJavaName(name));
            }
        }

        return list.toArray(new String[0]);
    }

    public ASMMethodNodeAdapter getDeclaredMethod(final String methodName, final String desc) {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        return findDeclaredMethod(methodName, desc);
    }

    public List<ASMMethodNodeAdapter> getDeclaredConstructors() {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        return findDeclaredMethod("<init>");
    }

    public boolean hasDeclaredMethod(final String methodName, final String desc) {
        return findDeclaredMethod(methodName, desc) != null;
    }

    private ASMMethodNodeAdapter findDeclaredMethod(final String methodName, final String desc) {
        Objects.requireNonNull(methodName, "methodName");

        final List<MethodNode> declaredMethods = classNode.methods;
        if (CollectionUtils.isEmpty(declaredMethods)) {
            return null;
        }

        for (MethodNode methodNode : declaredMethods) {
            if (!StringMatchUtils.equals(methodNode.name, methodName)) {
                continue;
            }

            if (desc == null || StringMatchUtils.startWith(methodNode.desc, desc)) {
                return new ASMMethodNodeAdapter(getInternalName(), methodNode);
            }
        }

        return null;
    }

    private List<ASMMethodNodeAdapter> findDeclaredMethod(final String methodName) {
        Objects.requireNonNull(methodName, "methodName");

        final List<MethodNode> declaredMethods = classNode.methods;
        if (CollectionUtils.isEmpty(declaredMethods)) {
            return Collections.emptyList();
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<>();
        for (MethodNode methodNode : declaredMethods) {
            if (!StringMatchUtils.equals(methodNode.name, methodName)) {
                continue;
            }

            methodNodes.add(new ASMMethodNodeAdapter(getInternalName(), methodNode));
        }
        return methodNodes;
    }


    public List<ASMMethodNodeAdapter> getDeclaredMethods() {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        final List<MethodNode> methods = this.classNode.methods;
        if (CollectionUtils.isEmpty(methods)) {
            return Collections.emptyList();
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<>(methods.size());
        for (MethodNode methodNode : methods) {
            final String methodName = methodNode.name;
            if (methodName == null || methodName.equals("<init>") || methodName.equals("<clinit>")) {
                // skip constructor(<init>) and static initializer block(<clinit>)
                continue;
            }
            methodNodes.add(new ASMMethodNodeAdapter(getInternalName(), methodNode));
        }

        return methodNodes;
    }

    public boolean hasOutClass(final String methodName, final String desc) {
        if (methodName == null || this.classNode.outerClass == null || this.classNode.outerMethod == null
                || !this.classNode.outerMethod.equals(methodName)) {
            return false;
        }

        if (desc == null) {
            return true;
        }
        return StringMatchUtils.startWith(this.classNode.outerMethodDesc, desc);
    }

    public boolean hasMethod(final String methodName, final String desc) {
        if (hasDeclaredMethod(methodName, desc)) {
            return true;
        }

        if (this.classNode.superName != null) {
            // skip code.
            final ASMClassNodeAdapter classNode = ASMClassNodeAdapter
                    .get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain,
                            this.classNode.superName, true);
            if (classNode != null) {
                return classNode.hasMethod(methodName, desc);
            }
        }

        return false;
    }

    public ASMFieldNodeAdapter getField(final String fieldName, final String fieldDesc) {
        Objects.requireNonNull(fieldName, "fieldName");

        if (this.classNode.fields == null) {
            return null;
        }

        final List<FieldNode> fields = this.classNode.fields;
        for (FieldNode fieldNode : fields) {
            if (StringMatchUtils.equals(fieldNode.name, fieldName) && (fieldDesc == null || (StringMatchUtils
                    .equals(fieldNode.desc, fieldDesc)))) {
                return new ASMFieldNodeAdapter(fieldNode);
            }
        }

        // find interface.
        final List<String> interfaces = this.classNode.interfaces;
        if (CollectionUtils.hasLength(interfaces)) {
            for (String interfaceClassName : interfaces) {
                if (interfaceClassName == null) {
                    continue;
                }

                final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter
                        .get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain,
                                interfaceClassName, true);
                if (classNodeAdapter != null) {
                    final ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField(fieldName, fieldDesc);
                    if (fieldNode != null) {
                        return fieldNode;
                    }
                }
            }
        }

        // find super class.
        if (this.classNode.superName != null) {
            final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter
                    .get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain,
                            this.classNode.superName, true);
            if (classNodeAdapter != null) {
                final ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField(fieldName, fieldDesc);
                if (fieldNode != null) {
                    return fieldNode;
                }
            }
        }

        return null;
    }

    public ASMFieldNodeAdapter addField(final String fieldName, final String fieldDesc) {
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(fieldDesc, "fieldDesc");
        final FieldNode fieldNode = new FieldNode(getFieldAccessFlags(), fieldName, fieldDesc, null, null);
        addFieldNode0(fieldNode);

        return new ASMFieldNodeAdapter(fieldNode);
    }

    private int getFieldAccessFlags() {
        // Field added by pinpoint must not be serialized
        return Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT;
    }

    private void addFieldNode0(FieldNode fieldNode) {
        if (this.classNode.fields == null) {
            this.classNode.fields = new ArrayList<>();
        }
        this.classNode.fields.add(fieldNode);
    }

    public ASMMethodNodeAdapter addDelegatorMethod(final ASMMethodNodeAdapter superMethodNode) {
        Objects.requireNonNull(superMethodNode, "superMethodNode");

        final String[] exceptions = getSuperMethodExceptions(superMethodNode);

        final MethodNode rawMethodNode = new MethodNode(superMethodNode.getAccess(), superMethodNode.getName(),
                superMethodNode.getDesc(), superMethodNode.getSignature(), exceptions);
        final ASMMethodNodeAdapter methodNode = new ASMMethodNodeAdapter(getInternalName(), rawMethodNode);
        methodNode.addDelegator(superMethodNode.getDeclaringClassInternalName());
        addMethodNode0(methodNode.getMethodNode());

        return methodNode;
    }

    private String[] getSuperMethodExceptions(ASMMethodNodeAdapter superMethodNode) {
        final List<String> superMethodNodeExceptions = superMethodNode.getExceptions();
        if (superMethodNodeExceptions == null) {
            return null;
        }
        return superMethodNodeExceptions.toArray(new String[0]);
    }

    private void addMethodNode0(MethodNode methodNode) {
        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<>();
        }
        this.classNode.methods.add(methodNode);
    }

    private InsnList getInsnList(MethodNode methodNode) {
        if (methodNode.instructions == null) {
            methodNode.instructions = new InsnList();
        }
        return methodNode.instructions;
    }

    public List<ASMClassNodeAdapter> getInnerClasses() {
        if (this.classNode.innerClasses == null) {
            return Collections.emptyList();
        }

        final List<ASMClassNodeAdapter> innerClasses = new ArrayList<>();
        final List<InnerClassNode> innerClassNodes = this.classNode.innerClasses;
        for (InnerClassNode node : innerClassNodes) {
            if (node.name == null) {
                continue;
            }
            // skip code.
            ASMClassNodeAdapter adapter = get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain,
                    node.name, true);
            if (adapter != null) {
                innerClasses.add(adapter);
            }
        }

        return innerClasses;
    }

    public int getMajorVersion() {
        final int majorVersion = this.classNode.version & 0xFFFF;
        return majorVersion;
    }

    public byte[] toByteArray() {
        final int majorVersion = this.classNode.version & 0xFFFF;
        int flags = ClassWriter.COMPUTE_FRAMES;
        if (majorVersion <= 49) {
            // java 1.5 and less.
            flags = ClassWriter.COMPUTE_MAXS;
        }

        final ClassWriter classWriter = new ASMClassWriter(this.pluginInputStreamProvider, flags, this.classLoader);
        this.classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
