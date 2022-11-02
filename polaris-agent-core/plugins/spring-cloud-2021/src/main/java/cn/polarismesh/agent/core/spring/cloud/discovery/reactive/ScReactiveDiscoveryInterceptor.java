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


package cn.polarismesh.agent.core.spring.cloud.discovery.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.util.DiscoveryUtils;
import cn.polarismesh.agent.core.spring.cloud.util.NacosUtils;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.cloud.common.pojo.PolarisServiceInstance;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import shade.polaris.com.google.gson.Gson;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.commons.publisher.CloudFlux;

/**
 * Spring Cloud 服务发现通用拦截器
 */
public class ScReactiveDiscoveryInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScReactiveDiscoveryInterceptor.class);

	private static final String NACOS_DISCOVERY_CLIENT_CLASS = "com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClient";

	@Override
	public void before(Object target, Object[] args) {
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		ReflectionUtils.doWithFields(target.getClass(), field -> {
			ReflectionUtils.makeAccessible(field);
			List<ReactiveDiscoveryClient> discoveryClients = (List<ReactiveDiscoveryClient>) ReflectionUtils.getField(field, target);
			List<ReactiveDiscoveryClient> wraps = new ArrayList<>();
			discoveryClients.forEach(discoveryClient -> wraps.add(new ProxyReactiveDiscoveryClient(discoveryClient)));
			ReflectionUtils.setField(field, target, wraps);
		}, field -> StringUtils.equals(field.getName(), "discoveryClients"));
	}

	public static class ProxyReactiveDiscoveryClient implements ReactiveDiscoveryClient {

		private final Map<String, Object> cacheValues = new ConcurrentHashMap<>();

		private final ReactiveDiscoveryClient target;

		private Boolean enableDiscovery = false;

		public ProxyReactiveDiscoveryClient(ReactiveDiscoveryClient target) {
			this.target = target;
			this.enableDiscovery = SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_DISCOVERY_ENABLE, true);
		}

		@Override
		public String description() {
			return target.description();
		}

		@Override
		public Flux<ServiceInstance> getInstances(String serviceId) {
			List<Flux<ServiceInstance>> serviceInstances = new ArrayList<>();

			if (SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_MULTI_REGISTER_ENABLE, true)) {
				this.enableDiscovery = true;
				serviceInstances.add(target.getInstances(serviceId).map(DiscoveryUtils.convertToPolarisServiceInstance()));
			}
			else {
				LOGGER.debug("[PolarisAgent] ignore get instance by reactive from {}", target.getClass()
						.getCanonicalName());
			}

			if (!enableDiscovery) {
				return CloudFlux.firstNonEmpty(serviceInstances).map(DiscoveryUtils.convertToPolarisServiceInstance());
			}

			String namespace = "";

			if (StringUtils.equals(target.getClass().getCanonicalName(), NACOS_DISCOVERY_CLIENT_CLASS)) {
				String[] tmp = processNacosDiscovery(target, serviceId);
				namespace = tmp[0];
				serviceId = tmp[1];
			}

			LOGGER.debug("[PolarisAgent] get instances by reactive from polaris namespace={} service={}", namespace, serviceId);

			List<PolarisServiceInstance> polarisInstances = Arrays.stream(PolarisSingleton.getPolarisOperator()
					.getAvailableInstances(namespace, serviceId)).map(PolarisServiceInstance::new).collect(Collectors.toList());
			if (LOGGER.isDebugEnabled()) {
				Gson gson = new Gson();
				LOGGER.debug("[POLARIS] to getInstances result from polaris {}", gson.toJson(serviceInstances));
			}

			serviceInstances.add(Flux.fromIterable(polarisInstances));
			return CloudFlux.firstNonEmpty(serviceInstances);
		}

		@Override
		public Flux<String> getServices() {
			return target.getServices();
		}

		@Override
		public int getOrder() {
			return target.getOrder();
		}

		/**
		 * Spring Cloud Alibaba 服务发现需要特殊处理，需要获取 Group 信息
		 *
		 * @param target
		 * @param serviceId
		 * @return
		 */
		private String[] processNacosDiscovery(Object target, String serviceId) {
			cacheValues.computeIfAbsent("namespace", key -> {
				Object serviceDiscovery = ReflectionUtils.getObjectByFieldName(target, "serviceDiscovery");
				Object discoveryProperties = ReflectionUtils.getObjectByFieldName(serviceDiscovery, "discoveryProperties");
				return ReflectionUtils.getObjectByFieldName(discoveryProperties, "namespace");
			});
			cacheValues.computeIfAbsent("group", key -> {
				Object serviceDiscovery = ReflectionUtils.getObjectByFieldName(target, "serviceDiscovery");
				Object discoveryProperties = ReflectionUtils.getObjectByFieldName(serviceDiscovery, "discoveryProperties");
				return ReflectionUtils.getObjectByFieldName(discoveryProperties, "group");
			});

			String namespace = (String) cacheValues.get("namespace");
			String groupName = (String) cacheValues.get("group");

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

}
