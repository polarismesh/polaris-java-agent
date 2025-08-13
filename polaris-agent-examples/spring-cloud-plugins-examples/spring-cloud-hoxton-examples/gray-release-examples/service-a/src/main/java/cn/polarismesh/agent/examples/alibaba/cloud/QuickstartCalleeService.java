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

package cn.polarismesh.agent.examples.alibaba.cloud;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Quickstart callee feign client.
 *
 * @author Haotian Zhang
 */
@FeignClient(value = "service-b-hoxton")
public interface QuickstartCalleeService {

	/**
	 * Get sum of two value.
	 *
	 * @param value1 value 1
	 * @param value2 value 2
	 * @return sum
	 */
	@GetMapping("/sum")
	String sum(@RequestParam("value1") int value1, @RequestParam("value2") int value2);
}
