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


package cn.polarismesh.agent.core.spring.cloud.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.util.DiscoveryUtils;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.cloud.common.pojo.PolarisServiceInstance;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shade.polaris.com.google.gson.Gson;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Spring Cloud 服务发现通用拦截器
 */
public class ScDiscoveryInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScDiscoveryInterceptor.class);

	private static final String NACOS_DISCOVERY_CLIENT_CLASS = "com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient";

	@Override
	public void before(Object target, Object[] args) {
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		ReflectionUtils.doWithFields(target.getClass(), field -> {
			ReflectionUtils.makeAccessible(field);
			List<? extends DiscoveryClient> discoveryClients = (List<? extends DiscoveryClient>) ReflectionUtils.getField(field, target);
			List<DiscoveryClient> wraps = new ArrayList<>();
			discoveryClients.forEach(discoveryClient -> wraps.add(new ProxyDiscoveryClient(discoveryClient)));
			ReflectionUtils.setField(field, target, wraps);
		}, field -> StringUtils.equals(field.getName(), "discoveryClients"));
	}

	public static class ProxyDiscoveryClient implements DiscoveryClient {

		private final Map<String, Object> cacheValues = new ConcurrentHashMap<>();

		private final DiscoveryClient target;

		private Boolean enableDiscovery = false;

		public ProxyDiscoveryClient(DiscoveryClient target) {
			this.target = target;
			this.enableDiscovery = SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_DISCOVERY_ENABLE, true);
		}

		@Override
		public String description() {
			return target.description();
		}

		@Override
		public List<ServiceInstance> getInstances(String serviceId) {
			List<ServiceInstance> result = new ArrayList<>();
			if (SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_MULTI_REGISTER_ENABLE, true)) {
				this.enableDiscovery = true;
				List<ServiceInstance> tmp = target.getInstances(serviceId);
				result.addAll(tmp);
			}
			else {
				LOGGER.debug("[PolarisAgent] ignore get instance from {}", target.getClass().getCanonicalName());
			}

			if (!enableDiscovery) {
				return result;
			}

			String namespace = "";

			if (StringUtils.equals(target.getClass().getCanonicalName(), NACOS_DISCOVERY_CLIENT_CLASS)) {
				String[] tmp = DiscoveryUtils.processNacosDiscovery(cacheValues, target, serviceId);
				namespace = tmp[0];
				serviceId = tmp[1];
			}

			List<ServiceInstance> instances = new ArrayList<>();

			Instance[] serviceInstances = PolarisSingleton.getPolarisOperator()
					.getAvailableInstances(namespace, serviceId);
			for (Instance instance : serviceInstances) {
				instances.add(new PolarisServiceInstance(instance));
			}
			if (LOGGER.isDebugEnabled()) {
				Gson gson = new Gson();
				LOGGER.debug("[POLARIS] to getInstances result from polaris {}", gson.toJson(serviceInstances));
			}

			Function<ServiceInstance, ServiceInstance> convert = DiscoveryUtils.convertToPolarisServiceInstance();
			result.forEach(serviceInstance -> instances.add(convert.apply(serviceInstance)));

			result.clear();
			result.addAll(instances);

			LOGGER.debug("[PolarisAgent] get instances from polaris namespace={} service={} instances={}", namespace, serviceId, result);

			return result;
		}

		@Override
		public List<String> getServices() {
			return target.getServices();
		}

		@Override
		public int getOrder() {
			return target.getOrder();
		}

	}

}
