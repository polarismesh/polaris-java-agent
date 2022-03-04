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

package cn.polarismesh.agent.pinpoint.dubbox;

import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.*;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.integration.RegistryProtocol;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import com.alibaba.dubbo.rpc.protocol.AbstractExporter;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import com.alibaba.dubbo.rpc.protocol.injvm.InjvmProtocol;
import com.alibaba.dubbo.rpc.protocol.memcached.MemcachedProtocol;
import com.alibaba.dubbo.rpc.protocol.redis.RedisProtocol;
import com.alibaba.dubbo.rpc.protocol.thrift.ThriftProtocol;
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
import java.util.Map;

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
        transformTemplate
                .transform("com.alibaba.dubbo.registry.integration.RegistryProtocol", RegistryProtocolTransform.class);

        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol", ProtocolTransform.class);
        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol", ProtocolTransform.class);
        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.redis.RedisProtocol", ProtocolTransform.class);
        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.injvm.InjvmProtocol", ProtocolTransform.class);
        transformTemplate
                .transform("com.alibaba.dubbo.rpc.protocol.memcached.MemcachedProtocol", ProtocolTransform.class);
        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.thrift.ThriftProtocol", ProtocolTransform.class);

        transformTemplate.transform("com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker",
                ClusterInvokerTransform.class);

        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.AbstractExporter", ExporterTransform.class);

        transformTemplate.transform("com.alibaba.dubbo.common.URL", UrlConstructorTransform.class);
    }

    public static class RegistryProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target
                    .getDeclaredMethod("setRegistryFactory", RegistryFactory.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboRegistryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target
                    .getDeclaredMethod("refer", Class.class.getCanonicalName(), URL.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboInvokerInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ClusterInvokerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target
                    .getConstructor(Directory.class.getCanonicalName(), URL.class.getCanonicalName());
            if (constructor != null) {
                constructor.addInterceptor(DubboClusterInvokerInterceptor.class);
            }
            InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", Invocation.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboInvokeInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ExporterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor(Invoker.class.getCanonicalName());
            if (constructor != null) {
                constructor.addInterceptor(DubboExporterInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class UrlConstructorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target
                    .getConstructor(String.class.getCanonicalName(), String.class.getCanonicalName(),
                            String.class.getCanonicalName(), String.class.getCanonicalName(),
                            int.class.getCanonicalName(), String.class.getCanonicalName(),
                            Map.class.getCanonicalName());
            if (constructor != null) {
                constructor.addInterceptor(DubboUrlInterceptor.class);
            }
            return target.toBytecode();
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
