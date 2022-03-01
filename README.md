
# polaris-java-agent

README：

- [介绍](#介绍)
- [使用指南](#使用指南)

## 介绍

polaris-java-agent提供无侵入的方式，供Java应用与polaris进行对接，进行服务治理，提供以下功能：

- 服务注册发现
- 动态路由及负载均衡
- 故障节点熔断
- 服务限流[开发中]

当前支持基于以下框架开发的Java应用进行接入：

- dubbox(version >= 2.8.4)
- dubbo(version >= 2.7.0)[开发中]
- spring-cloud(version >= 2020.0.0)[开发中]

本文档介绍如何使用polaris-java-agent接入polaris服务治理。

## 技术架构



## 使用指南

- [dubbox应用接入](./polaris-agent-examples/README.md)