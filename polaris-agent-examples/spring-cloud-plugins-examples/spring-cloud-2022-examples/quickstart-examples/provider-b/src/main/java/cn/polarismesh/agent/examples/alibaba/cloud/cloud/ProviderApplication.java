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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
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

	//	@RefreshScope
	@RestController
	public static class EchoController {

		private static final Logger LOG = LoggerFactory.getLogger(ProviderApplication.class);

		@Value("${name:}")
		private String name;

		@Value("${server.port}")
		private String port;

		@Value("${spring.cloud.client.ip-address:127.0.0.1}")
		private String ip;

		@GetMapping("/circuitBreak")
		public ResponseEntity<String> circuitBreak() throws InterruptedException {
			LOG.info("Quickstart Callee Service [{}:{}] is called wrong.", ip, port);
			return new ResponseEntity<>("failed for call quickstart callee service.", HttpStatus.BAD_GATEWAY);
		}

		@GetMapping("/echo/{string}")
		public String echo(@PathVariable String string) {
			return "Hello, I'm provider, receive msg : "
					+ string
					+ ", my metadata : "
					+ " name config : "
					+ name
					+ "    "
			        + port;
		}

	}

}