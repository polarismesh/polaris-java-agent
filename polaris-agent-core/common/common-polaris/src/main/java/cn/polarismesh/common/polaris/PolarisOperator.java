/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
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

package cn.polarismesh.common.polaris;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.rpc.GetHealthyInstancesRequest;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.rpc.ServiceCallResult;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.client.api.SDKContext;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.api.RouterAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import com.tencent.polaris.ratelimit.api.core.LimitAPI;
import com.tencent.polaris.ratelimit.factory.LimitAPIFactory;
import com.tencent.polaris.router.api.core.RouterAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisOperator.class);

    private final Object lock = new Object();

    private final AtomicBoolean inited = new AtomicBoolean(false);

    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final PolarisConfig polarisConfig;

    private SDKContext sdkContext;

    private ConsumerAPI consumerAPI;

    private ProviderAPI providerAPI;

    private LimitAPI limitAPI;

    private RouterAPI routerAPI;

    public PolarisOperator(PolarisConfig polarisConfig) {
        this.polarisConfig = polarisConfig;
    }

    public void init() {
        if (inited.get()) {
            return;
        }
        synchronized (lock) {
            if (inited.get()) {
                return;
            }
            String agentDir = polarisConfig.getAgentDir();
            Configuration configuration;
            try {
                configuration = loadPolarisConfig(agentDir);
            }
            catch (IOException e) {
                LOGGER.error("fail to load polaris config: {}", e.getMessage());
                return;
            }
            ((ConfigurationImpl) configuration).setDefault();
            ((ConfigurationImpl) configuration).getGlobal().getServerConnector()
                    .setAddresses(Collections.singletonList(polarisConfig.getRegistryAddress()));
            sdkContext = SDKContext.initContextByConfig(configuration);
            consumerAPI = DiscoveryAPIFactory.createConsumerAPIByContext(sdkContext);
            providerAPI = DiscoveryAPIFactory.createProviderAPIByContext(sdkContext);
            limitAPI = LimitAPIFactory.createLimitAPIByContext(sdkContext);
            routerAPI = RouterAPIFactory.createRouterAPIByContext(sdkContext);
            inited.set(true);
        }
    }

    public SDKContext getSdkContext() {
        return sdkContext;
    }

    public ConsumerAPI getConsumerAPI() {
        return consumerAPI;
    }

    public ProviderAPI getProviderAPI() {
        return providerAPI;
    }

    public LimitAPI getLimitAPI() {
        return limitAPI;
    }

    public RouterAPI getRouterAPI() {
        return routerAPI;
    }

    private Configuration loadPolarisConfig(String agentDir) throws IOException {
        String polarisConfigFile = agentDir + File.separator + PolarisReflectConst.POLARIS_CONF_DIR + File.separator
                + PolarisReflectConst.POLARIS_CONF_FILE;
        try (InputStream inputStream = new FileInputStream(polarisConfigFile)) {
            return ConfigAPIFactory.loadConfig(inputStream);
        }
    }

    public void destroy() {
        synchronized (lock) {
            if (!inited.get()) {
                return;
            }
            heartbeatExecutor.shutdown();
            sdkContext.close();
            inited.set(false);
        }
    }

    /**
     * 服务注册
     */
    public void registerInstance(InstanceRegisterRequest request) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to register instance {}, polaris init failed", request);
            return;
        }
        LOGGER.info("[POLARIS] start to register: instance {}", request);
        String token = polarisConfig.getToken();
        if (StringUtils.isBlank(request.getNamespace())) {
            request.setNamespace(polarisConfig.getNamespace());
        }
        if (request.getTtl() == null) {
            request.setTtl(polarisConfig.getTtl());
        }
        request.setToken(token);
        InstanceRegisterResponse response = providerAPI.registerInstance(request);
        LOGGER.info("register result is {} for service {}", response, request.getService());
    }

    public void deregister(InstanceDeregisterRequest request) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to deregister {}, polaris init failed", request);
            return;
        }
        LOGGER.info("[POLARIS] start to deregister: service {}", request);
        if (StringUtils.isBlank(request.getNamespace())) {
            request.setNamespace(polarisConfig.getNamespace());
        }
        request.setToken(polarisConfig.getToken());
        providerAPI.deRegister(request);
        LOGGER.info("[POLARIS] deregister service {}", request);
    }

    /**
     * 调用CONSUMER_API获取实例信息
     *
     * @param service 服务的service
     * @return Polaris选择的Instance对象
     */
    public Instance[] getAvailableInstances(String namespace, String service) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to getInstances {}, polaris init failed", service);
            return null;
        }
        GetHealthyInstancesRequest request = new GetHealthyInstancesRequest();
        request.setNamespace(namespace);
        if (StringUtils.isBlank(namespace)) {
            request.setNamespace(polarisConfig.getNamespace());
        }
        request.setService(service);
        LOGGER.info("[POLARIS] start to getInstances {} from polaris", request);
        InstancesResponse instances = consumerAPI.getHealthyInstances(request);
        return instances.getInstances();
    }

    /**
     * 调用CONSUMER_API上报服务请求结果
     *
     * @param delay 本次服务调用延迟，单位ms
     */
    public void reportInvokeResult(String service, String method, String host, int port, long delay, boolean success,
            int code) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to getInstances {}, polaris init failed", service);
            return;
        }
        ServiceCallResult serviceCallResult = new ServiceCallResult();
        serviceCallResult.setNamespace(polarisConfig.getNamespace());
        serviceCallResult.setService(service);
        serviceCallResult.setMethod(method);
        serviceCallResult.setHost(host);
        serviceCallResult.setPort(port);
        serviceCallResult.setDelay(delay);
        serviceCallResult.setRetStatus(success ? RetStatus.RetSuccess : RetStatus.RetFail);
        serviceCallResult.setRetCode(code);
        consumerAPI.updateServiceCallResult(serviceCallResult);
    }

}