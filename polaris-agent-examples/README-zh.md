# 快速开始样例

## 样例说明

本样例演示如何使用 polaris-java-agent 在spring cloud 2021 或 dubbo2框架中完成被调端以及主调端应用接入polaris，并完成服务调用流程。

## 样例

### 构建样例

打包编译为jar包：

- 启动被调方：在 `quickstart-example-provider` 项目下，执行 `mvn clean package` 将工程编译打包。
- 启动主调方：在 `quickstart-example-consumer` 项目下，执行 `mvn clean package` 将工程编译打包。

### 执行样例

使用polaris-java-agent以agent方式启动jar包：

+ 启动被调方：在polaris-java-agent所在目录执行`java -jar -javaagent:polaris-java-agent/pinpoint-bootstrap.jar -Dpinpoint.agentId=quickstart-example-provider -Dpinpoint.applicationName=quickstart-example-provider -Dpolaris.server.address=${polaris-address:(ip:port)} ${jar-file}`

+ 启动主调方：在polaris-java-agent所在目录执行`java -jar -javaagent:polaris-java-agent/pinpoint-bootstrap.jar -Dpinpoint.agentId=quickstart-example-consumer -Dpinpoint.applicationName=quickstart-example-consumer -Dpolaris.server.address=${polaris-address:(ip:port)} ${jar-file}`
