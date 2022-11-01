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

package cn.polarismesh.agent.core.spring.cloud.invoker;

import java.util.List;

import cn.polarismesh.agent.core.spring.cloud.filter.ScServletWebFilterInterceptor;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.tencent.cloud.metadata.core.EncodeTransferMedataRestTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScRestTemplateInterceptor implements AbstractInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScServletWebFilterInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		LOGGER.info("[PolarisAgent] add RestTemplate to build traffic route ability");
		List<ClientHttpRequestInterceptor> ret = (List<ClientHttpRequestInterceptor>) result;
		ret.add(0, new ProxyClientHttpRequestInterceptor());
	}

	public static class ProxyClientHttpRequestInterceptor extends EncodeTransferMedataRestTemplateInterceptor {
	}
}
