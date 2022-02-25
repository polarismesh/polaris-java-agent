# polaris-java-agent

English | [简体中文](./README-zh.md)

polaris-java-agent is the polaris adapter developed using Java agent technology, up to now it can support popular Java frameworks such as `spring cloud 2021` and `Dubbo2`

## How to use

Download the latest `polaris-java-agent-$version.zip` file from [releases](https://github.com/polarismesh/polaris-java-agent/releases)

1. unzip file

    ```
    unzip polaris-java-agent.zip
    ```

2. add JVM parameters
    
    |         parameter         |          description          |                       example                        | default | Required |
    | :-----------------------: | :---------------------------: | :--------------------------------------------------: | :-----: | :------: |
    |         javaagent         | path of polaris-bootstrap.jar | -javaagent:/polaris-java-agent/polaris-bootstrap.jar |  null   | Required |
    |     Dpinpoint.agentId     |      pinpoint parameter       |          -Dpinpoint.agentId=dubbo-provider           |  null   | Required |
    | Dpinpoint.applicationName |      pinpoint parameter       |         -Dpinpoint.applicationName=PROVIDER          |  null   | Required |
    |  Dpolaris.server.address  |        polaris address        |       -Dpolaris.server.address=localhost:8091        |  null   | Required |
    |    Dpolaris.namespace     |     namespace of service      |              -Dpolaris.namespace=Dubbo               | default | Optional |
    |       Dpolaris.ttl        |        ttl of hearbeat        |                   -Dpolaris.ttl=5                    |    5    | Optional |

3. start the project

## Examples

A polaris-agent-examples module is included in our project for you to get started with polaris-java-agent quickly. It contains multiple examples, and you can refer to the readme file in the example project for a quick walkthrough.

[QuickStart Example](./polaris-agent-examples/README.md)