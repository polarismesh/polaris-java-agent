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

package cn.polarismesh.agent.core.bootstrap;

import cn.polarismesh.agent.core.bootstrap.starter.BootStrapStarter;
import cn.polarismesh.agent.core.bootstrap.util.AgentDirUtils;
import cn.polarismesh.agent.core.common.utils.JarFileUtils;
import cn.polarismesh.agent.core.bootstrap.PolarisInitProperties;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.jar.JarFile;

public class PolarisAgentBootStrap {

    private static final BootLogger logger = BootLogger.getLogger(PolarisAgentBootStrap.class);

    public static boolean isAttach = false;

    private static final LoadState STATE = new LoadState();

    public static void premain(String agentArgs, Instrumentation instrumentation, String agentDirPath) {
        final boolean success = STATE.start();
        if (!success) {
            logger.warn("[Bootstrap] polaris-bootstrap already started. skipping agent loading.");
            return;
        }

        PolarisInitProperties polarisInitProperties = new PolarisInitProperties();
        polarisInitProperties.initialize();

        logger.info("[Bootstrap] polaris-agent agentArgs:" + agentArgs);
        logger.info("[Bootstrap] polarisAgentBootStrap.ClassLoader:" + PolarisAgentBootStrap.class.getClassLoader());
        logger.info("[Bootstrap] contextClassLoader:" + Thread.currentThread().getContextClassLoader());

        appendToBootstrapClassLoader(instrumentation, agentDirPath);
        appendToBootstrapClassLoader(instrumentation, AgentDirUtils.getBootDir(agentDirPath));

        ClassLoader parentClassLoader = getParentClassLoader();
        BootStrapStarter starter = new BootStrapStarter();
        logger.info(String.format("[Bootstrap] start bootStrapStarter:%s", starter.name()));
        starter.start(agentDirPath, instrumentation, parentClassLoader);
    }

    private static void appendToBootstrapClassLoader(Instrumentation instrumentation, String bootDir) {
        List<String> jarFileNames = AgentDirUtils.resolveJarPaths(bootDir);
        if (jarFileNames.isEmpty()) {
            return;
        }
        List<JarFile> jarFiles = JarFileUtils.openJarFiles(jarFileNames);
        for (JarFile jarFile : jarFiles) {
            logger.info("appendToBootstrapClassLoader:" + jarFile.getName());
            instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        }
    }

    private static ClassLoader getParentClassLoader() {
        ClassLoader classLoader = PolarisAgentBootStrap.class.getClassLoader();
        if (classLoader == Object.class.getClassLoader()) {
            logger.info("parentClassLoader:BootStrapClassLoader:" + classLoader );
        } else {
            logger.info("parentClassLoader:" + classLoader);
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }
}
