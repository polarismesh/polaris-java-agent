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

import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboAbstractDirectoryInterceptor;
import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboExporterInterceptor;
import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboExtensionLoaderInterceptor;
import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboInvokeInterceptor;
import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboRegistryDirectoryInterceptor;
import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboRegistryFactoryInterceptor;
import cn.polarismesh.agent.pinpoint.dubbox.Interceptor.DubboUrlInterceptor;
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
import java.util.List;
import java.util.Map;

/**
 * @author K
 */
public class DubboPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform(ClassNames.REGISTRY_PROTOCOL_NAME, RegistryProtocolTransform.class);
        transformTemplate.transform(ClassNames.REGISTRY_DIRECTORY_NAME, RegistryDirectoryTransform.class);
        transformTemplate.transform(ClassNames.ABSTRACT_EXPORTER_NAME, AbstractExporterTransform.class);
        transformTemplate.transform(ClassNames.URL_NAME, UrlConstructorTransform.class);
        transformTemplate.transform(ClassNames.CLUSTER_INVOKER_NAME, ClusterInvokerTransform.class);
        transformTemplate.transform(ClassNames.DIRECTORY_NAME, AbstractDirectoryTransform.class);
        transformTemplate.transform(ClassNames.EXTENSION_LOADER_NAME, AbstractExtensionLoaderTransform.class);
    }

    public static class RegistryProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target
                    .getDeclaredMethod("setRegistryFactory", ClassNames.REGISTRY_FACTORY_NAME);
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboRegistryFactoryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class RegistryDirectoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("toInvokers", List.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboRegistryDirectoryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractExtensionLoaderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target
                    .getDeclaredMethod("createExtension", String.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboExtensionLoaderInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractDirectoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target
                    .getConstructor(ClassNames.URL_NAME, ClassNames.URL_NAME, List.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboAbstractDirectoryInterceptor.class);
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
            InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", ClassNames.INVOCATION_NAME);
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboInvokeInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractExporterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor(ClassNames.RPC_INVOKER_NAME);
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
