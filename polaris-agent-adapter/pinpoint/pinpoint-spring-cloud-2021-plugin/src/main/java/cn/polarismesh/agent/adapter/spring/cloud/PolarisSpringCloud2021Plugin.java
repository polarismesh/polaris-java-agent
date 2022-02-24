/*
 * Copyright 2015 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.adapter.spring.cloud;

import cn.polarismesh.agent.adapter.spring.cloud.interceptor.*;
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
 * Polaris Spring Cloud 2021 Plugin
 *
 * @author zhuyuhan
 */
public class PolarisSpringCloud2021Plugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addPolarisTransformers();
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    /**
     * add polaris transformers
     */
    private void addPolarisTransformers() {
        transformTemplate.transform("org.springframework.boot.SpringApplication", PolarisAgentPropertiesTransform.class);
        transformTemplate.transform("org.springframework.context.support.AbstractApplicationContext", PolarisRegistryTransform.class);
        transformTemplate.transform("org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient", PolarisDiscoveryTransform.class);
        transformTemplate.transform("org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient", PolarisServiceInfoTransform.class);
        transformTemplate.transform("feign.SynchronousMethodHandler", PolarisFeignInvokeTransform.class);
        transformTemplate.transform("org.springframework.web.client.RestTemplate", PolarisRestTemplateInvokeTransform.class);
        transformTemplate.transform("org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient", PolarisFeignInvokeStatusTransform.class);
        transformTemplate.transform("com.netflix.loadbalancer.LoadBalancerContext", PolarisLoadBalancerTransform.class);
    }

    public static class PolarisAgentPropertiesTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("refreshContext", "org.springframework.context.ConfigurableApplicationContext");
            if (method != null) {
                method.addInterceptor(PolarisAgentPropertiesInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisRegistryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("finishRefresh");
            if (method != null) {
                method.addInterceptor(PolarisRegistryInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisDiscoveryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("<init>", "java.util.List");
            if (method != null) {
                method.addInterceptor(PolarisDiscoveryInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisLoadBalancerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {


            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("<init>", "com.netflix.loadbalancer.ILoadBalancer", "com.netflix.client.config.IClientConfig");
            if (method != null) {
                method.addInterceptor(PolarisRibbonInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisServiceInfoTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {


            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod choose = target.getDeclaredMethod("choose", "java.lang.String", "org.springframework.cloud.client.loadbalancer.Request");
            if (choose != null) {
                choose.addInterceptor(PolarisServiceInfoInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisFeignInvokeTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {


            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.Object[]");
            if (method != null) {
                method.addInterceptor(PolarisFeignInvokeInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisRestTemplateInvokeTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {


            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("handleResponse", "java.net.URI", "org.springframework.http.HttpMethod", "org.springframework.http.client.ClientHttpResponse");
            if (method != null) {
                method.addInterceptor(PolarisFeignInvokeInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public static class PolarisFeignInvokeStatusTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {


            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("execute", "feign.Request", "feign.Request$Options");
            if (method != null) {
                method.addInterceptor(PolarisFeignInvokeStatusInterceptor.class);
            }

            return target.toBytecode();
        }

    }

}