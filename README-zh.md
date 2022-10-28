# polaris-java-agent

[English](./README.md) | 中文

- [介绍](#介绍)
- [技术架构](#技术架构)
- [配置项说明](#配置项说明)
- [使用指南](#使用指南)

## 介绍

polaris-java-agent提供无侵入的方式，供Java应用与polaris进行对接，进行服务治理，提供以下功能：

- [x] 服务注册发现
- [x] 动态路由及负载均衡
- [x] 故障节点熔断
- [x] 服务限流

当前支持基于以下框架开发的Java应用进行接入：

- [x] dubbox(version >= 2.8.4)
- [x] dubbo(version >= 2.7.0)
- [ ] spring-cloud(version >= 2020.0.0)

本文档介绍如何使用polaris-java-agent接入polaris服务治理。

## 技术架构

polaris-java-agent通过Java字节码增强技术，将拦截器注入到应用中，实现北极星服务治理能力的对接。

![](pic/arch.png)

## 配置项说明

polaris-java-agent提供以下配置项，所有的配置项都可以通过文件（polaris.config）以及系统变量（-D参数）的方式进行配置。

| 配置项                            | 含义                     | 是否必填 | 默认值  |
| --------------------------------- | ------------------------ | -------- | ------- |
| agent.application.namespace       | 服务的命名空间           | 否       | default |
| agent.application.name            | 应用名，用于监控展示     | 是       | 无      |
| agent.polaris.registry            | 北极星服务端IP地址       | 是       | 无      |
| agent.application.healthcheck.ttl | 服务检查检查TTL，单位秒  | 否       | 5       |
| agent.consumer.refresh.interval   | 服务列表刷新周期，单位秒 | 否       | 2       |


## 使用指南

- [dubbo2 接入](./polaris-agent-examples/dubbo2/README.md)
- [dubbox 接入](./polaris-agent-examples/dubbox/README.md)
