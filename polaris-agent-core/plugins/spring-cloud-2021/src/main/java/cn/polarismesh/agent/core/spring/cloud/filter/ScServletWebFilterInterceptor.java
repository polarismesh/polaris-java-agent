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

import java.util.ArrayList;
import java.util.List;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.Holder;
import cn.polarismesh.agent.core.spring.cloud.filter.ratelimit.ScLimitHandlerAdapter;
import cn.polarismesh.agent.core.spring.cloud.filter.router.ScRouterHandlerAdapter;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.HandlerAdapter;

import static com.tencent.cloud.polaris.util.OkHttpUtil.LOGGER;

/**
 * hack {@link org.springframework.web.servlet.DispatcherServlet#initStrategies(ApplicationContext)}
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScServletWebFilterInterceptor extends BaseInterceptor {

	@Override
	public void before(Object target, Object[] args) {
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		List<HandlerAdapter> adapters = (List<HandlerAdapter>) ReflectionUtils.getObjectByFieldName(target, "handlerAdapters");

		List<HandlerAdapter> newAdapters = new ArrayList<>();

		adapters.forEach(handlerAdapter -> {
			boolean enableRouter = Holder.getRouterProperties().isEnabled();
			if (enableRouter) {
				LOGGER.info("[PolarisAgent] {} enable add ServletFilter to build transfer metadata ability", target.getClass().getCanonicalName());
				handlerAdapter = new ScRouterHandlerAdapter(handlerAdapter);
			}

			boolean enableRateLimit = Holder.getRateLimitProperties().isEnabled();
			if (enableRateLimit) {
				LOGGER.info("[PolarisAgent] {} enable add ServletFilter to build RateLimit ability", target.getClass().getCanonicalName());
				handlerAdapter = new ScLimitHandlerAdapter(handlerAdapter);
			}

			newAdapters.add(handlerAdapter);
		});

		ReflectionUtils.setValueByFieldName(target, "handlerAdapters", newAdapters);
	}


}
