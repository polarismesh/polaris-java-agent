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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@Service
public class ScheduleTask {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleTask.class);

    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate template;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    @Value("${consumer.auto.test.enabled:false}")
    private Boolean autoTest;

    @Scheduled(fixedDelayString = "${consumer.auto.test.interval:30000}")
    public void autoSendRequest() {
        if (!autoTest) {
            return;
        }
        String[] testStrings = {"test1", "test2", "auto-test"};
        for (String str : testStrings) {
            try {
                ResponseEntity<String> response = circuitBreakerFactory.create("autoRequestCircuitBreaker")
                        .run(() -> template.getForEntity("http://service-provider-hoxton/echo/" + str, String.class),
                                throwable -> {
                                    LOG.error("自动请求失败: {}", throwable.getMessage());
                                    return ResponseEntity.status(503).body("服务暂不可用");
                                });
                LOG.info("自动请求[{}]响应: {}", str, response.getBody());
            } catch (Exception e) {
                LOG.error("自动请求异常: {}", e.getMessage());
            }
        }
    }

}
