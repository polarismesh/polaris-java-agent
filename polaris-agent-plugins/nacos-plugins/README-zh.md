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

### Nacos 多活容灾

#### 集群1的接入方式

启动Java应用

```
java -jar x -Dsource.nacos.cluster.name=cluster-1 -Dtarget.nacos.server.addr=xx.xx.xx.xx -Drouter.nearby.level=nacos-cluster
```

参数说明：

- `source.nacos.cluster.name`：默认是自己所在的集群，应用默认配置是默认集群的访问地址
- `target.nacos.server.addr`：集群2的访问地址

#### 集群2的接入方式

启动Java应用

```
java -jar x -Dsource.nacos.cluster.name=cluster-2 -Dtarget.nacos.server.addr=xx.xx.xx.xx -Drouter.nearby.level=nacos-cluster
```

参数说明：

- `source.nacos.cluster.name`：默认是自己所在的集群，应用默认配置是默认集群的访问地址
- `target.nacos.server.addr`：集群1的访问地址

## 参数列表

polaris-java-agent提供以下配置项，所有的配置项通过系统变量（-D参数）的方式进行配置。

| 配置项                            | 含义                     | 是否必填 | 默认值  |
| --------------------------------- | ---------------------- | -------- | ------- |
| source.nacos.cluster.name         | 标注clusrer label       | 是       | 无       |
| target.nacos.server.addr          | 目标nacos访问地址        | 是       | 无 |
| router.nearby.level               | 就近路由级别（none, nacos-cluster） | 否       | none |

## 版本支持

当前支持的nacos client版本：

- [x] 1.3.0
- [x] 1.3.1
- [x] 1.4.1
- [x] 2.1.0
