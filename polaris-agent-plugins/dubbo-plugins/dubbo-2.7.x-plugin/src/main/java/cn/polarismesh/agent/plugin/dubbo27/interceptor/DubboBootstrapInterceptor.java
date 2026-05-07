/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.dubbo27.interceptor;

import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.dubbo27.DubboConfigProvider;
import cn.polarismesh.agent.plugin.dubbo27.constants.DubboConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;

/**
 * DubboBootstrap.initialize() 拦截器，在初始化完成后替换注册中心为 Polaris.
 *
 * <p>在 DubboBootstrap.initialize() 的 after 阶段执行，确保在
 * {@code startConfigCenter()} 内部的 {@code configManager.refreshAll()}
 * 以及 {@code loadRemoteConfigs()} 全部完成后再替换，避免被 refresh 机制覆盖。
 * 通过 ConfigManager 获取所有 RegistryConfig，将非 polaris 协议的注册中心
 * 替换为 polaris 协议和地址，同时注入扩展参数。
 * Polaris 地址依次从 JVM 系统属性 {@code dubbo.registry.address}、
 * 本地配置文件、默认值 {@code polaris://127.0.0.1:8091} 读取.</p>
 */
public class DubboBootstrapInterceptor implements Interceptor {

    private static final Logger LOGGER =
            Logger.getLogger(
                    DubboBootstrapInterceptor.class.getName());

    @Override
    public void after(Object target, Object[] args,
            Object result, Throwable throwable) {
        ConfigManager configManager =
                ApplicationModel.getConfigManager();
        Collection<RegistryConfig> registries =
                configManager.getRegistries();

        if (registries == null || registries.isEmpty()) {
            LOGGER.info("No registries found in ConfigManager, "
                    + "adding polaris registry");
            addPolarisRegistry(configManager);
            return;
        }

        String polarisAddress =
                DubboConfigProvider.getPolarisServerAddress();
        Map<String, String> params =
                DubboConfigProvider.getRegistryParameters();
        for (RegistryConfig registry : registries) {
            if (!DubboConstants.POLARIS_PROTOCOL.equals(
                    registry.getProtocol())) {
                LOGGER.info("Replacing registry protocol ["
                        + registry.getProtocol() + "] -> ["
                        + DubboConstants.POLARIS_PROTOCOL
                        + "], address -> ["
                        + polarisAddress + "]");
                registry.setProtocol(
                        DubboConstants.POLARIS_PROTOCOL);
                registry.setAddress(polarisAddress);
                if (!params.isEmpty()) {
                    mergeParameters(registry, params);
                }
            }
        }
    }

    private void addPolarisRegistry(
            ConfigManager configManager) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol(
                DubboConstants.POLARIS_PROTOCOL);
        registryConfig.setAddress(
                DubboConfigProvider.getPolarisServerAddress());
        Map<String, String> params =
                DubboConfigProvider.getRegistryParameters();
        if (!params.isEmpty()) {
            mergeParameters(registryConfig, params);
        }
        configManager.addRegistry(registryConfig);
    }

    /**
     * 将配置文件中的 parameters 合并到 registry 已有的 parameters 中.
     * 已有的 key 会被配置文件的值覆盖，不存在的 key 追加。
     */
    private void mergeParameters(RegistryConfig registry,
            Map<String, String> newParams) {
        Map<String, String> existing = registry.getParameters();
        if (existing == null || existing.isEmpty()) {
            registry.setParameters(newParams);
        } else {
            Map<String, String> merged =
                    new HashMap<String, String>(existing);
            merged.putAll(newParams);
            registry.setParameters(merged);
        }
    }
}
