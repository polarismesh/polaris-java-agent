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

package cn.polarismesh.agent.core.asm.instrument.classloading;

import cn.polarismesh.agent.core.asm.concurrent.jsr166.ConcurrentWeakHashMap;
import cn.polarismesh.agent.core.asm.instrument.classreading.SimpleClassMetadata;
import cn.polarismesh.agent.core.asm.instrument.classreading.SimpleClassMetadataReader;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.ClassLoadingChecker;
import cn.polarismesh.agent.core.common.utils.ExtensionFilter;
import cn.polarismesh.agent.core.common.utils.FileBinary;
import cn.polarismesh.agent.core.common.utils.JarReader;
import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlainClassLoaderHandler implements PluginClassInjector {

    private final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(getClass().getName());

    private final JarReader pluginJarReader;

    private final ConcurrentMap<ClassLoader, ClassLoaderAttachment> classLoaderAttachment = new ConcurrentWeakHashMap<ClassLoader, ClassLoaderAttachment>();


    private final PluginConfig pluginConfig;

    public PlainClassLoaderHandler(PluginConfig pluginConfig) {
        this.pluginConfig = Objects.requireNonNull(pluginConfig, "pluginConfig");

        this.pluginJarReader = new JarReader(pluginConfig.getPluginJarFile());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        if (classLoader == Object.class.getClassLoader()) {
            throw new IllegalStateException("BootStrapClassLoader");
        }

        try {
            if (!isPluginPackage(className)) {
                return loadClass(classLoader, className);
            }
            return (Class<T>) injectClass0(classLoader, className);
        } catch (Exception e) {
            logger.warn(String.format("Failed to load plugin class %s with classLoader %s", className, classLoader), e);
            throw new PolarisAgentException(
                    "Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            final String name = JavaAssistUtils.jvmNameToJavaName(internalName);
            if (!isPluginPackage(name)) {
                return targetClassLoader.getResourceAsStream(internalName);
            }

            getClassLoaderAttachment(targetClassLoader, internalName);
            final InputStream inputStream = getPluginInputStream(internalName);
            if (inputStream != null) {
                return inputStream;
            }

            if (logger.isInfoEnabled()) {
                logger.info(String.format("can not find resource : %s %s", internalName,
                        pluginConfig.getPluginJarURLExternalForm()));
            }
            // fallback
            return targetClassLoader.getResourceAsStream(internalName);
        } catch (Exception e) {
            logger.warn(String.format("Failed to load plugin resource as stream %s with classLoader %s", internalName,
                    targetClassLoader), e);
            return null;
        }
    }

    private boolean isPluginPackage(String className) {
        return pluginConfig.getPluginPackageFilter().accept(className);
    }


    private Class<?> injectClass0(ClassLoader classLoader, String className) throws IllegalArgumentException {
        logger.info(String.format("Inject class className:%s cl:%s", className, classLoader));
        final String pluginJarPath = pluginConfig.getPluginJarURLExternalForm();
        final ClassLoaderAttachment attachment = getClassLoaderAttachment(classLoader, pluginJarPath);
        final Class<?> findClazz = attachment.getClass(className);
        if (findClazz != null) {
            return findClazz;
        }

        logger.info(
                String.format("can not find class : %s %s ", className, pluginConfig.getPluginJarURLExternalForm()));
        // fallback
        return loadClass(classLoader, className);
    }

    private InputStream getPluginInputStream(String classPath) throws IllegalArgumentException {
        logger.info(String.format("get input stream className:%s", classPath));
        try {
            return pluginJarReader.getInputStream(classPath);
        } catch (Exception ex) {
            logger.warn(String.format("failed to read plugin jar: %s", pluginConfig.getPluginJarURLExternalForm()), ex);
        }

        // not found.
        return null;
    }

    private ClassLoaderAttachment getClassLoaderAttachment(ClassLoader classLoader, final String pluginJarPath) {
        final ClassLoaderAttachment attachment = getClassLoaderAttachment(classLoader);

        final PluginLock pluginLock = attachment.getPluginLock(pluginJarPath);
        synchronized (pluginLock) {
            if (!pluginLock.isLoaded()) {
                pluginLock.setLoaded();
                defineJarClass(classLoader, attachment);
            }
        }

        return attachment;
    }

    private ClassLoaderAttachment getClassLoaderAttachment(ClassLoader classLoader) {

        final ClassLoaderAttachment exist = classLoaderAttachment.get(classLoader);
        if (exist != null) {
            return exist;
        }
        final ClassLoaderAttachment newInfo = new ClassLoaderAttachment();
        final ClassLoaderAttachment old = classLoaderAttachment.putIfAbsent(classLoader, newInfo);
        if (old != null) {
            return old;
        }
        return newInfo;
    }


    private <T> Class<T> loadClass(ClassLoader classLoader, String className) {
        try {
            if (classLoader == Object.class.getClassLoader()) {
                return (Class<T>) Class.forName(className, false, classLoader);
            } else {
                return (Class<T>) classLoader.loadClass(className);
            }
        } catch (ClassNotFoundException ex) {
            logger.warn(String.format("ClassNotFound %s cl:%s", ex.getMessage(), classLoader));
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void defineJarClass(ClassLoader classLoader, ClassLoaderAttachment attachment) {
        List<FileBinary> fileBinaryList = readJar();

        Map<String, SimpleClassMetadata> classEntryMap = parse(fileBinaryList);

        for (Map.Entry<String, SimpleClassMetadata> entry : classEntryMap.entrySet()) {

            final SimpleClassMetadata classMetadata = entry.getValue();
            ClassLoadingChecker classLoadingChecker = new ClassLoadingChecker();
            classLoadingChecker.isFirstLoad(classMetadata.getClassName());
            define0(classLoader, attachment, classMetadata, classEntryMap, classLoadingChecker);
        }
    }

    private List<FileBinary> readJar() {
        try {
            return pluginJarReader.read(ExtensionFilter.CLASS_FILTER);
        } catch (IOException ex) {
            throw new RuntimeException(pluginConfig.getPluginJarURLExternalForm() + " read fail." + ex.getMessage(),
                    ex);
        }
    }

    private Map<String, SimpleClassMetadata> parse(List<FileBinary> fileBinaryList) {
        Map<String, SimpleClassMetadata> parseMap = new HashMap<String, SimpleClassMetadata>();
        for (FileBinary fileBinary : fileBinaryList) {
            SimpleClassMetadata classNode = parseClass(fileBinary);
            parseMap.put(classNode.getClassName(), classNode);
        }
        return parseMap;
    }

    private SimpleClassMetadata parseClass(FileBinary fileBinary) {
        byte[] fileBinaryArray = fileBinary.getFileBinary();
        SimpleClassMetadata classMetadata = SimpleClassMetadataReader.readSimpleClassMetadata(fileBinaryArray);
        return classMetadata;
    }

    private void define0(final ClassLoader classLoader, ClassLoaderAttachment attachment,
            SimpleClassMetadata currentClass, Map<String, SimpleClassMetadata> classMetaMap,
            ClassLoadingChecker classLoadingChecker) {
        if ("java.lang.Object".equals(currentClass.getClassName())) {
            return;
        }
        if (attachment.containsClass(currentClass.getClassName())) {
            return;
        }

        final String superName = currentClass.getSuperClassName();
        if (!"java.lang.Object".equals(superName)) {
            if (!isSkipClass(superName, classLoadingChecker)) {
                SimpleClassMetadata superClassBinary = classMetaMap.get(superName);
                define0(classLoader, attachment, superClassBinary, classMetaMap, classLoadingChecker);

            }
        }

        final List<String> interfaceList = currentClass.getInterfaceNames();
        for (String interfaceName : interfaceList) {
            if (!isSkipClass(interfaceName, classLoadingChecker)) {
                SimpleClassMetadata interfaceClassBinary = classMetaMap.get(interfaceName);
                define0(classLoader, attachment, interfaceClassBinary, classMetaMap, classLoadingChecker);
            }
        }

        final Class<?> clazz = defineClass(classLoader, currentClass);
        attachment.putClass(currentClass.getClassName(), clazz);
    }

    private Class<?> defineClass(ClassLoader classLoader, SimpleClassMetadata classMetadata) {
        final String className = classMetadata.getClassName();
        final byte[] classBytes = classMetadata.getClassBinary();
        return DefineClassFactory
                .getDefineClass().defineClass(classLoader, className, classBytes);
    }

    private boolean isSkipClass(final String className, final ClassLoadingChecker classLoadingChecker) {
        if (!isPluginPackage(className)) {
            return true;
        }
        if (!classLoadingChecker.isFirstLoad(className)) {
            return true;
        }

        return false;
    }

    @Override
    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    @Override
    public boolean match(ClassLoader classLoader) {
        return true;
    }

    private class ClassLoaderAttachment {

        private final ConcurrentMap<String, PluginLock> pluginLock = new ConcurrentHashMap<String, PluginLock>();

        private final ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();

        public PluginLock getPluginLock(String jarFile) {
            final PluginLock exist = this.pluginLock.get(jarFile);
            if (exist != null) {
                return exist;
            }

            final PluginLock newPluginLock = new PluginLock();
            final PluginLock old = this.pluginLock.putIfAbsent(jarFile, newPluginLock);
            if (old != null) {
                return old;
            }
            return newPluginLock;
        }

        public void putClass(String className, Class<?> clazz) {
            final Class<?> duplicatedClass = this.classCache.putIfAbsent(className, clazz);
            if (duplicatedClass != null) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("duplicated pluginClass %s", className));
                }
            }
        }

        public Class<?> getClass(String className) {
            return this.classCache.get(className);
        }

        public boolean containsClass(String className) {
            return this.classCache.containsKey(className);
        }
    }

    private static class PluginLock {

        private boolean loaded = false;

        public boolean isLoaded() {
            return this.loaded;
        }

        public void setLoaded() {
            this.loaded = true;
        }

    }

}