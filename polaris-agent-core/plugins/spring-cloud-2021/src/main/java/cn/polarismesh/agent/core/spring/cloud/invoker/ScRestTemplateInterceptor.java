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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.Holder;
import cn.polarismesh.agent.core.spring.cloud.filter.router.ScRouterServletWebFilterInterceptor;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.cloud.metadata.core.EncodeTransferMedataRestTemplateInterceptor;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.router.RouterRuleLabelResolver;
import com.tencent.cloud.polaris.router.resttemplate.RouterLabelRestTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScRestTemplateInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScRouterServletWebFilterInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (!SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_ROUTER_ENABLE)) {
			LOGGER.debug("[PolarisAgent] {} disable build RestTemplate traffic route ability", target.getClass()
					.getCanonicalName());
			return;
		}

		List<ClientHttpRequestInterceptor> ret = (List<ClientHttpRequestInterceptor>) result;

		LOGGER.debug("[PolarisAgent] {} begin build RestTemplate invoke traffic route ability", target.getClass()
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
				new RouterRuleLabelResolver(new ServiceRuleManager(PolarisSingleton.getPolarisOperator()
						.getSdkContext())),
				Holder.getPolarisContextProperties()
		));
		tmp.addAll(ret);
		ret.clear();
		ret.addAll(tmp);
		LOGGER.debug("[PolarisAgent] {} add RestTemplate to build traffic route ability", target.getClass()
				.getCanonicalName());
	}

}
