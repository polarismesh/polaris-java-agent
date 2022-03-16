
# dubbox-example

## 安装服务端

需要先安装北极星服务端，可参考[安装指南](https://polarismesh.cn/zh/doc/快速入门/安装服务端/安装单机版.html)

## 安装调用链组件

需要安装collector以及hbase组件，可参考[安装指南](https://github.com/polarismesh/polaris-java-agent/issues/20)

## 配置java-agent软件包

- 软件包下载：从[release](https://github.com/polarismesh/polaris-java-agent/releases/tag/v1.0.0)下载最新版本的polaris-pinpoint-agent-${version}.zip，并解压。
- 配置北极星服务端地址：进入polaris-pinpoint-agent-${version}目录，打开polaris.config文件，修改agent.polaris.registry配置项为北极星服务端IP端口地址，端口使用8091。
- 配置collector地址：进入polaris-pinpoint-agent-${version}目录，打开pinpoint-root.config，修改profiler.transport.grpc.collector.ip为collector的IP地址。

## 启动dubbox应用

- 下载dubbox的样例：[dubbox-2.8.4](https://github.com/dangdangdotcom/dubbox/tree/dubbox-2.8.4)

- 增加java-agent启动参数，并启动样例：
  
  ![pinpoint-startup](./pic/pinpoint-startup.png)
  
  - DemoProvider：-javaagent:${java-agent安装目录}/polaris-java-agent-v1.0.0/pinpoint-polaris-bootstrap-1.0.0-SNAPSHOT.jar -Dagent.application.name=demo-provider
  - DemoConsumer：-javaagent:${java-agent安装目录}/polaris-java-agent-v1.0.0/pinpoint-polaris-bootstrap-1.0.0-SNAPSHOT.jar -Dagent.application.name=demo-consumer

## 安装验证

- 检查服务是否已经注册到北极星：

  通过浏览器打开```https://${北极星服务端IP}:8080```，打开北极星控制台，可以看到demo注册的服务，假如服务下存在健康实例，则证明服务注册成功。

  ![](pic/polaris-server-services.png)    

- 检查调用跟踪：

  通过浏览器打开```https://${collector安装IP}:10010```，选择demo-consumer.default应用，可以看到调用关系拓扑。
  
  ![](pic/pinpoint-trace.png)    
  
## 功能使用

### 服务路由

北极星支持服务路由能力，通过设置路由规则，支持根据主调方的请求标签的匹配关系，寻址到带有特定标签的被调方实例列表，可支持版本灰度，金丝雀测试，A/B测试等场景诉求。

Polaris-Java-Agent支持用户通过以下标签来进行规则匹配：

- 主调方标签
  - 方法名：本次调用的目标方法名，key为method
  - 默认静态标签：dubbo的默认静态标签，包括application, interface, path, version, protocol。
  - 自定义静态标签：通过在reference中添加<dubbo:parameter>方式配置的静态标签
  - 动态标签：服务调用的附件数据，可通过RPCContext.setAttachment的方式传入。

- 被调方标签
  - 实例元数据信息：通过service中添加<dubbo:parameter>配置的标签数据。

使用样例：

1. 启动`dubbo-demo-provider-1`与`dubbo-demo-provider-2`

2. 启动`dubbo-demo-consumer-1`

3. 打开北极星控制台，打开服务名为`com.alibaba.dubbo.demo.bid.BidService`的服务，在路由规则处新建路由规则

    ![](pic/polaris-server-services-routing.png)  
    
4. 分别新建路由规则如下：

    ![](pic/polaris-routing-1.png)   

    ![](pic/polaris-routing-2.png)  

5. 观察`consumer`端输出：`v1`请求永远路由至`20880`端口，`v2`请求永远路由至`20890`端口，表示路由规则生效

    ![](pic/polaris-routing-result.png)  
    
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

1. 启动`dubbo-demo-provider-1`与`dubbo-demo-provider-2`

2. 启动`dubbo-demo-consumer-1`

3. 关闭其中一个`provider`，所有请求将会导入另一个`provider`

### 限流

1. 启动`dubbo-demo-provider-1`



2. 打开北极星控制台，打开服务名为`com.alibaba.dubbo.demo.bid.BidService`的服务，在限流规则处新建限流规则

    ![](pic/polaris-server-services-ratelimit.png)  
    
3. 新建限流规则，可以根据请求标签进行限流，并设定限流规则，新建规则后即可生效

    ![](pic/polaris-ratelimit.png) 
    
4. 可以选择调整`dubbo-demo-consumer-1`中的请求速率，使之匹配或超出限流规则

5. 启动`dubbo-demo-consumer-1`，若请求速率超出限流规则，可以看到相应报错

    ![](pic/polaris-ratelimit-result.png) 
    
### 服务治理监控

1. 需要到`$polaris-java-agent安装目录/polaris/conf`目录中，修改polaris.yml配置，开启监控数据上报：
````
global:
  statReporter:
    # 开启监控数据上报
    enable: true
    plugin:
      prometheus:
        # pushgateway地址
        pushgatewayAddress: 127.0.0.1:9091
```

2. 登录到北极星控制台，在左边栏可观测性可以看到监控图表数据。
