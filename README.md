#polaris-java-agent

English | [中文](./README-zh.md)

- [introduction](#introduction)
- [Usage Guide](#Usage Guide)

## Introduce

polaris-java-agent provides a non-invasive way for Java applications to connect with polaris for service management, and provides the following functions:

- Service registration discovery
- Dynamic routing and load balancing
- The faulty node is blown
- Service throttling [under development]

Currently, Java applications developed based on the following frameworks are supported for access:

- dubbox(version >= 2.8.4)
- dubbo2(version >= 2.7.0)
- spring-cloud (version >= 2020.0.0) [under development]

This document describes how to use polaris-java-agent to access polaris service governance.

## Technology Architecture

polaris-java-agent injects interceptors into applications through Java bytecode enhancement technology to realize the docking of Polaris service governance capabilities.

![](pic/arch.png)

## Configuration item description

polaris-java-agent provides the following configuration items, all of which can be configured through files (polaris.config) and system variables (-D parameters).

| Configuration item | Meaning | Required | Default value |
| --------------------------------- | ------------------------ | -------- | ------- |
| agent.application.namespace | namespace for the service | no | default |
| agent.application.name | application name, used for monitoring display | yes | no |
| agent.polaris.registry | Polaris server IP address | Yes | No |
| agent.application.healthcheck.ttl | Service check check TTL, in seconds | no | 5 |
| agent.consumer.refresh.interval | Service list refresh interval, in seconds | No | 2 |

## User Guidance

- [dubbo2](./polaris-agent-examples/dubbo2/README.md)
- [dubbox](./polaris-agent-examples/dubbox/README.md)