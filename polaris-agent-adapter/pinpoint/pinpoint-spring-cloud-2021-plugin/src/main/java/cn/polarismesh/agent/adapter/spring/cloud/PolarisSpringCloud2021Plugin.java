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

import cn.polarismesh.agent.adapter.spring.cloud.interceptor.*;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.nacos.NacosDiscoveryInterceptor;
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
        transformTemplate.transform("org.springframework.cloud.client.serviceregistry.ServiceRegistry", SpringCloudRegistryTransform.class);
        transformTemplate.transform("org.springframework.cloud.client.serviceregistry.ServiceRegistry", SpringCloudDeRegistryTransform.class);
        transformTemplate.transform("org.springframework.cloud.client.discovery.DiscoveryClient", SpringCloudCommonDiscoveryTransform.class);
        transformTemplate.transform("com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient", SpringCloudNacosDiscoveryTransform.class);
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
            InstrumentMethod method = target.getDeclaredMethod("register");
            if (method != null) {
                method.addInterceptor(RegistryInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    /**
     * SpringCloud 反注册拦截
     */
    public static class SpringCloudDeRegistryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("deregister");
            if (method != null) {
                method.addInterceptor(RegistryInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    /**
     * Spring Cloud Alibaba 服务发现拦截
     */
    public static class SpringCloudNacosDiscoveryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("getInstances");
            if (method != null) {
                method.addInterceptor(NacosDiscoveryInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    /**
     * Spring Cloud Eureka/Consul 服务发现拦截
     */
    public static class SpringCloudCommonDiscoveryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classFileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("getInstances");
            if (method != null) {
                method.addInterceptor(NacosDiscoveryInterceptor.class);
            }

            return target.toBytecode();
        }
    }

}