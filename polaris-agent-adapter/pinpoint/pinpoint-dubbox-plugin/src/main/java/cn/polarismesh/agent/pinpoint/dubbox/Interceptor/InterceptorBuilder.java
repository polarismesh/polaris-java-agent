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

package cn.polarismesh.agent.pinpoint.dubbox.Interceptor;

import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboCreateURLInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboDiscoveryInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboLoadBalanceInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRateLimitInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRegistryInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboReportInvokeInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRouterInterceptor;
import cn.polarismesh.pinpoint.common.InterceptorFactory;

public class InterceptorBuilder {

    static void buildInterceptors() {
        DubboCreateURLInterceptor dubboCreateURLInterceptor = new DubboCreateURLInterceptor();
        DubboDiscoveryInterceptor dubboDiscoveryInterceptor = new DubboDiscoveryInterceptor();
        DubboLoadBalanceInterceptor dubboLoadBalanceInterceptor = new DubboLoadBalanceInterceptor();
        DubboRateLimitInterceptor dubboRateLimitInterceptor = new DubboRateLimitInterceptor();
        DubboReportInvokeInterceptor dubboReportInvokeInterceptor = new DubboReportInvokeInterceptor();
        DubboRouterInterceptor dubboRouterInterceptor = new DubboRouterInterceptor();
        DubboRegistryInterceptor dubboRegistryInterceptor = new DubboRegistryInterceptor();

        InterceptorFactory.addInterceptor(DubboRegistryDirectoryInterceptor.class, dubboDiscoveryInterceptor);
        InterceptorFactory.addInterceptor(DubboUrlInterceptor.class, dubboCreateURLInterceptor);
        InterceptorFactory.addInterceptor(DubboExporterInterceptor.class, dubboRateLimitInterceptor);
        InterceptorFactory.addInterceptor(DubboInvokeInterceptor.class, dubboReportInvokeInterceptor);
        InterceptorFactory.addInterceptor(DubboExtensionLoaderInterceptor.class, dubboLoadBalanceInterceptor);
        InterceptorFactory.addInterceptor(DubboAbstractDirectoryInterceptor.class, dubboRouterInterceptor);
        InterceptorFactory.addInterceptor(DubboRegistryFactoryInterceptor.class, dubboRegistryInterceptor);
    }
}
