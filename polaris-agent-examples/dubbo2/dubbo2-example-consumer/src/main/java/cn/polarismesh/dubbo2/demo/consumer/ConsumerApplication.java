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

import cn.polarismesh.dubbo2.api.DemoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConsumerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerApplication.class);

    private static final int LISTEN_PORT = 15700;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(LISTEN_PORT), 0);
        server.createContext("/echo", new EchoClientHandler());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
        }));
        server.start();
    }

    private static class EchoClientHandler implements HttpHandler {

        private final DemoService demoService;

        public EchoClientHandler() {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/dubbo-consumer.xml");
            context.start();
            demoService = context.getBean("demoService", DemoService.class);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String resp = doRequest(exchange, demoService);
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }
    }

    private static String doRequest(HttpExchange exchange, DemoService demoService)
            throws UnsupportedEncodingException {
        Map<String, String> parameters = splitQuery(exchange.getRequestURI());
        String echoValue = parameters.get("value");
        return demoService.sayHello(echoValue);
    }

    private static Map<String, String> splitQuery(URI uri) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String query = uri.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
