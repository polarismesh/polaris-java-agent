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

import cn.polarismesh.agent.core.asm.instrument.ASMEngine;
import cn.polarismesh.agent.core.asm.instrument.DynamicTransformTrigger;
import cn.polarismesh.agent.core.asm.instrument.GuardInstrumentContext;
import cn.polarismesh.agent.core.asm.instrument.InstrumentContext;
import cn.polarismesh.agent.core.asm.instrument.InstrumentEngine;
import cn.polarismesh.agent.core.asm.instrument.classloading.BootstrapCore;
import cn.polarismesh.agent.core.asm.instrument.classloading.ClassInjector;
import cn.polarismesh.agent.core.asm.instrument.classloading.ClassInjectorFactory;
import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinitionFactory;
import cn.polarismesh.agent.core.asm.instrument.plugin.AgentPackageSkipFilter;
import cn.polarismesh.agent.core.asm.instrument.plugin.ClassFileTransformerLoader;
import cn.polarismesh.agent.core.asm.instrument.plugin.ClassNameFilter;
import cn.polarismesh.agent.core.asm.instrument.plugin.ClassNameFilterChain;
import cn.polarismesh.agent.core.asm.instrument.plugin.DefaultPluginContext;
import cn.polarismesh.agent.core.asm.instrument.plugin.JarPlugin;
import cn.polarismesh.agent.core.asm.instrument.plugin.JarPluginComponents;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginInstrumentContext;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginJar;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginLoader;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginPackageFilter;
import cn.polarismesh.agent.core.asm.instrument.transform.DefaultClassFileTransformerDispatcher;
import cn.polarismesh.agent.core.asm.instrument.transform.DefaultDynamicTransformerRegistry;
import cn.polarismesh.agent.core.asm.instrument.transform.DynamicTransformService;
import cn.polarismesh.agent.core.asm.instrument.transform.DynamicTransformerRegistry;
import cn.polarismesh.agent.core.asm.instrument.transform.TransformTemplate;
import cn.polarismesh.agent.core.bootstrap.BootLogger;
import cn.polarismesh.agent.core.bootstrap.util.AgentDirUtils;
import cn.polarismesh.agent.core.common.utils.CollectionUtils;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

public class DefaultBootStrapStarter implements BootStrapStarter {

    private static final BootLogger logger = BootLogger.getLogger(DefaultBootStrapStarter.class);

    private final ClassNameFilter agentPackageFilter = new AgentPackageSkipFilter();

    private static final String NAME = "default";

    private static final String BOOT_DIR = "boot";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start(String agentDirPath, String agentArgs, Instrumentation instrumentation) {
        InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();
        InstrumentEngine instrumentEngine = new ASMEngine(instrumentation, interceptorDefinitionFactory);
        DynamicTransformerRegistry dynamicTransformerRegistry = new DefaultDynamicTransformerRegistry();
        DynamicTransformTrigger dynamicTransformTrigger = new DynamicTransformService(instrumentation,
                dynamicTransformerRegistry);
        ClassFileTransformerLoader transformerRegistry = new ClassFileTransformerLoader(dynamicTransformTrigger);
        BootstrapCore bootstrapCore = createBootstrapCore(agentDirPath);
        ClassInjectorFactory classInjectorFactory = new ClassInjectorFactory(instrumentEngine, bootstrapCore);
        List<PluginJar> pluginJars = PluginCreator.createPluginJars(agentDirPath);

        ClassLoader pluginClassLoader = PluginLoader.createPluginClassLoader(pluginJars);
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
            instrumentation.addTransformer(classFileTransformer, true);
        }
    }

    private ClassNameFilter createPluginFilterChain(List<String> packageList) {

        ClassNameFilter pluginPackageFilter = new PluginPackageFilter(packageList);

        List<ClassNameFilter> chain = Arrays.asList(agentPackageFilter, pluginPackageFilter);

        return new ClassNameFilterChain(chain);
    }

    private BootstrapCore createBootstrapCore(String agentDirPath) {
        List<String> fileNames = AgentDirUtils.resolveJarPaths(AgentDirUtils.getBootDir(agentDirPath));
        return new BootstrapCore(fileNames);
    }

}
