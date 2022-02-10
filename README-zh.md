# polaris-java-agent

[English](./README.md) | 简体中文

Polaris-java-agent是使用Java agent技术开发的Polaris适配组件，目前可以支持`spring cloud 2021`和`Dubbo2`这些主流的Java框架

## 如何使用

从[releases](https://github.com/polarismesh/polaris-java-agent/releases)中下载最新的`polaris-java-agent.zip`文件

1. 解压

    ```
    unzip polaris-java-agent.zip
    ```

2. 添加JVM启动参数

    ```
    -javaagent:...\pinpoint-bootstrap.jar
    -Dpinpoint.agentId=xxx
    -Dpinpoint.applicationName=xxx
    ```
    
    其中`-javaagent`的值为`polaris-java-agent/pinpoint-bootstrap.jar`所在路径，`-Dpinpoint.agentId`和`-Dpinpoint.applicationName`是pinpoint相关参数，可以为任意值
    
    以上三个参数为必填，还有以下可选参数
    
    |  参数名   |        描述        |       示例        | 默认值  |
    | :-------: | :----------------: | :---------------: | :-----: |
    | namespace | 服务所属的命名空间 | -Dnamespace=Dubbo | default |
    |    ttl    |  服务心跳上报间隔  |      -Dttl=5      |    5    |

3. 启动项目

## 示例
```
java -javaagent:/polaris-java-agent/pinpoint-bootstrap.jar -Dpinpoint.agentId=dubbo-provider -Dpinpoint.applicationName=PROVIDER -Dnamespace=Dubbo -Dttl=5 -jar xxx.jar
```