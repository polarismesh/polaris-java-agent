# QuickStart Example

## Example Instruction

This example illustrates how to use polaris-java-agent for consumer or provider applications to connect to polaris with spring cloud 2021 or dubbo2 framework, and complete the service invocation.

## Example

### Build Example

Build a jar:

- as provider: Execute command `mvn clean package` in project `quickstart-example-provider` to build a jar.
- as consumer: Execute command `mvn clean package` in project `quickstart-example-consumer` to build a jar.

### Start Example

Execute with polaris-java-agent:

+ as provider: Then execute the jar with `java -jar -javaagent:polaris-java-agent/pinpoint-bootstrap.jar -Dpinpoint.agentId=quickstart-example-provider -Dpinpoint.applicationName=quickstart-example-provider -Dpolaris.server.address=${polaris-address:(ip:port)} ${jar-file}` in the directory where polaris-java-agent is located

+ as consumer: Then execute the jar with `java -jar -javaagent:polaris-java-agent/pinpoint-bootstrap.jar -Dpinpoint.agentId=quickstart-example-consumer -Dpinpoint.applicationName=quickstart-example-consumer -Dpolaris.server.address=${polaris-address:(ip:port)} ${jar-file}` in the directory where polaris-java-agent is located
