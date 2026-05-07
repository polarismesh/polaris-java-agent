# Dubbo 2.7.x Agent 示例

本目录包含两组示例，演示如何通过 **polaris-java-agent** 以无侵入方式将 Dubbo 2.7.x 应用接入北极星（Polaris）服务治理平台。

## 目录结构

```
dubbo-2.7.x-examples/
├── dubbo-example-api/          # 公共接口定义（GreetingService、EchoService）
├── dubbo-example-provider/     # 纯 Dubbo Provider
├── dubbo-example-consumer/     # 纯 Dubbo Consumer（内嵌 HTTP Server）
└── spring-cloud-dubbo-examples/
    ├── sc-dubbo-provider/      # Spring Cloud + Dubbo Provider
    └── sc-dubbo-consumer/      # Spring Cloud + Dubbo Consumer（RestController）
```

---

## 一、纯 Dubbo 示例

### 服务接口

`dubbo-example-api` 定义了两个服务接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `GreetingService` | `sayHello(String name)` | 基础问候 |
| `GreetingService` | `sayHi(String name)` | 支持标签路由的问候（返回 `DUBBO_LABELS`） |
| `EchoService` | `echo(String message)` | 回显（版本 `1.0.0`） |

### 编译打包

```bash
# 在项目根目录执行
mvn clean package -pl polaris-agent-examples/dubbo-plugins-examples/dubbo-2.7.x-examples -am -DskipTests
```

### 配置说明

**Provider**（`dubbo-example-provider/src/main/resources/spring/dubbo-provider.properties`）

```properties
dubbo.application.name=dubbo-agent-example-provider
dubbo.registry.address=nacos://127.0.0.1:8848   # 原有注册中心地址，Agent 会替换为 Polaris
dubbo.protocol.port=20880
```

**Consumer**（`dubbo-example-consumer/src/main/resources/spring/dubbo-consumer.properties`）

```properties
dubbo.application.name=dubbo-agent-example-consumer
dubbo.registry.address=nacos://127.0.0.1:8848   # 原有注册中心地址，Agent 会替换为 Polaris
dubbo.protocol.port=30880
```

> **说明**：Polaris 服务端地址可通过以下方式配置（优先级从高到低）：
> 1. JVM 系统属性 `-Ddubbo.registry.address=polaris://<host>:<port>`
> 2. Agent 配置文件 `conf/plugin/dubbo/dubbo-polaris.properties`（参见下方 [配置文件说明](#四配置文件说明)）
> 3. 硬编码默认值 `polaris://127.0.0.1:8091`
>
> Agent 会将所有非 Polaris 协议的注册中心替换为该地址。

### 启动方式

> 需要先完成 Agent 构建，参考 [polaris-agent-build](../../polaris-agent-build/README.md)。

**启动 Provider**

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-2.7.x-plugin \
     -jar dubbo-example-provider/target/dubbo-agent-example-provider-*.jar
```

也可以省略 `-Ddubbo.registry.address`，通过配置文件指定（参见第四节）。

**启动 Consumer**

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-2.7.x-plugin \
     -Dhttp.listen.port=15700 \
     -jar dubbo-example-consumer/target/dubbo-agent-example-consumer-*.jar
```

Consumer 启动后内嵌 HTTP Server 监听 `15700` 端口（可通过 `-Dhttp.listen.port` 修改）。

### 验证接口

```bash
# 调用 GreetingService.sayHello
curl "http://localhost:15700/echo?method=sayHello&value=world"

# 调用 GreetingService.sayHi（演示标签路由）
curl "http://localhost:15700/echo?method=sayHi&value=world"

# 调用 EchoService.echo
curl "http://localhost:15700/echo?method=echo&value=hello"
```

---

## 二、Spring Cloud + Dubbo 示例

`spring-cloud-dubbo-examples` 演示 Spring Cloud Alibaba 2021 + Dubbo 2.7.x 混合场景，同时支持 Dubbo RPC 调用和 Spring Cloud RestTemplate 调用。

### 配置说明

**sc-dubbo-provider**（`application.properties`）

```properties
spring.application.name=sc-dubbo-provider
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848  # 原有注册中心地址，Agent 替换为 Polaris
dubbo.registry.address=nacos://127.0.0.1:8848             # 原有注册中心地址，Agent 替换为 Polaris
server.port=17080
dubbo.protocol.port=20880
```

**sc-dubbo-consumer**（`application.properties`）

```properties
spring.application.name=sc-dubbo-consumer
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848  # 原有注册中心地址，Agent 替换为 Polaris
dubbo.registry.address=nacos://127.0.0.1:8848             # 原有注册中心地址，Agent 替换为 Polaris
server.port=17081
dubbo.protocol.port=30880
```

> **说明**：Polaris 服务端地址配置方式与纯 Dubbo 示例相同，参见 [配置文件说明](#四配置文件说明)。

### 启动方式

**启动 Provider**

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-2.7.x-plugin,spring-cloud-2021-plugin \
     -jar sc-dubbo-provider/target/sc-dubbo-provider-*.jar
```

**启动 Consumer**

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Ddubbo.registry.address=polaris://127.0.0.1:8091 \
     -Dplugins.enable=dubbo-2.7.x-plugin,spring-cloud-2021-plugin \
     -jar sc-dubbo-consumer/target/sc-dubbo-consumer-*.jar
```

### 验证接口

Consumer 启动后监听 `17081` 端口，提供以下 HTTP 端点：

```bash
# Dubbo RPC 调用 GreetingService.sayHello
curl "http://localhost:17081/dubbo/sayHello?name=polaris"

# Dubbo RPC 调用 GreetingService.sayHi（演示标签路由）
curl "http://localhost:17081/dubbo/sayHi?name=polaris"

# Dubbo RPC 调用 EchoService.echo
curl "http://localhost:17081/dubbo/echo?message=test"

# Spring Cloud RestTemplate 调用 Provider REST 端点
curl "http://localhost:17081/rest/echo/hello"
```

Provider 同时提供 REST 端点（端口 `17080`）：

```bash
curl "http://localhost:17080/echo/hello"
```

---

## 三、Nacos 双注册双发现

在 Polaris 接管注册中心的基础上，同时向 Nacos 注册并从 Nacos 发现服务，实现 Polaris 与 Nacos 并行运行。

> **前置条件**：Polaris 服务端和 Nacos 服务端均已就绪。

### 原理

`polaris-java-agent` 已内置 `dubbo-java-polaris` 依赖，其中包含 `NacosBootConfigHandler`（Dubbo SPI `BootConfigHandler` 扩展）。检测到 `polaris_nacos_enabled=true` 时，Polaris SDK 自动切换为多连接器模式，同时向 Polaris GRPC 和 Nacos 进行注册/发现。无需修改应用代码或重新打包 Agent。

### 配置方式

**推荐方式：配置文件**（参见第四节）

编辑 `conf/plugin/dubbo/dubbo-polaris.properties`，取消注释 Nacos 相关参数：

```properties
dubbo.registry.address=polaris://127.0.0.1:8091
dubbo.registry.parameters.polaris_nacos_enabled=true
dubbo.registry.parameters.polaris_nacos_server_addr=127.0.0.1:8848
dubbo.registry.parameters.polaris_nacos_username=nacos
dubbo.registry.parameters.polaris_nacos_password=nacos
dubbo.registry.parameters.polaris_nacos_dubbo_adapt=true
```

然后正常启动即可，无需在命令行传任何额外参数：

```bash
java -javaagent:/path/to/polaris-java-agent-{version}/polaris-agent-core-bootstrap.jar \
     -Dplugins.enable=dubbo-2.7.x-plugin \
     -jar dubbo-example-provider/target/dubbo-agent-example-provider-*.jar
```

| 参数键 | 说明 |
|---|---|
| `polaris_nacos_enabled` | 必填，`true` 启用双注册双发现 |
| `polaris_nacos_server_addr` | Nacos 服务地址，格式 `HOST:PORT` |
| `polaris_nacos_username` | Nacos 用户名 |
| `polaris_nacos_password` | Nacos 密码 |
| `polaris_nacos_dubbo_adapt` | 必填，`true` 使 Nacos 中服务名采用 Dubbo 兼容格式 |

### 验证

验证方式与第一节、第二节相同，使用对应示例的验证接口即可。

---

## 四、配置文件说明

Dubbo 插件支持从本地配置文件读取配置，免去在启动命令中传递大量 `-D` 参数。

### 文件位置

```
polaris-java-agent-{version}/
└── conf/
    └── plugin/
        └── dubbo/
            └── dubbo-polaris.properties    ← 修改此文件
```

### 配置项

```properties
# 北极星服务端地址
dubbo.registry.address=polaris://127.0.0.1:8091

# 注册中心扩展参数（按需取消注释）
# dubbo.registry.parameters.polaris_nacos_enabled=true
# dubbo.registry.parameters.polaris_nacos_server_addr=127.0.0.1:8848
# dubbo.registry.parameters.polaris_nacos_dubbo_adapt=true
```

### 优先级

配置项生效优先级（从高到低）：

| 优先级 | 来源 | 示例 |
|--------|------|------|
| 1（最高） | JVM 系统属性 | `-Ddubbo.registry.address=polaris://10.0.0.1:8091` |
| 2 | 配置文件 | `conf/plugin/dubbo/dubbo-polaris.properties` |
| 3（最低） | 硬编码默认值 | `polaris://127.0.0.1:8091` |

> **注意**：`dubbo.registry.parameters.*` 仅从配置文件读取，不支持通过 `-D` 系统属性逐一传入。

---

## 相关文档

- [polaris-java-agent 主文档](../../../../README-zh.md)
- [Agent 构建说明](../../../polaris-agent-build/README.md)
- [北极星官网](https://polarismesh.cn)
