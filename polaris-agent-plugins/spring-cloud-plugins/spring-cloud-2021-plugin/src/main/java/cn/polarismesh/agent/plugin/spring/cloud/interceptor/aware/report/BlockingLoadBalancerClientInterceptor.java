/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware.report;

import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.common.metadata.MetadataContextHolder;

import org.springframework.cloud.client.ServiceInstance;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class BlockingLoadBalancerClientInterceptor extends BaseInterceptor {

	@Override
	public void onBefore(Object target, Object[] args) {
		Object server = args[0];
		if (server instanceof ServiceInstance) {
			ServiceInstance instance = (ServiceInstance) server;
			MetadataContextHolder.get().setLoadbalancer("host", instance.getHost());
			MetadataContextHolder.get().setLoadbalancer("port", String.valueOf(instance.getPort()));
		}
	}

}
