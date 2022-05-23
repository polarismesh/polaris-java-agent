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

import cn.polarismesh.agent.plugin.dubbo2.polaris.PolarisSingleton;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * interceptor for org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker#invoke(org.apache.dubbo.rpc.Invocation)
 */
public class DubboReportInvokeInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboReportInvokeInterceptor.class);

    private ThreadLocal<Long> startTimeMilli = new ThreadLocal<>();

    @Override
    public void before(Object target, Object[] args) {
        this.startTimeMilli.set(System.currentTimeMillis());
    }

    /**
     * 在AbstractClusterInvoker的invoke()方法之后进行拦截，统计本次服务调用时延数据，上报polaris
     * public Result invoke(final Invocation invocation) throws RpcException
     *
     * @param target    拦截方法所属的对象
     * @param args      拦截方法的入参  args[0] : Invocation对象
     * @param result    拦截方法的返回值 Result对象
     * @param throwable 拦截方法抛出的异常，无异常则为null
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        long delay = System.currentTimeMillis() - this.startTimeMilli.get();
        Invocation invocation = (Invocation) args[0];
        URL url = invocation.getInvoker().getUrl();
        if (result instanceof AsyncRpcResult) {
            // 异步
            CompletableFuture<AppResponse> future = ((AsyncRpcResult) result).getResponseFuture();
            future.whenComplete((rpcResult, exception) -> {
                boolean isSuccess = throwable == null && null == exception && null != rpcResult && !rpcResult.hasException();
                this.report(url, invocation, delay, isSuccess);
            });
        } else {
            // 同步
            Result rpcResult = (Result) result;
            boolean isSuccess = null == throwable && null != rpcResult && !rpcResult.hasException();
            this.report(url, invocation, delay, isSuccess);
        }
    }

    private void report(URL url, Invocation invocation, long delay, boolean isSuccess) {
        PolarisSingleton.getPolarisWatcher()
                .reportInvokeResult(url.getServiceInterface(), invocation.getMethodName(), url.getHost(), url.getPort(),
                        delay, isSuccess, isSuccess ? 0 : -1);
    }
}
