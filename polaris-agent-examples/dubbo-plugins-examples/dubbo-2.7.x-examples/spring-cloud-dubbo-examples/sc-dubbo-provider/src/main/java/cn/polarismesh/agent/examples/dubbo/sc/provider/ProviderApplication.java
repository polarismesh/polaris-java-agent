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

package cn.polarismesh.agent.examples.dubbo.sc.provider;

import static org.apache.dubbo.common.constants.CommonConstants.DUBBO_LABELS;

import cn.polarismesh.agent.examples.dubbo.api.EchoService;
import cn.polarismesh.agent.examples.dubbo.api.GreetingService;
import org.apache.dubbo.common.config.ConfigurationUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Cloud Alibaba 2021 + Dubbo 2.7 Provider 启动类.
 * 同时暴露 REST 端点和 Dubbo 服务.
 */
@SpringBootApplication
@EnableDubbo
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    /**
     * REST 端点，供 Spring Cloud 方式调用.
     */
    @RestController
    public static class EchoController {

        private static final Logger LOG =
                LoggerFactory.getLogger(EchoController.class);

        @Value("${spring.application.name}")
        private String appName;

        @Value("${server.port:0}")
        private int port;

        @GetMapping("/echo/{string}")
        public String echo(@PathVariable String string) {
            String result = String.format(
                    "[REST] Hello %s, from %s:%d",
                    string, appName, port);
            LOG.info("echo called: {}", result);
            return result;
        }
    }

    /**
     * Dubbo GreetingService 实现.
     */
    @DubboService
    public static class GreetingServiceImpl implements GreetingService {

        @Override
        public String sayHello(String name) {
            RpcContext ctx = RpcContext.getContext();
            return "hello, " + name + " (provider host: " + ctx.getLocalHost() + ", port: " + ctx.getLocalPort() + ")";
        }

        @Override
        public String sayHi(String name) {
            String dubboLabel = ConfigurationUtils.getProperty(DUBBO_LABELS);
            RpcContext ctx = RpcContext.getContext();
            return "[provider by polaris] hi, " + name + " (labels: " + dubboLabel + ", provider host: " + ctx.getLocalHost() + ", port: " + ctx.getLocalPort() + ")";
        }
    }

    /**
     * Dubbo EchoService 实现.
     */
    @DubboService(version = "1.0.0")
    public static class EchoServiceImpl implements EchoService {

        @Override
        public String echo(String message) {
            RpcContext ctx = RpcContext.getContext();
            return "echo: " + message + " (provider host: " + ctx.getLocalHost() + ", port: " + ctx.getLocalPort() + ")";
        }
    }
}
