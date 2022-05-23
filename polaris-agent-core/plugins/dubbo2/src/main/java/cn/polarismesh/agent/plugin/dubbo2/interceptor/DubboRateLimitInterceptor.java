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

package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.plugin.dubbo2.polaris.PolarisFilterWrapper;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.rpc.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interceptor for org.apache.dubbo.rpc.protocol.AbstractExporter#AbstractExporter(org.apache.dubbo.rpc.Invoker)
 */
public class DubboRateLimitInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboRateLimitInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 拦截org.apache.dubbo.rpc.protocol.AbstractExporter的构造器
     * 替换其invoker对象为自定义的invoker对象，用于接入限流功能
     */
    @SuppressWarnings("unchecked")
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        LOGGER.debug("[POLARIS] set {}.invoker filter with rate limit", target.getClass());
        Invoker invoker = PolarisFilterWrapper.buildInvokerChain((Invoker) args[0]);
        ReflectionUtils.setSuperValueByFieldName(target, "invoker", invoker);
    }
}