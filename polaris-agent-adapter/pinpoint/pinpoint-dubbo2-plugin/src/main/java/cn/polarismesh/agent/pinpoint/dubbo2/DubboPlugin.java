/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.pinpoint.dubbo2;

import cn.polarismesh.agent.pinpoint.dubbo2.Interceptor.*;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

/**
 * @author K
 */
public class DubboPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        this.addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform("org.apache.dubbo.config.context.ConfigManager", ConfigManagerTransform.class);
        transformTemplate.transform("org.apache.dubbo.registry.integration.RegistryProtocol", RegistryProtocolTransform.class);
        transformTemplate.transform("org.apache.dubbo.rpc.protocol.AbstractProtocol", AbstractProtocolTransform.class);
        transformTemplate.transform("org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker", AbstractClusterInvokerTransform.class);
    }

    public static class ConfigManagerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod1 = target.getDeclaredMethod("getConfigCenters");
            if (invokeMethod1 != null) {
                invokeMethod1.addInterceptor(DubboConfigInterceptor.class);
            }
            InstrumentMethod invokeMethod2 = target.getDeclaredMethod("getMetadataConfigs");
            if (invokeMethod2 != null) {
                invokeMethod2.addInterceptor(DubboMetadataInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class RegistryProtocolTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("setRegistryFactory", "org.apache.dubbo.registry.RegistryFactory");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboRegistryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractProtocolTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("refer", "java.lang.Class", "org.apache.dubbo.common.URL");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboInvokerInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractClusterInvokerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("org.apache.dubbo.rpc.cluster.Directory", "org.apache.dubbo.common.URL");
            if (constructor != null) {
                constructor.addInterceptor(DubboClusterInvokerInterceptor.class);
            }
            InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "org.apache.dubbo.rpc.Invocation");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboInvokeInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
