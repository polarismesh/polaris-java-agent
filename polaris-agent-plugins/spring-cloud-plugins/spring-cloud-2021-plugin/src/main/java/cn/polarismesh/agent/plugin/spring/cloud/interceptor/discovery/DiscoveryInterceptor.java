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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.discovery;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.DiscoveryUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.polaris.api.utils.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Cloud Nacos/Eureka/Consul 服务发现拦截器
 *
 * @author zhuyuhan
 */
public class DiscoveryInterceptor extends BaseInterceptor {

	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
		if (!Holder.isAllowDiscovery()) {
			return;
		}

		PolarisServiceDiscovery discovery = new PolarisServiceDiscovery(Holder.getNacosContextProperties(), Holder.getDiscoveryProperties(),
				DiscoveryUtils.buildDiscoveryHandler());

		ReflectionUtils.doWithFields(target.getClass(), field -> {
			ReflectionUtils.makeAccessible(field);
			List<DiscoveryClient> discoveryClients = (List<DiscoveryClient>) ReflectionUtils.getField(field, target);
			List<DiscoveryClient> wraps = new ArrayList<>();
			discoveryClients.forEach(discoveryClient -> wraps.add(new ProxyDiscoveryClient(discovery)));
			ReflectionUtils.setField(field, target, wraps);
		}, field -> StringUtils.equals(field.getName(), "discoveryClients"));
	}

	private static class ProxyDiscoveryClient implements DiscoveryClient {

		public final String description = "Spring Cloud Tencent Polaris Discovery Client.";

		private final PolarisServiceDiscovery discovery;

		private ProxyDiscoveryClient(PolarisServiceDiscovery discovery) {
			this.discovery = discovery;
		}

		@Override
		public String description() {
			return description;
		}

		@Override
		public List<ServiceInstance> getInstances(String serviceId) {
			return discovery.getInstances(serviceId);
		}

		@Override
		public List<String> getServices() {
			return discovery.getServices();
		}
	}

}

