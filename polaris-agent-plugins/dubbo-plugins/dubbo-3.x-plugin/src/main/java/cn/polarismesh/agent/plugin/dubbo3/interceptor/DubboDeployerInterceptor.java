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

package cn.polarismesh.agent.plugin.dubbo3.interceptor;

import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.dubbo3.DubboConfigProvider;
import cn.polarismesh.agent.plugin.dubbo3.constants.DubboConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.config.deploy.DefaultApplicationDeployer;
import org.apache.dubbo.rpc.model.ApplicationModel;

public class DubboDeployerInterceptor implements Interceptor {

    private static final Logger LOGGER =
            Logger.getLogger(DubboDeployerInterceptor.class.getName());

    @Override
    public void before(Object target, Object[] args) {
        try {
            ConfigManager configManager = resolveConfigManager(target);
            injectConfigCenter(configManager);
            injectMetadataReport(configManager);
        } catch (Throwable th) {
            LOGGER.warning("Polaris before() interceptor failed; "
                    + "falling back to original config: " + th.getMessage());
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        try {
            ConfigManager configManager = resolveConfigManager(target);
            Collection<RegistryConfig> registries = configManager.getRegistries();
            if (registries == null || registries.isEmpty()) {
                LOGGER.info("No registries found, adding polaris registry");
                addPolarisRegistry(configManager);
                return;
            }
            if (findPolaris(registries) != null) {
                LOGGER.info("Polaris registry already present, skip registry rewrite");
                return;
            }
            rewriteFirstRemoveRest(configManager, registries);
        } catch (Throwable th) {
            LOGGER.warning("Polaris registry rewrite failed; fallback to original: "
                    + th.getMessage());
        }
    }

    ConfigManager resolveConfigManager(Object target) {
        if (target instanceof DefaultApplicationDeployer) {
            return ((DefaultApplicationDeployer) target)
                    .getApplicationModel().getApplicationConfigManager();
        }
        return ApplicationModel.defaultModel().getApplicationConfigManager();
    }

    private void injectConfigCenter(ConfigManager configManager) {
        try {
            if (!DubboConfigProvider.isConfigCenterEnabled()) {
                LOGGER.info("Polaris config-center injection disabled by "
                        + DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED + "=false");
                return;
            }
            Collection<ConfigCenterConfig> centers = configManager.getConfigCenters();
            String polarisAddress = DubboConfigProvider.getConfigCenterAddress();
            Map<String, String> polarisParameters = DubboConfigProvider.getConfigCenterParameters();

            if (centers == null || centers.isEmpty()) {
                LOGGER.info("No ConfigCenter found, adding Polaris ConfigCenter at " + polarisAddress);
                ConfigCenterConfig cc = new ConfigCenterConfig();
                applyPolarisFields(cc, polarisAddress, polarisParameters);
                configManager.addConfigCenter(cc);
                return;
            }
            for (ConfigCenterConfig cc : centers) {
                if (DubboConstants.POLARIS_PROTOCOL.equals(cc.getProtocol())) {
                    LOGGER.info("Polaris ConfigCenter already present, skip config-center rewrite");
                    return;
                }
            }
            rewriteFirstConfigCenterRemoveRest(configManager, centers,
                    polarisAddress, polarisParameters);
        } catch (Throwable th) {
            LOGGER.warning("Polaris config-center rewrite failed; fallback to original: "
                    + th.getMessage());
        }
    }

    private void rewriteFirstConfigCenterRemoveRest(ConfigManager configManager,
            Collection<ConfigCenterConfig> centers,
            String polarisAddress, Map<String, String> polarisParameters) {
        // 单配置为常态,直接取首个;不像 registry 那样按 #0 id 选位
        ConfigCenterConfig first = centers.iterator().next();
        LOGGER.info("Rewriting first ConfigCenter [" + first.getProtocol() + "://"
                + first.getAddress() + "] -> [" + polarisAddress + "]");
        applyPolarisFields(first, polarisAddress, polarisParameters);
        List<ConfigCenterConfig> toRemove = new ArrayList<ConfigCenterConfig>();
        for (ConfigCenterConfig cc : centers) {
            if (cc != first) {
                toRemove.add(cc);
            }
        }
        for (ConfigCenterConfig cc : toRemove) {
            LOGGER.info("Removing extra ConfigCenter [" + cc.getProtocol() + "://"
                    + cc.getAddress() + "]");
            configManager.removeConfig(cc);
        }
    }

    private void applyPolarisFields(ConfigCenterConfig cc, String polarisAddress,
            Map<String, String> polarisParameters) {
        cc.setParameters(new HashMap<String, String>());
        cc.setCluster(null);
        cc.setUsername(null);
        cc.setPassword(null);
        cc.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        cc.setAddress(polarisAddress);
        cc.setPort(null);
        if (!polarisParameters.isEmpty()) {
            mergeParameters(cc, polarisParameters);
        }
    }

    private void mergeParameters(ConfigCenterConfig cc, Map<String, String> newParams) {
        Map<String, String> existing = cc.getParameters();
        if (existing == null || existing.isEmpty()) {
            cc.setParameters(newParams);
        } else {
            Map<String, String> merged = new HashMap<String, String>(existing);
            merged.putAll(newParams);
            cc.setParameters(merged);
        }
    }

    private void injectMetadataReport(ConfigManager configManager) {
        try {
            if (!DubboConfigProvider.isMetadataReportEnabled()) {
                LOGGER.info("Polaris metadata-report injection disabled by "
                        + DubboConstants.KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED + "=false");
                return;
            }
            Collection<MetadataReportConfig> reports = configManager.getMetadataConfigs();
            String polarisAddress = DubboConfigProvider.getMetadataReportAddress();
            Map<String, String> polarisParameters = DubboConfigProvider.getMetadataReportParameters();

            if (reports == null || reports.isEmpty()) {
                LOGGER.info("No MetadataReport found, adding Polaris MetadataReport at " + polarisAddress);
                MetadataReportConfig mrc = new MetadataReportConfig();
                applyPolarisMetadataFields(mrc, polarisAddress, polarisParameters);
                configManager.addMetadataReport(mrc);
                return;
            }
            for (MetadataReportConfig mrc : reports) {
                if (DubboConstants.POLARIS_PROTOCOL.equals(mrc.getProtocol())) {
                    LOGGER.info("Polaris MetadataReport already present, skip metadata rewrite");
                    return;
                }
            }
            // 单配置为常态,直接取首个;不像 registry 那样按 #0 id 选位
            MetadataReportConfig first = reports.iterator().next();
            LOGGER.info("Rewriting first MetadataReport [" + first.getProtocol() + "://"
                    + first.getAddress() + "] -> [" + polarisAddress + "]");
            applyPolarisMetadataFields(first, polarisAddress, polarisParameters);
            List<MetadataReportConfig> toRemove = new ArrayList<MetadataReportConfig>();
            for (MetadataReportConfig mrc : reports) {
                if (mrc != first) {
                    toRemove.add(mrc);
                }
            }
            for (MetadataReportConfig mrc : toRemove) {
                LOGGER.info("Removing extra MetadataReport [" + mrc.getAddress() + "]");
                configManager.removeConfig(mrc);
            }
        } catch (Throwable th) {
            LOGGER.warning("Polaris metadata-report rewrite failed; fallback to original: "
                    + th.getMessage());
        }
    }

    private void applyPolarisMetadataFields(MetadataReportConfig mrc, String polarisAddress,
            Map<String, String> polarisParameters) {
        mrc.setParameters(new HashMap<String, String>());
        mrc.setUsername(null);
        mrc.setPassword(null);
        mrc.setAddress(polarisAddress);
        mrc.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        if (!polarisParameters.isEmpty()) {
            mergeParameters(mrc, polarisParameters);
        }
    }

    private void mergeParameters(MetadataReportConfig mrc, Map<String, String> newParams) {
        Map<String, String> existing = mrc.getParameters();
        if (existing == null || existing.isEmpty()) {
            mrc.setParameters(newParams);
        } else {
            Map<String, String> merged = new HashMap<String, String>(existing);
            merged.putAll(newParams);
            mrc.setParameters(merged);
        }
    }

    private void addPolarisRegistry(ConfigManager configManager) {
        RegistryConfig rc = new RegistryConfig();
        rc.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        rc.setAddress(DubboConfigProvider.getPolarisServerAddress());
        Map<String, String> params = DubboConfigProvider.getRegistryParameters();
        if (!params.isEmpty()) {
            mergeParameters(rc, params);
        }
        configManager.addRegistry(rc);
    }

    private RegistryConfig findPolaris(Collection<RegistryConfig> registries) {
        for (RegistryConfig r : registries) {
            if (DubboConstants.POLARIS_PROTOCOL.equals(r.getProtocol())) {
                return r;
            }
        }
        return null;
    }

    private void rewriteFirstRemoveRest(ConfigManager configManager,
            Collection<RegistryConfig> registries) {
        String polarisAddress = DubboConfigProvider.getPolarisServerAddress();
        Map<String, String> params = DubboConfigProvider.getRegistryParameters();
        RegistryConfig first = pickFirstRegistry(registries);
        if (first == null) {
            return;
        }
        LOGGER.info("Rewriting first registry [" + first.getProtocol() + "://"
                + first.getAddress() + "] -> [" + polarisAddress + "]");
        first.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        first.setAddress(polarisAddress);
        if (!params.isEmpty()) {
            mergeParameters(first, params);
        }
        List<RegistryConfig> toRemove = new ArrayList<RegistryConfig>();
        for (RegistryConfig r : registries) {
            if (r != first) {
                toRemove.add(r);
            }
        }
        for (RegistryConfig r : toRemove) {
            LOGGER.info("Removing extra registry [" + r.getProtocol() + "://"
                    + r.getAddress() + "]");
            configManager.removeConfig(r);
        }
    }

    private RegistryConfig pickFirstRegistry(Collection<RegistryConfig> registries) {
        for (RegistryConfig r : registries) {
            if (DubboConstants.AUTO_REGISTRY_ID_PREFIX_ZERO.equals(r.getId())) {
                return r;
            }
        }
        return registries.iterator().next();
    }

    private void mergeParameters(RegistryConfig registry, Map<String, String> newParams) {
        Map<String, String> existing = registry.getParameters();
        if (existing == null || existing.isEmpty()) {
            registry.setParameters(newParams);
        } else {
            Map<String, String> merged = new HashMap<String, String>(existing);
            merged.putAll(newParams);
            registry.setParameters(merged);
        }
    }
}
