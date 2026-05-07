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

package cn.polarismesh.agent.examples.dubbo.consumer;

import cn.polarismesh.agent.examples.dubbo.api.EchoService;
import cn.polarismesh.agent.examples.dubbo.api.GreetingService;
import org.apache.dubbo.common.config.ConfigurationUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Component;

/**
 * Dubbo 服务消费者，封装对远程服务的调用
 */
@Component("greetingServiceConsumer")
public class GreetingServiceConsumer {

    @DubboReference
    private GreetingService greetingService;

    @DubboReference(version = "1.0.0", providedBy = "dubbo-agent-example-provider")
    private EchoService echoService;

    /**
     * 调用 sayHello，支持标签路由
     *
     * @param name 名字
     * @return 问候语
     */
    public String doSayHello(String name) {
        String tagValue = ConfigurationUtils.getProperty(CommonConstants.TAG_KEY);
        if (tagValue != null) {
            RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, tagValue);
        }
        return greetingService.sayHello(name);
    }

    /**
     * 调用 sayHi，支持标签路由
     *
     * @param name 名字
     * @return 问候语
     */
    public String doSayHi(String name) {
        String tagValue = ConfigurationUtils.getProperty(CommonConstants.TAG_KEY);
        if (tagValue != null) {
            RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, tagValue);
        }
        return greetingService.sayHi(name);
    }

    /**
     * 调用 echo，支持标签路由
     *
     * @param value 输入值
     * @return 回显结果
     */
    public String doEcho(String value) {
        String tagValue = ConfigurationUtils.getProperty(CommonConstants.TAG_KEY);
        if (tagValue != null) {
            RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, tagValue);
        }
        return echoService.echo(value);
    }
}
