/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.plugin.dubbo3;

import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.dubbo3.constants.DubboConstants;
import cn.polarismesh.agent.plugin.dubbo3.interceptor.DubboDeployerInterceptor;

import java.security.ProtectionDomain;
import java.util.logging.Logger;

public class Dubbo3MainPlugin implements AgentPlugin {

    private static final Logger LOGGER = Logger.getLogger(Dubbo3MainPlugin.class.getName());

    @Override
    public void init(PluginContext pluginContext) {
        System.setProperty("__agent_conf_path__", pluginContext.getAgentDirPath());
        TransformOperations ops = pluginContext.getTransformOperations();
        ops.transform(DubboConstants.DUBBO_DEPLOYER_CLASS, DeployerTransform.class);
        LOGGER.info("Dubbo 3.x plugin initialized");
    }

    public static class DeployerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader,
                String className, Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(
                    classLoader, className, protectionDomain, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("initialize");
            if (method != null) {
                method.addInterceptor(DubboDeployerInterceptor.class);
            } else {
                LOGGER.warning("initialize() method not found on " + className
                        + ", Dubbo3 polaris transform skipped");
            }
            return target.toBytecode();
        }
    }
}
