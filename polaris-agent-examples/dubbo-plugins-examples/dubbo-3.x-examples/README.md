# Dubbo 3.2.x Agent 示例

本目录包含三组示例，演示如何通过 **polaris-java-agent** 以无侵入方式将 Dubbo 3.2.x 应用接入北极星（Polaris）服务治理平台。

> Agent 插件实现见 `polaris-agent-plugins/dubbo-plugins/dubbo-3.x-plugin`。插件拦截 `DefaultApplicationDeployer#initialize()`（API / Spring / Spring Boot 三种启动路径的汇聚点），在运行期将注册中心 / 配置中心 / 元数据上报改写为 Polaris，并把 Polaris 扩展注入应用 classloader。

## 目录结构

```
dubbo-3.x-examples/
├── dubbo3-example-api/           # 公共接口定义（GreetingService、EchoService）
├── dubbo3-example-provider/      # 纯 Dubbo3 Provider
├── dubbo3-example-consumer/      # 纯 Dubbo3 Consumer（内嵌 HTTP Server）
├── spring-boot-dubbo3-example/
│   ├── sb-dubbo3-provider/       # Spring Boot + Dubbo3 Provider
│   └── sb-dubbo3-consumer/       # Spring Boot + Dubbo3 Consumer（RestController）
└── spring-cloud-dubbo3-examples/
    ├── sc3-dubbo-provider/       # Spring Cloud Alibaba 2021 + Dubbo3 Provider
    └── sc3-dubbo-consumer/       # Spring Cloud Alibaba 2021 + Dubbo3 Consumer（RestController）
```

版本：Dubbo `3.2.17`、Spring Boot `2.7.18`、Spring Cloud Alibaba `2021.0.5.0`、JDK 8。

### 编译打包

```bash
# 在项目根目录执行（以本目录为 reactor 根，构建全部子模块）
mvn -f polaris-agent-examples/dubbo-plugins-examples/dubbo-3.x-examples/pom.xml clean package -DskipTests
```

> 需要先完成 Agent 构建，参考 [polaris-agent-build](../../../polaris-agent-build/README.md)。

---

## 一、纯 Dubbo3 示例

### 服务接口

`dubbo3-example-api` 定义了两个服务接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `GreetingService` | `sayHello(String name)` | 基础问候 |
| `GreetingService` | `sayHi(String name)` | 支持标签路由的问候（返回 `DUBBO_LABELS`） |
| `EchoService` | `echo(String message)` | 回显（版本 `1.0.0`） |

### 配置说明

**Provider**（`dubbo3-example-provider/src/main/resources/spring/dubbo-provider.properties`）

```properties
dubbo.application.name=dubbo3-agent-example-provider
dubbo.registry.address=nacos://127.0.0.1:8848      # 原有注册中心地址，Agent 会替换为 Polaris
dubbo.config-center.address=zookeeper://127.0.0.1:2181  # 原有配置中心，Agent 会替换为 Polaris
dubbo.protocol.port=20880
```

**Consumer**（`dubbo3-example-consumer/src/main/resources/spring/dubbo-consumer.properties`）

```properties
dubbo.application.name=dubbo3-agent-example-consumer
dubbo.registry.address=nacos://127.0.0.1:8848      # 原有注册中心地址，Agent 会替换为 Polaris
dubbo.protocol.port=30880
```

> **说明**：Polaris 服务端地址可通过以下方式配置（优先级从高到低）：
> 1. JVM 系统属性 `-Ddubbo.registry.address=polaris://<host>:<port>`
> 2. Agent 配置文件 `conf/plugin/dubbo/dubbo-polaris.properties`（见 [配置文件说明](#四配置文件说明)）
> 3. 硬编码默认值 `polaris://127.0.0.1:8091`

### 启动方式

**启动 Provider**

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-3.x-plugin \
     -jar dubbo3-example-provider/target/dubbo3-agent-example-provider-*.jar
```

**启动 Consumer**

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-3.x-plugin \
     -Dhttp.listen.port=15700 \
     -jar dubbo3-example-consumer/target/dubbo3-agent-example-consumer-*.jar
```

Consumer 启动后内嵌 HTTP Server 监听 `15700` 端口（可通过 `-Dhttp.listen.port` 修改）。

### 验证接口

```bash
curl "http://localhost:15700/echo?method=sayHello&value=world"
curl "http://localhost:15700/echo?method=sayHi&value=world"
curl "http://localhost:15700/echo?method=echo&value=hello"
```

---

## 二、Spring Boot + Dubbo3 示例

`spring-boot-dubbo3-example` 演示标准 Spring Boot 2.7 + Dubbo 3.2 应用，Provider 同时暴露 Dubbo 服务与 REST 端点。

### 配置说明

**sb-dubbo3-provider**（`application.properties`）：`server.port=0`（随机）、`dubbo.registry.address=zookeeper://127.0.0.1:2181`、`dubbo.protocol.port=20880`。

**sb-dubbo3-consumer**（`application.properties`）：`server.port=17082`、`dubbo.registry.address=zookeeper://127.0.0.1:2181`、`dubbo.protocol.port=30880`。

### 启动方式

```bash
# Provider
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-3.x-plugin \
     -jar sb-dubbo3-provider/target/sb-dubbo3-provider-*.jar

# Consumer
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-3.x-plugin \
     -jar sb-dubbo3-consumer/target/sb-dubbo3-consumer-*.jar
```

### 验证接口（Consumer 监听 `17082`）

```bash
curl "http://localhost:17082/dubbo/sayHello?name=polaris"
curl "http://localhost:17082/dubbo/sayHi?name=polaris"
curl "http://localhost:17082/dubbo/echo?message=test"
```

---

## 三、Spring Cloud + Dubbo3 示例

`spring-cloud-dubbo3-examples` 演示 Spring Cloud Alibaba 2021 + Dubbo 3.2 混合场景，同时支持 Dubbo RPC 调用与 Spring Cloud RestTemplate 调用。

### 配置说明

**sc3-dubbo-provider**（`application.properties`）：`server.port=0`、`spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848`、`dubbo.registry.address=nacos://127.0.0.1:8848`、`dubbo.protocol.port=20880`。

**sc3-dubbo-consumer**（`application.properties`）：`server.port=17081`、同样的 Nacos 与 Dubbo 配置、`dubbo.protocol.port=30880`。

> Nacos 地址（`spring.cloud.nacos.discovery.server-addr` 与 `dubbo.registry.address`）都会被 Agent 替换为 Polaris。

### 启动方式

```bash
# Provider
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-3.x-plugin,spring-cloud-2021-plugin \
     -jar sc3-dubbo-provider/target/sc3-dubbo-provider-*.jar

# Consumer
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-3.x-plugin,spring-cloud-2021-plugin \
     -jar sc3-dubbo-consumer/target/sc3-dubbo-consumer-*.jar
```

### 验证接口（Consumer 监听 `17081`）

```bash
# Dubbo RPC
curl "http://localhost:17081/dubbo/sayHello?name=polaris"
curl "http://localhost:17081/dubbo/sayHi?name=polaris"
curl "http://localhost:17081/dubbo/echo?message=test"
# Spring Cloud RestTemplate 调用 Provider REST 端点
curl "http://localhost:17081/rest/echo/hello"
```

---

## 四、配置文件说明

Dubbo 插件支持从本地配置文件读取配置，免去在启动命令中传递大量 `-D` 参数。

### 文件位置

```
polaris-java-agent-{version}/
└── conf/plugin/dubbo/dubbo-polaris.properties
```

### 配置项

```properties
# 注册中心（服务发现）
dubbo.registry.address=polaris://127.0.0.1:8091
# 配置中心（默认端口 8093；关闭改写：polaris.agent.dubbo.config-center.enabled=false）
dubbo.config-center.address=polaris://127.0.0.1:8093
# 元数据上报（默认端口 8091；关闭改写：polaris.agent.dubbo.metadata-report.enabled=false）
dubbo.metadata-report.address=polaris://127.0.0.1:8091
```

### 开关（默认开启）

| 能力 | 开关键 | 默认 |
|------|--------|------|
| 注册中心 | 始终开启（核心能力） | on |
| 配置中心 | `polaris.agent.dubbo.config-center.enabled` | true |
| 元数据上报 | `polaris.agent.dubbo.metadata-report.enabled` | true |

### 优先级

| 优先级 | 来源 | 示例 |
|--------|------|------|
| 1（最高） | JVM 系统属性 | `-Ddubbo.registry.address=polaris://10.0.0.1:8091` |
| 2 | 配置文件 | `conf/plugin/dubbo/dubbo-polaris.properties` |
| 3（最低） | 硬编码默认值 | `polaris://127.0.0.1:8091` |

> **注意**：`dubbo.registry.parameters.*` 等扩展参数支持配置文件与 `-D` 两种方式（`-D` 覆盖文件）。
>
> 若你已自行把地址配成 `polaris://`，Agent 检测到已是 Polaris 会自动跳过改写（no-op），此时插件仅负责把 Polaris 扩展注入 classpath。

---

## 相关文档

- [polaris-java-agent 主文档](../../../../README-zh.md)
- [Agent 构建说明](../../../polaris-agent-build/README.md)
- [Dubbo 2.7.x 示例](../dubbo-2.7.x-examples/README.md)
- [北极星官网](https://polarismesh.cn)
