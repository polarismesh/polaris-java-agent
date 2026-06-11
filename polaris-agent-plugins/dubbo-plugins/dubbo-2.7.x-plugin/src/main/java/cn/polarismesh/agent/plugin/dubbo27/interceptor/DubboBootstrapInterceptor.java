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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;

public class DubboBootstrapInterceptor implements Interceptor {

    private static final Logger LOGGER =
            Logger.getLogger(
                    DubboBootstrapInterceptor.class.getName());

    @Override
    public void before(Object target, Object[] args) {
        injectConfigCenter();
        disableMetadataReport();

    }

    @Override
    public void after(Object target, Object[] args,
            Object result, Throwable throwable) {
        modifyExistedRegistry();
    }

    public void injectConfigCenter() {
        // fail-safe 总入口: ASM 框架对 before() 不包 try/catch,
        // 此处任何抛出都会让 DubboBootstrap.initialize() 失败 → 应用启动失败,
        // 因此整段必须 try { ... } catch (Throwable) 兜底。
        try {
            if (!DubboConfigProvider.isConfigCenterEnabled()) {
                LOGGER.info("Polaris config-center injection disabled by "
                        + DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED
                        + "=false");
                return;
            }
            ConfigManager configManager =
                    ApplicationModel.getConfigManager();
            Collection<ConfigCenterConfig> centers =
                    configManager.getConfigCenters();

            String polarisAddress =
                    DubboConfigProvider.getConfigCenterAddress();
            Map<String, String> polarisParameters =
                    DubboConfigProvider.getConfigCenterParameters();

            // 分支 0: 无任何 config-center → 新增 polaris
            if (centers == null || centers.isEmpty()) {
                LOGGER.info("No ConfigCenter found, adding Polaris "
                        + "ConfigCenter at " + polarisAddress);
                ConfigCenterConfig cc = new ConfigCenterConfig();
                applyPolarisFields(cc, polarisAddress, polarisParameters);
                configManager.addConfigCenter(cc);
                return;
            }

            // 分支 1: 已有 polaris config-center → no-op
            for (ConfigCenterConfig cc : centers) {
                if (DubboConstants.POLARIS_PROTOCOL.equals(cc.getProtocol())) {
                    LOGGER.info("Polaris ConfigCenter already present (id="
                            + cc.getId() + ", address="
                            + cc.getAddress()
                            + "), agent skips config-center rewrite");
                    return;
                }
            }

            // 分支 2: 改写首位为 polaris,删除其余
            rewriteFirstConfigCenterRemoveRest(configManager, centers,
                    polarisAddress, polarisParameters);
        } catch (Throwable th) {
            LOGGER.warning("Polaris config-center rewrite failed; "
                    + "falling back to original config-center: "
                    + th.getMessage());
        }
    }


    private void rewriteFirstConfigCenterRemoveRest(ConfigManager configManager,
            Collection<ConfigCenterConfig> centers,
            String polarisAddress,
            Map<String, String> polarisParameters) {
        ConfigCenterConfig first = pickFirstConfigCenter(centers);
        if (first == null) {
            return;
        }

        LOGGER.info("Rewriting first ConfigCenter [" + first.getProtocol()
                + "://" + first.getAddress() + "] -> ["
                + DubboConstants.POLARIS_PROTOCOL + "://"
                + polarisAddress + "]");
        applyPolarisFields(first, polarisAddress, polarisParameters);

        // 收集"要删的"到独立 list,避免 CME
        List<ConfigCenterConfig> toRemove =
                new ArrayList<ConfigCenterConfig>();
        for (ConfigCenterConfig cc : centers) {
            if (cc != first) {
                toRemove.add(cc);
            }
        }
        for (ConfigCenterConfig cc : toRemove) {
            LOGGER.info("Removing extra ConfigCenter ["
                    + cc.getProtocol() + "://" + cc.getAddress() + "]");
            configManager.removeConfig(cc);
        }
    }

    /**
     * 选首位: 优先 id 等于 Dubbo 自动赋的 ConfigCenterBean#0;
     * 找不到退回 iterator().next()。
     */
    private ConfigCenterConfig pickFirstConfigCenter(
            Collection<ConfigCenterConfig> centers) {
        if (centers == null || centers.isEmpty()) {
            return null;
        }
        for (ConfigCenterConfig cc : centers) {
            if (DubboConstants.AUTO_CONFIG_CENTER_ID_PREFIX_ZERO
                    .equals(cc.getId())) {
                return cc;
            }
        }
        return centers.iterator().next();
    }

    private void disableMetadataReport() {
        ConfigManager configManager =
                ApplicationModel.getConfigManager();
        Collection<MetadataReportConfig> metadataReportConfigs =
                configManager.getMetadataConfigs();
        if (metadataReportConfigs != null && !metadataReportConfigs.isEmpty()) {
            // remove all existing metadata report configs
            // polaris does not support metadata report yet
            metadataReportConfigs.clear();
        }

        Optional<ApplicationConfig> applicationConfig =
                configManager.getApplication();
        // 注意: ApplicationConfig.metadataType 在用户未配置
        // dubbo.application.metadata-type 时为 null,
        if (applicationConfig.isPresent()
                && "remote".equals(applicationConfig.get().getMetadataType())) {
            LOGGER.warning("metadata-type is remote, change to local "
                    + "cause polaris does not support remote metadata report yet");
            applicationConfig.get().setMetadataType("local");
        }
        Collection<RegistryConfig> registries =
                configManager.getRegistries();
        if (registries == null || registries.isEmpty()) {
            return;
        }
        for (RegistryConfig registryConfig : registries) {
            // 禁用 metadata report / config center,以及取消默认标识
            registryConfig.setUseAsMetadataCenter(false);
            registryConfig.setUseAsConfigCenter(false);
        }
    }

    private void modifyExistedRegistry() {
        // fail-safe 总入口: 与 injectConfigCenter() 同样的语义 —
        // plugin 任何异常都不应阻塞 DubboBootstrap.initialize() 主流程,
        // 否则会让用户应用直接启动失败。
        try {
            ConfigManager configManager =
                    ApplicationModel.getConfigManager();
            Collection<RegistryConfig> registries =
                    configManager.getRegistries();

            // 加一个默认 polaris (前移到 for 循环之前,
            // 避免 registries == null 时 for-each 直接 NPE)
            if (registries == null || registries.isEmpty()) {
                LOGGER.info("No registries found in ConfigManager, "
                        + "adding polaris registry");
                addPolarisRegistry(configManager);
                return;
            }

            // 分支 0: 已有 polaris registry → no-op
            RegistryConfig existingPolaris = findPolaris(registries);
            if (existingPolaris != null) {
                LOGGER.info("Polaris registry already present (id="
                        + existingPolaris.getId() + ", address="
                        + existingPolaris.getAddress()
                        + "), agent skips registry rewrite");
                return;
            }

            // 分支 2: 改写首位为 polaris,删除其余
            rewriteFirstRemoveRest(configManager, registries);
        } catch (Throwable th) {
            LOGGER.warning("Polaris registry rewrite failed; "
                    + "falling back to original registry: "
                    + th.getMessage());
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
     * 在 registries 中查找首个 protocol == polaris 的 RegistryConfig。
     * 找不到返回 null。null-safe 入参。
     *
     * <p>顺序由 registries 自身的 iterator 决定;在调用方 after() 中只用于
     * 判定"是否存在",任意一个匹配项都可以,不依赖顺序。</p>
     */
    private RegistryConfig findPolaris(Collection<RegistryConfig> registries) {
        if (registries == null || registries.isEmpty()) {
            return null;
        }
        for (RegistryConfig r : registries) {
            if (DubboConstants.POLARIS_PROTOCOL.equals(r.getProtocol())) {
                return r;
            }
        }
        return null;
    }

    /**
     * 改写"首位"为 polaris,删除其余。
     *
     * <p>"首位"定义:优先选 id == "org.apache.dubbo.config.RegistryConfig#0"
     * 的 RegistryConfig (对应dubbo.registry.address的配置);若找不到 #0,退回 iterator().next() </p>
     *
     * <p>关键: 删除时必须先把要删的 collect 到独立 list,再统一调
     * removeConfig,否则 fail-fast iterator 抛 ConcurrentModificationException
     * (configManager.removeConfig() 修改的 configsCache 与正在迭代的
     * collection 共享底层 map)。</p>
     *
     * <p>注: after() 的分支 0 已拦截了"含 polaris"场景,所以本方法的所有
     * registry 都保证 protocol != polaris,无需再做防御性 skip。</p>
     */
    private void rewriteFirstRemoveRest(ConfigManager configManager,
            Collection<RegistryConfig> registries) {
        String polarisAddress =
                DubboConfigProvider.getPolarisServerAddress();
        Map<String, String> params =
                DubboConfigProvider.getRegistryParameters();

        RegistryConfig first = pickFirstRegistry(registries);
        if (first == null) {
            return; // 防御性: 调用方已保证非空
        }

        LOGGER.info("Rewriting first registry [" + first.getProtocol()
                + "://" + first.getAddress() + "] -> ["
                + DubboConstants.POLARIS_PROTOCOL + "://"
                + polarisAddress + "]");
        first.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        first.setAddress(polarisAddress);
        if (!params.isEmpty()) {
            mergeParameters(first, params);
        }

        // 收集"要删的"到独立 list,避免 CME
        List<RegistryConfig> toRemove =
                new ArrayList<RegistryConfig>();
        for (RegistryConfig r : registries) {
            if (r != first) {
                toRemove.add(r);
            }
        }
        for (RegistryConfig r : toRemove) {
            LOGGER.info("Removing extra registry ["
                    + r.getProtocol() + "://" + r.getAddress() + "]");
            configManager.removeConfig(r);
        }
    }

    /**
     * 选首位: 优先 id 等于 Dubbo 自动赋的 "...RegistryConfig#0";
     * 找不到退回 iterator().next()。
     */
    private RegistryConfig pickFirstRegistry(Collection<RegistryConfig> registries) {
        if (registries == null || registries.isEmpty()) {
            return null;
        }
        for (RegistryConfig r : registries) {
            if (DubboConstants.AUTO_REGISTRY_ID_PREFIX_ZERO
                    .equals(r.getId())) {
                return r;
            }
        }
        return registries.iterator().next();
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

    private void applyPolarisFields(ConfigCenterConfig cc,
            String polarisAddress,
            Map<String, String> polarisParameters) {
        // 1. 先清空 parameters,避免 setAddress 内部把残留拼回去
        cc.setParameters(new HashMap<String, String>());
        // 2. 清掉认证残值(setAddress 的 updatePropertyIfAbsent 只填 absent 字段,
        //    所以需要先 null 再让 setAddress 解析 polaris URL 时不会把 nacos 残值带入)
        cc.setCluster(null);
        cc.setUsername(null);
        cc.setPassword(null);
        // 3. 设 polaris 协议与地址
        cc.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        cc.setAddress(polarisAddress);
        // 4. setAddress 内部 updatePropertyIfAbsent 会用 polaris URL 里的端口
        //    重新填入 port 字段;我们在这里把它清掉以符合"port → null"语义。
        //    (polaris 协议不使用独立 port 字段,仅靠 address 字符串)
        cc.setPort(null);
        // 5. 把 plugin 的 polaris 参数 merge 进 (此时 cc.parameters 是空 Map)
        if (!polarisParameters.isEmpty()) {
            mergeParameters(cc, polarisParameters);
        }
    }

    /**
     * 把 newParams merge 到 cc.parameters,同 key 时新值覆盖。
     *
     * <p>与 after() 内的 mergeParameters(RegistryConfig, Map) 行为相同,
     * 但参数类型不同,必须独立实现。</p>
     */
    private void mergeParameters(ConfigCenterConfig cc,
            Map<String, String> newParams) {
        Map<String, String> existing = cc.getParameters();
        if (existing == null || existing.isEmpty()) {
            cc.setParameters(newParams);
        } else {
            Map<String, String> merged =
                    new HashMap<String, String>(existing);
            merged.putAll(newParams);
            cc.setParameters(merged);
        }
    }
}
