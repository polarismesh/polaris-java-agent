/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;



/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SpringBootApplication
public class ConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}


	@RestController
	public static class EchoController {

		private RestTemplate template;

		public EchoController(RestTemplate restTemplate) {
			this.template = restTemplate;
		}

		@Autowired
		@Qualifier("defaultRestTemplate")
		private RestTemplate defaultRestTemplate;


		@Autowired
		private CircuitBreakerFactory circuitBreakerFactory;

		@GetMapping("/rest")
		public String circuitBreakRestTemplate() {
			return circuitBreakerFactory
					.create("service-provider-2022#/circuitBreak")
					.run(() -> defaultRestTemplate.getForObject("/circuitBreak", String.class),
							throwable -> "trigger the refuse for service callee."
					);
		}



		@Bean
		@LoadBalanced
		public RestTemplate defaultRestTemplate() {
			DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory("http://service-provider-2022");
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setUriTemplateHandler(uriBuilderFactory);
			return restTemplate;
		}



		@GetMapping("/echo/{str}")
		public ResponseEntity<String> rest(@PathVariable String str) {
			ResponseEntity<String> response = template.getForEntity("http://service-provider-2022/echo/" + str,
					String.class);
			return response;
		}




	}
	@FeignClient(name = "service-provider-2022", contextId = "fallback-from-polaris")
	public interface CircuitBreakerQuickstartCalleeService {

		/**
		 * Check circuit break.
		 *
		 * @return circuit break info
		 */
		@GetMapping("/circuitBreak")
		String circuitBreak();
	}
	@Component
	public class CircuitBreakerQuickstartCalleeServiceFallback implements CircuitBreakerQuickstartCalleeServiceWithFallback {

		@Override
		public String circuitBreak() {
			return "fallback: trigger the refuse for service callee.";
		}
	}

	@FeignClient(name = "service-provider-2022", contextId = "fallback-from-code", fallback = CircuitBreakerQuickstartCalleeServiceFallback.class)
	public interface CircuitBreakerQuickstartCalleeServiceWithFallback {

		/**
		 * Check circuit break.
		 *
		 * @return circuit break info
		 */
		@GetMapping("/circuitBreak")
		String circuitBreak();
	}

}
