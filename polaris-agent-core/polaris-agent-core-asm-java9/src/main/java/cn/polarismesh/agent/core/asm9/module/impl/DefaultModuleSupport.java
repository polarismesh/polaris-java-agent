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

package cn.polarismesh.agent.core.asm9.module.impl;

import cn.polarismesh.agent.core.asm9.module.JavaModule;
import cn.polarismesh.agent.core.asm9.module.Providers;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.starter.ModuleSupport;
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;
import jdk.internal.module.Modules;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class DefaultModuleSupport implements ModuleSupport {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(DefaultModuleSupport.class.getCanonicalName());
    private final Instrumentation instrumentation;

    private final JavaModule javaBaseModule;
    private final JavaModule bootstrapModule;


    public DefaultModuleSupport(Instrumentation instrumentation) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.javaBaseModule = wrapJavaModule(Object.class);
        this.bootstrapModule = wrapJavaModule(this.getClass());
    }

    public void setup() {
        // pinpoint module name : unnamed
        JavaModule bootstrapModule = this.bootstrapModule;
        logger.info("pinpoint Module id:" + bootstrapModule);
        logger.info("pinpoint Module.isNamed:" + bootstrapModule.isNamed());
        logger.info("pinpoint Module.name:" + bootstrapModule.getName());

        JavaModule baseModule = this.javaBaseModule;
        baseModule.addExports("jdk.internal.loader", bootstrapModule);
        baseModule.addExports("jdk.internal.misc", bootstrapModule);
        baseModule.addExports("jdk.internal.module", bootstrapModule);
        baseModule.addOpens("java.net", bootstrapModule);

    }


    public JavaModule wrapJavaModule(Class<?> clazz) {
        return new Java9Module(instrumentation, clazz.getModule());
    }

    public JavaModule wrapJavaModule(Module module) {
        return new Java9Module(instrumentation, module);
    }

    public void defineAgentModule(ClassLoader classLoader) {
        JavaModule agentModule = wrapJavaModule(classLoader.getUnnamedModule());
        prepareAgentModule(classLoader, agentModule);
    }

    public void baseModuleAddOpens(List<String> packageNames, JavaModule targetModule) {
        for (String packageName: packageNames) {
            javaBaseModule.addOpens(packageName, targetModule);
        }
    }

    private void prepareAgentModule(ClassLoader classLoader, JavaModule agentModule) {

        // Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make protected void java.net.URLClassLoader.addURL(java.net.URL) accessible:
        // module java.base does not "opens java.net" to agentModule
        JavaModule baseModule = this.javaBaseModule;
        baseModule.addOpens("java.net", agentModule);
        // java.lang.reflect.InaccessibleObjectException: Unable to make private java.nio.DirectByteBuffer(long,int) accessible: module java.base does not "opens java.nio" to module pinpoint.agent
        //   at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:337)
        baseModule.addOpens("java.nio", agentModule);

        // for Java9DefineClass
        baseModule.addExports("jdk.internal.misc", agentModule);
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_11)) {
            final String internalAccessModule = "jdk.internal.access";
            if (baseModule.getPackages().contains(internalAccessModule)) {
                baseModule.addExports(internalAccessModule, agentModule);
            } else {
                logger.info(internalAccessModule + " package not found");
            }
            baseModule.addOpens("jdk.internal.loader", agentModule);
        }

        agentModule.addReads(baseModule);

        final JavaModule instrumentModule = loadModule("java.instrument");
        agentModule.addReads(instrumentModule);

        final JavaModule managementModule = loadModule("java.management");
        agentModule.addReads(managementModule);

        // DefaultCpuLoadMetric : com.sun.management.OperatingSystemMXBean
        final JavaModule jdkManagement = loadModule("jdk.management");
        agentModule.addReads(jdkManagement);

        // for grpc's NameResolverProvider
        final JavaModule jdkUnsupported = loadModule("jdk.unsupported");
        agentModule.addReads(jdkUnsupported);

        Class<?> pluginClazz = forName("cn.polarismesh.agent.core.extension.AgentPlugin", classLoader);
        agentModule.addUses(pluginClazz);

//        List<Providers> providersList = agentModule.getProviders();
//        for (Providers providers : providersList) {
//            logger.info("discard provider:" + providers);
//        }
    }

    private JavaModule loadModule(String moduleName) {
        // force base-module loading
        logger.info("loadModule:" + moduleName);
        final Module module = Modules.loadModule(moduleName);
        return wrapJavaModule(module);
    }

    private Class<?> forName(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(className + " not found Caused by:" + e.getMessage(), e);
        }
    }

}