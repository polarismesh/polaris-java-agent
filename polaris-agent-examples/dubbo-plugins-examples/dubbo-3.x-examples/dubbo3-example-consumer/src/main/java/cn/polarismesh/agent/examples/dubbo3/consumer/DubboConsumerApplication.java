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

package cn.polarismesh.agent.examples.dubbo3.consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dubbo3 Consumer 启动类，内嵌 HTTP Server 按需调用服务。
 *
 * <p>HTTP 接口与 Spring Boot 示例(sb-dubbo3-consumer)对齐:
 * <ul>
 *   <li>GET /dubbo/sayHello?name=xxx</li>
 *   <li>GET /dubbo/sayHi?name=xxx</li>
 *   <li>GET /dubbo/echo?message=xxx</li>
 * </ul>
 * 监听端口默认 17082(与 sb-dubbo3-consumer 的 server.port 对齐),
 * 可通过 -Dhttp.listen.port 覆盖。</p>
 */
public class DubboConsumerApplication {

    private static final int LISTEN_PORT = Integer.getInteger("http.listen.port", 17082);

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(
                        ConsumerConfiguration.class);
        context.start();

        final GreetingServiceConsumer consumer =
                context.getBean(GreetingServiceConsumer.class);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(LISTEN_PORT), 0);
        // 与 sb/sc consumer 对齐:每方法一路径 + name/message 查询参数
        server.createContext("/dubbo/sayHello", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String name = param(exchange, "name", "polaris");
                respond(exchange, consumer.doSayHello(name));
            }
        });
        server.createContext("/dubbo/sayHi", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String name = param(exchange, "name", "polaris");
                respond(exchange, consumer.doSayHi(name));
            }
        });
        server.createContext("/dubbo/echo", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String message = param(exchange, "message", "test");
                respond(exchange, consumer.doEcho(message));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                server.stop(1);
            }
        }));
        server.start();
        System.out.println("Consumer HTTP server started on port "
                + LISTEN_PORT);
    }

    /** 读取查询参数,缺省时返回 defaultValue。 */
    private static String param(HttpExchange exchange, String key, String defaultValue)
            throws UnsupportedEncodingException {
        String value = splitQuery(exchange.getRequestURI()).get(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    private static void respond(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static Map<String, String> splitQuery(URI uri)
            throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new LinkedHashMap<String, String>();
        String query = uri.getQuery();
        if (query == null) {
            return queryPairs;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx < 0) {
                continue;
            }
            queryPairs.put(
                    URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return queryPairs;
    }

    @Configuration
    @EnableDubbo
    @PropertySource("classpath:/spring/dubbo-consumer.properties")
    @ComponentScan(value = {
            "cn.polarismesh.agent.examples.dubbo3.consumer"})
    static class ConsumerConfiguration {
    }
}
