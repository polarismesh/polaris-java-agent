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

package cn.polarismesh.agent.examples.dubbo.consumer;

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
 * Dubbo Consumer 启动类，内嵌 HTTP Server 按需调用服务
 */
public class ConsumerMain {

    private static final int LISTEN_PORT = Integer.getInteger("http.listen.port", 15700);

    private static final String PATH = "/echo";

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(
                        ConsumerConfiguration.class);
        context.start();

        GreetingServiceConsumer consumer =
                context.getBean(GreetingServiceConsumer.class);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(LISTEN_PORT), 0);
        server.createContext(PATH,
                new EchoClientHandler(consumer));
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

    /**
     * HTTP 请求处理器，解析查询参数并分发到对应的 Dubbo 服务调用
     */
    private static class EchoClientHandler implements HttpHandler {

        private final GreetingServiceConsumer consumer;

        EchoClientHandler(GreetingServiceConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> parameters =
                    splitQuery(exchange.getRequestURI());
            String value = parameters.get("value");
            String method = parameters.get("method");
            String response = "";
            switch (method == null ? "" : method) {
                case "sayHello":
                    response = consumer.doSayHello(value);
                    break;
                case "sayHi":
                    response = consumer.doSayHi(value);
                    break;
                case "echo":
                    response = consumer.doEcho(value);
                    break;
                default:
                    response = "unknown method: " + method;
                    break;
            }
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private static Map<String, String> splitQuery(URI uri)
                throws UnsupportedEncodingException {
            Map<String, String> queryPairs =
                    new LinkedHashMap<String, String>();
            String query = uri.getQuery();
            if (query == null) {
                return queryPairs;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                queryPairs.put(
                        URLDecoder.decode(
                                pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(
                                pair.substring(idx + 1), "UTF-8"));
            }
            return queryPairs;
        }
    }

    @Configuration
    @EnableDubbo
    @PropertySource("classpath:/spring/dubbo-consumer.properties")
    @ComponentScan(value = {
            "cn.polarismesh.agent.examples.dubbo.consumer"})
    static class ConsumerConfiguration {
    }
}
