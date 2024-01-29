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
import java.util.Objects;

public class BootstrapClassLoaderHandler implements PluginClassInjector {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(BootstrapClassLoaderHandler.class.getCanonicalName());

    private final PluginConfig pluginConfig;
    private final InstrumentEngine instrumentEngine;

    private final Object lock = new Object();
    private volatile boolean injectedToRoot = false;

    public BootstrapClassLoaderHandler(PluginConfig pluginConfig, InstrumentEngine instrumentEngine) {
        this.pluginConfig = Objects.requireNonNull(pluginConfig, "pluginConfig");
        this.instrumentEngine = Objects.requireNonNull(instrumentEngine, "instrumentEngine");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        if (classLoader != Object.class.getClassLoader()) {
            throw new IllegalStateException("not BootStrapClassLoader");
        }
        try {
            return (Class<T>) injectClass0(className);
        } catch (Exception e) {
            logger.warn(String.format("Failed to load plugin class %s with classLoader %s", className, classLoader), e);
            throw new PolarisAgentException(
                    "Failed to load plugin class " + className + " with classLoader " + classLoader,
                    e);
        }
    }

    private Class<?> injectClass0(String className) throws IllegalArgumentException, ClassNotFoundException {
        appendToBootstrapClassLoaderSearch();
        return Class.forName(className, false, null);
    }

    private void appendToBootstrapClassLoaderSearch() {
        // DCL
        if (injectedToRoot) {
            return;
        }
        synchronized (lock) {
            if (!this.injectedToRoot) {
                instrumentEngine.appendToBootstrapClassPath(pluginConfig.getPluginJarFile());
                // Memory visibility WARNING
                // Reordering is not recommended.
                this.injectedToRoot = true;
            }
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        ClassLoader currentClassloader = null;
        try {
            if (targetClassLoader == null) {
                currentClassloader = ClassLoader.getSystemClassLoader();
                if (currentClassloader == null) {
                    return null;
                }
                appendToBootstrapClassLoaderSearch();
                return currentClassloader.getResourceAsStream(internalName);
            }
        } catch (Exception e) {
            logger.warn(String.format("failed to load plugin resource as stream %s with classLoader %s", internalName,
                    currentClassloader), e);
            return null;
        }
        logger.warn(String.format("Invalid bootstrap class loader. cl=%s", targetClassLoader));
        return null;
    }

    @Override
    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    @Override
    public boolean match(ClassLoader classLoader) {
        return classLoader == null;
    }
}