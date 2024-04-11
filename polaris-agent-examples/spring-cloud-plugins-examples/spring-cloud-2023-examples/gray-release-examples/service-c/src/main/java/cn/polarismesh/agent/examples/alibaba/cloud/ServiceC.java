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

package cn.polarismesh.agent.examples.alibaba.cloud;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SpringBootApplication
public class ServiceC {

	public static void main(String[] args) {
		SpringApplication.run(ServiceC.class, args);
	}

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@RestController
	public static class EchoController {

		private Registration registration;

		@Value("${spring.cloud.tencent.metadata.content.lane:base}")
		private String lane;

		public EchoController(Registration registration) {
			this.registration = registration;
		}


		@GetMapping("/echo")
		public String echo() {
			return String.format("%s[%s]", registration.getServiceId(), lane);
		}

		@GetMapping("/sum")
		public String sum(@RequestParam("value1") int value1, @RequestParam int value2) {
			String content = String.format("%s[%s] -> ", registration.getServiceId(), lane);
			String resp = String.valueOf(value1 + value2);
			content += resp;
			return content;
		}

	}

}
