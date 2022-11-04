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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.Holder;
import cn.polarismesh.agent.core.spring.cloud.util.PolarisSingleton;
import com.tencent.cloud.metadata.core.EncodeTransferMedataFeignInterceptor;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.router.RouterRuleLabelResolver;
import com.tencent.cloud.polaris.router.feign.RouterLabelFeignInterceptor;
import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * org.springframework.cloud.openfeign.FeignClientFactoryBean#getInheritedAwareInstances(org.springframework.cloud.openfeign.FeignContext, java.lang.Class)
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScFeignInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScFeignInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
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
					new RouterRuleLabelResolver(new ServiceRuleManager(PolarisSingleton.getPolarisOperator()
							.getSdkContext())),
					Holder.getPolarisContextProperties()

			));
			ret.put(EncodeTransferMedataFeignInterceptor.class.getCanonicalName(), new EncodeTransferMedataFeignInterceptor());
		}
	}

}
