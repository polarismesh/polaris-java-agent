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

package cn.polarismesh.agent.examples.alibaba.sc2025;

import java.util.Collections;
import java.util.Map;

import com.alibaba.nacos.common.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ProviderController {
    private static final Logger LOG = LoggerFactory.getLogger(ProviderController.class);

    private final ObjectProvider<Registration> registration;
    private final Environment environment;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${spring.cloud.client.ip-address:127.0.0.1}")
    private String ip;

    @Value("${name:}")
    private String name;

    @Value("${nacos.config:none}")
    private String nacosConfig;

    @Value("${polaris.config:none}")
    private String polarisConfig;

    @Value("${demo.message:hello-from-sca-2025.1}")
    private String message;

    public ProviderController(ObjectProvider<Registration> registration, Environment environment) {
        this.registration = registration;
        this.environment = environment;
    }

    @GetMapping("/nacos/config")
    public ResponseEntity<String> getNacosConfig() {
        LOG.info("{} [{}:{}] is called right. nacos config:{}", serviceName, ip, port(), nacosConfig);
        return new ResponseEntity<>(String.valueOf(nacosConfig), HttpStatus.OK);
    }

    @GetMapping("/polaris/config")
    public ResponseEntity<String> getPolarisConfig() {
        LOG.info("{} [{}:{}] is called right. polaris config:{}", serviceName, ip, port(), polarisConfig);
        return new ResponseEntity<>(String.valueOf(polarisConfig), HttpStatus.OK);
    }

    @GetMapping("/echo/{string}")
    public String echo(@RequestHeader Map<String, String> headerMap,
            @PathVariable("string") String string,
            @RequestParam(name = "param", required = false) String param) {
        Registration instance = registration.getIfAvailable();
        Map<String, String> metadata = instance == null ? Collections.emptyMap() : instance.getMetadata();
        String result = String.format("Hello, I'm %s[%s:%s], receive msg : %s, param : %s, header : %s, "
                + "my metadata : %s, name config:%s", serviceName, ip, port(), string, param,
                JacksonUtils.toJson(headerMap), JacksonUtils.toJson(metadata), name);
        LOG.info("{} -- response result: {}", serviceName, result);
        return result;
    }

    @GetMapping("/config")
    public String config() {
        return message;
    }

    private int port() {
        return environment.getProperty("local.server.port", Integer.class,
                environment.getProperty("server.port", Integer.class, 0));
    }

    @GetMapping("/circuitBreak")
    public String circuitBreak() {
        LOG.info("{} [{}:{}] is called right.", serviceName, ip, port());
        return String.format("Quickstart Callee Service [%s:%s] is called right.", ip, port());
    }
}
