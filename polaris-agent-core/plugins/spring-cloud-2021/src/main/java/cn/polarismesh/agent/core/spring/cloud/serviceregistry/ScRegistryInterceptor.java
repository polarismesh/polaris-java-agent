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


package cn.polarismesh.agent.core.spring.cloud.serviceregistry;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.Holder;
import cn.polarismesh.agent.core.spring.cloud.util.DiscoveryUtils;
import cn.polarismesh.agent.core.spring.cloud.util.PolarisSingleton;
import com.tencent.cloud.polaris.PolarisDiscoveryProperties;
import com.tencent.cloud.polaris.registry.PolarisRegistration;
import com.tencent.cloud.polaris.registry.PolarisServiceRegistry;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
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
public class ScRegistryInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScRegistryInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {
		LOGGER.debug("[PolarisAgent] replace ServiceRegistry to ProxyServiceRegistry, target : {}", target);

		String clsName = target.getClass().getCanonicalName();
		if (clsName.contains("org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration")) {
			ServiceRegistry<Registration> registry = (ServiceRegistry<Registration>) ReflectionUtils.getObjectByFieldName(target, "serviceRegistry");
			ReflectionUtils.setValueByFieldName(target, "serviceRegistry", new ProxyServiceRegistry(registry));
		}
		else {
			ServiceRegistry<Registration> registry = (ServiceRegistry<Registration>) ReflectionUtils.getSuperObjectByFieldName(target, "serviceRegistry");
			ReflectionUtils.setSuperValueByFieldName(target, "serviceRegistry", new ProxyServiceRegistry(registry));
		}

		LOGGER.debug("[PolarisAgent] finished replace ServiceRegistry to ProxyServiceRegistry");
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
	}

	public static class ProxyServiceRegistry implements ServiceRegistry<Registration> {

		private final ServiceRegistry<Registration> target;

		private final PolarisServiceRegistry polarisRegistry;

		public ProxyServiceRegistry(ServiceRegistry<Registration> target) {
			this.target = target;

			this.polarisRegistry = new PolarisServiceRegistry(Holder.getDiscoveryProperties(),
					DiscoveryUtils.buildDiscoveryHandler(), Holder.getStaticMetadataManager());
		}

		@Override
		public void register(Registration registration) {
			LOGGER.info("[PolarisAgent] begin do register to polaris action.");

			PolarisDiscoveryProperties properties = Holder.getDiscoveryProperties();
			properties.setPort(registration.getPort());

			polarisRegistry.register(new PolarisRegistration(Holder.getDiscoveryProperties(),
					Holder.getConsulContextProperties(),
					Holder.getNacosContextProperties(),
					PolarisSingleton.getPolarisOperator().getSdkContext(),
					Holder.getStaticMetadataManager()
					));
		}

		@Override
		public void deregister(Registration registration) {
			LOGGER.info("[PolarisAgent] begin de deregister from polaris action.");

			PolarisDiscoveryProperties properties = Holder.getDiscoveryProperties();
			properties.setPort(registration.getPort());

			polarisRegistry.deregister(new PolarisRegistration(Holder.getDiscoveryProperties(),
					Holder.getConsulContextProperties(),
					Holder.getNacosContextProperties(),
					PolarisSingleton.getPolarisOperator().getSdkContext(),
					Holder.getStaticMetadataManager()
			));
		}

		@Override
		public void close() {
			target.close();
			polarisRegistry.close();
		}

		@Override
		public void setStatus(Registration registration, String status) {
		}

		@Override
		public <T> T getStatus(Registration registration) {
			return null;
		}
	}

}
