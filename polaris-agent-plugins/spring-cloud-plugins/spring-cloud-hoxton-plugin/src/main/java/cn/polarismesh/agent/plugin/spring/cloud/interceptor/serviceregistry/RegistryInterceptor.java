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
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * hack {@link AbstractAutoServiceRegistration#start()}
 * <p>
 * Polaris Ribbon Server 实现类
 */
public class RegistryInterceptor extends BaseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryInterceptor.class);

    public RegistryInterceptor() {

    }

    @Override
    public void onBefore(Object target, Object[] args) {
        if (!Holder.isAllowDiscovery()) {
            return;
        }

        LOGGER.debug("[PolarisAgent] replace ServiceRegistry to ProxyServiceRegistry, target : {}", target);
        ServiceRegistry<Registration> registry;
        LosslessProxyServiceRegistry losslessProxyServiceRegistry;

        String clsName = target.getClass().getCanonicalName();

        if (clsName.contains("org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration")) {
            registry = (ServiceRegistry<Registration>) ReflectionUtils.getObjectByFieldName(target, "serviceRegistry");
            losslessProxyServiceRegistry = new LosslessProxyServiceRegistry(registry);
            ReflectionUtils.setValueByFieldName(target, "serviceRegistry", losslessProxyServiceRegistry);
        } else {
            registry = (ServiceRegistry<Registration>) ReflectionUtils.getSuperObjectByFieldName(target, "serviceRegistry");
            losslessProxyServiceRegistry = new LosslessProxyServiceRegistry(registry);
            ReflectionUtils.setSuperValueByFieldName(target, "serviceRegistry", losslessProxyServiceRegistry);
        }
        LOGGER.debug("[PolarisAgent] finished replace ServiceRegistry to ProxyServiceRegistry");
    }
}
