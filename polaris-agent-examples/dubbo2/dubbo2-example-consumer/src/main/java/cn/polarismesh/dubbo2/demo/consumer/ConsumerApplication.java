/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.polarismesh.dubbo2.demo.consumer;

import cn.polarismesh.dubbo2.demo.DemoService;
import cn.polarismesh.dubbo2.demo.HelloReply;
import cn.polarismesh.dubbo2.demo.HelloRequest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CompletableFuture;

public class ConsumerApplication {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/dubbo-consumer.xml");
        context.start();
        DemoService demoService = context.getBean("demoService", DemoService.class);
        for(int i = 0; i < 5; i++){
            doRequest(demoService);
            System.out.println("===========================================");
            doRequestAsync(demoService);
            System.out.println("===========================================");
            Thread.sleep(1000);
        }
        System.in.read();
        for(int i = 0; i < 5; i++){
            doRequest(demoService);
            System.out.println("===========================================");
            doRequestAsync(demoService);
            System.out.println("===========================================");
            Thread.sleep(1000);
        }
        System.in.read();
    }

    private static void doRequest(DemoService demoService){
        HelloRequest request = HelloRequest.newBuilder().setName("Hello").build();
        HelloReply reply = demoService.sayHello(request);
        System.out.println("result: " + reply.getMessage());
    }

    private static void doRequestAsync(DemoService demoService) throws Exception {
        HelloRequest asyncRequest = HelloRequest.newBuilder().setName("async").build();
        CompletableFuture<HelloReply> asyncReply = demoService.sayHelloAsync(asyncRequest);
        System.out.println("result: " + asyncReply.get());
    }
}
