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

package cn.polarismesh.agent.examples.dubbo.sc.consumer;

import cn.polarismesh.agent.examples.dubbo.api.EchoService;
import cn.polarismesh.agent.examples.dubbo.api.GreetingService;
import org.apache.dubbo.common.config.ConfigurationUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Spring Cloud Alibaba 2021 + Dubbo 2.7 Consumer 启动类.
 * 同时支持 Dubbo RPC 调用和 Spring Cloud RestTemplate 调用.
 */
@SpringBootApplication
@EnableDubbo
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Dubbo 服务消费者，封装 Dubbo RPC 调用.
     */
    @Component
    public static class DubboServiceConsumer {

        @DubboReference
        private GreetingService greetingService;

        @DubboReference(version = "1.0.0", providedBy = "sc-dubbo-provider")
        private EchoService echoService;

        /**
         * 调用 sayHello，支持标签路由.
         *
         * @param name 名字
         * @return 问候语
         */
        public String sayHello(String name) {
            String tagValue = ConfigurationUtils.getProperty(CommonConstants.TAG_KEY);
            if (tagValue != null) {
                RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, tagValue);
            }
            return greetingService.sayHello(name);
        }

        /**
         * 调用 sayHi，支持标签路由.
         *
         * @param name 名字
         * @return 问候语
         */
        public String sayHi(String name) {
            String tagValue = ConfigurationUtils.getProperty(CommonConstants.TAG_KEY);
            if (tagValue != null) {
                RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, tagValue);
            }
            return greetingService.sayHi(name);
        }

        /**
         * 调用 echo，支持标签路由.
         *
         * @param message 输入值
         * @return 回显结果
         */
        public String echo(String message) {
            String tagValue = ConfigurationUtils.getProperty(CommonConstants.TAG_KEY);
            if (tagValue != null) {
                RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, tagValue);
            }
            return echoService.echo(message);
        }
    }

    /**
     * REST Controller，暴露 HTTP 端点调用 Dubbo 和 Spring Cloud 服务.
     */
    @RestController
    public static class ConsumerController {

        private static final Logger LOG =
                LoggerFactory.getLogger(ConsumerController.class);

        @Autowired
        private DubboServiceConsumer dubboConsumer;

        @Autowired
        private RestTemplate restTemplate;

        /**
         * 通过 Dubbo RPC 调用 GreetingService.
         */
        @GetMapping("/dubbo/sayHello")
        public String dubboSayHello(
                @RequestParam(defaultValue = "polaris") String name) {
            String result = dubboConsumer.sayHello(name);
            LOG.info("Dubbo sayHello: {}", result);
            return result;
        }

        /**
         * 通过 Dubbo RPC 调用 GreetingService.sayHi.
         */
        @GetMapping("/dubbo/sayHi")
        public String dubboSayHi(
                @RequestParam(defaultValue = "polaris") String name) {
            String result = dubboConsumer.sayHi(name);
            LOG.info("Dubbo sayHi: {}", result);
            return result;
        }

        /**
         * 通过 Dubbo RPC 调用 EchoService.
         */
        @GetMapping("/dubbo/echo")
        public String dubboEcho(
                @RequestParam(defaultValue = "test") String message) {
            String result = dubboConsumer.echo(message);
            LOG.info("Dubbo echo: {}", result);
            return result;
        }

        /**
         * 通过 Spring Cloud RestTemplate 调用 provider 的 REST 端点.
         */
        @GetMapping("/rest/echo/{string}")
        public String restEcho(@PathVariable String string) {
            String url = "http://sc-dubbo-provider/echo/" + string;
            String result = restTemplate.getForObject(url, String.class);
            LOG.info("REST echo: {}", result);
            return result;
        }
    }
}
