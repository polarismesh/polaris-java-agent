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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.router;

import java.util.List;

import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.rpc.enhancement.resttemplate.EnhancedRestTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.InterceptingHttpAccessor;

public class InterceptingHttpAccessorInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterceptingHttpAccessorInterceptor.class);

	@Override
	public void onBefore(Object target, Object[] args) {

	}

	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
		List<ClientHttpRequestInterceptor> interceptors = ((InterceptingHttpAccessor)target).getInterceptors();
		if (null == interceptors || interceptors.isEmpty()) {
			return;
		}
		int restTemplateInterceptorIdx = -1;
		for (int i = 0; i < interceptors.size(); i++) {
			if (interceptors.get(i) instanceof EnhancedRestTemplateInterceptor) {
				restTemplateInterceptorIdx = i;
				break;
			}
		}
		if (restTemplateInterceptorIdx == -1) {
			return;
		}
		LOGGER.info("[PolarisAgent] {} begin adjust invoke traffic route ability", target.getClass()
				.getCanonicalName());
		if (restTemplateInterceptorIdx == interceptors.size() - 1) {
			// 已经在末尾
			return;
		}
		ClientHttpRequestInterceptor clientHttpRequestInterceptor = interceptors.get(restTemplateInterceptorIdx);
		for (int i = restTemplateInterceptorIdx; i < interceptors.size() - 1; i++) {
			interceptors.set(i, interceptors.get(i + 1));
		}
		interceptors.set(interceptors.size() - 1, clientHttpRequestInterceptor);
		LOGGER.info("[PolarisAgent] {} add RestTemplate to build traffic route ability", target.getClass()
				.getCanonicalName());
	}
}
