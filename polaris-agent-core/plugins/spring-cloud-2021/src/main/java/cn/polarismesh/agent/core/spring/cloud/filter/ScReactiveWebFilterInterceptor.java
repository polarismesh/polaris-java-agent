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


package cn.polarismesh.agent.core.spring.cloud.filter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.cloud.metadata.core.DecodeTransferMetadataReactiveFilter;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.ratelimit.RateLimitRuleLabelResolver;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitProperties;
import com.tencent.cloud.polaris.ratelimit.filter.QuotaCheckReactiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.FilteringWebHandler;

/**
 * hack {@link FilteringWebHandler#FilteringWebHandler(WebHandler, List)}
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScReactiveWebFilterInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScReactiveWebFilterInterceptor.class);

	private final AtomicReference<RateLimitRuleLabelResolver> reference = new AtomicReference<>();

	/**
	 * 针对入参的 List<WebFilter> filters 进行处理，添加自定义收集流量标签的 WebFilter
	 *
	 * @param target
	 * @param args
	 */
	@Override
	public void before(Object target, Object[] args) {
		boolean enableRateLimit = SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_LIMITER_ENABLE);
		if (!enableRateLimit) {
			LOGGER.info("[PolarisAgent] {} disable add WebFilter to build RateLimit ability", target.getClass().getCanonicalName());
			return;
		}
		LOGGER.info("[PolarisAgent] {} enable add WebFilter to build RateLimit ability", target.getClass().getCanonicalName());

		reference.compareAndSet(null, new RateLimitRuleLabelResolver(new ServiceRuleManager(PolarisSingleton.getPolarisOperator()
				.getSdkContext())));

		List<WebFilter> filters = (List<WebFilter>) args[1];
		filters.add(0, new QuotaCheckReactiveFilter(
				PolarisSingleton.getPolarisOperator().getLimitAPI(),
				null,
				new PolarisRateLimitProperties(),
				reference.get()
		));
		LOGGER.info("[PolarisAgent] {} add WebFilter to build RateLimit ability", target.getClass().getCanonicalName());

		boolean enableRouter = SystemPropertyUtils.getBoolean(AgentConfig.KEY_PLUGIN_SPRINGCLOUD_ROUTER_ENABLE);
		if (!enableRouter) {
			LOGGER.info("[PolarisAgent] {} disable add WebFilter to build transfer metadata ability", target.getClass().getCanonicalName());
			return;
		}
		LOGGER.info("[PolarisAgent] {} enable add WebFilter to build transfer metadata ability", target.getClass().getCanonicalName());

		filters.add(0, new DecodeTransferMetadataReactiveFilter());
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {

	}
}
