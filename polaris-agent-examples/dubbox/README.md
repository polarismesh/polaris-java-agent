
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
  
  ![](pic/pinpoint-startup.png)
  
  - DemoProvider：-javaagent:${java-agent安装目录}/polaris-java-agent-v1.0.0/pinpoint-polaris-bootstrap-1.0.0-SNAPSHOT.jar -Dagent.application.name=demo-provider
  - DemoConsumer：-javaagent:${java-agent安装目录}/polaris-java-agent-v1.0.0/pinpoint-polaris-bootstrap-1.0.0-SNAPSHOT.jar -Dagent.application.name=demo-consumer

## 安装验证

- 检查服务是否已经注册到北极星：

  通过浏览器打开```https://${北极星服务端IP}:8080```，打开北极星控制台，可以看到demo注册的服务，假如服务下存在健康实例，则证明服务注册成功。

  ![](pic/polaris-server-services.png)    

- 检查调用跟踪：

  通过浏览器打开```https://${collector安装IP}:10010```，选择demo-consumer.default应用，可以看到调用关系拓扑。
  
  ![](pic/pinpoint-trace.png)    
