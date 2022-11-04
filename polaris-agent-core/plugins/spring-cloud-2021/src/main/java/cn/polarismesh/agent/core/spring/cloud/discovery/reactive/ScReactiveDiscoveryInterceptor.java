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
import java.util.List;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.util.DiscoveryUtils;
import com.tencent.cloud.polaris.discovery.PolarisServiceDiscovery;
import com.tencent.cloud.polaris.discovery.reactive.PolarisReactiveDiscoveryClient;
import com.tencent.polaris.api.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

/**
 * Spring Cloud 服务发现通用拦截器
 */
public class ScReactiveDiscoveryInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScReactiveDiscoveryInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		PolarisServiceDiscovery discovery = new PolarisServiceDiscovery(DiscoveryUtils.buildDiscoveryHandler());

		ReflectionUtils.doWithFields(target.getClass(), field -> {
			ReflectionUtils.makeAccessible(field);
			List<ReactiveDiscoveryClient> discoveryClients = (List<ReactiveDiscoveryClient>) ReflectionUtils.getField(field, target);
			List<ReactiveDiscoveryClient> wraps = new ArrayList<>();
			discoveryClients.forEach(discoveryClient -> wraps.add(new PolarisReactiveDiscoveryClient(discovery)));
			ReflectionUtils.setField(field, target, wraps);
		}, field -> StringUtils.equals(field.getName(), "discoveryClients"));
	}

}
