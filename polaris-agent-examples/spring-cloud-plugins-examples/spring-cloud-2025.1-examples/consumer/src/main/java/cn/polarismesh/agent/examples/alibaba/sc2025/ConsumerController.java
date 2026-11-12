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

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {
    private final RestTemplate restTemplate;
    private final ProviderClient providerClient;
    private final DiscoveryClient discoveryClient;

    public ConsumerController(RestTemplate restTemplate, ProviderClient providerClient,
            DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.providerClient = providerClient;
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/rest/echo/{message}")
    public String restEcho(@PathVariable("message") String message) {
        return restTemplate.getForObject("http://service-provider-2025-1/echo/{message}",
                String.class, message);
    }

    @GetMapping("/feign/echo/{message}")
    public String feignEcho(@PathVariable("message") String message) {
        return providerClient.echo(message);
    }

    @GetMapping("/instances")
    public List<String> instances() {
        return discoveryClient.getInstances("service-provider-2025-1").stream()
                .map(ServiceInstance::getUri).map(Object::toString).toList();
    }
}
