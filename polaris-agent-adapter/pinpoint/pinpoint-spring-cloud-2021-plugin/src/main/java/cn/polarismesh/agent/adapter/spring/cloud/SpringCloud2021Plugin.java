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

package cn.polarismesh.agent.adapter.spring.cloud;

import cn.polarismesh.agent.adapter.spring.cloud.interceptor.aware.ApplicationContextAwareInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.discovery.DiscoveryInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.discovery.ReactiveDiscoveryInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.discovery.RegistryInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.filter.ReactiveWebFilterInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.filter.ServletWebFilterInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.invoker.FeignInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.invoker.RestTemplateInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.router.ServiceInstanceListSupplierBuilderInterceptor;
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
import java.util.logging.Logger;

import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Polaris Spring Cloud 2021 Plugin
 *
 * @author zhuyuhan
 */
public class SpringCloud2021Plugin implements ProfilerPlugin, TransformTemplateAware {

    private static final Logger LOGGER = Logger.getGlobal();

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
        transformTemplate.transform(ClassNames.SERVICE_REGISTRATION, SpringCloudRegistryTransform.class);
        transformTemplate.transform(ClassNames.DISCOVERY_CLIENT, SpringCloudDiscoveryTransform.class);
        transformTemplate.transform(ClassNames.REACTIVE_DISCOVERY_CLIENT, SpringCloudReactiveDiscoveryTransform.class);

        // 北极星路由执行
        transformTemplate.transform(ClassNames.ROUTER, SpringCloudTrafficRouterTransform.class);

        // 流量标签信息收集
        transformTemplate.transform(ClassNames.REACTIVE_WEB_FILTER, ReactiveWebFilterTransform.class);
        transformTemplate.transform(ClassNames.SERVLET_WEB_FILTER, ServletWebFilterTransform.class);

        // 请求发起时需要收集流量标签信息
        transformTemplate.transform(ClassNames.REST_TEMPLATE, RestTemplateTransform.class);
        transformTemplate.transform(ClassNames.FEIGN_TEMPLATE, FeignTransform.class);

        // 在 agent 中注入 Spring 的 ApplicationContext
        transformTemplate.transform(ClassNames.APPLICATION_CONTEXT_AWARE, ApplicationContextAwareTransform.class);

        // EnvironmentPostProcessor 处理
        transformTemplate.transform(ClassNames.ENVIRONMENT_POST_PROCESSOR, DisableSpringCloudAlibabaTransform.class);
    }

    /**
     * SpringCloud 注册拦截
     */
    public static class SpringCloudRegistryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod registerMethod = target.getDeclaredMethod("start");
            if (registerMethod != null) {
                registerMethod.addInterceptor(RegistryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    /**
     * Spring Cloud 服务发现拦截
     */
    public static class SpringCloudDiscoveryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getConstructor("java.util.List");
            if (constructMethod != null) {
                constructMethod.addInterceptor(DiscoveryInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SpringCloudReactiveDiscoveryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getConstructor("java.util.List");
            if (constructMethod != null) {
                constructMethod.addInterceptor(ReactiveDiscoveryInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SpringCloudTrafficRouterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);

            InstrumentMethod withBlockingDiscoveryClient = target.getDeclaredMethod("withBlockingDiscoveryClient");
            if (withBlockingDiscoveryClient != null) {
                withBlockingDiscoveryClient.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderBlockingInterceptor.class);
            }

            InstrumentMethod withDiscoveryClient = target.getDeclaredMethod("withDiscoveryClient");
            if (withDiscoveryClient != null) {
                withDiscoveryClient.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderReactiveInterceptor.class);
            }

            InstrumentMethod withCaching = target.getDeclaredMethod("withCaching");
            if (withCaching != null) {
                withCaching.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderDisableCachingInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveWebFilterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getConstructor("org.springframework.web.server.WebHandler", "java.util.List");
            if (constructMethod != null) {
                constructMethod.addInterceptor(ReactiveWebFilterInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ServletWebFilterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getDeclaredMethod(/* "doDispatch" */ "initStrategies",
                    "org.springframework.context.ApplicationContext");
            if (constructMethod != null) {
                constructMethod.addInterceptor(ServletWebFilterInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class RestTemplateTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getDeclaredMethod("getInterceptors");
            if (constructMethod != null) {
                constructMethod.addInterceptor(RestTemplateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class FeignTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getDeclaredMethod("getInheritedAwareInstances",
                    "org.springframework.cloud.openfeign.FeignContext", "java.lang.Class");
            if (constructMethod != null) {
                constructMethod.addInterceptor(FeignInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    /**
     * 注入 spring 的 {@link ApplicationContext}
     */
    public static class ApplicationContextAwareTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getConstructor("org.springframework.context.ConfigurableApplicationContext");
            if (constructMethod != null) {
                constructMethod.addInterceptor(ApplicationContextAwareInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DisableSpringCloudAlibabaTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod constructMethod = target.getDeclaredMethod("", "java.util.stream.Stream");
            if (constructMethod != null) {
                constructMethod.addInterceptor(ApplicationContextAwareInterceptor.class);
            }

            return target.toBytecode();
        }
    }

}