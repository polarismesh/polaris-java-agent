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


import com.alibaba.cloud.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;


@RestController
@RequestMapping("/")
public class ConsumerController {

	private static final Logger LOG = LoggerFactory.getLogger(ConsumerController.class);

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate template;

	@Autowired
	@Qualifier("defaultRestTemplate")
	private RestTemplate defaultRestTemplate;

    @Autowired
	private ConsumerServiceWithFallback consumerServiceWithFallback;

	@Autowired
	private ConsumerService consumerService;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@GetMapping("/rest/circuitBreak")
	public String circuitBreakRestTemplate() {
		return circuitBreakerFactory
				.create("service-provider-2023#/circuitBreak")
				.run(() -> defaultRestTemplate.getForObject("/circuitBreak", String.class),
						throwable -> "trigger the refuse for service callee."
				);
	}

	@GetMapping("/echo/{str}")
    public ResponseEntity<String> rest(@RequestHeader Map<String, String> headerMap,
                                       @PathVariable String str,
                                       @RequestParam(required = false) String param) {
        String url = UriComponentsBuilder
                .fromHttpUrl("http://service-provider-2023/echo/" + str)
                .queryParam("param", param)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())
                    && !entry.getKey().contains("sct-")
                    && !entry.getKey().contains("SCT-")
                    && !entry.getKey().contains("polaris-")
                    && !entry.getKey().contains("POLARIS-")) {
                headers.add(entry.getKey(), entry.getValue());
            }
        }

        // 创建 HttpEntity 实例并传入 HttpHeaders
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 使用 exchange 方法发送 GET 请求，并获取响应
        try {
            ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, entity, String.class);
            LOG.info("response:{}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException httpClientErrorException) {
            return new ResponseEntity<>(httpClientErrorException.getResponseBodyAsString(), httpClientErrorException.getStatusCode());
        }
	}

    @GetMapping("/rest/circuitBreak/fallbackFromPolaris")
    public ResponseEntity<String> circuitBreakRestTemplateFallbackFromPolaris() {
        try {
            return defaultRestTemplate.getForEntity("/circuitBreak", String.class);
        } catch (HttpClientErrorException | HttpServerErrorException httpClientErrorException) {
            return new ResponseEntity<>(httpClientErrorException.getResponseBodyAsString(), httpClientErrorException.getStatusCode());
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

}
