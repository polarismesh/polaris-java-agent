# Spring Cloud Alibaba 2025.1 Demo

本示例使用 SCA `2025.1.0.0`、Spring Cloud `2025.1.1`、Spring Boot `4.0.2` 和 Nacos Client `3.1.1`，要求 JDK 17 或以上。业务应用只依赖 SCA，通过 `-javaagent` 接入 SCT 2025.1 插件。

与之前版本一样，应用位于 `quickstart-examples/`：

- `scg`：Spring Cloud Gateway WebFlux 网关，默认端口 48080。
- `consumer`：通过 RestTemplate 和 Feign 调用 provider，默认端口 48082。
- `provider-a`：默认端口 48083。
- `provider-b`：默认端口 48084。

两个 provider 共用服务名 `service-provider-2025-1`，echo 返回与 2022/2023 示例相同的文本格式，通过 `[IP:端口]` 区分实例。调用链为 `SCG → consumer → provider-a / provider-b`；SCG 也提供直接访问 provider 的路由。

SCA 的 2025.0 系列对应 Spring Boot 3.5，2025.1 系列对应 Spring Boot 4.0；本示例使用与当前 Agent 插件匹配的 2025.1 系列。

## 构建

在仓库根目录执行，设置项目构建需要的 `JAVA_8_HOME`、`JAVA_11_HOME`、`JAVA_17_HOME`：

```bash
mvn -pl polaris-agent-examples/spring-cloud-plugins-examples/spring-cloud-2025.1-examples -am package
```

也可以只构建这个示例及四个应用：

```bash
mvn -f polaris-agent-examples/spring-cloud-plugins-examples/spring-cloud-2025.1-examples/pom.xml package
```

生成的可执行 JAR 位于 `quickstart-examples/<module>/target/`，版本继承项目 `revision`。SCG 单独使用 WebFlux，consumer 和两个 provider 使用 Web MVC。

## 本地运行

`local` profile 关闭 Nacos 和 Polaris，使用静态服务发现。以下命令在 `quickstart-examples/` 目录执行，在四个终端分别启动：

```bash
java -jar provider-a/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-a-2.2.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

```bash
java -jar provider-b/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-b-2.2.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

```bash
java -jar consumer/target/polaris-javaagent-demo-sc-quickstart-2025.1-consumer-2.2.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

```bash
java -jar scg/target/polaris-javaagent-demo-sc-quickstart-2025.1-scg-2.2.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

直接调用各服务：

```bash
curl -H 'X-Demo: hello' 'http://127.0.0.1:48083/echo/hello?param=sample'
curl -H 'X-Demo: hello' 'http://127.0.0.1:48084/echo/hello?param=sample'
curl http://127.0.0.1:48083/nacos/config
curl http://127.0.0.1:48083/polaris/config
curl http://127.0.0.1:48084/nacos/config
curl http://127.0.0.1:48084/polaris/config
curl -i http://127.0.0.1:48083/circuitBreak
curl -i http://127.0.0.1:48084/circuitBreak
curl -H 'X-Demo: hello' 'http://127.0.0.1:48082/echo/hello?param=sample'
curl http://127.0.0.1:48082/rest/echo/hello
curl http://127.0.0.1:48082/feign/echo/hello
curl http://127.0.0.1:48082/instances
```

consumer 的 `/instances` 返回两个 provider 地址。连续调用 `/echo/{str}`、RestTemplate 或 Feign 接口，可以看到两个不同的 provider 端口。

通过 SCG 调用：

```bash
curl -H 'X-Demo: hello' 'http://127.0.0.1:48080/consumer/echo/hello?param=sample'
curl http://127.0.0.1:48080/consumer/rest/echo/hello
curl http://127.0.0.1:48080/consumer/feign/echo/hello
curl http://127.0.0.1:48080/consumer/instances
curl http://127.0.0.1:48080/provider/echo/hello
curl http://127.0.0.1:48080/provider/nacos/config
curl http://127.0.0.1:48080/provider/polaris/config
curl -i http://127.0.0.1:48080/provider/circuitBreak
curl http://127.0.0.1:48080/service-consumer-2025-1/rest/echo/hello
curl http://127.0.0.1:48080/actuator/health
```

网关的 `/consumer/**` 和 `/provider/**` 路由移除首段路径，再通过 `lb://` 转发到对应服务；同时启用基于服务发现的 `/<service-name>/**` 路由。

可使用 `SCG_PORT`、`CONSUMER_PORT`、`PROVIDER_A_PORT`、`PROVIDER_B_PORT` 修改端口。请在各终端使用同一组环境变量，本地 consumer 和 SCG 据此生成静态实例地址。`PROVIDER_PORT` 仍作为 provider-a 的兼容配置，优先使用 `PROVIDER_A_PORT`。

## 接口与返回

provider-a 和 provider-b 都提供与 2022/2023 示例一致的接口：

- `GET /echo/{string}`：接受可选 query 参数 `param` 和请求 header，返回服务名、IP、实际端口、消息、参数、header JSON、注册元数据 JSON 和 `name` 配置值。
- `GET /nacos/config`：返回 `nacos.config`，默认 `none`，HTTP 200。
- `GET /polaris/config`：返回 `polaris.config`，默认 `none`，HTTP 200。
- `GET /circuitBreak`：provider-a 返回 `Quickstart Callee Service [IP:端口] is called right.`，HTTP 200。provider-b 默认 `ifBadGateway=true`，返回 `服务名 [IP:端口] is called wrong.`，HTTP 502；设置 `--ifBadGateway=false --ifDelay=true` 后等待 200ms，返回 `is called slow.`，HTTP 200；两项均为 false 时返回 `is called right.`，HTTP 200。

例如 echo 返回格式为：

```text
Hello, I'm service-provider-2025-1[127.0.0.1:48083], receive msg : hello, param : sample, header : {"x-demo":"hello",...}, my metadata : {}, name config:
```

`name`、`nacos.config`、`polaris.config` 通过 Spring 环境读取，controller 使用 `@RefreshScope`。正常注册模式的 metadata 来自 `Registration`；本地模式没有注册对象时输出 `{}`。保留已有 `/config` 接口用于读取 `demo.message`。

consumer 提供原版接口及返回行为：

- `GET /echo/{str}?param=...`：通过 RestTemplate 调用 provider，转发普通 header，过滤 `sct-` / `polaris-` header（不区分大小写），返回下游响应内容和 HTTP 状态。
- `GET /rest/circuitBreak`：通过 `CircuitBreakerFactory` 调用 provider；失败时返回 `trigger the refuse for service callee.`。
- `GET /rest/circuitBreak/fallbackFromPolaris`：返回 provider 的响应内容和 HTTP 状态；例如 provider-b 默认返回 HTTP 502。
- `GET /feign/circuitBreak/fallbackFromCode`：Feign 降级类返回 `fallback: trigger the refuse for service callee.`。
- `GET /feign/circuitBreak/fallbackFromPolaris`：使用没有代码降级的 Feign client。接入 Agent 后由 Polaris 规则决定降级行为；本地模式下 provider-b 的 HTTP 502 作为 Feign 调用异常向外传播。

```bash
curl -i http://127.0.0.1:48082/rest/circuitBreak
curl -i http://127.0.0.1:48082/rest/circuitBreak/fallbackFromPolaris
curl -i http://127.0.0.1:48082/feign/circuitBreak/fallbackFromCode
curl -i http://127.0.0.1:48082/feign/circuitBreak/fallbackFromPolaris
curl -i http://127.0.0.1:48080/service-consumer-2025-1/rest/circuitBreak
curl -i http://127.0.0.1:48080/consumer/feign/circuitBreak/fallbackFromCode
```

两个 provider 共用服务名，连续请求会在成功和故障实例之间负载均衡。需要全部成功时，为 provider-b 设置 `--ifBadGateway=false`。`/rest/echo/{str}` 保留为 `/echo/{str}` 的别名；`/feign/echo/{str}` 同样支持 `param` 与普通 header，`/instances` 返回实例地址。

consumer 的 `ScheduleTask` 与之前示例一致，默认关闭；设置 `--consumer.auto.test.enabled=true` 可每隔 `consumer.auto.test.interval`（默认 30000ms）发送测试请求。

SCG 与旧版一致启用服务发现路由，可用 `/<service-name>/echo/...` 和 `/<service-name>/circuitBreak` 访问对应服务；原有 `/consumer/**`、`/provider/**` 简写路由保留，下游 body 和状态码直接转发。

`local` profile 使用 Resilience4J 验证 Spring Cloud CircuitBreaker 和 Feign 代码降级，正常模式关闭 Resilience4J 自动配置，由 Polaris Agent 提供熔断实现。业务应用不依赖 SCT。

## 接入 Nacos

不指定 `local` profile 时启用 Nacos 注册发现。准备可访问的 Nacos 服务，再设置：

```bash
export NACOS_SERVER_ADDR=127.0.0.1:8848
# 开启鉴权时，设置对应用户名和密码。
export NACOS_USERNAME=your-username
export NACOS_PASSWORD=your-password
```

可选环境变量 `NACOS_NAMESPACE` 和 `NACOS_GROUP` 分别配置命名空间 ID 和服务分组，默认使用 public 命名空间和 `DEFAULT_GROUP`。

使用上面的四个启动命令并去掉 `--spring.profiles.active=local`。两个 provider 注册为 `service-provider-2025-1` 的不同实例；consumer 和 SCG 的服务名分别为 `service-consumer-2025-1`、`scg-gateway-2025-1`。

provider 默认关闭 Nacos 配置中心，需要验证配置刷新时，为两个 provider 加上 `--spring.cloud.nacos.config.enabled=true`。在 Nacos 中创建 `service-provider-2025-1.properties`，分组与 `NACOS_GROUP` 一致，配置：

```properties
name=hello-from-nacos
nacos.config=hello-from-nacos
polaris.config=hello-from-polaris
demo.message=hello-from-nacos
```

访问两个 provider 的 `/nacos/config`、`/polaris/config` 和 `/echo/{string}` 可查看上述配置；`/config` 返回 `demo.message`，`@RefreshScope` 用于配置变化后的刷新。consumer 和 SCG 的配置文件分别为 `service-consumer-2025-1.properties`、`scg-gateway-2025-1.properties`。

## 接入 Polaris Java Agent

先在仓库根目录构建 Agent 发行包：

```bash
cd polaris-agent-build/bin
bash build.sh
```

发行包的 `conf/polaris-agent.config` 可配置：

```properties
plugins.enable=spring-cloud-2025.1-plugin
```

也可保持 `plugins.enable` 为空，Agent 会根据 JAR 中的 Spring Boot 4.0 版本自动选择此插件。容器初始化脚本对应环境变量为 `JAVA_AGENT_FRAMEWORK_NAME=spring-cloud`、`JAVA_AGENT_FRAMEWORK_VERSION=2025.1`。

在 `conf/plugin/spring-cloud/application.properties` 中配置 Polaris 服务端地址和需要启用的能力。在 `quickstart-examples/` 目录的四个终端分别启动：

```bash
export AGENT_DIR=/absolute/path/to/polaris-java-agent-2.2.1.0-SNAPSHOT
java -javaagent:"${AGENT_DIR}/polaris-agent-core-bootstrap.jar" -jar provider-a/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-a-2.2.1.0-SNAPSHOT.jar
java -javaagent:"${AGENT_DIR}/polaris-agent-core-bootstrap.jar" -jar provider-b/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-b-2.2.1.0-SNAPSHOT.jar
java -javaagent:"${AGENT_DIR}/polaris-agent-core-bootstrap.jar" -jar consumer/target/polaris-javaagent-demo-sc-quickstart-2025.1-consumer-2.2.1.0-SNAPSHOT.jar
java -javaagent:"${AGENT_DIR}/polaris-agent-core-bootstrap.jar" -jar scg/target/polaris-javaagent-demo-sc-quickstart-2025.1-scg-2.2.1.0-SNAPSHOT.jar
```

只验证 Agent 装载时，在 Agent 的 Spring Cloud 配置中设置 `spring.cloud.polaris.enabled=false`，并为四个应用加上 `--spring.profiles.active=local`。Nacos / Polaris 注册发现联调需要实际服务端；本地模式验证 HTTP 调用、两个 provider 的静态负载均衡、网关转发及 Agent 装载。

四个模块均提供 Dockerfile。构建上下文为对应模块目录，例如在 `quickstart-examples/` 中执行：

```bash
docker build --build-arg file_name=polaris-javaagent-demo-sc-quickstart-2025.1-provider-a-2.2.1.0-SNAPSHOT.jar -t sca2025-provider-a provider-a
docker build --build-arg file_name=polaris-javaagent-demo-sc-quickstart-2025.1-provider-b-2.2.1.0-SNAPSHOT.jar -t sca2025-provider-b provider-b
docker build --build-arg file_name=polaris-javaagent-demo-sc-quickstart-2025.1-consumer-2.2.1.0-SNAPSHOT.jar -t sca2025-consumer consumer
docker build --build-arg file_name=polaris-javaagent-demo-sc-quickstart-2025.1-scg-2.2.1.0-SNAPSHOT.jar -t sca2025-scg scg
```

## 版本来源

- [SCA 官方版本说明](https://github.com/alibaba/spring-cloud-alibaba/blob/2025.1.0.0/README-zh.md)
- [SCA 官方 POM](https://github.com/alibaba/spring-cloud-alibaba/blob/2025.1.0.0/pom.xml)
- [Maven Central 已发布版本](https://repo.maven.apache.org/maven2/com/alibaba/cloud/spring-cloud-alibaba-dependencies/maven-metadata.xml)
