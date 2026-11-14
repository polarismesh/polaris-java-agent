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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/")
public class ConsumerController {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerController.class);

    private final RestTemplate template;
    private final RestTemplate defaultRestTemplate;
    private final ConsumerServiceWithFallback consumerServiceWithFallback;
    private final ConsumerService consumerService;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ProviderClient providerClient;
    private final DiscoveryClient discoveryClient;

    public ConsumerController(@Qualifier("restTemplate") RestTemplate template,
            @Qualifier("defaultRestTemplate") RestTemplate defaultRestTemplate,
            ConsumerServiceWithFallback consumerServiceWithFallback, ConsumerService consumerService,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory, ProviderClient providerClient,
            DiscoveryClient discoveryClient) {
        this.template = template;
        this.defaultRestTemplate = defaultRestTemplate;
        this.consumerServiceWithFallback = consumerServiceWithFallback;
        this.consumerService = consumerService;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.providerClient = providerClient;
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/rest/circuitBreak")
    public String circuitBreakRestTemplate() {
        return circuitBreakerFactory.create("service-provider-2025-1#/circuitBreak")
                .run(() -> defaultRestTemplate.getForObject("/circuitBreak", String.class),
                        throwable -> "trigger the refuse for service callee.");
    }

    @GetMapping("/echo/{str}")
    public ResponseEntity<String> rest(@RequestHeader Map<String, String> headerMap,
            @PathVariable("str") String str,
            @RequestParam(name = "param", required = false) String param) {
        String url = UriComponentsBuilder.fromUriString("http://service-provider-2025-1")
                .pathSegment("echo", "{str}")
                .queryParamIfPresent("param", Optional.ofNullable(param).map(value -> "{param}"))
                .encode().buildAndExpand(str, param == null ? "" : param).toUriString();
        HttpHeaders headers = new HttpHeaders();
        forwardedHeaders(headerMap).forEach(headers::add);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = template.exchange(java.net.URI.create(url),
                    HttpMethod.GET, entity, String.class);
            LOG.info("response:{}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException error) {
            return new ResponseEntity<>(error.getResponseBodyAsString(), error.getStatusCode());
        }
    }

    @GetMapping("/rest/circuitBreak/fallbackFromPolaris")
    public ResponseEntity<String> circuitBreakRestTemplateFallbackFromPolaris() {
        try {
            return defaultRestTemplate.getForEntity("/circuitBreak", String.class);
        } catch (HttpClientErrorException | HttpServerErrorException error) {
            return new ResponseEntity<>(error.getResponseBodyAsString(), error.getStatusCode());
        }
    }

    @GetMapping("/feign/circuitBreak/fallbackFromCode")
    public String circuitBreakFeignFallbackFromCode() {
        return consumerServiceWithFallback.circuitBreak();
    }

    @GetMapping("/feign/circuitBreak/fallbackFromPolaris")
    public String circuitBreakFeignFallbackFromPolaris() {
        return consumerService.circuitBreak();
    }

    @GetMapping("/rest/echo/{str}")
    public ResponseEntity<String> restEcho(@RequestHeader Map<String, String> headerMap,
            @PathVariable("str") String str,
            @RequestParam(name = "param", required = false) String param) {
        return rest(headerMap, str, param);
    }

    @GetMapping("/feign/echo/{str}")
    public String feignEcho(@RequestHeader Map<String, String> headerMap,
            @PathVariable("str") String str,
            @RequestParam(name = "param", required = false) String param) {
        return providerClient.echo(forwardedHeaders(headerMap), str, param);
    }

    @GetMapping("/instances")
    public List<String> instances() {
        return discoveryClient.getInstances("service-provider-2025-1").stream()
                .map(ServiceInstance::getUri).map(Object::toString).toList();
    }

    private Map<String, String> forwardedHeaders(Map<String, String> headerMap) {
        Map<String, String> headers = new LinkedHashMap<>();
        headerMap.forEach((key, value) -> {
            String normalizedKey = key.toLowerCase(Locale.ROOT);
            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)
                    && !normalizedKey.contains("sct-") && !normalizedKey.contains("polaris-")) {
                headers.put(key, value);
            }
        });
        return headers;
    }
}
