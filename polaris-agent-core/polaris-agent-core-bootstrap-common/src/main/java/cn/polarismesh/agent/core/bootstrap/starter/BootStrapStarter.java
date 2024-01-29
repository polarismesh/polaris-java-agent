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

package cn.polarismesh.agent.core.bootstrap.starter;

import cn.polarismesh.agent.core.asm.instrument.*;
import cn.polarismesh.agent.core.asm.instrument.classloading.BootstrapCore;
import cn.polarismesh.agent.core.asm.instrument.classloading.ClassInjector;
import cn.polarismesh.agent.core.asm.instrument.classloading.ClassInjectorFactory;
import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinitionFactory;
import cn.polarismesh.agent.core.asm.instrument.plugin.*;
import cn.polarismesh.agent.core.asm.instrument.transform.*;
import cn.polarismesh.agent.core.bootstrap.BootLogger;
import cn.polarismesh.agent.core.bootstrap.ModuleBootLoader;
import cn.polarismesh.agent.core.bootstrap.util.AgentDirUtils;
import cn.polarismesh.agent.core.bootstrap.util.Java9ClazzFinder;
import cn.polarismesh.agent.core.bootstrap.util.ModuleUtils;
import cn.polarismesh.agent.core.common.conf.ConfigManager;
import cn.polarismesh.agent.core.common.starter.InstrumentationStarter;
import cn.polarismesh.agent.core.common.utils.CollectionUtils;
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BootStrapStarter {

    private static final BootLogger logger = BootLogger.getLogger(BootStrapStarter.class);

    private final ClassNameFilter agentPackageFilter = new AgentPackageSkipFilter();

    private static final String NAME = "default";
    public String name() {
        return NAME;
    }

    public void start(String agentDirPath, Instrumentation instrumentation, ClassLoader parentClassLoader) {
        ConfigManager.INSTANCE.initConfig(agentDirPath);
        if (ModuleUtils.isModuleSupported()) {
            //Thread.currentThread().setContextClassLoader(urlClassLoader);
            ModuleBootLoader moduleBootLoader = loadModuleBootLoader(instrumentation, parentClassLoader);
            logger.info("defineAgentModule");

            initModuleBootLoader(moduleBootLoader, parentClassLoader);
        }
        InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();
        InstrumentEngine instrumentEngine = new ASMEngine(instrumentation, interceptorDefinitionFactory);
        DynamicTransformerRegistry dynamicTransformerRegistry = new DefaultDynamicTransformerRegistry();
        DynamicTransformTrigger dynamicTransformTrigger = new DynamicTransformService(instrumentation,
                dynamicTransformerRegistry);
        ClassFileTransformerLoader transformerRegistry = new ClassFileTransformerLoader(dynamicTransformTrigger);
        BootstrapCore bootstrapCore = createBootstrapCore(agentDirPath);
        ClassInjectorFactory classInjectorFactory = new ClassInjectorFactory(instrumentEngine, bootstrapCore);
        List<PluginJar> pluginJars = PluginCreator.createPluginJars(agentDirPath);

        ClassLoader pluginClassLoader = PluginLoader.createPluginClassLoader(pluginJars, parentClassLoader);
        List<AgentPlugin> agentPlugins = PluginLoader.loadPlugins(pluginClassLoader);
        if (CollectionUtils.isEmpty(agentPlugins)) {
            return;
        }

        JarPluginComponents jarPluginComponents = new JarPluginComponents(pluginJars);
        for (AgentPlugin agentPlugin : agentPlugins) {
            jarPluginComponents.addAgentPlugin(agentPlugin);
        }
        Iterable<JarPlugin<AgentPlugin>> jarPlugins = jarPluginComponents.buildJarPlugins();
        for (JarPlugin<AgentPlugin> jarPlugin : jarPlugins) {
            logger.info(
                    String.format("%s Plugin %s:%s", jarPlugin.getClass().getCanonicalName(), PluginJar.PLUGIN_PACKAGE,
                            jarPlugin.getPackageList()));
            logger.info(String.format("Loading plugin:%s pluginPackage:%s", jarPlugin.getClass().getName(), jarPlugin));
            List<String> pluginPackageList = jarPlugin.getPackageList();
            ClassNameFilter pluginFilterChain = createPluginFilterChain(pluginPackageList);
            PluginConfig pluginConfig = new PluginConfig(jarPlugin, pluginFilterChain);
            ClassInjector classInjector = classInjectorFactory.newClassInjector(pluginConfig);
            InstrumentContext instrumentContext = new PluginInstrumentContext(instrumentEngine, dynamicTransformTrigger,
                    classInjector, transformerRegistry);
            List<AgentPlugin> instanceList = jarPlugin.getInstanceList();
            for (AgentPlugin agentPlugin : instanceList) {
                GuardInstrumentContext guardInstrumentContext = new GuardInstrumentContext(instrumentContext);
                TransformTemplate transformTemplate = new TransformTemplate(guardInstrumentContext);
                PluginContext pluginContext = new DefaultPluginContext(agentDirPath, transformTemplate);
                try {
                    logger.info(String.format("%s Plugin setup", agentPlugin.getClass().getName()));
                    agentPlugin.init(pluginContext);
                } finally {
                    guardInstrumentContext.close();
                }
            }
            ClassFileTransformer classFileTransformer = new DefaultClassFileTransformerDispatcher(
                    transformerRegistry.getClassTransformerList(), dynamicTransformerRegistry);
            final JvmVersion version = JvmUtils.getVersion();
            if (version.onOrAfter(JvmVersion.JAVA_9)) {
                InstrumentationStarter java9InstrumentationStarter = Java9ClazzFinder.lookup(parentClassLoader);
                classFileTransformer = java9InstrumentationStarter.wrapTransformer(instrumentation, classFileTransformer);
            }
            instrumentation.addTransformer(classFileTransformer, true);
        }
    }

    private void initModuleBootLoader(ModuleBootLoader moduleBootLoader, ClassLoader parentClassLoader) {
        moduleBootLoader.defineAgentModule(parentClassLoader);
    }

    private static ModuleBootLoader loadModuleBootLoader(Instrumentation instrumentation, ClassLoader clazzLoader) {
        // support java9 features, we need to init the java9 library
        logger.info("java9 module detected");
        logger.info("ModuleBootLoader start");
        ModuleBootLoader moduleBootLoader = new ModuleBootLoader(instrumentation);
        moduleBootLoader.loadModuleSupport(clazzLoader);
        return moduleBootLoader;
    }

    private ClassNameFilter createPluginFilterChain(List<String> packageList) {

        ClassNameFilter pluginPackageFilter = new PluginPackageFilter(packageList);

        List<ClassNameFilter> chain = Arrays.asList(agentPackageFilter, pluginPackageFilter);

        return new ClassNameFilterChain(chain);
    }

    private BootstrapCore createBootstrapCore(String agentDirPath) {
        List<String> fileNames = AgentDirUtils.resolveJarPaths(AgentDirUtils.getRootDir(agentDirPath));
        fileNames.addAll(AgentDirUtils.resolveJarPaths(AgentDirUtils.getBootDir(agentDirPath)));
        return new BootstrapCore(fileNames);
    }

}
