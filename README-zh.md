# polaris-java-agent

[English](./README.md) | 简体中文

Polaris-java-agent是使用Java agent技术开发的Polaris适配组件，目前可以支持`spring cloud 2021`和`Dubbo2`这些主流的Java框架

## 如何使用

从[releases](https://github.com/polarismesh/polaris-java-agent/releases)中下载最新的`polaris-java-agent-$version.zip`文件

1. 解压
   
   ```
   unzip polaris-java-agent.zip
   ```

2. 添加JVM启动参数
   
   | 参数名                       | 描述                        | 示例                                                   | 默认值     | 是否必填 |
   |:-------------------------:|:-------------------------:|:----------------------------------------------------:|:-------:|:----:|
   | javaagent                 | polaris-bootstrap.jar所在路径 | -javaagent:/polaris-java-agent/polaris-bootstrap.jar | 无       | 必填   |
   | Dpinpoint.agentId         | pinpoint自带参数              | -Dpinpoint.agentId=dubbo-provider                    | 无       | 必填   |
   | Dpinpoint.applicationName | pinpoint自带参数              | -Dpinpoint.applicationName=PROVIDER                  | 无       | 必填   |
   | Dpolaris.server.address   | polaris地址                 | -Dpolaris.server.address=localhost:8091              | 无       | 必填   |
   | Dpolaris.namespace        | 服务所属的命名空间                 | -Dpolaris.namespace=Dubbo                            | default | 可选   |
   | Dpolaris.ttl              | 服务心跳上报间隔                  | -Dpolaris.ttl=5                                      | 5       | 可选   |

3. 启动项目

## 功能样例

为了演示功能如何使用，polaris-java-agent 项目包含了一个子模块polaris-agent-examples。此模块中提供了演示用的 example ，您可以阅读对应的 example 工程下的 README-zh 文档，根据里面的步骤来体验。

[快速开始样例](./polaris-agent-examples/README-zh.md)