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

package cn.polarismesh.agent.plugin.dubbo27;

import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.dubbo27.constants.DubboConstants;
import cn.polarismesh.agent.plugin.dubbo27.interceptor.DubboBootstrapInterceptor;

import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * Dubbo 2.7.x 插件入口，负责注册字节码转换器.
 *
 * <p>通过 SPI 加载后，在 init 方法中注册对 DubboBootstrap.initialize()
 * 的字节码转换，在 Dubbo 初始化完成后将注册中心替换为 Polaris.</p>
 */
public class DubboMainPlugin implements AgentPlugin {

    private static final Logger LOGGER =
            Logger.getLogger(DubboMainPlugin.class.getName());

    @Override
    public void init(PluginContext pluginContext) {
        System.setProperty("__agent_conf_path__",
                pluginContext.getAgentDirPath());
        TransformOperations ops =
                pluginContext.getTransformOperations();

        // 注册 DubboBootstrap.initialize() 拦截
        ops.transform(DubboConstants.DUBBO_BOOTSTRAP_CLASS,
                DubboBootstrapTransform.class);

        LOGGER.info("Dubbo 2.7.x plugin initialized");
    }

    /**
     * DubboBootstrap 字节码转换回调，拦截 initialize 方法.
     */
    public static class DubboBootstrapTransform
            implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor,
                ClassLoader classLoader, String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target =
                    instrumentor.getInstrumentClass(
                            classLoader, className,
                            protectionDomain, classfileBuffer);
            InstrumentMethod method =
                    target.getDeclaredMethod("initialize");
            if (method != null) {
                method.addInterceptor(
                        DubboBootstrapInterceptor.class);
            }
            return target.toBytecode();
        }
    }
}
