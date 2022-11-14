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

package cn.polarismesh.agent.plugin.main;

import cn.polarismesh.agent.core.common.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.interceptor.LoggingInterceptor;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import java.security.ProtectionDomain;

public class MainPlugin implements AgentPlugin {

    //private static final Logger LOGGER = LoggerFactory.getLogger(MainPlugin.class);

    @Override
    public void init(PluginContext pluginContext) {
        TransformOperations transformTemplate = pluginContext.getTransformOperations();
        addTransformers(transformTemplate);
    }

    private void addTransformers(TransformOperations transformTemplate) {
        transformTemplate.transform("cn.polarismesh.test.MainHandler", MainEntryTransform.class);
    }

    public static class MainEntryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod mainMethod = target
                    .getDeclaredMethod("foo", Object[].class.getCanonicalName());
            if (mainMethod != null) {
                mainMethod.addInterceptor(LoggingInterceptor.class);
                System.out.printf("[POLARIS] add interceptor for %s, method %s%n", className, "foo");

            }
            return target.toBytecode();
        }
    }
}
