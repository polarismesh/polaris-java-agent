# dubbox-example

English | [中文](./README-zh.md)

## Install server

You need to install the Polaris server first, you can refer to [Installation Guide](https://polarismesh.cn/zh/doc/Quick Start/Install Server/Install Standalone.html)

## Install call chain components

Collector and hbase components need to be installed, please refer to [Installation Guide](https://github.com/polarismesh/polaris-java-agent/issues/20)

## Configure the java-agent package

- Package download: Download the latest version of **polaris-pinpoint-agent-${version}.zip** from [release](https://github.com/polarismesh/polaris-java-agent/releases/tag/v1.0.0) , and unzip it.
- Configure the Polaris server address: enter the **polaris-pinpoint-agent-${version}** directory, open the polaris.config file, and modify the agent.polaris.registry configuration item to the Polaris server IP port address, and the port uses 8091.
- Configure the collector address: Enter the **polaris-pinpoint-agent-${version}** directory, open pinpoint-root.config, and modify profiler.transport.grpc.collector.ip to the collector's IP address.

## Start the dubbox application

- Download the dubbox example: [dubbox-2.8.4](https://github.com/dangdangdotcom/dubbox/tree/dubbox-2.8.4)

- Add java-agent startup parameters and start the sample:

  ![pinpoint-startup](./pic/pinpoint-startup.png)

    - DemoProvider: -javaagent:${java-agent installation directory}/polaris-java-agent-v1.0.0/pinpoint-polaris-bootstrap-1.0.0-SNAPSHOT.jar -Dagent.application.name=demo-provider
    - DemoConsumer: -javaagent:${java-agent installation directory}/polaris-java-agent-v1.0.0/pinpoint-polaris-bootstrap-1.0.0-SNAPSHOT.jar -Dagent.application.name=demo-consumer

## Installation verification

- Check if the service is registered to Polaris:

  Open ```https://${Polaris server IP}:8080``` through the browser, open the Polaris console, you can see the demo registered service, if there is a healthy instance under the service, it proves that the service registration is successful.

  ![](pic/polaris-server-services.png)

- Check the call trace:

  Open ```https://${collector installation IP}:10010``` through a browser, select the demo-consumer.default application, and you can see the calling relationship topology.

  ![](pic/pinpoint-trace.png)

## Function usage

### Service routing

Polaris supports service routing capabilities. By setting routing rules, it supports addressing the list of dispatched instances with specific labels according to the matching relationship between the request labels of the main dispatcher. It can support version grayscale, canary testing, A/ Scenario demands such as B testing.

Polaris-Java-Agent supports users to match rules through the following tags:

- Key Dispatcher tab
    - Method name: the target method name of this call, the key is method
    - Default static label: The default static label of dubbo, including application, interface, path, version, protocol.
    - Custom static tags: static tags configured by adding <dubbo:parameter> to the reference
    - Dynamic tags: Attachment data for service calls, which can be passed in through RPCContext.setAttachment.

- Scheduled label
    - Instance metadata information: add the tag data configured by <dubbo:parameter> in the service.

Example of use:

1. Start `dubbo-demo-provider-1` and `dubbo-demo-provider-2`

2. Start `dubbo-demo-consumer-1`

3. Open the Polaris console, open the service named `com.alibaba.dubbo.demo.bid.BidService`, and create a new routing rule at the routing rule

   ![](pic/polaris-server-services-routing.png)

4. Create new routing rules as follows:

   ![](pic/polaris-routing-1.png)

   ![](pic/polaris-routing-2.png)

5. Observe the output of `consumer`: `v1` requests are always routed to `20880` port, `v2` requests are always routed to `20890` port, indicating that the routing rules are in effect

   ![](pic/polaris-routing-result.png)

### Load Balancing

Polaris remote load balancing configuration does not yet support remote configuration. Currently, Polaris and Polaris load balancing can be used by modifying the client configuration.

You can modify the configuration content of polaris.yml:

````
#Description: Host configuration
consumer:
  #Description: Load balancing related configuration
  loadbalancer:
    #Description: Currently supports weightedRandom (weighted random), ringHash (consistent hash)
    type: weightedRandom
````

### Fusing

1. Start `dubbo-demo-provider-1` and `dubbo-demo-provider-2`

2. Start `dubbo-demo-consumer-1`

3. Close one of the `provider`, all requests will import the other `provider`

### Limiting

1. Start `dubbo-demo-provider-1`



2. Open the Polaris console, open the service named `com.alibaba.dubbo.demo.bid.BidService`, and create a new current limiting rule at the current limiting rule

   ![](pic/polaris-server-services-ratelimit.png)

3. Create a new current limiting rule, you can limit the current according to the request label, and set the current limiting rule, which will take effect after the new rule is created.

   ![](pic/polaris-ratelimit.png)

4. You can choose to adjust the request rate in `dubbo-demo-consumer-1` to match or exceed the current limit rule

5. Start `dubbo-demo-consumer-1`, if the request rate exceeds the current limit rule, you can see the corresponding error

   ![](pic/polaris-ratelimit-result.png)

### Service governance monitoring

1. You need to go to the `$polaris-java-agent installation directory/polaris/conf` directory, modify the polaris.yml configuration, and enable monitoring data reporting:
````
global:
  statReporter:
    # Enable monitoring data reporting
    enable: true
    plugin:
      prometheus:
        # pushgateway address
        pushgatewayAddress: 127.0.0.1:9091
````

2. Log in to the Polaris console, and you can see the monitoring chart data in the left column of Observability.