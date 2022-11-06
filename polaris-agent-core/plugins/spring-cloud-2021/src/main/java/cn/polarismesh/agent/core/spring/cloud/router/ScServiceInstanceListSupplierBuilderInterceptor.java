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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.common.tools.SystemPropertyUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import cn.polarismesh.agent.core.spring.cloud.Holder;
import cn.polarismesh.agent.core.spring.cloud.util.PolarisSingleton;
import com.tencent.cloud.polaris.loadbalancer.PolarisServiceInstanceListSupplier;
import com.tencent.cloud.polaris.router.PolarisRouterServiceInstanceListSupplier;
import com.tencent.cloud.polaris.router.config.properties.PolarisMetadataRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisNearByRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisRuleBasedRouterProperties;
import com.tencent.cloud.polaris.router.interceptor.MetadataRouterRequestInterceptor;
import com.tencent.cloud.polaris.router.interceptor.NearbyRouterRequestInterceptor;
import com.tencent.cloud.polaris.router.interceptor.RuleBasedRouterRequestInterceptor;
import com.tencent.cloud.polaris.router.spi.RouterRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link ServiceInstanceListSupplierBuilder} 拦截 Spring Cloud 中 LoadBalancer 中的实例选择
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScServiceInstanceListSupplierBuilderInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScServiceInstanceListSupplierBuilderInterceptor.class);

	public static class ScServiceInstanceListSupplierBuilderBlockingInterceptor extends BaseInterceptor {

		@Override
		public void before(Object target, Object[] args) {

		}

		@Override
		public void after(Object target, Object[] args, Object result, Throwable throwable) {
			if (!Holder.getRouterProperties().isEnabled()) {
				return;
			}
			LOGGER.info("[PolarisAgent] build loadbalancer for BlockingClient ability");
			ServiceInstanceListSupplierBuilder.Creator creator =
					(ServiceInstanceListSupplierBuilder.Creator) ReflectionUtils.getObjectByFieldName(target, "baseCreator");

			ReflectionUtils.setValueByFieldName(target, "baseCreator", new ProxyCreator(creator));
		}
	}

	public static class ScServiceInstanceListSupplierBuilderReactiveInterceptor extends BaseInterceptor {

		@Override
		public void before(Object target, Object[] args) {

		}

		@Override
		public void after(Object target, Object[] args, Object result, Throwable throwable) {
			if (!Holder.getRouterProperties().isEnabled()) {
				return;
			}
			LOGGER.info("[PolarisAgent] build loadbalancer for ReactiveClient ability");
			ServiceInstanceListSupplierBuilder.Creator creator =
					(ServiceInstanceListSupplierBuilder.Creator) ReflectionUtils.getObjectByFieldName(target, "baseCreator");

			ReflectionUtils.setValueByFieldName(target, "baseCreator", new ProxyCreator(creator));
		}
	}

	public static class ScServiceInstanceListSupplierBuilderDisableCachingInterceptor extends BaseInterceptor {

		@Override
		public void before(Object target, Object[] args) {

		}

		@Override
		public void after(Object target, Object[] args, Object result, Throwable throwable) {
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
					PolarisSingleton.getPolarisOperator().getRouterAPI(), Arrays.asList(
					new RuleBasedRouterRequestInterceptor(Holder.getRouterProperties()),
					new MetadataRouterRequestInterceptor(Holder.getMetadataRouterProperties()),
					new NearbyRouterRequestInterceptor(Holder.getNearByRouterProperties())
			), Collections.emptyList());
		}
	}

}
