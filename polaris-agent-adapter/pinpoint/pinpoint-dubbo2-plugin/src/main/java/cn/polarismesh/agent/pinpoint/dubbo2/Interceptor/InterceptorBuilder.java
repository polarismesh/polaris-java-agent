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

package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;


import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboConfigCenterInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboCreateURLInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboDiscoveryInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboLoadBalanceInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboMetadataCenterInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboRateLimitInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboRegistryInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboReportInvokeInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboRouterInterceptor;
import cn.polarismesh.pinpoint.common.InterceptorFactory;

public class InterceptorBuilder {

    static void buildInterceptors() {
        DubboConfigCenterInterceptor dubboConfigCenterInterceptor = new DubboConfigCenterInterceptor();
        DubboMetadataCenterInterceptor dubboMetadataCenterInterceptor = new DubboMetadataCenterInterceptor();
        DubboRegistryInterceptor dubboRegistryInterceptor = new DubboRegistryInterceptor();
        DubboDiscoveryInterceptor dubboDiscoveryInterceptor = new DubboDiscoveryInterceptor();
        DubboRateLimitInterceptor dubboRateLimitInterceptor = new DubboRateLimitInterceptor();
        DubboCreateURLInterceptor dubboCreateURLInterceptor = new DubboCreateURLInterceptor();
        DubboReportInvokeInterceptor dubboReportInvokeInterceptor = new DubboReportInvokeInterceptor();
        DubboRouterInterceptor dubboRouterInterceptor = new DubboRouterInterceptor();
        DubboLoadBalanceInterceptor dubboLoadBalanceInterceptor = new DubboLoadBalanceInterceptor();

        InterceptorFactory.addInterceptor(DubboConfigInterceptor.class, dubboConfigCenterInterceptor);
        InterceptorFactory.addInterceptor(DubboMetadataInterceptor.class, dubboMetadataCenterInterceptor);
        InterceptorFactory.addInterceptor(DubboRegistryFactoryInterceptor.class, dubboRegistryInterceptor);
        InterceptorFactory.addInterceptor(DubboRegistryDirectoryInterceptor.class, dubboDiscoveryInterceptor);
        InterceptorFactory.addInterceptor(DubboExporterInterceptor.class, dubboRateLimitInterceptor);
        InterceptorFactory.addInterceptor(DubboUrlInterceptor.class, dubboCreateURLInterceptor);
        InterceptorFactory.addInterceptor(DubboInvokeInterceptor.class, dubboReportInvokeInterceptor);
        InterceptorFactory.addInterceptor(DubboAbstractDirectoryInterceptor.class, dubboRouterInterceptor);
        InterceptorFactory.addInterceptor(DubboExtensionLoaderInterceptor.class, dubboLoadBalanceInterceptor);
    }
}
