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

import cn.polarismesh.agent.core.spring.cloud.interceptor.SpringCloudDeRegistryInterceptor;
import cn.polarismesh.agent.core.spring.cloud.interceptor.SpringCloudDiscoveryInterceptor;
import cn.polarismesh.agent.core.spring.cloud.interceptor.SpringCloudRegistryInterceptor;
import cn.polarismesh.pinpoint.common.InterceptorFactory;

public class InterceptorBuilder {

    static void buildInterceptors() {
        // nacos、eureka、consul 可以走共同的逻辑
        InterceptorFactory.addInterceptor(DiscoveryInterceptor.class, new SpringCloudDiscoveryInterceptor());

        // 注册、反注册
        InterceptorFactory.addInterceptor(RegistryInterceptor.class, new SpringCloudRegistryInterceptor());
        InterceptorFactory.addInterceptor(DeRegistryInterceptor.class, new SpringCloudDeRegistryInterceptor());
    }

}
