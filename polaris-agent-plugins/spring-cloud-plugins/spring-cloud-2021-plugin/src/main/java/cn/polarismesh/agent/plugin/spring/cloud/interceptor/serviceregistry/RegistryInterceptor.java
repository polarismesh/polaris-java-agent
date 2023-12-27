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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.serviceregistry;


import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.DiscoveryUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.cloud.polaris.PolarisDiscoveryProperties;
import com.tencent.cloud.polaris.registry.PolarisRegistration;
import com.tencent.cloud.polaris.registry.PolarisServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.Collections;

/**
 * hack {@link AbstractAutoServiceRegistration#start()}
 * <p>
 * Polaris Ribbon Server 实现类
 */
public class RegistryInterceptor extends BaseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryInterceptor.class);

    @Override
    public void onBefore(Object target, Object[] args) {
        if (!Holder.isAllowDiscovery()) {
            return;
        }

        LOGGER.debug("[PolarisAgent] replace ServiceRegistry to ProxyServiceRegistry, target : {}", target);

        String clsName = target.getClass().getCanonicalName();
        if (clsName.contains("org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration")) {
            ServiceRegistry<Registration> registry = (ServiceRegistry<Registration>) ReflectionUtils.getObjectByFieldName(target, "serviceRegistry");
            ReflectionUtils.setValueByFieldName(target, "serviceRegistry", new ProxyServiceRegistry(registry));
        } else {
            ServiceRegistry<Registration> registry = (ServiceRegistry<Registration>) ReflectionUtils.getSuperObjectByFieldName(target, "serviceRegistry");
            ReflectionUtils.setSuperValueByFieldName(target, "serviceRegistry", new ProxyServiceRegistry(registry));
        }

        LOGGER.debug("[PolarisAgent] finished replace ServiceRegistry to ProxyServiceRegistry");
    }

    public static class ProxyServiceRegistry implements ServiceRegistry<Registration> {

        private final ServiceRegistry<Registration> target;

        private final PolarisServiceRegistry polarisRegistry;

        public ProxyServiceRegistry(ServiceRegistry<Registration> target) {
            this.target = target;
            this.polarisRegistry = new PolarisServiceRegistry(Holder.getDiscoveryProperties(), Holder.getContextManager(),
                    DiscoveryUtils.buildDiscoveryHandler(), Holder.getStaticMetadataManager(), Holder.getPolarisStatProperties());
        }

        private PolarisRegistration buildPolarisRegistration() {
            return new PolarisRegistration(
                    Holder.getDiscoveryProperties(),
                    Holder.getPolarisContextProperties(),
                    Holder.getConsulContextProperties(),
                    Holder.getContextManager().getSDKContext(),
                    Holder.getStaticMetadataManager(),
                    Holder.getNacosContextProperties(),
                    getBeanIfExist(ServletWebServerApplicationContext.class),
                    getBeanIfExist(ReactiveWebServerApplicationContext.class),
                    Collections.emptyList()
            );
        }

        @Override
        public void register(Registration registration) {
            LOGGER.info("[PolarisAgent] begin do register to polaris action.");
            Holder.getPolarisContextProperties().setLocalPort(registration.getPort());
            PolarisRegistration polarisRegistration = buildPolarisRegistration();
            polarisRegistry.register(polarisRegistration);
        }

        @Override
        public void deregister(Registration registration) {
            LOGGER.info("[PolarisAgent] begin de deregister from polaris action.");
            Holder.getPolarisContextProperties().setLocalPort(registration.getPort());
            PolarisRegistration polarisRegistration = buildPolarisRegistration();
            polarisRegistry.deregister(polarisRegistration);
        }

        @Override
        public void close() {
            target.close();
            polarisRegistry.close();
        }

        @Override
        public void setStatus(Registration registration, String status) {
            PolarisRegistration polarisRegistration = buildPolarisRegistration();
            polarisRegistry.setStatus(polarisRegistration, status);
        }

        @Override
        public <T> T getStatus(Registration registration) {
            PolarisRegistration polarisRegistration = buildPolarisRegistration();
            return (T) polarisRegistry.getStatus(polarisRegistration);
        }

        private <T> T getBeanIfExist(Class<T> cls) {
            try {
                return ApplicationContextAwareUtils.getApplicationContext().getBean(cls);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                return null;
            }
        }

    }

}
