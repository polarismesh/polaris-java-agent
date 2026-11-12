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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ProviderController {
    @Value("${spring.application.name}")
    private String serviceName;

    private final Environment environment;

    public ProviderController(Environment environment) {
        this.environment = environment;
    }

    @Value("${demo.message:hello-from-sca-2025.1}")
    private String message;

    @GetMapping("/echo/{message}")
    public String echo(@PathVariable("message") String value) {
        return serviceName + ":" + environment.getProperty("local.server.port", "0") + " echo=" + value + " config=" + message;
    }

    @GetMapping("/config")
    public String config() {
        return message;
    }
}
