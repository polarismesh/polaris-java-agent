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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.router;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.polaris.router.PolarisRouterServiceInstanceListSupplier;
import com.tencent.cloud.polaris.router.interceptor.MetadataRouterRequestInterceptor;
import com.tencent.cloud.polaris.router.interceptor.NearbyRouterRequestInterceptor;
import com.tencent.cloud.polaris.router.interceptor.RuleBasedRouterRequestInterceptor;
import com.tencent.cloud.rpc.enhancement.transformer.PolarisInstanceTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ServiceInstanceListSupplierBuilderInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceListSupplierBuilderInterceptor.class);

	public static class ServiceInstanceListSupplierBuilderBlockingInterceptor extends BaseInterceptor {

		@Override
		public void onBefore(Object target, Object[] args) {

		}

		@Override
		public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
			if (!Holder.getRouterProperties().isEnabled()) {
				return;
			}
			LOGGER.info("[PolarisAgent] build loadbalancer for BlockingClient ability");
			ServiceInstanceListSupplierBuilder.Creator creator =
					(ServiceInstanceListSupplierBuilder.Creator) ReflectionUtils.getObjectByFieldName(target, "baseCreator");

			ReflectionUtils.setValueByFieldName(target, "baseCreator", new ProxyCreator(creator));
		}
	}

	public static class ServiceInstanceListSupplierBuilderReactiveInterceptor extends BaseInterceptor {

		@Override
		public void onBefore(Object target, Object[] args) {

		}

		@Override
		public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
			if (!Holder.getRouterProperties().isEnabled()) {
				return;
			}
			LOGGER.info("[PolarisAgent] build loadbalancer for ReactiveClient ability");
			ServiceInstanceListSupplierBuilder.Creator creator =
					(ServiceInstanceListSupplierBuilder.Creator) ReflectionUtils.getObjectByFieldName(target, "baseCreator");

			ReflectionUtils.setValueByFieldName(target, "baseCreator", new ProxyCreator(creator));
		}
	}

	public static class ServiceInstanceListSupplierBuilderDisableCachingInterceptor extends BaseInterceptor {

		@Override
		public void onBefore(Object target, Object[] args) {

		}

		@Override
		public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
			if (!Holder.getRouterProperties().isEnabled()) {
				return;
			}
			LOGGER.info("[PolarisAgent] disable loadbalancer caching ability");
			ReflectionUtils.setValueByFieldName(target, "cachingCreator", null);
		}
	}

	public static class ProxyCreator implements ServiceInstanceListSupplierBuilder.Creator {

		private final ServiceInstanceListSupplierBuilder.Creator creator;

		public ProxyCreator(ServiceInstanceListSupplierBuilder.Creator creator) {
			this.creator = creator;
		}

		@Override
		public ServiceInstanceListSupplier apply(ConfigurableApplicationContext context) {
			ServiceInstanceListSupplier supplier = creator.apply(context);
			if (supplier instanceof PolarisRouterServiceInstanceListSupplier) {
				return supplier;
			}
			return new PolarisRouterServiceInstanceListSupplier(
					supplier,
					Holder.getContextManager().getRouterAPI(), Arrays.asList(
					new RuleBasedRouterRequestInterceptor(Holder.getRouterProperties()),
					new MetadataRouterRequestInterceptor(Holder.getMetadataRouterProperties()),
					new NearbyRouterRequestInterceptor(Holder.getNearByRouterProperties())
			), Collections.emptyList(), new PolarisInstanceTransformer());
		}
	}

}
