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

package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisSingleton;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interceptor for com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker#invoke(com.alibaba.dubbo.rpc.Invocation)
 */
public class DubboReportInvokeInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboRegistryInterceptor.class);

    private final ThreadLocal<Long> startTimeMilli = new ThreadLocal<>();

    @Override
    public void before(Object target, Object[] args) {
        this.startTimeMilli.set(System.currentTimeMillis());
    }

    /**
     * 在AbstractClusterInvoker的invoke()方法之后进行拦截，统计本次服务调用时延数据，上报polaris
     * public Result invoke(final Invocation invocation) throws RpcException
     *
     * @param target 拦截方法所属的对象
     * @param args 拦截方法的入参  args[0] : Invocation对象
     * @param result 拦截方法的返回值 Result对象
     * @param throwable 拦截方法抛出的异常，无异常则为null
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        long delay = System.currentTimeMillis() - this.startTimeMilli.get();
        Invocation invocation = (Invocation) args[0];
        Invoker<?> invoker = invocation.getInvoker();
        if (null == invoker) {
            LOGGER.info("[POLARIS] invoker is null, ignore report result");
            return;
        }
        URL url = invoker.getUrl();
        PolarisSingleton.getPolarisOperator()
                .reportInvokeResult(url.getServiceInterface(), invocation.getMethodName(), url.getHost(), url.getPort(),
                        delay, null == throwable && !((RpcResult) result).hasException(),
                        null != throwable || ((RpcResult) result).hasException() ? -1 : 0);
    }
}
