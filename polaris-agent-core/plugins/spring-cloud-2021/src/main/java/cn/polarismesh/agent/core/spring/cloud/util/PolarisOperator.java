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

package cn.polarismesh.agent.core.spring.cloud.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.client.api.SDKContext;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.api.RouterAPIFactory;
import com.tencent.polaris.ratelimit.api.core.LimitAPI;
import com.tencent.polaris.ratelimit.factory.LimitAPIFactory;
import com.tencent.polaris.router.api.core.RouterAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisOperator.class);

    private final Object lock = new Object();

    private final AtomicBoolean inited = new AtomicBoolean(false);

    private SDKContext sdkContext;

    private ConsumerAPI consumerAPI;

    private ProviderAPI providerAPI;

    private LimitAPI limitAPI;

    private RouterAPI routerAPI;

    private Configuration configuration;

    public PolarisOperator(Configuration configuration) {
        this.configuration = configuration;
    }

    private void init() {
        if (inited.get()) {
            return;
        }
        synchronized (lock) {
            if (inited.get()) {
                return;
            }
            sdkContext = SDKContext.initContextByConfig(configuration);
            consumerAPI = DiscoveryAPIFactory.createConsumerAPIByContext(sdkContext);
            providerAPI = DiscoveryAPIFactory.createProviderAPIByContext(sdkContext);
            limitAPI = LimitAPIFactory.createLimitAPIByContext(sdkContext);
            routerAPI = RouterAPIFactory.createRouterAPIByContext(sdkContext);
            inited.set(true);
        }
    }

    public SDKContext getSdkContext() {
        init();
        return sdkContext;
    }

    public ConsumerAPI getConsumerAPI() {
        init();
        return consumerAPI;
    }

    public ProviderAPI getProviderAPI() {
        init();
        return providerAPI;
    }

    public LimitAPI getLimitAPI() {
        init();
        return limitAPI;
    }

    public RouterAPI getRouterAPI() {
        init();
        return routerAPI;
    }

    public void destroy() {
        synchronized (lock) {
            if (!inited.get()) {
                return;
            }
            sdkContext.close();
            inited.set(false);
        }
    }

}