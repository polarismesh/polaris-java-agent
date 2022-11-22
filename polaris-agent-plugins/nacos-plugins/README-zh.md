# Nacos Java Agent

- [简介](#简介)
- [使用指南](#使用指南)
  - [Nacos 迁移](#nacos-迁移)
  - [Nacos 多活容灾](#nacos-多活容灾)
- [参数列表](#参数列表)
- [版本支持](#版本支持) 

## 简介

polaris-java-agent通过Java字节码增强技术，将拦截器注入到应用中，实现应用的双注册发现以及就近路由。

nacos-plugins根据不同的nacos client版本，提供无侵入的方式，供Java应用来对接，支持以下应用场景：

- Nacos 迁移
- Nacos 多活容灾

## 使用指南

### Nacos 迁移

![](pic/nacos-double-registry.png)

应用场景说明

#### 接入方式

### Nacos 多活容灾

应用场景说明

#### 接入方式

启动应用，接入 Nacos 集群1

```shell
java -jar x
  -Dnacos.cluster.name=cluster-1
  -Dother.nacos.server.addr=xx.xx.xx.xx
  -Drouter.nearby.level=nacos-cluster
```

启动应用，接入 Nacos 集群2

```shell
java -jar x
  -Dnacos.cluster.name=cluster-2
  -Dother.nacos.server.addr=xx.xx.xx.xx
  -Drouter.nearby.level=nacos-cluster
```

## 参数配置

polaris-java-agent提供以下配置项，所有的配置项通过系统变量（-D参数）的方式进行配置。

| 配置项                     | 描述 | 必填 | 可选值 | 默认值 |
| ------------------------- | --- | --- | --- | --- |
| nacos.cluster.name        | 主 Nacos 集群名称 | 是 | | 无 |
| other.nacos.server.addr   | 另一个 Nacos 集群的访问地址 | 是 | | 无 |
| router.nearby.level       | 就近路由级别 | 否 | none, nacos-cluster | none |

## 版本支持

当前支持的nacos client版本：

- [x] 1.3.0
- [x] 1.3.1
- [x] 1.4.1
- [x] 2.1.0
