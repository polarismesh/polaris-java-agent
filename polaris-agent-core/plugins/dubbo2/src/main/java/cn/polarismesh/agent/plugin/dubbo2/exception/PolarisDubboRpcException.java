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

package cn.polarismesh.agent.plugin.dubbo2.exception;

import org.apache.dubbo.rpc.RpcException;

import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class PolarisDubboRpcException extends RpcException {


    private final String namespace;
    private final String service;
    private final String method;
    private final Map<String, String> metadata;

    public PolarisDubboRpcException(String message) {
        this(message, null, null, null, null);
    }

    public PolarisDubboRpcException(String message, String namespace, String service, String method, Map<String, String> metadata) {
        super(message);
        this.namespace = namespace;
        this.service = service;
        this.method = method;
        this.metadata = metadata;
    }

    @Override
    public String getMessage() {
        return super.getMessage() +
                ", namespace: " + namespace +
                ", service: " + service +
                ", method: " + method +
                ", metadata: " + metadata;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

}
