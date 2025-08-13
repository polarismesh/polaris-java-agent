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

package cn.polarismesh.agent.examples.alibaba.cloud.cloud;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @RefreshScope
    @RestController
    public static class EchoController {

        private static final Logger LOG = LoggerFactory.getLogger(ProviderApplication.class);

        @Value("${spring.application.name}")
        private String svcName;

        @Value("${name:none}")
        private String name;

        @Value("${server.port:0}")
        private String port;

        @Value("${spring.cloud.client.ip-address:127.0.0.1}")
        private String ip;

        @Value("${nacos.config:none}")
        private String nacosConfig;

        @Value("${polaris.config:none}")
        private String polarisConfig;

        @Value("${ifBadGateway:true}")
        private Boolean ifBadGateway;

        @Value("${ifDelay:false}")
        private Boolean ifDelay;

        @GetMapping("/nacos/config")
        public ResponseEntity<String> getNacosConfig() {
            LOG.info("{} [{}:{}] is called right. nacos config:{}", svcName, ip, port, nacosConfig);
            return new ResponseEntity<>(String.valueOf(nacosConfig), HttpStatus.OK);
        }

        @GetMapping("/polaris/config")
        public ResponseEntity<String> getPolarisConfig() {
            LOG.info("{} [{}:{}] is called right. polaris config:{}", svcName, ip, port, polarisConfig);
            return new ResponseEntity<>(String.valueOf(polarisConfig), HttpStatus.OK);
        }

        @GetMapping("/circuitBreak")
        public ResponseEntity<String> circuitBreak() {
            if (ifBadGateway) {
                String response = String.format("%s [%s:%s] is called wrong.", svcName, ip, port);
                LOG.info("Circuit break triggered - bad gateway: {}", response);
                return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
            }
            if (ifDelay) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    LOG.warn("Sleep interrupted", e);
                    Thread.currentThread().interrupt();
                }
                String response = String.format("%s [%s:%s] is called slow.", svcName, ip, port);
                LOG.info("Circuit break triggered - slow response: {}", response);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            String response = String.format("%s [%s:%s] is called right.", svcName, ip, port);
            LOG.info("Normal response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @GetMapping("/echo/{string}")
        public String echo(@PathVariable String string) {
            String result = String.format("Hello, I'm %s [%s:%s], receive msg : %s, name config:%s", svcName, ip, port,
                    string, name);
            LOG.info("{} -- response result: {}", svcName, result);
            return result;
        }

    }

}