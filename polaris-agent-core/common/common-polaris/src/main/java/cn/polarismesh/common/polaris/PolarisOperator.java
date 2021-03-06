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

import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.listener.ServiceListener;
import com.tencent.polaris.api.pojo.DefaultServiceInstances;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.pojo.ServiceInfo;
import com.tencent.polaris.api.pojo.ServiceKey;
import com.tencent.polaris.api.rpc.Criteria;
import com.tencent.polaris.api.rpc.GetInstancesRequest;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceHeartbeatRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.rpc.ServiceCallResult;
import com.tencent.polaris.api.rpc.UnWatchServiceRequest;
import com.tencent.polaris.api.rpc.WatchServiceRequest;
import com.tencent.polaris.client.api.SDKContext;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.api.RouterAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import com.tencent.polaris.ratelimit.api.core.LimitAPI;
import com.tencent.polaris.ratelimit.api.rpc.QuotaRequest;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResponse;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResultCode;
import com.tencent.polaris.ratelimit.factory.LimitAPIFactory;
import com.tencent.polaris.router.api.core.RouterAPI;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceRequest;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceResponse;
import com.tencent.polaris.router.api.rpc.ProcessRoutersRequest;
import com.tencent.polaris.router.api.rpc.ProcessRoutersResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisOperator.class);

    private final Object lock = new Object();

    private final AtomicBoolean inited = new AtomicBoolean(false);

    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final Map<InstanceIdentifier, ScheduledFuture<?>> scheduledFutures = new HashMap<>();

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
            } catch (IOException e) {
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
     * ????????????
     */
    public void register(String service, String host, int port, String protocol, String version, int weight,
            Map<String, String> metadata) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to register address {}:{} to {}, polaris init failed", host, port, service);
            return;
        }
        LOGGER.info(
                "[POLARIS] start to register: service {}, host {}, port {}??? protocol {}, version {}, weight {}, metadata {}",
                service, host, port, protocol, version, weight, metadata);
        String namespace = polarisConfig.getNamespace();
        int ttl = polarisConfig.getTtl();
        String token = polarisConfig.getToken();
        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace(namespace);
        instanceRegisterRequest.setService(service);
        instanceRegisterRequest.setHost(host);
        instanceRegisterRequest.setPort(port);
        instanceRegisterRequest.setWeight(weight);
        instanceRegisterRequest.setVersion(version);
        instanceRegisterRequest.setTtl(ttl);
        instanceRegisterRequest.setMetadata(metadata);
        instanceRegisterRequest.setProtocol(protocol);
        instanceRegisterRequest.setToken(token);
        InstanceRegisterResponse response = providerAPI.register(instanceRegisterRequest);
        LOGGER.info("register result is {} for service {}", response, service);
        // ?????????????????????????????????
        LOGGER.info("heartbeat task start, ttl is {}", ttl);
        Runnable heartbeatTask = new Runnable() {
            @Override
            public void run() {
                heartbeat(service, host, port);
            }
        };
        ScheduledFuture<?> future = heartbeatExecutor.scheduleWithFixedDelay(heartbeatTask, ttl, ttl,
                TimeUnit.SECONDS);
        scheduledFutures.put(new InstanceIdentifier(service, host, port), future);
    }

    public void deregister(String service, String host, int port) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to deregister address {}:{} to {}, polaris init failed", host, port, service);
            return;
        }
        LOGGER.info("[POLARIS] start to deregister: service {}, host {}, port {}", service, host, port);
        ScheduledFuture<?> future = scheduledFutures
                .get(new InstanceIdentifier(service, host, port));
        if (null != future) {
            future.cancel(true);
        }
        InstanceDeregisterRequest instanceDeregisterRequest = new InstanceDeregisterRequest();
        instanceDeregisterRequest.setNamespace(polarisConfig.getNamespace());
        instanceDeregisterRequest.setService(service);
        instanceDeregisterRequest.setPort(port);
        instanceDeregisterRequest.setHost(host);
        instanceDeregisterRequest.setToken(polarisConfig.getToken());
        providerAPI.deRegister(instanceDeregisterRequest);
        LOGGER.info("[POLARIS] deregister service {}", service);
    }

    private void heartbeat(String service, String host, int port) {
        InstanceHeartbeatRequest instanceHeartbeatRequest = new InstanceHeartbeatRequest();
        instanceHeartbeatRequest.setNamespace(polarisConfig.getNamespace());
        instanceHeartbeatRequest.setService(service);
        instanceHeartbeatRequest.setHost(host);
        instanceHeartbeatRequest.setPort(port);
        instanceHeartbeatRequest.setToken(polarisConfig.getToken());
        providerAPI.heartbeat(instanceHeartbeatRequest);
    }

    public boolean watchService(String service, ServiceListener listener) {
        WatchServiceRequest watchServiceRequest = new WatchServiceRequest();
        watchServiceRequest.setNamespace(polarisConfig.getNamespace());
        watchServiceRequest.setService(service);
        watchServiceRequest.setListeners(Collections.singletonList(listener));
        return consumerAPI.watchService(watchServiceRequest).isSuccess();
    }

    public void unwatchService(String service, ServiceListener serviceListener) {
        UnWatchServiceRequest watchServiceRequest = UnWatchServiceRequest.UnWatchServiceRequestBuilder.anUnWatchServiceRequest()
                .namespace(polarisConfig.getNamespace())
                .service(service)
                .listeners(Collections.singletonList(serviceListener))
                .build();
        consumerAPI.unWatchService(watchServiceRequest);
    }

    /**
     * ??????CONSUMER_API??????????????????
     *
     * @param service ?????????service
     * @return Polaris?????????Instance??????
     */
    public Instance[] getAvailableInstances(String service) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to getInstances {}, polaris init failed", service);
            return null;
        }
        GetInstancesRequest request = new GetInstancesRequest();
        request.setNamespace(polarisConfig.getNamespace());
        request.setService(service);
        InstancesResponse instances = consumerAPI.getInstances(request);
        return instances.getInstances();
    }

    /**
     * ??????CONSUMER_API????????????????????????
     *
     * @param delay ?????????????????????????????????ms
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

    public List<Instance> route(String service, String method, Map<String, String> labels, List<Instance> instances) {
        ServiceKey serviceKey = new ServiceKey(polarisConfig.getNamespace(), service);
        DefaultServiceInstances defaultServiceInstances = new DefaultServiceInstances(serviceKey, instances);
        ServiceInfo serviceInfo = null;
        if (null != labels && labels.size() > 0) {
            serviceInfo = new ServiceInfo();
            serviceInfo.setMetadata(labels);
        }
        ProcessRoutersRequest request = new ProcessRoutersRequest();
        request.setDstInstances(defaultServiceInstances);
        request.setMethod(method);
        request.setSourceService(serviceInfo);
        ProcessRoutersResponse processRoutersResponse = routerAPI.processRouters(request);
        return processRoutersResponse.getServiceInstances().getInstances();
    }

    public Instance loadBalance(String service, String hashKey, List<Instance> instances) {
        ServiceKey serviceKey = new ServiceKey(polarisConfig.getNamespace(), service);
        DefaultServiceInstances defaultServiceInstances = new DefaultServiceInstances(serviceKey, instances);
        ProcessLoadBalanceRequest processLoadBalanceRequest = new ProcessLoadBalanceRequest();
        processLoadBalanceRequest.setDstInstances(defaultServiceInstances);
        Criteria criteria = new Criteria();
        criteria.setHashKey(hashKey);
        processLoadBalanceRequest.setCriteria(criteria);
        ProcessLoadBalanceResponse processLoadBalanceResponse = routerAPI.processLoadBalance(processLoadBalanceRequest);
        return processLoadBalanceResponse.getTargetInstance();
    }

    /**
     * ??????LIMIT_API??????????????????
     *
     * @param count ?????????????????????
     * @return ??????????????????false??????????????????????????????
     */
    public boolean getQuota(String service, String method, Map<String, String> labels, int count) {
        init();
        if (!inited.get()) {
            LOGGER.error("[POLARIS] fail to get quota, service:{}, method:{}, polaris init failed", service, method);
            throw new RuntimeException("polaris init failed");
        }
        QuotaRequest quotaRequest = new QuotaRequest();
        quotaRequest.setNamespace(polarisConfig.getNamespace());
        quotaRequest.setService(service);
        quotaRequest.setMethod(method);
        quotaRequest.setLabels(labels);
        quotaRequest.setCount(count);
        QuotaResponse quota = limitAPI.getQuota(quotaRequest);
        return quota.getCode() == QuotaResultCode.QuotaResultOk;
    }
}
