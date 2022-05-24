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

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisRouter;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory;

/**
 * interceptor for com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory#AbstractDirectory(com.alibaba.dubbo.common.URL,
 * com.alibaba.dubbo.common.URL, java.util.List)
 */
public class DubboRouterInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        URL url = (URL) args[0];
        PolarisRouter polarisRouter = new PolarisRouter(url);
        AbstractDirectory<?> directory = (AbstractDirectory<?>) target;
        directory.getRouters().add(polarisRouter);
    }
}
