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
import com.tencent.cloud.metadata.core.EncodeTransferMedataFeignInterceptor;
import com.tencent.cloud.polaris.router.RouterRuleLabelResolver;
import com.tencent.cloud.polaris.router.feign.RouterLabelFeignInterceptor;
import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class FeignInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeignInterceptor.class);

	@Override
	public void onBefore(Object target, Object[] args) {

	}

	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
		if (!Holder.getRouterProperties().isEnabled()) {
			LOGGER.info("[PolarisAgent] {} disable build Feign traffic route ability", target.getClass()
					.getCanonicalName());
			return;
		}

		Class<?> cls = (Class<?>) args[1];

		if (Objects.equals(cls.getCanonicalName(), RequestInterceptor.class.getCanonicalName())) {
			LOGGER.info("[PolarisAgent] {} build Feign traffic route ability", target.getClass()
					.getCanonicalName());
			Map<String, RequestInterceptor> ret = (Map<String, RequestInterceptor>) result;
			ret.put(RouterLabelFeignInterceptor.class.getCanonicalName(), new RouterLabelFeignInterceptor(
					Collections.emptyList(),
					Holder.getStaticMetadataManager(),
					new RouterRuleLabelResolver(Holder.newServiceRuleManager()),
					Holder.getPolarisContextProperties()

			));
			ret.put(EncodeTransferMedataFeignInterceptor.class.getCanonicalName(), new EncodeTransferMedataFeignInterceptor());
		}

	}
}
