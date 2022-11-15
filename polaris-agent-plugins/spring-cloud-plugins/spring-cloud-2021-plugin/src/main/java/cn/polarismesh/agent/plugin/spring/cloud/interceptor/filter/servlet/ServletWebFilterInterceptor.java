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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.filter.servlet;

import java.util.ArrayList;
import java.util.List;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.filter.servlet.ratelimit.RateLimitHandlerAdapter;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.filter.servlet.router.RouterHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.HandlerAdapter;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ServletWebFilterInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServletWebFilterInterceptor.class);

	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
		List<HandlerAdapter> adapters = (List<HandlerAdapter>) ReflectionUtils.getObjectByFieldName(target, "handlerAdapters");

		List<HandlerAdapter> newAdapters = new ArrayList<>();

		adapters.forEach(handlerAdapter -> {
			boolean enableRouter = Holder.getRouterProperties().isEnabled();
			if (enableRouter) {
				LOGGER.info("[PolarisAgent] {} enable add ServletFilter to build transfer metadata ability", target.getClass().getCanonicalName());
				handlerAdapter = new RouterHandlerAdapter(handlerAdapter);
			}

			boolean enableRateLimit = Holder.getRateLimitProperties().isEnabled();
			if (enableRateLimit) {
				LOGGER.info("[PolarisAgent] {} enable add ServletFilter to build RateLimit ability", target.getClass().getCanonicalName());
				handlerAdapter = new RateLimitHandlerAdapter(handlerAdapter);
			}

			newAdapters.add(handlerAdapter);
		});

		ReflectionUtils.setValueByFieldName(target, "handlerAdapters", newAdapters);
	}
}
