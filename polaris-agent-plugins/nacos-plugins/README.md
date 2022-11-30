# Nacos Java Agent

- [简介](#简介)
- [使用指南](#使用指南)
  - [Nacos 迁移](#nacos-迁移)
  - [Nacos 多活容灾](#nacos-多活容灾)
- [参数列表](#参数列表)
- [版本支持](#版本支持) 

## 简介

polaris-java-agent通过Java字节码增强技术，将拦截器注入到应用中，实现应用的双注册发现以及就近路由。

nacos-plugins根据不同的nacos client版本，通过配置系统变量-Dplugins.enable=nacos-xxx-plugin(详见最后的版本支持介绍)来自动选择对应版本的插件进行加载，提供无侵入的方式，供Java应用来对接，支持以下应用场景：

- Nacos 迁移
- Nacos 多活容灾

## 使用指南

### Nacos 迁移

![](pic/nacos-double-registry.png)

#### 应用场景说明
支持将应用从当前nacos集群平滑迁移到另一个nacos集群，如：将应用从自建nacos集群迁移到腾讯云的TSE nacos集群，同时TSE nacos还提供了数据迁移的能力，可以将nacos依赖的db里面的数据实时同步到TSE nacos的db里面。
#### 接入方式

```shell
java
  -javaagent:/***/polaris-java-agent-v*/polaris-agent-core-bootstrap.jar
  -Dplugins.enable=nacos-xxx-plugin  
  -Dnacos.cluster.name=cluster-1
  -Dother.nacos.server.addr=xx.xx.xx.xx
-jar xx.jar
```

启动应用，接入 Nacos 集群2

```shell
java
  -javaagent:/***/polaris-java-agent-v*/polaris-agent-core-bootstrap.jar
  -Dplugins.enable=nacos-xxx-plugin
  -Dnacos.cluster.name=cluster-2
  -Dother.nacos.server.addr=xx.xx.xx.xx
-jar xx.jar
```

### Nacos 多活容灾

####应用场景说明

支持不同云、IDC机房之间的应用访问，提供同一云内或者同一IDC机房内优先路由的能力，如：用户在自建IDC机房和腾讯云分别部署了一整套服务应用和nacos集群，当腾讯云内的应用A调用应用B服务时，优先访问腾讯云内的应用B，如果找不到，则从自建IDC机房访问应用B。
#### 接入方式

启动应用，接入 Nacos 集群1

```shell
java
  -javaagent:/***/polaris-java-agent-v*/polaris-agent-core-bootstrap.jar
  -Dplugins.enable=nacos-xxx-plugin
  -Dnacos.cluster.name=cluster-1
  -Dother.nacos.server.addr=xx.xx.xx.xx
  -Drouter.nearby.level=nacos-cluster
-jar xx.jar
```

启动应用，接入 Nacos 集群2

```shell
java
  -javaagent:/***/polaris-java-agent-v*/polaris-agent-core-bootstrap.jar
  -Dplugins.enable=nacos-xxx-plugin
  -Dnacos.cluster.name=cluster-2
  -Dother.nacos.server.addr=xx.xx.xx.xx
  -Drouter.nearby.level=nacos-cluster
-jar xx.jar
```

## 参数配置

polaris-java-agent提供以下配置项，所有的配置项通过系统变量（-D参数）的方式进行配置。

| 配置项                     | 描述 | 必填 | 可选值 | 默认值 |
| ------------------------- | --- | --- | --- | --- |
| plugins.enable       | 选择需要加载的插件 | 是 |nacos-130-plugin, nacos-131-plugin, nacos-141-plugin, nacos-210-plugin | 无 |
| nacos.cluster.name        | 主 Nacos 集群名称 | 是 | | 无 |
| other.nacos.server.addr   | 另一个 Nacos 集群的访问地址 | 是 | | 无 |
| router.nearby.level       | 就近路由级别 | 否 | null, nacos_cluster | null |

## 版本支持
[Github 地址](https://github.com/polarismesh/polaris-java-agent/releases)
下载 Polaris Java Agent,
当前支持的nacos client版本:

- [x] 1.3.0 (对应插件: nacos-130-plugin)
- [x] 1.3.1 (对应插件: nacos-131-plugin)
- [x] 1.4.1 (对应插件: nacos-141-plugin)
- [x] 2.1.0 (对应插件: nacos-210-plugin)
