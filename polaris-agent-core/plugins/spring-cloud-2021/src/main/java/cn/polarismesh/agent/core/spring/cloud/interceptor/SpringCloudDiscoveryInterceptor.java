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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.spring.cloud.model.PolarisServiceInstance;
import cn.polarismesh.agent.core.spring.cloud.util.NacosUtils;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.polaris.api.pojo.Instance;
import org.apache.commons.lang3.StringUtils;

import org.springframework.cloud.client.ServiceInstance;

/**
 * Spring Cloud 服务发现通用拦截器
 */
public class SpringCloudDiscoveryInterceptor implements AbstractInterceptor {

	public enum DiscoveryType {
		Nacos,
		Consul,
		Eureka,
		Common,
	}

	private final DiscoveryType discoveryType;

	private final Map<String, Object> cacheValues = new ConcurrentHashMap<>();

	public SpringCloudDiscoveryInterceptor(DiscoveryType discoveryType) {
		this.discoveryType = discoveryType;
	}

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		String canonicalName = target.getClass().getCanonicalName();

		List<ServiceInstance> ret = (List<ServiceInstance>) result;

		String serviceId = (String) args[0];

		switch (discoveryType) {
		case Nacos:
			if (Objects.equals(canonicalName, "org.springframework.cloud.client.discovery.DiscoveryClient")) {
				return;
			}
			serviceId = processNacosDiscovery(target, args);
			break;
		default:
			serviceId = (String) args[0];
		}

		List<ServiceInstance> instances = new ArrayList<>();
		Instance[] serviceInstances = PolarisSingleton.getPolarisOperator().getAvailableInstances(serviceId);
		for (Instance instance : serviceInstances) {
			instances.add(new PolarisServiceInstance(instance));
		}

		ret.addAll(instances);
	}

	/**
	 * Spring Cloud Alibaba 服务发现需要特殊处理，需要获取 Group 信息
	 *
	 * @param target
	 * @param args
	 * @return
	 */
	private String processNacosDiscovery(Object target, Object[] args) {
		cacheValues.computeIfAbsent("group", key -> {
			Object serviceDiscovery = ReflectionUtils.getObjectByFieldName(target, "serviceDiscovery");
			Object discoveryProperties = ReflectionUtils.getObjectByFieldName(serviceDiscovery, "discoveryProperties");
			return ReflectionUtils.getObjectByFieldName(discoveryProperties, "group");
		});

		String groupName = (String) cacheValues.get("group");

		String serviceName;
		if (StringUtils.isBlank(groupName) || StringUtils.equals(NacosUtils.DEFAULT_GROUP, groupName)) {
			serviceName = (String) args[0];
		}
		else {
			serviceName = groupName + "__" + (String) args[0];
		}

		cacheValues.put("group", groupName);
		return serviceName;
	}
}
