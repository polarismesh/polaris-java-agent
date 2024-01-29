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

import cn.polarismesh.agent.core.asm.instrument.InstrumentEngine;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JarProfilerPluginClassInjector implements ClassInjector {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(JarProfilerPluginClassInjector.class.getCanonicalName());

    private final BootstrapCore bootstrapCore;

    private final List<PluginClassInjector> pluginClassInjectors = new ArrayList<>();

    private final PluginClassInjector plainClassLoaderHandler;

    public JarProfilerPluginClassInjector(PluginConfig pluginConfig, InstrumentEngine instrumentEngine,
            BootstrapCore bootstrapCore) {
        Objects.requireNonNull(pluginConfig, "pluginConfig");

        this.bootstrapCore = Objects.requireNonNull(bootstrapCore, "bootstrapCore");
        pluginClassInjectors.add(new BootstrapClassLoaderHandler(pluginConfig, instrumentEngine));
        pluginClassInjectors.add(new URLClassLoaderHandler(pluginConfig));
        PluginClassInjector builtinClassLoaderHandler = createBuiltinClassLoaderHandler(pluginConfig);
        if (builtinClassLoaderHandler != null) {
            pluginClassInjectors.add(builtinClassLoaderHandler);
        }
        plainClassLoaderHandler = new PlainClassLoaderHandler(pluginConfig);
    }

    private PluginClassInjector createBuiltinClassLoaderHandler(PluginConfig pluginConfig) {
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            final ClassLoader agentClassLoader = JarProfilerPluginClassInjector.class.getClassLoader();
            final String name = "cn.polarismesh.agent.core.asm9.instrument.BuiltinClassLoaderHandler";
            try {
                Class<PluginClassInjector> defineClassClazz = (Class<PluginClassInjector>) agentClassLoader.loadClass(name);
                Constructor<PluginClassInjector> constructor = defineClassClazz.getDeclaredConstructor(PluginConfig.class);
                return constructor.newInstance(pluginConfig);
            } catch (Exception e) {
                throw new IllegalStateException(name + " create fail Caused by:" + e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (bootstrapCore.isBootstrapPackage(className)) {
                return bootstrapCore.loadClass(className);
            }
            for (PluginClassInjector pluginClassInjector : pluginClassInjectors) {
                if (pluginClassInjector.match(classLoader)) {
                    return pluginClassInjector.injectClass(classLoader, className);
                }
            }
            return plainClassLoaderHandler.injectClass(classLoader, className);
        } catch (Throwable e) {
            // fixed for LinkageError
            logger.warn(String.format("failed to load plugin class %s with classLoader %s", className, classLoader), e);
            throw new PolarisAgentException(
                    "Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            if (bootstrapCore.isBootstrapPackageByInternalName(internalName)) {
                return bootstrapCore.openStream(internalName);
            }
            for (PluginClassInjector pluginClassInjector : pluginClassInjectors) {
                if (pluginClassInjector.match(targetClassLoader)) {
                    return pluginClassInjector.getResourceAsStream(targetClassLoader, internalName);
                }
            }
            return plainClassLoaderHandler.getResourceAsStream(targetClassLoader, internalName);
        } catch (Throwable e) {
            logger.warn(String.format("failed to load plugin resource as stream %s with classLoader %s", internalName,
                    targetClassLoader), e);
            return null;
        }
    }
}