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

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.tencent.polaris.api.pojo.Instance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisRouter implements Router {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisRouter.class);


    private final URL url;

    private final int priority;

    public PolarisRouter(URL url) {
        this.url = url;
        LOGGER.info("[POLARIS] init service router, url is {}, parameters are {}", url,
                url.getParameters());
        this.priority = url.getParameter(Constants.PRIORITY_KEY, 0);
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public int compareTo(Router o) {
        return (this.getPriority() < o.getPriority()) ? -1 : ((this.getPriority() == o.getPriority()) ? 0 : 1);
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (null == invokers || invokers.size() == 0) {
            return invokers;
        }
        List<Instance> instances = (List<Instance>) ((List<?>) invokers);
        Map<String, String> srcLabels = url.getParameters();
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if (null != attachments && !attachments.isEmpty()) {
            srcLabels = new HashMap<>(srcLabels);
            srcLabels.putAll(attachments);
        }
        String service = url.getServiceInterface();
        LOGGER.debug("[POLARIS] list service {}, method {}, attachment {}, labels {}, url {}", service,
                invocation.getMethodName(),
                attachments, srcLabels, url);
        List<Instance> resultInstances = PolarisSingleton.getPolarisOperator()
                .route(service, invocation.getMethodName(), srcLabels, instances);
        return (List<Invoker<T>>) ((List<?>) resultInstances);
    }
}
