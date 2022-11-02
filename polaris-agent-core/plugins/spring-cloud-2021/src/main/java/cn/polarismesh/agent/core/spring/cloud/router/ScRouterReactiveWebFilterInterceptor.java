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


package cn.polarismesh.agent.core.spring.cloud.router;

import java.util.List;

import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import com.tencent.cloud.metadata.core.DecodeTransferMetadataReactiveFilter;
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
public class ScRouterReactiveWebFilterInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScRouterReactiveWebFilterInterceptor.class);

	/**
	 * 针对入参的 List<WebFilter> filters 进行处理，添加自定义收集流量标签的 WebFilter
	 *
	 * @param target
	 * @param args
	 */
	@Override
	public void before(Object target, Object[] args) {
		List<WebFilter> filters = (List<WebFilter>) args[1];
		filters.add(0, new DecodeTransferMetadataReactiveFilter());
		LOGGER.debug("[PolarisAgent] add WebFilter to build transfer metadata ability");
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {

	}
}
