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

package cn.polarismesh.agent.pinpoint;

import cn.polarismesh.agent.bootstrap.extension.BootStrapStarter;
import com.navercorp.pinpoint.bootstrap.AgentIdResolver;
import com.navercorp.pinpoint.bootstrap.ArgsParser;
import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.bootstrap.ModuleBootLoader;
import com.navercorp.pinpoint.bootstrap.ModuleUtils;
import com.navercorp.pinpoint.bootstrap.PinpointStarter;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirBaseClassPathResolver;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirectory;
import com.navercorp.pinpoint.bootstrap.agentdir.BootDir;
import com.navercorp.pinpoint.bootstrap.agentdir.ClassPathResolver;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.JarFile;

public class PolarisPinpointBootStrap implements BootStrapStarter {

    private static final BootLogger logger = BootLogger.getLogger(PolarisPinpointBootStrap.class);

    @Override
    public String name() {
        return "pinpoint";
    }

    @Override
    public void start(String agentDirPath, Properties configProperties, String agentArgs,
            Instrumentation instrumentation) {
        final ClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentDirPath);

        final AgentDirectory agentDirectory = resolveAgentDir(classPathResolver);
        if (agentDirectory == null) {
            logger.warn("Agent Directory Verify fail. skipping agent loading.");
            logPinpointAgentLoadFail();
            return;
        }
        logger.info(String.format("agentDirectory is %s, configProperties : %s", agentDirectory, configProperties));
        BootDir bootDir = agentDirectory.getBootDir();
        appendToBootstrapClassLoader(instrumentation, bootDir);
        ClassLoader parentClassLoader = getParentClassLoader();
        final Map<String, String> agentArgsMap = argsToMap(agentArgs);
        final ModuleBootLoader moduleBootLoader = loadModuleBootLoader(instrumentation, parentClassLoader);

        String agentId = System.getProperty(AgentIdResolver.AGENT_ID_SYSTEM_PROPERTY);
        if (null == agentId || agentId.length() == 0) {
            System.setProperty(AgentIdResolver.AGENT_ID_SYSTEM_PROPERTY, UUID.randomUUID().toString());
        }
        String application = System.getProperty(AgentIdResolver.APPLICATION_NAME_SYSTEM_PROPERTY);
        if (null == application || application.length() == 0) {
            System.setProperty(AgentIdResolver.APPLICATION_NAME_SYSTEM_PROPERTY,
                    configProperties.getProperty("spring.application.name"));
        }
        PinpointStarter bootStrap = new PinpointStarter(parentClassLoader, agentArgsMap, agentDirectory,
                instrumentation, moduleBootLoader);
        if (!bootStrap.start()) {
            logPinpointAgentLoadFail();
        }
    }

    private static ModuleBootLoader loadModuleBootLoader(Instrumentation instrumentation,
            ClassLoader parentClassLoader) {
        if (!ModuleUtils.isModuleSupported()) {
            return null;
        }
        logger.info("java9 module detected");
        logger.info("ModuleBootLoader start");
        ModuleBootLoader moduleBootLoader = new ModuleBootLoader(instrumentation, parentClassLoader);
        moduleBootLoader.loadModuleSupport();
        return moduleBootLoader;
    }

    private static Map<String, String> argsToMap(String agentArgs) {
        ArgsParser argsParser = new ArgsParser();
        Map<String, String> agentArgsMap = argsParser.parse(agentArgs);
        if (!agentArgsMap.isEmpty()) {
            logger.info("agentParameter:" + agentArgs);
        }
        return agentArgsMap;
    }

    private static ClassLoader getParentClassLoader() {
        final ClassLoader classLoader = getPinpointBootStrapClassLoader();
        if (classLoader == Object.class.getClassLoader()) {
            logger.info("parentClassLoader:BootStrapClassLoader:" + classLoader);
        } else {
            logger.info("parentClassLoader:" + classLoader);
        }
        return classLoader;
    }


    private static ClassLoader getPinpointBootStrapClassLoader() {
        return PolarisPinpointBootStrap.class.getClassLoader();
    }

    private static void appendToBootstrapClassLoader(Instrumentation instrumentation, BootDir bootDir) {
        List<JarFile> jarFiles = bootDir.openJarFiles();
        for (JarFile jarFile : jarFiles) {
            logger.info("appendToBootstrapClassLoader:" + jarFile.getName());
            instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        }
    }


    private static void logPinpointAgentLoadFail() {
        final String errorLog =
                "*****************************************************************************\n" +
                        "* Pinpoint Agent load failure\n" +
                        "*****************************************************************************";
        System.err.println(errorLog);
    }

    private static AgentDirectory resolveAgentDir(ClassPathResolver classPathResolver) {
        try {
            return classPathResolver.resolve();
        } catch (Exception e) {
            logger.warn("AgentDir resolve fail Caused by:" + e.getMessage(), e);
            return null;
        }
    }
}
