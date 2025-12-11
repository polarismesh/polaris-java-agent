# Polaris Java Agent 构建工具

## 概述

`polaris-agent-build` 模块提供了构建脚本、Docker 镜像构建脚本以及启动脚本，用于自动化构建和部署流程。

## 目录结构

```
polaris-agent-build/
├── bin/                    # 构建脚本目录
│   ├── build.sh           # 标准构建脚本
│   ├── dev.sh             # 开发环境构建脚本（保留配置）
│   ├── build_docker.sh    # Docker 镜像构建脚本
│   ├── build_example_docker.sh # polaris-agent-example 应用的 Docker 构建脚本
│   └── start.sh           # 启动脚本（用于容器环境）
├── conf/                   # 配置文件目录
│   └── polaris-agent.config # Agent 主配置文件
└── pom.xml                # Maven 构建配置
```

## 构建脚本说明

### build.sh - 标准构建脚本
- 清理并重新构建整个项目
- 打包所有必要的 JAR 文件到发行目录
- 生成最终的 ZIP 发行包
- 支持 Docker 环境构建（通过 `use_docker_env=true` 环境变量）

### dev.sh - 开发环境构建脚本
- 保留现有的配置文件（自动备份和恢复）
- 适用于开发过程中的快速重建
- 其他功能与 `build.sh` 相同

### start.sh - 容器启动脚本
- 在容器环境中自动解压和配置 Agent
- 根据环境变量动态配置插件
- 自动获取云环境元数据（地域、可用区信息）
- 注入 Polaris 服务器地址配置

## 构建流程

### 1. 环境准备
确保系统已安装：
- Java 8 或更高版本
- Maven 3.8.6 或更高版本
- zip 工具

### 2. 执行构建
```bash
# 标准构建
cd polaris-agent-build/bin
./build.sh

# 开发环境构建（保留配置）
./dev.sh

# 使用 Docker 环境构建
export use_docker_env=true
./build.sh
```

### 3. 输出结果
构建完成后，将在项目根目录生成：
- `polaris-java-agent-{version}/` - 解压后的发行目录
- `polaris-java-agent-{version}.zip` - 压缩发行包

发行包包含：
- `polaris-agent-core-bootstrap.jar` - Agent 引导程序
- `lib/` - 核心库文件
- `lib/java9/` - Java 9+ 专用库
- `plugins/` - 插件文件（Spring Cloud 等）
- `conf/` - 配置文件
- `boot/` - 扩展库

## 配置文件

### polaris-agent.config
Agent 主配置文件，主要配置项：
- `plugins.enable` - 启用的插件 ID（如：spring-cloud-2023-plugin）

## Docker 构建

### 构建 Agent Docker 镜像
```bash
./build_docker.sh
```

### 构建示例应用 Docker 镜像
```bash
./build_example_docker.sh
```

## 容器部署说明

### 容器环境部署
在容器启动时，`start.sh` 脚本会自动：
1. 解压 Agent 发行包
2. 根据环境变量配置插件
3. 注入 Polaris 服务器地址
4. 配置地域和可用区信息

### 关键环境变量
- `JAVA_AGENT_DIR` - Agent 安装目录
- `JAVA_AGENT_FRAMEWORK_NAME` - 框架名称（如：spring-cloud）
- `JAVA_AGENT_FRAMEWORK_VERSION` - 框架版本（如：2023）
- `POLARIS_SERVER_IP` - Polaris 服务器 IP
- `POLARIS_DISCOVER_PORT` - Polaris 发现服务端口
- `POLARIS_CONFIG_IP` - Polaris 配置服务 IP
- `POLARIS_CONFIG_PORT` - Polaris 配置服务端口

## 相关文档

- [Polaris Java Agent 主文档](../README-zh.md)
- [Spring Cloud 接入指南](https://github.com/Tencent/spring-cloud-tencent/wiki/SCT-Agent-%E6%A6%82%E8%BF%B0)

## 许可证

本项目基于 Apache License 2.0 许可证开源。