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

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.cloud.plugin.lossless.SpringCloudLosslessActionProvider;
import com.tencent.cloud.plugin.lossless.transfomer.DiscoveryNamespaceGetter;
import com.tencent.cloud.plugin.lossless.transfomer.NacosDiscoveryNamespaceGetter;
import com.tencent.cloud.plugin.lossless.transfomer.PolarisDiscoveryNamespaceGetter;
import com.tencent.cloud.polaris.PolarisDiscoveryProperties;
import com.tencent.polaris.api.pojo.BaseInstance;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * Lossless proxy for {@link ServiceRegistry}.
 *
 * @author Shedfree Wu
 */
public class LosslessProxyServiceRegistry implements ServiceRegistry<Registration> {

	private final ServiceRegistry<Registration> target;

	public LosslessProxyServiceRegistry(ServiceRegistry<Registration> target) {
		this.target = target;;
	}

	@Override
	public void register(Registration registration) {
		Runnable originalRegisterAction = () -> target.register(registration);
		SpringCloudLosslessActionProvider losslessActionProvider = new SpringCloudLosslessActionProvider(
			target, registration, Holder.getLosslessProperties(), originalRegisterAction);

		DiscoveryNamespaceGetter discoveryNamespaceGetter = null;
		switch (registration.getClass().getName()) {
			case "com.alibaba.cloud.nacos.registry.NacosRegistration":
				NacosDiscoveryProperties nacosDiscoveryProperties =
					ApplicationContextAwareUtils.getApplicationContext().getBean(NacosDiscoveryProperties.class);

				discoveryNamespaceGetter = new NacosDiscoveryNamespaceGetter(nacosDiscoveryProperties);
				break;
			case "com.tencent.cloud.polaris.registry.PolarisRegistration":
				PolarisDiscoveryProperties polarisDiscoveryProperties =
					ApplicationContextAwareUtils.getApplicationContext().getBean(PolarisDiscoveryProperties.class);
				discoveryNamespaceGetter = new PolarisDiscoveryNamespaceGetter(polarisDiscoveryProperties);
				break;
		}
		BaseInstance baseInstance = SpringCloudLosslessActionProvider.getBaseInstance(
			registration, discoveryNamespaceGetter);
		Holder.getContextManager().getLosslessAPI().setLosslessActionProvider(
			baseInstance, losslessActionProvider);
		// replace by lossless register
		Holder.getContextManager().getLosslessAPI().losslessRegister(baseInstance);
	}

	@Override
	public void deregister(Registration registration) {
		target.deregister(registration);
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
