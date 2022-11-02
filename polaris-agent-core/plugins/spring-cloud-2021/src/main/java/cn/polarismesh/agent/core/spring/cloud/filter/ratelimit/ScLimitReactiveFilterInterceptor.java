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

package cn.polarismesh.agent.core.spring.cloud.filter.ratelimit;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.ratelimit.RateLimitRuleLabelResolver;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitProperties;
import com.tencent.cloud.polaris.ratelimit.filter.QuotaCheckReactiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.server.WebFilter;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScLimitReactiveFilterInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScLimitReactiveFilterInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {

	}
}
