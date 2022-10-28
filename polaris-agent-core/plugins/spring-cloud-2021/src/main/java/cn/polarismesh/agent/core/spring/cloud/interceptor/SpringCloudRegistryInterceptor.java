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


package cn.polarismesh.agent.core.spring.cloud.interceptor;


import java.util.Map;

import cn.polarismesh.agent.core.spring.cloud.util.NacosUtils;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import cn.polarismesh.common.polaris.PolarisReflectConst;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * Polaris Ribbon Server 实现类
 */
public class SpringCloudRegistryInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudRegistryInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (null != throwable) {
            LOGGER.warn("skip to register instance to polaris for the previous registration failed");
            return;
        }
        LOGGER.info("[PolarisAgent] registering from Polaris Server now...");
        Registration registration = (Registration) args[0];
        String canonicalName = registration.getClass().getCanonicalName();
        InstanceRegisterRequest instanceObject;
        if ("com.alibaba.cloud.nacos.registry.NacosRegistration".equals(canonicalName)) {
            instanceObject = parseNacosRegistrationToInstance(registration);
        } else {
            instanceObject = parseRegistrationToInstance(registration);
        }

        PolarisSingleton.getPolarisOperator().registerInstance(instanceObject);
        LOGGER.info("[PolarisAgent] registration finished.");
    }

    private static InstanceRegisterRequest parseNacosRegistrationToInstance(Registration registration) {
        InstanceRegisterRequest instanceObject = parseRegistrationToInstance(registration);
        String groupName = NacosUtils.resolveGroupName(registration);
        String serviceName;
        if (StringUtils.isBlank(groupName) || StringUtils.equals(NacosUtils.DEFAULT_GROUP, groupName)) {
            serviceName = registration.getServiceId();
        } else {
            serviceName = groupName + "__" + registration.getServiceId();
        }
        instanceObject.setService(serviceName);
        instanceObject.setWeight(NacosUtils.resolveWeight(registration));
        return instanceObject;
    }

    private static InstanceRegisterRequest parseRegistrationToInstance(Registration registration) {
        InstanceRegisterRequest instanceObject = new InstanceRegisterRequest();
        instanceObject.setService(registration.getServiceId());
        instanceObject.setHost(registration.getHost());
        instanceObject.setPort(registration.getPort());
        String protocol = "http";
        String scheme = registration.getScheme();
        if (null != scheme) {
            protocol = scheme;
        } else if (registration.isSecure()) {
            protocol = "https";
        }
        instanceObject.setProtocol(protocol);
        String version = "";
        Map<String, String> metadata = registration.getMetadata();
        if (!MapUtils.isEmpty(metadata)) {
            version = metadata.get("version");
        }
        instanceObject.setVersion(version);
        instanceObject.setWeight(PolarisReflectConst.POLARIS_DEFAULT_WEIGHT);
        return instanceObject;
    }
}
