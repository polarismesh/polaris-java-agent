/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.examples.dubbo.provider;

import cn.polarismesh.agent.examples.dubbo.api.EchoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;

/**
 * EchoService 实现
 */
@DubboService(version = "1.0.0")
public class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String message) {
        String host = RpcContext.getContext().getLocalHost();
        String port = String.valueOf(RpcContext.getContext().getLocalPort());
        return "echo: " + message + " (provider host: " + host + ", port: " + port + ")";
    }
}
