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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	@RefreshScope
	public static class EchoController {

		private Registration registration;

		private RestTemplate template;

		@Value("${server.port}")
		private int port;

		@Value("${custom.config:none}")
		private String customConfig;

		public EchoController(RestTemplate restTemplate, Registration registration) {
			this.template = restTemplate;
			this.registration = registration;
		}

//		@GetMapping("/echo/{str}")
//		public ResponseEntity<String> rest(@PathVariable String str) {
//			String content = String.format("%s[%d] -> ", registration.getServiceId(), port);
//			ResponseEntity<String> response = template.getForEntity("http://service-provider-2023/echo/" + str +"123",
//					String.class);
//			content += response.getBody();
//			return new ResponseEntity<>(content, HttpStatus.OK);
//		}

		@GetMapping("/echo/{str}")
		public ResponseEntity<String> rest(@PathVariable String str) {
			ResponseEntity<String> response = template.getForEntity("http://service-provider-2023/echo/" + str,
					String.class);
			return response;
		}

		@GetMapping("/custom/config")
		public ResponseEntity<String> getCustomConfig() {
			return new ResponseEntity<>(String.valueOf(customConfig), HttpStatus.OK);
		}

	}

}
