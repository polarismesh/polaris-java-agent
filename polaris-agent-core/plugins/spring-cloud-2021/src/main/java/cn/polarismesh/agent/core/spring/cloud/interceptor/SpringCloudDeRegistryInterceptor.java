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

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.spring.cloud.util.NacosUtils;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class SpringCloudDeRegistryInterceptor implements AbstractInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudDeRegistryInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		LOGGER.info("[PolarisAgent] de-registering from Polaris Server now...");
		Registration registration = (Registration) ReflectionUtils.invokeMethodByName(target, "getRegistration", null);
		String canonicalName = registration.getClass().getCanonicalName();
		InstanceDeregisterRequest instanceObject;
		if ("com.alibaba.cloud.nacos.registry.NacosRegistration".equals(canonicalName)) {
			instanceObject = parseNacosDeRegistrationToInstance(registration);
		} else {
			instanceObject = parseDeRegistrationToInstance(registration);
		}
		PolarisSingleton.getPolarisOperator().deregister(instanceObject);
		LOGGER.info("[PolarisAgent] de-registration finished.");
	}

	private static InstanceDeregisterRequest parseDeRegistrationToInstance(Registration registration) {
		InstanceDeregisterRequest instanceObject = new InstanceDeregisterRequest();
		instanceObject.setService(registration.getServiceId());
		instanceObject.setHost(registration.getHost());
		instanceObject.setPort(registration.getPort());
		return instanceObject;
	}

	private static InstanceDeregisterRequest parseNacosDeRegistrationToInstance(Registration registration) {
		InstanceDeregisterRequest instanceObject = parseDeRegistrationToInstance(registration);
		String namespace = NacosUtils.resolveNamespace(registration);
		String groupName = NacosUtils.resolveGroupName(registration);
		String serviceName;
		if (StringUtils.isBlank(groupName) || StringUtils.equals(NacosUtils.DEFAULT_GROUP, groupName)) {
			serviceName = registration.getServiceId();
		} else {
			serviceName = groupName + "__" + registration.getServiceId();
		}
		instanceObject.setNamespace(namespace);
		instanceObject.setService(serviceName);
		return instanceObject;
	}
}
