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

package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.common.polaris.PolarisBlockException;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisFilter.class);

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String service = invoker.getInterface().getName();
        String method = invocation.getMethodName();
        boolean result = true;
        try {
            result = PolarisSingleton.getPolarisOperator().getQuota(service, method, invocation.getAttachments(), 1);
        } catch (RuntimeException e) {
            LOGGER.error("[POLARIS] get quota fail, {}", e.getMessage());
        }
        if (!result) {
            // 请求被限流，则抛出异常
            String namespace = PolarisSingleton.getPolarisConfig().getNamespace();
            throw new PolarisBlockException("rate limit", namespace, service, method, invocation.getAttachments());
        }
        return invoker.invoke(invocation);
    }
}
