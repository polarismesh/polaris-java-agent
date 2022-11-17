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

package cn.polarismesh.agent.plugin.spring.cloud.common;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ClassNames {


	/**
	 * {@link org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration#start()}
	 */
	public static final String SERVICE_REGISTRATION = "org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration";

	/**
	 * {@link org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient#CompositeDiscoveryClient(List)}
	 */
	public static final String DISCOVERY_CLIENT = "org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient";

	/**
	 * {@link org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient#ReactiveCompositeDiscoveryClient(List)}
	 */
	public static final String REACTIVE_DISCOVERY_CLIENT = "org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient";

	/**
	 * {@link org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder#withDiscoveryClient()}
	 * {@link org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder#withBlockingDiscoveryClient()}
	 * {@link org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder#withCaching()}
	 */
	public static final String ROUTER = "org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder";

	/**
	 * {@link org.springframework.web.server.handler.FilteringWebHandler#FilteringWebHandler(org.springframework.web.server.WebHandler, List)}
	 */
	public static final String REACTIVE_WEB_FILTER = "org.springframework.web.server.handler.FilteringWebHandler";

	/**
	 * {@link org.springframework.web.servlet.DispatcherServlet#initStrategies(ApplicationContext)}
	 */
	public static final String SERVLET_WEB_FILTER = "org.springframework.web.servlet.DispatcherServlet";

	/**
	 * {@link org.springframework.http.client.support.InterceptingHttpAccessor#getInterceptors()}
	 */
	public static final String REST_TEMPLATE = "org.springframework.http.client.support.InterceptingHttpAccessor";

	/**
	 * {@link org.springframework.cloud.openfeign.FeignClientFactoryBean#getInheritedAwareInstances(FeignContext, Class)}
	 */
	public static final String FEIGN_TEMPLATE = "org.springframework.cloud.openfeign.FeignClientFactoryBean";

	/**
	 * {@link org.springframework.context.support.ApplicationContextAwareProcessor#ApplicationContextAwareProcessor(ConfigurableApplicationContext)}
	 */
	public static final String APPLICATION_CONTEXT_AWARE = "org.springframework.context.support.ApplicationContextAwareProcessor";

	/**
	 * {@link org.springframework.boot.env.EnvironmentPostProcessorApplicationListener#onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent)}
	 */
	public static final String ENVIRONMENT_POST_PROCESSOR = "org.springframework.boot.env.EnvironmentPostProcessorApplicationListener";

	/**
	 * {@link org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient}
	 */
	public static final String BLOCKING_LOADBALANCER_CLIENT = "org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient";
}
