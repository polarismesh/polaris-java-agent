# dubbo2-example

[English](./README.md) | 中文

## 安装服务端

需要先安装北极星服务端，可参考[安装指南](https://polarismesh.cn/zh/doc/快速入门/安装服务端/安装单机版.html)

## 安装调用链组件[可选]

需要安装collector以及hbase组件，可参考[安装指南](https://github.com/polarismesh/polaris-java-agent/issues/20)

## 配置java-agent软件包

- 软件包下载：从 [release](https://github.com/polarismesh/polaris-java-agent/releases/tag/${version}) 下载最新版本的 **polaris-pinpoint-agent-${version}.zip** ，并解压。
- 配置北极星服务端地址：进入 **polaris-pinpoint-agent-${version}** 目录，打开polaris.config文件，修改 **agent.polaris.registry** 配置项为北极星服务端IP端口地址，端口使用8091。
- 配置collector地址：进入 **polaris-pinpoint-agent-${version}** 目录，打开pinpoint-root.config，修改 **profiler.transport.grpc.collector.ip** 为collector的IP地址。

## 启动dubbo2应用

- 运行代码样例子：[dubbo2-example](./)

- 增加java-agent启动VM参数，并启动样例：

- DubboProvider
```shell
java -javaagent:${java-agent安装目录}/polaris-java-agent-${version}/pinpoint-polaris-bootstrap-${version}.jar -Dagent.application.name=DubboProvider -jar xxx.jar
```

- DubboConsumer

```shell
java -javaagent:${java-agent安装目录}/polaris-java-agent-${version}/pinpoint-polaris-bootstrap-${version}.jar -Dagent.application.name=DubboConsumer -jar xxx.jar
```

## 安装验证

- 检查服务是否已经注册到北极星：通过浏览器打开```https://${北极星服务端IP}:8080```，打开北极星控制台，可以看到demo注册的服务，假如服务下存在健康实例，则证明服务注册成功。

![](pic/polaris-server-services.png)

## 功能使用

### 服务路由

北极星支持服务路由能力，通过设置路由规则，支持根据主调方的请求标签的匹配关系，寻址到带有特定标签的被调方实例列表，可支持一下场景诉求

- 版本灰度
- 金丝雀测试
- A/B测试

**Polaris-Java-Agent** 支持用户通过以下标签来进行规则匹配：

- 主调方标签
   - 方法名：本次调用的目标方法名，key为method
   - 默认静态标签：dubbo 的默认静态标签，包括 application, interface, path, version, protocol。
   - 自定义静态标签：通过在 reference 中添加 <dubbo:parameter> 方式配置的静态标签
   - 动态标签：服务调用的附件数据，可通过 RPCContext.setAttachment 的方式传入。

- 被调方标签
   - 实例元数据信息：通过 service 中添加 <dubbo:parameter> 配置的标签数据。

使用样例：

- 分别启动`DubboProvider-1`、`DubboProvider-2`、`DubboConsumer-1`、`DubboConsumer-2`
  - DubboProvider-1：配置**spring/dubbo-provider.xml**文件
  ```xml
  <dubbo:service interface="cn.polarismesh.dubbo2.api.DemoService" ref="demoServiceImpl" version="v1.0.0" />
  ```
  - DubboProvider-2：配置**spring/dubbo-provider.xml**文件
  ```xml
  <dubbo:service interface="cn.polarismesh.dubbo2.api.DemoService" ref="demoServiceImpl" version="v2.0.0" />
  ```
  - DubboConsumer-1：配置**spring/dubbo-consumer.xml**文件
  ```xml
  <dubbo:reference id="demoService" check="false" interface="cn.polarismesh.dubbo2.api.DemoService" version="v1.0.0"/>
  ```
  - DubboConsumer-2：配置**spring/dubbo-consumer.xml**文件
  ```xml
  <dubbo:reference id="demoService" check="false" interface="cn.polarismesh.dubbo2.api.DemoService" version="v2.0.0"/>
  ```
- 打开北极星控制台，打开服务名为`cn.polarismesh.dubbo2.api.DemoService`的服务，在路由规则处新建路由规则:
![](pic/polaris-server-services-routing.png)
- 分别新建路由规则如下：
![](pic/polaris-routing.png)
- 发起对**DubboConsumer**的http请求调用
```shell
curl http://127.0.0.1:${CONSUMER的监听端口}/echo
```
- 观察`DubboConsumer`端输出：版本为`v1.0.0`的`DubboConsumer`请求永远路由至`20880`端口，版本为`v2.0.0`的`DubboConsumer`请求永远路由至`20890`端口，表示路由规则生效


### 负载均衡

北极星远程负载均衡配置还不支持远程配置，当前可以通过修改客户端配置的方式使用北极星北极星的负载均衡。

可以修改polaris.yml的配置内容：

````
#描述: 主调端配置
consumer:
  #描述:负载均衡相关配置
  loadbalancer:
    #描述: 当前支持weightedRandom（权重随机），ringHash（一致性hash）
    type: weightedRandom  
````

### 熔断

- 分别启动`DubboProvider-1`、`DubboProvider-2`、`DubboConsumer`
- 关闭其中一个`provider`，所有请求将会导入另一个`provider`

### 限流

- 启动`DubboProvider`以及`DubboConsumer`
- 打开北极星控制台，打开服务名为`cn.polarismesh.dubbo2.api.DemoService`的服务，在限流规则处新建限流规则
![](pic/polaris-server-services-ratelimit.png)
- 新建限流规则，可以根据请求标签进行限流，并设定限流规则，新建规则后即可生效
![](pic/polaris-ratelimit.png)
- 启动`DubboConsumer`，若请求速率超出限流规则，可以看到相应报错
![](pic/polaris-ratelimit-result.png)

### 服务治理监控

- 需要到`${polaris-java-agent安装目录}/polaris/conf`目录中，修改polaris.yml配置，开启监控数据上报：
```
global:
  statReporter:
    # 开启监控数据上报
    enable: true
    plugin:
      prometheus:
        # pushgateway地址
        pushgatewayAddress: 127.0.0.1:9091
```
- 登录到北极星控制台，在左边栏可观测性可以看到监控图表数据。
