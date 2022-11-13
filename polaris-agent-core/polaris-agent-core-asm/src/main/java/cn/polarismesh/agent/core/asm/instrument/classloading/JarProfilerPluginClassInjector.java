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
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Objects;

public class JarProfilerPluginClassInjector implements ClassInjector {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(JarProfilerPluginClassInjector.class.getCanonicalName());

    private final BootstrapCore bootstrapCore;
    private final ClassInjector bootstrapClassLoaderHandler;
    private final ClassInjector urlClassLoaderHandler;
    private final ClassInjector plainClassLoaderHandler;

    public JarProfilerPluginClassInjector(PluginConfig pluginConfig, InstrumentEngine instrumentEngine,
            BootstrapCore bootstrapCore) {
        Objects.requireNonNull(pluginConfig, "pluginConfig");

        this.bootstrapCore = Objects.requireNonNull(bootstrapCore, "bootstrapCore");
        this.bootstrapClassLoaderHandler = new BootstrapClassLoaderHandler(pluginConfig, instrumentEngine);
        this.urlClassLoaderHandler = new URLClassLoaderHandler(pluginConfig);
        this.plainClassLoaderHandler = new PlainClassLoaderHandler(pluginConfig);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (bootstrapCore.isBootstrapPackage(className)) {
                return bootstrapCore.loadClass(className);
            }
            if (classLoader == null) {
                return bootstrapClassLoaderHandler.injectClass(null, className);
            } else if (classLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                return urlClassLoaderHandler.injectClass(urlClassLoader, className);
            } else {
                return plainClassLoaderHandler.injectClass(classLoader, className);
            }
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
            if (targetClassLoader == null) {
                return bootstrapClassLoaderHandler.getResourceAsStream(null, internalName);
            } else if (targetClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) targetClassLoader;
                return urlClassLoaderHandler.getResourceAsStream(urlClassLoader, internalName);
            } else {
                return plainClassLoaderHandler.getResourceAsStream(targetClassLoader, internalName);
            }
        } catch (Throwable e) {
            logger.warn(String.format("failed to load plugin resource as stream %s with classLoader %s", internalName,
                    targetClassLoader), e);
            return null;
        }
    }
}