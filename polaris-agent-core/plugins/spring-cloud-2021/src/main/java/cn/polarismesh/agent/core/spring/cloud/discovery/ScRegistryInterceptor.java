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

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.util.DiscoveryUtils;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
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

	private static final String NACOS_REGISTRATION = "com.alibaba.cloud.nacos.registry.NacosRegistration";

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

		public ProxyServiceRegistry(ServiceRegistry<Registration> target) {
			this.target = target;
		}

		@Override
		public void register(Registration registration) {
			boolean regisToPolaris = SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_REGISTER_ENABLE, true);
			if (SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_MULTI_REGISTER_ENABLE, true)) {
				regisToPolaris = true;
				target.register(registration);
			}
			else {
				LOGGER.debug("[PolarisAgent] ignore do register to {} action.", target.getClass().getCanonicalName());
			}

			if (regisToPolaris) {
				LOGGER.info("[PolarisAgent] begin do register to polaris action.");
				String canonicalName = registration.getClass().getCanonicalName();
				InstanceRegisterRequest instanceObject;
				if (NACOS_REGISTRATION.equals(canonicalName)) {
					instanceObject = DiscoveryUtils.parseNacosRegistrationToInstance(registration);
				}
				else {
					instanceObject = DiscoveryUtils.parseRegistrationToInstance(registration);
				}

				PolarisSingleton.getPolarisOperator().registerInstance(instanceObject);
			}
		}

		@Override
		public void deregister(Registration registration) {
			boolean deregisterFromPolaris = SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_REGISTER_ENABLE, true);
			if (SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_MULTI_REGISTER_ENABLE, true)) {
				deregisterFromPolaris = true;
				target.deregister(registration);
			}
			else {
				LOGGER.debug("[PolarisAgent] ignore do deregister to {} action.", target.getClass().getCanonicalName());
			}

			if (deregisterFromPolaris) {
				LOGGER.debug("[PolarisAgent] begin de deregister from polaris action.");
				String canonicalName = registration.getClass().getCanonicalName();
				InstanceDeregisterRequest instanceObject;
				if (NACOS_REGISTRATION.equals(canonicalName)) {
					instanceObject = DiscoveryUtils.parseNacosDeRegistrationToInstance(registration);
				}
				else {
					instanceObject = DiscoveryUtils.parseDeRegistrationToInstance(registration);
				}
				PolarisSingleton.getPolarisOperator().deregister(instanceObject);
			}
		}

		@Override
		public void close() {
			target.close();
		}

		@Override
		public void setStatus(Registration registration, String status) {
			target.setStatus(registration, status);
		}

		@Override
		public <T> T getStatus(Registration registration) {
			return target.getStatus(registration);
		}
	}

}
