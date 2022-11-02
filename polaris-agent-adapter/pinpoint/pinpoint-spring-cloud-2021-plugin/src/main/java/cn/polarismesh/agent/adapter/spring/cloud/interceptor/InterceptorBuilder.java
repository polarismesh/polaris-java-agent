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

package cn.polarismesh.agent.adapter.spring.cloud.interceptor;

import cn.polarismesh.agent.adapter.spring.cloud.interceptor.aware.ApplicationContextAwareInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.discovery.DiscoveryInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.discovery.ReactiveDiscoveryInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.discovery.RegistryInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.filter.ReactiveWebFilterInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.filter.ServletWebFilterInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.invoker.FeignInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.invoker.RestTemplateInterceptor;
import cn.polarismesh.agent.adapter.spring.cloud.interceptor.router.ServiceInstanceListSupplierBuilderInterceptor;
import cn.polarismesh.agent.core.spring.cloud.aware.ApplicationContextAwareProcessorInterceptor;
import cn.polarismesh.agent.core.spring.cloud.discovery.ScDiscoveryInterceptor;
import cn.polarismesh.agent.core.spring.cloud.discovery.reactive.ScReactiveDiscoveryInterceptor;
import cn.polarismesh.agent.core.spring.cloud.discovery.ScRegistryInterceptor;
import cn.polarismesh.agent.core.spring.cloud.filter.ScServletWebFilterInterceptor;
import cn.polarismesh.agent.core.spring.cloud.filter.router.ScRouterReactiveWebFilterInterceptor;
import cn.polarismesh.agent.core.spring.cloud.invoker.ScFeignInterceptor;
import cn.polarismesh.agent.core.spring.cloud.invoker.ScRestTemplateInterceptor;
import cn.polarismesh.agent.core.spring.cloud.router.ScServiceInstanceListSupplierBuilderInterceptor;
import cn.polarismesh.pinpoint.common.InterceptorFactory;

public class InterceptorBuilder {

    static void buildInterceptors() {
        // nacos、eureka、consul 可以走共同的逻辑
        InterceptorFactory.addInterceptor(DiscoveryInterceptor.class, new ScDiscoveryInterceptor());
        InterceptorFactory.addInterceptor(ReactiveDiscoveryInterceptor.class, new ScReactiveDiscoveryInterceptor());

        // 注册、反注册
        InterceptorFactory.addInterceptor(RegistryInterceptor.class, new ScRegistryInterceptor());

        // 服务路由 & 服务限流
        InterceptorFactory.addInterceptor(ReactiveWebFilterInterceptor.class, new ScRouterReactiveWebFilterInterceptor());
        InterceptorFactory.addInterceptor(ServletWebFilterInterceptor.class, new ScServletWebFilterInterceptor());

        // 路由能力
        InterceptorFactory.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderBlockingInterceptor.class,
                new ScServiceInstanceListSupplierBuilderInterceptor.ScServiceInstanceListSupplierBuilderBlockingInterceptor());
        InterceptorFactory.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderReactiveInterceptor.class,
                new ScServiceInstanceListSupplierBuilderInterceptor.ScServiceInstanceListSupplierBuilderReactiveInterceptor());
        // 使用路由能力的话，需要禁用 Loadbalancer 的 Cache 能力
        InterceptorFactory.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderDisableCachingInterceptor.class,
                new ScServiceInstanceListSupplierBuilderInterceptor.ScServiceInstanceListSupplierBuilderDisableCachingInterceptor());

        // 请求发起 -- 针对 RestTemplate
        InterceptorFactory.addInterceptor(RestTemplateInterceptor.class, new ScRestTemplateInterceptor());
        InterceptorFactory.addInterceptor(FeignInterceptor.class, new ScFeignInterceptor());

        // agent 中注入 Spring ApplicationContext
        InterceptorFactory.addInterceptor(ApplicationContextAwareInterceptor.class, new ApplicationContextAwareProcessorInterceptor());
    }

}
