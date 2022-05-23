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

package cn.polarismesh.agent.plugin.dubbo2.polaris;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.*;

public class PolarisFilterWrapper {
    public static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker) {
        Filter filter = new PolarisFilter();
        return new Invoker<T>() {

            public Class<T> getInterface() {
                return invoker.getInterface();
            }

            public URL getUrl() {
                return invoker.getUrl();
            }

            public boolean isAvailable() {
                return invoker.isAvailable();
            }

            public Result invoke(Invocation invocation) throws RpcException {
                return filter.invoke(invoker, invocation);
            }

            public void destroy() {
                invoker.destroy();
            }

            @Override
            public String toString() {
                return invoker.toString();
            }
        };
    }
}
