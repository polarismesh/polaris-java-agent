# Spring Cloud Alibaba 2025.1 Demo

本示例使用 SCA `2025.1.0.0`、Spring Cloud `2025.1.1`、Spring Boot `4.0.2` 和 Nacos Client `3.1.1`，要求 JDK 17 或以上。业务应用只依赖 SCA，通过 `-javaagent` 接入 SCT 2025.1 插件。

SCA 的 2025.0 系列对应 Spring Boot 3.5，2025.1 系列对应 Spring Boot 4.0；本示例使用与当前 Agent 插件匹配的 2025.1 系列。

## 构建

在仓库根目录执行，并设置项目构建需要的 `JAVA_8_HOME`、`JAVA_11_HOME`、`JAVA_17_HOME`：

```bash
mvn -pl polaris-agent-examples/spring-cloud-plugins-examples/spring-cloud-2025.1-examples/provider,polaris-agent-examples/spring-cloud-plugins-examples/spring-cloud-2025.1-examples/consumer -am package
```

生成的可执行 JAR 位于 `provider/target/` 和 `consumer/target/`，版本继承项目 `revision`。

## 本地运行

`local` profile 关闭 Nacos 和 Polaris，使用 Spring Cloud 的静态服务发现，能够直接验证两个调用方式。以下命令在本示例目录执行，在两个终端分别启动：

```bash
java -jar provider/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-2.2.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

```bash
java -jar consumer/target/polaris-javaagent-demo-sc-quickstart-2025.1-consumer-2.2.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

```bash
curl http://127.0.0.1:48083/echo/hello
curl http://127.0.0.1:48083/config
curl http://127.0.0.1:48082/rest/echo/hello
curl http://127.0.0.1:48082/feign/echo/hello
curl http://127.0.0.1:48082/instances
curl http://127.0.0.1:48082/actuator/health
```

默认 provider 端口为 48083、consumer 端口为 48082。用 `PROVIDER_PORT` 和 `CONSUMER_PORT` 环境变量可修改端口；consumer 的本地服务发现也使用 `PROVIDER_PORT`。

## 接入 Nacos

不指定 `local` profile 即启用 Nacos 注册发现和配置中心。准备可访问的 Nacos 服务，再设置：

```bash
export NACOS_SERVER_ADDR=127.0.0.1:8848
# 开启鉴权时，设置对应用户名和密码。
export NACOS_USERNAME=your-username
export NACOS_PASSWORD=your-password
```

可选环境变量 `NACOS_NAMESPACE` 和 `NACOS_GROUP` 分别配置命名空间 ID 和服务分组，默认使用 public 命名空间和 `DEFAULT_GROUP`。

```bash
java -jar provider/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-2.2.1.0-SNAPSHOT.jar
java -jar consumer/target/polaris-javaagent-demo-sc-quickstart-2025.1-consumer-2.2.1.0-SNAPSHOT.jar
```

同样在两个终端分别启动。注册服务名为 `service-provider-2025-1` 和 `service-consumer-2025-1`，consumer 通过服务名调用 provider。

在 Nacos 中创建 `service-provider-2025-1.properties`，分组与 `NACOS_GROUP` 一致，配置：

```properties
demo.message=hello-from-nacos
```

访问 provider 的 `/config` 可查看当前配置值，`@RefreshScope` 用于配置变化后的刷新。consumer 的配置文件为 `service-consumer-2025-1.properties`。

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

在 `conf/plugin/spring-cloud/application.properties` 中配置 Polaris 服务端地址和需要启用的能力，再分别启动：

```bash
export AGENT_DIR=/absolute/path/to/polaris-java-agent-2.2.1.0-SNAPSHOT
java -javaagent:"${AGENT_DIR}/polaris-agent-core-bootstrap.jar" -jar provider/target/polaris-javaagent-demo-sc-quickstart-2025.1-provider-2.2.1.0-SNAPSHOT.jar
java -javaagent:"${AGENT_DIR}/polaris-agent-core-bootstrap.jar" -jar consumer/target/polaris-javaagent-demo-sc-quickstart-2025.1-consumer-2.2.1.0-SNAPSHOT.jar
```

只验证 Agent 装载时，可在 Agent 的 Spring Cloud 配置中设置 `spring.cloud.polaris.enabled=false`，并为业务应用加上 `--spring.profiles.active=local`。Nacos / Polaris 注册发现联调需要实际服务端；本地模式验证的是 HTTP 调用、静态服务发现及 Agent 装载。

provider 与 consumer 都提供 Dockerfile。构建上下文为对应模块目录，例如：

```bash
docker build --build-arg file_name=polaris-javaagent-demo-sc-quickstart-2025.1-provider-2.2.1.0-SNAPSHOT.jar -t sca2025-provider provider
```

## 版本来源

- [SCA 官方版本说明](https://github.com/alibaba/spring-cloud-alibaba/blob/2025.1.0.0/README-zh.md)
- [SCA 官方 POM](https://github.com/alibaba/spring-cloud-alibaba/blob/2025.1.0.0/pom.xml)
- [Maven Central 已发布版本](https://repo.maven.apache.org/maven2/com/alibaba/cloud/spring-cloud-alibaba-dependencies/maven-metadata.xml)
