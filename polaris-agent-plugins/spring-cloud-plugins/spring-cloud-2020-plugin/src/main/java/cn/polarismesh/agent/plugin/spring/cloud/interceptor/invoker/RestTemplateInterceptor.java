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
package cn.polarismesh.agent.plugin.spring.cloud.interceptor.invoker;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.metadata.core.EncodeTransferMedataRestTemplateInterceptor;
import com.tencent.cloud.polaris.router.RouterRuleLabelResolver;
import com.tencent.cloud.polaris.router.resttemplate.RouterLabelRestTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RestTemplateInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateInterceptor.class);

	@Override
	public void onBefore(Object target, Object[] args) {

	}

	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
		if (!Holder.getRouterProperties().isEnabled()) {
			LOGGER.info("[PolarisAgent] {} disable build RestTemplate traffic route ability", target.getClass()
					.getCanonicalName());
			return;
		}

		List<ClientHttpRequestInterceptor> ret = (List<ClientHttpRequestInterceptor>) result;

		LOGGER.info("[PolarisAgent] {} begin build RestTemplate invoke traffic route ability", target.getClass()
				.getCanonicalName());

		boolean find = false;
		for (ClientHttpRequestInterceptor interceptor : ret) {
			if (Objects.equals(interceptor.getClass()
					.getCanonicalName(), EncodeTransferMedataRestTemplateInterceptor.class.getCanonicalName())) {
				find = true;
				break;
			}
		}

		if (find) {
			LOGGER.debug("[PolarisAgent] {} ignore to build RestTemplate invoke traffic route ability", target.getClass()
					.getCanonicalName());
			return;
		}
		List<ClientHttpRequestInterceptor> tmp = new ArrayList<>();
		tmp.add(new EncodeTransferMedataRestTemplateInterceptor());
		tmp.add(new RouterLabelRestTemplateInterceptor(
				Collections.emptyList(),
				Holder.getStaticMetadataManager(),
				new RouterRuleLabelResolver(Holder.newServiceRuleManager()),
				Holder.getPolarisContextProperties()
		));
		tmp.addAll(ret);
		ret.clear();
		ret.addAll(tmp);
		LOGGER.info("[PolarisAgent] {} add RestTemplate to build traffic route ability", target.getClass()
				.getCanonicalName());
	}
}
