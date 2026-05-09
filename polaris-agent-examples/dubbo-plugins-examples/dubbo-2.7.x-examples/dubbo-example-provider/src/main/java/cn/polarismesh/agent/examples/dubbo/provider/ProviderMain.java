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

package cn.polarismesh.agent.examples.dubbo.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.CountDownLatch;

/**
 * Dubbo Provider 启动类
 */
public class ProviderMain {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(ProviderConfiguration.class);
        context.start();
        System.out.println("Dubbo provider started");
        new CountDownLatch(1).await();
    }

    @Configuration
    @EnableDubbo(scanBasePackages = "cn.polarismesh.agent.examples.dubbo.provider")
    @PropertySource("classpath:/spring/dubbo-provider.properties")
    @ComponentScan(value = {"cn.polarismesh.agent.examples.dubbo.provider"})
    static class ProviderConfiguration {
    }
}
