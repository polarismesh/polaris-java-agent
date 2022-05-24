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

package cn.polarismesh.agent.plugin.dubbox.entity;

import cn.polarismesh.agent.plugin.dubbox.constants.PolarisConstants;
import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisSingleton;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.tencent.polaris.api.pojo.CircuitBreakerStatus;
import com.tencent.polaris.api.pojo.CircuitBreakerStatus.Status;
import com.tencent.polaris.api.pojo.DefaultInstance;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.StatusDimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceInvoker<T> implements Instance, Invoker<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceInvoker.class);

    private final Invoker<T> invoker;

    private final DefaultInstance defaultInstance;

    public InstanceInvoker(Invoker<T> invoker) {
        this.invoker = invoker;
        defaultInstance = new DefaultInstance();
        defaultInstance.setNamespace(PolarisSingleton.getPolarisConfig().getNamespace());
        URL url = invoker.getUrl();
        defaultInstance.setService(url.getServiceInterface());
        defaultInstance.setHost(url.getHost());
        defaultInstance.setPort(url.getPort());
        defaultInstance.setId(url.getParameter(PolarisConstants.KEY_ID));
        defaultInstance.setHealthy(Boolean.parseBoolean(url.getParameter(PolarisConstants.KEY_HEALTHY)));
        defaultInstance.setIsolated(Boolean.parseBoolean(url.getParameter(PolarisConstants.KEY_ISOLATED)));
        defaultInstance.setVersion(url.getParameter(Constants.VERSION_KEY));
        defaultInstance.setWeight(url.getParameter(Constants.WEIGHT_KEY, 100));
        String circuitBreakerStr = url.getParameter(PolarisConstants.KEY_CIRCUIT_BREAKER);
        Map<StatusDimension, CircuitBreakerStatus> statusDimensionCircuitBreakerStatusMap = stringToCircuitBreakers(
                circuitBreakerStr);
        defaultInstance.getCircuitBreakerStatuses().putAll(statusDimensionCircuitBreakerStatusMap);
        defaultInstance.setMetadata(url.getParameters());
        LOGGER.info("[POLARIS] construct instance from invoker, url {}, instance {}", url, defaultInstance);
    }

    @Override
    public Class<T> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public URL getUrl() {
        return invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return invoker.isAvailable();
    }

    @Override
    public void destroy() {
        invoker.destroy();
    }

    @Override
    public String getNamespace() {
        return defaultInstance.getNamespace();
    }

    @Override
    public String getService() {
        return defaultInstance.getService();
    }

    @Override
    public String getRevision() {
        return defaultInstance.getRevision();
    }

    @Override
    public CircuitBreakerStatus getCircuitBreakerStatus() {
        return defaultInstance.getCircuitBreakerStatus();
    }

    @Override
    public Collection<StatusDimension> getStatusDimensions() {
        return defaultInstance.getStatusDimensions();
    }

    @Override
    public CircuitBreakerStatus getCircuitBreakerStatus(StatusDimension statusDimension) {
        return defaultInstance.getCircuitBreakerStatus(statusDimension);
    }

    @Override
    public boolean isHealthy() {
        return defaultInstance.isHealthy();
    }

    @Override
    public boolean isIsolated() {
        return defaultInstance.isIsolated();
    }

    @Override
    public String getProtocol() {
        return defaultInstance.getProtocol();
    }

    @Override
    public String getId() {
        return defaultInstance.getId();
    }

    @Override
    public String getHost() {
        return defaultInstance.getHost();
    }

    @Override
    public int getPort() {
        return defaultInstance.getPort();
    }

    @Override
    public String getVersion() {
        return defaultInstance.getVersion();
    }

    @Override
    public Map<String, String> getMetadata() {
        return defaultInstance.getMetadata();
    }

    @Override
    public boolean isEnableHealthCheck() {
        return defaultInstance.isEnableHealthCheck();
    }

    @Override
    public String getRegion() {
        return defaultInstance.getRegion();
    }

    @Override
    public String getZone() {
        return defaultInstance.getZone();
    }

    @Override
    public String getCampus() {
        return defaultInstance.getCampus();
    }

    @Override
    public int getPriority() {
        return defaultInstance.getPriority();
    }

    @Override
    public int getWeight() {
        return defaultInstance.getWeight();
    }

    @Override
    public String getLogicSet() {
        return defaultInstance.getLogicSet();
    }

    @Override
    public int compareTo(Instance o) {
        return defaultInstance.compareTo(o);
    }

    private static final String SEP_CIRCUIT_BREAKER = ",";

    private static final String SEP_CIRCUIT_BREAKER_VALUE = ":";

    public static String circuitBreakersToString(Instance instance) {
        List<String> values = new ArrayList<>();
        Collection<StatusDimension> statusDimensions = instance.getStatusDimensions();
        if (null != statusDimensions && statusDimensions.size() > 0) {
            for (StatusDimension statusDimension : statusDimensions) {
                CircuitBreakerStatus circuitBreakerStatus = instance.getCircuitBreakerStatus(statusDimension);
                if (null != circuitBreakerStatus) {
                    values.add(
                            statusDimension.getMethod() + SEP_CIRCUIT_BREAKER_VALUE + circuitBreakerStatus.getStatus()
                                    .name());
                }
            }
        }
        if (values.isEmpty()) {
            return "";
        }
        return String.join(SEP_CIRCUIT_BREAKER, values.toArray(new String[0]));
    }

    public static Map<StatusDimension, CircuitBreakerStatus> stringToCircuitBreakers(String value) {
        Map<StatusDimension, CircuitBreakerStatus> values = new HashMap<>();
        if (null == value || value.length() == 0) {
            return values;
        }

        String[] tokens = value.split(SEP_CIRCUIT_BREAKER);
        for (String token : tokens) {
            String[] splits = token.split(SEP_CIRCUIT_BREAKER_VALUE);
            if (splits.length != 2) {
                continue;
            }
            StatusDimension dimension = new StatusDimension(splits[0], null);
            CircuitBreakerStatus circuitBreakerStatus = new CircuitBreakerStatus(
                    "", Status.valueOf(splits[1]), 0);
            values.put(dimension, circuitBreakerStatus);
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstanceInvoker)) {
            return false;
        }
        InstanceInvoker<?> that = (InstanceInvoker<?>) o;
        return Objects.equals(defaultInstance, that.defaultInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultInstance);
    }
}
