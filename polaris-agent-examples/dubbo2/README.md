# Polaris agent examples - Dubbo2

1. run `cn.polarismesh.dubbo2.demo.provider.Application` with jvm properties

    ```
    java -javaagent:xxx\pinpoint-bootstrap.jar -Dpinpoint.agentId=xxx -Dpinpoint.applicationName=XXX -Dpolaris.namespace=Dubbo -Dpolaris.server.address=localhost:8091 -Dpolaris.ttl=6 -jar dubbo2-example-provider-$version.jar
    ```
    
2. run  `cn.polarismesh.dubbo2.demo.consumer.ConsumerApplication` with jvm properties
    
    ```
        java -javaagent:xxx\pinpoint-bootstrap.jar -Dpinpoint.agentId=xxx -Dpinpoint.applicationName=XXX -Dpolaris.namespace=Dubbo -Dpolaris.server.address=localhost:8091 -Dpolaris.ttl=6 -jar dubbo2-example-consumer-$version.jar
    ```