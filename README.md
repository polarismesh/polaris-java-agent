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
   
   | parameter                 | description               | example                                              | default | Required |
   |:-------------------------:|:-------------------------:|:----------------------------------------------------:|:-------:|:--------:|
   | javaagent                 | polaris-bootstrap.jar所在路径 | -javaagent:/polaris-java-agent/polaris-bootstrap.jar | null    | Required |
   | Dpinpoint.agentId         | pinpoint自带参数              | -Dpinpoint.agentId=dubbo-provider                    | null    | Required |
   | Dpinpoint.applicationName | pinpoint自带参数              | -Dpinpoint.applicationName=PROVIDER                  | null    | Required |
   | Dpolaris.server.address   | polaris地址                 | -Dpolaris.server.address=localhost:8091              | null    | Required |
   | Dpolaris.namespace        | 服务所属的命名空间                 | -Dpolaris.namespace=Dubbo                            | default | Optional |
   | Dpolaris.ttl              | 服务心跳上报间隔                  | -Dpolaris.ttl=5                                      | 5       | Optional |

3. start the project

## Examples

A polaris-agent-examples module is included in our project for you to get started with polaris-java-agent quickly. It contains multiple examples, and you can refer to the readme file in the example project for a quick walkthrough.

[QuickStart Example](./polaris-agent-examples/README.md)