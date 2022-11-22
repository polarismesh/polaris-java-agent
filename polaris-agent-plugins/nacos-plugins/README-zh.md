# nacos-plugins

 中文

- [介绍](#介绍)
- [技术架构](#技术架构)
- [配置项说明](#配置项说明)
- [使用指南](#使用指南)

## 介绍

nacos-plugins根据不同的nacos client版本，提供无侵入的方式，供Java应用来对接，提供以下功能：

- [x] 双注册发现
- [x] 就近路由

当前支持的nacos client版本：

- [x] 1.3.0
- [x] 1.3.1
- [x] 1.4.1
- [x] 2.1.0


## 技术架构

polaris-java-agent通过Java字节码增强技术，将拦截器注入到应用中，实现应用的双注册发现。

![](pic/nacos-double-registry.png)

## 配置项说明

polaris-java-agent提供以下配置项，所有的配置项通过系统变量（-D参数）的方式进行配置。

| 配置项                            | 含义                     | 是否必填 | 默认值  |
| --------------------------------- | ------------------------ | -------- | ------- |
| target.nacos.server.addr          | 目标nacos访问地址           | 是       | 无 |
| nearby.based.router.enable        | 是否开启就近路由         | 否       | false      |
| router.match.levels               | 就近路由级别，可多选（cloud、zone、region） | 否       | 无      |
| router.match.level.cloud.label    | 如果选择cloud方式就近路由，则需要标注cloud label  | 否       | 无       |


## 使用指南

- 启动Java应用时，增加环境变量 -Dtarget.nacos.server.addr=xx.xx.xx.xx 表示目标nacos的访问地址。
- 如果需要开启就近路由，则增加环境变量 -Dnearby.based.router.enable=true, 此时需要选择一种就近路由方式（cloud、zone、region），支持多选(以逗号分割)，如果多选则根据排序决定最终的优先规则。

    | router.match.level   | 含义                     |
    | -------------------- | ------------------------ | 
    | cloud                | 同一云内优先访问          | 
    | zone                 | 同一可用区优先访问         | 
    | region               | 同一区域优先访问 |
- 如果需要同一云内优先访问，则增加环境变量 -Drouter.match.levels=cloud，并且增加环境变量 -Drouter.match.level.cloud.label=xx，来标注cloud的标签。
- 假设用户想实现同一云内优先访问，最终的配置如下：
    - -Dtarget.nacos.server.addr=xx.xx.xx.xx
    - -Dnearby.based.router.enable=true
    - -Drouter.match.levels=cloud
    - -Drouter.match.level.cloud.label=xx