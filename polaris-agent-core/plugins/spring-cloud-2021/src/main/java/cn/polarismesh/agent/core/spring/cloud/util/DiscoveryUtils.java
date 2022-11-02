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

package cn.polarismesh.agent.core.spring.cloud.util;

import java.util.Map;
import java.util.function.Function;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.common.polaris.PolarisReflectConst;
import com.tencent.cloud.common.pojo.PolarisServiceInstance;
import com.tencent.polaris.api.pojo.DefaultInstance;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.utils.MapUtils;
import com.tencent.polaris.api.utils.StringUtils;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class DiscoveryUtils {

	public static Function<ServiceInstance, ServiceInstance> convertToPolarisServiceInstance() {
		return serviceInstance -> {
			DefaultInstance ins = new DefaultInstance();
			ins.setService(serviceInstance.getServiceId());
			ins.setId(serviceInstance.getInstanceId());
			ins.setHost(serviceInstance.getHost());
			ins.setPort(serviceInstance.getPort());
			ins.setMetadata(serviceInstance.getMetadata());
			if (serviceInstance.isSecure()) {
				ins.setProtocol("https");
			}
			else {
				ins.setProtocol("http");
			}

			// 这里由于 nacos 以及 polaris 获取到的都是处于健康状态的实例，因此这里强制设置为 true
			ins.setIsolated(false);
			ins.setHealthy(true);

			return new PolarisServiceInstance(ins);
		};
	}

	public static InstanceRegisterRequest parseNacosRegistrationToInstance(Registration registration) {
		InstanceRegisterRequest instanceObject = parseRegistrationToInstance(registration);
		String namespace = NacosUtils.resolveNamespace(registration);
		String groupName = NacosUtils.resolveGroupName(registration);
		String serviceName;
		if (StringUtils.isBlank(groupName) || StringUtils.equals(NacosUtils.DEFAULT_GROUP, groupName)) {
			serviceName = registration.getServiceId();
		}
		else {
			serviceName = groupName + "__" + registration.getServiceId();
		}
		instanceObject.setService(serviceName);
		instanceObject.setNamespace(namespace);
		instanceObject.setWeight(NacosUtils.resolveWeight(registration));

		String protocol = "http";
		String scheme = registration.getScheme();
		if (null != scheme) {
			protocol = scheme;
		}
		else if (registration.isSecure()) {
			protocol = "https";
		}
		instanceObject.setProtocol(protocol);
		String version = "";
		Map<String, String> metadata = registration.getMetadata();
		if (!MapUtils.isEmpty(metadata)) {
			version = metadata.get("version");
		}
		instanceObject.setVersion(version);
		instanceObject.setMetadata(registration.getMetadata());
		return instanceObject;
	}

	public static InstanceRegisterRequest parseRegistrationToInstance(Registration registration) {
		InstanceRegisterRequest instanceObject = new InstanceRegisterRequest();
		instanceObject.setService(registration.getServiceId());
		instanceObject.setHost(registration.getHost());
		instanceObject.setPort(registration.getPort());
		String protocol = "http";
		String scheme = registration.getScheme();
		if (null != scheme) {
			protocol = scheme;
		}
		else if (registration.isSecure()) {
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

	public static InstanceDeregisterRequest parseDeRegistrationToInstance(Registration registration) {
		InstanceDeregisterRequest instanceObject = new InstanceDeregisterRequest();
		instanceObject.setService(registration.getServiceId());
		instanceObject.setHost(registration.getHost());
		instanceObject.setPort(registration.getPort());
		return instanceObject;
	}

	public static InstanceDeregisterRequest parseNacosDeRegistrationToInstance(Registration registration) {
		InstanceDeregisterRequest instanceObject = parseDeRegistrationToInstance(registration);
		String namespace = NacosUtils.resolveNamespace(registration);
		String groupName = NacosUtils.resolveGroupName(registration);
		String serviceName;
		if (org.apache.commons.lang3.StringUtils.isBlank(groupName) || org.apache.commons.lang3.StringUtils.equals(NacosUtils.DEFAULT_GROUP, groupName)) {
			serviceName = registration.getServiceId();
		}
		else {
			serviceName = groupName + "__" + registration.getServiceId();
		}
		instanceObject.setNamespace(namespace);
		instanceObject.setService(serviceName);
		return instanceObject;
	}

	/**
	 * Spring Cloud Alibaba 服务发现需要特殊处理，需要获取 Group 信息
	 *
	 * @param target
	 * @param serviceId
	 * @return
	 */
	public static String[] processNacosDiscovery(Map<String, Object> cacheOptions, Object target, String serviceId) {
		cacheOptions.computeIfAbsent("namespace", key -> {
			Object serviceDiscovery = ReflectionUtils.getObjectByFieldName(target, "serviceDiscovery");
			Object discoveryProperties = ReflectionUtils.getObjectByFieldName(serviceDiscovery, "discoveryProperties");
			return ReflectionUtils.getObjectByFieldName(discoveryProperties, "namespace");
		});
		cacheOptions.computeIfAbsent("group", key -> {
			Object serviceDiscovery = ReflectionUtils.getObjectByFieldName(target, "serviceDiscovery");
			Object discoveryProperties = ReflectionUtils.getObjectByFieldName(serviceDiscovery, "discoveryProperties");
			return ReflectionUtils.getObjectByFieldName(discoveryProperties, "group");
		});

		String namespace = (String) cacheOptions.get("namespace");
		String groupName = (String) cacheOptions.get("group");

		String serviceName;
		if (StringUtils.isBlank(groupName) || StringUtils.equals(NacosUtils.DEFAULT_GROUP, groupName)) {
			serviceName = serviceId;
		}
		else {
			serviceName = groupName + "__" + serviceId;
		}

		return new String[] {namespace, serviceName};
	}

}
