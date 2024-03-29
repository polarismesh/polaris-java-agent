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

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ASMClassWriter extends ClassWriter {

    private static final String OBJECT_CLASS_INTERNAL_NAME = "java/lang/Object";

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ASMClassWriter.class.getCanonicalName());

    private final ClassInputStreamProvider pluginInputStreamProvider;
    private final ClassLoader classLoader;

    public ASMClassWriter(final ClassInputStreamProvider pluginInputStreamProvider, final int flags,
            final ClassLoader classLoader) {
        super(flags);
        this.pluginInputStreamProvider = pluginInputStreamProvider;
        this.classLoader = classLoader;
    }

    @Override
    protected String getCommonSuperClass(String classInternalName1, String classInternalName2) {
        return get(classInternalName1, classInternalName2);
    }


    private String get(final String classInternalName1, final String classInternalName2) {
        if (classInternalName1 == null || classInternalName1.equals(OBJECT_CLASS_INTERNAL_NAME)
                || classInternalName2 == null || classInternalName2.equals(OBJECT_CLASS_INTERNAL_NAME)) {
            // object is the root of the class hierarchy.
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        if (classInternalName1.equals(classInternalName2)) {
            // two equal.
            return classInternalName1;
        }

        final ClassReader classReader1 = getClassReader(classInternalName1);
        if (classReader1 == null) {
            logger.warn(String.format("Skip getCommonSuperClass(). not found class %s", classInternalName1));
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        final ClassReader classReader2 = getClassReader(classInternalName2);
        if (classReader2 == null) {
            logger.warn(String.format("Skip getCommonSuperClass(). not found class %s", classInternalName2));
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        // interface.
        if (isInterface(classReader1)) {
            // <interface, class> or <interface, interface>
            return getCommonInterface(classReader1, classReader2);
        }

        // interface.
        if (isInterface(classReader2)) {
            // <class, interface>
            return getCommonInterface(classReader2, classReader1);
        }

        // class.
        // <class, class>
        return getCommonClass(classReader1, classReader2);
    }

    private boolean isInterface(final ClassReader classReader) {
        return (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    // <interface, interface> or <interface, class>
    private String getCommonInterface(final ClassReader classReader1, final ClassReader classReader2) {
        final Set<String> interfaceHierarchy = new HashSet<>();
        traversalInterfaceHierarchy(interfaceHierarchy, classReader1);

        if (isInterface(classReader2)) {
            if (interfaceHierarchy.contains(classReader2.getClassName())) {
                return classReader2.getClassName();
            }
        }

        final String interfaceInternalName = getImplementedInterface(interfaceHierarchy, classReader2);
        if (interfaceInternalName != null) {
            return interfaceInternalName;
        }
        return OBJECT_CLASS_INTERNAL_NAME;
    }

    private void traversalInterfaceHierarchy(final Set<String> interfaceHierarchy, final ClassReader classReader) {
        if (classReader != null && interfaceHierarchy.add(classReader.getClassName())) {
            for (String interfaceInternalName : classReader.getInterfaces()) {
                traversalInterfaceHierarchy(interfaceHierarchy, getClassReader(interfaceInternalName));
            }
        }
    }

    private String getImplementedInterface(final Set<String> interfaceHierarchy, final ClassReader classReader) {
        ClassReader cr = classReader;
        while (cr != null) {
            final String[] interfaceInternalNames = cr.getInterfaces();
            for (String name : interfaceInternalNames) {
                if (name != null && interfaceHierarchy.contains(name)) {
                    return name;
                }
            }

            for (String name : interfaceInternalNames) {
                final String interfaceInternalName = getImplementedInterface(interfaceHierarchy, getClassReader(name));
                if (interfaceInternalName != null) {
                    return interfaceInternalName;
                }
            }

            final String superClassInternalName = cr.getSuperName();
            if (superClassInternalName == null || superClassInternalName.equals(OBJECT_CLASS_INTERNAL_NAME)) {
                break;
            }
            cr = getClassReader(superClassInternalName);
        }

        return null;
    }

    private String getCommonClass(final ClassReader classReader1, final ClassReader classReader2) {
        final Set<String> classHierarchy = new HashSet<>();
        classHierarchy.add(classReader1.getClassName());
        classHierarchy.add(classReader2.getClassName());

        String superClassInternalName1 = classReader1.getSuperName();
        if (!classHierarchy.add(superClassInternalName1)) {
            // find common super class.
            return superClassInternalName1;
        }

        String superClassInternalName2 = classReader2.getSuperName();
        if (!classHierarchy.add(superClassInternalName2)) {
            // find common super class.
            return superClassInternalName2;
        }

        while (superClassInternalName1 != null || superClassInternalName2 != null) {
            // for each.
            if (superClassInternalName1 != null) {
                superClassInternalName1 = getSuperClassInternalName(superClassInternalName1);
                if (superClassInternalName1 != null) {
                    if (!classHierarchy.add(superClassInternalName1)) {
                        return superClassInternalName1;
                    }
                }
            }

            if (superClassInternalName2 != null) {
                superClassInternalName2 = getSuperClassInternalName(superClassInternalName2);
                if (superClassInternalName2 != null) {
                    if (!classHierarchy.add(superClassInternalName2)) {
                        return superClassInternalName2;
                    }
                }
            }
        }

        return OBJECT_CLASS_INTERNAL_NAME;
    }


    private String getSuperClassInternalName(final String classInternalName) {
        final ClassReader classReader = getClassReader(classInternalName);
        if (classReader == null) {
            return null;
        }

        return classReader.getSuperName();
    }

    private ClassReader getClassReader(final String classInternalName) {
        if (classInternalName == null) {
            return null;
        }

        final String classFileName = classInternalName.concat(".class");
        final InputStream in = pluginInputStreamProvider.getResourceAsStream(this.classLoader, classFileName);
        if (in == null) {
            return null;
        }

        try {
            final byte[] bytes = IOUtils.toByteArray(in);
            return new ClassReader(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}
