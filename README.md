# polaris-java-agent

English | [简体中文](./README-zh.md)

polaris-java-agent is the polaris adapter developed using Java agent technology, up to now it can support popular Java frameworks such as `spring cloud 2021` and `Dubbo2`

## How to use

Download the latest `polaris-java-agent.zip` file from [releases](https://github.com/polarismesh/polaris-java-agent/releases)

1. unzip file

    ```
    unzip polaris-java-agent.zip
    ```

2. add JVM parameters

    ```
    -javaagent:...\pinpoint-bootstrap.jar
    -Dpinpoint.agentId=xxx
    -Dpinpoint.applicationName=xxx
    ```
    
    `-javaagent` represents the path of `polaris-java-agent/pinpoint-bootstrap.jar`, `-Dpinpoint.agentId` and `-Dpinpoint.applicationName` are parameters of pinpoint，both of them can be any value
    
    The above three parameters are required, and there are some optional parameters
    
    | parameter |       description        |      example      | default |
    | :-------: | :----------------------: | :---------------: | :-----: |
    | namespace | the namespace of service | -Dnamespace=Dubbo | default |
    |    ttl    |   the ttl of heartbeat   |      -Dttl=5      |    5    |

3. start the project

## Example
```
java -javaagent:/polaris-java-agent/pinpoint-bootstrap.jar -Dpinpoint.agentId=dubbo-provider -Dpinpoint.applicationName=PROVIDER -Dnamespace=Dubbo -Dttl=5 -jar xxx.jar
```