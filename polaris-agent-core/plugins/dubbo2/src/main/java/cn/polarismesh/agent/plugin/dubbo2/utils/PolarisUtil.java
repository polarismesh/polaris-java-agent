package cn.polarismesh.agent.plugin.dubbo2.utils;

import cn.polarismesh.agent.plugin.dubbo2.entity.InvokerMap;
import cn.polarismesh.agent.plugin.dubbo2.entity.Properties;
import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.pojo.ServiceInfo;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.rpc.*;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.polarismesh.agent.plugin.dubbo2.constants.PolarisConstants.DEFAULT_NAMESPACE;
import static cn.polarismesh.agent.plugin.dubbo2.constants.PolarisConstants.FILTERED_PARAMS;
import static org.apache.dubbo.common.constants.CommonConstants.PATH_KEY;

/**
 * 实现Polaris相关逻辑的工具类
 */
public class PolarisUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisUtil.class);

    private static final Properties properties = Properties.getInstance();

    private static final Configuration CONFIG = initConfig();

    private static final ConsumerAPI CONSUMER_API = DiscoveryAPIFactory.createConsumerAPIByConfig(CONFIG);
    private static final ProviderAPI PROVIDER_API = DiscoveryAPIFactory.createProviderAPIByConfig(CONFIG);

    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    /**
     * 初始化配置，绑定polaris地址
     *
     * @return Configuration对象
     */
    private static Configuration initConfig() {
        ConfigurationImpl configuration = (ConfigurationImpl) ConfigAPIFactory.defaultConfig();
        configuration.setDefault();
        configuration.getGlobal().getServerConnector().setAddresses(Collections.singletonList(properties.getAddress()));
        return configuration;
    }

    /**
     * 服务注册
     *
     * @param url Dubbo的URL对象，存有服务注册需要的相关信息
     */
    public static void register(URL url) {
        String namespace = properties.getNamespace();
        String service = url.getServiceInterface();
        String host = url.getHost();
        int port = url.getPort();
        int ttl = properties.getTtl();
        Map<String, String> parameters = new HashMap<>(url.getParameters());
        paramFilter(parameters);
        parameters.put(PATH_KEY, url.getPath());

        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace(namespace);
        instanceRegisterRequest.setService(service);
        instanceRegisterRequest.setHost(host);
        instanceRegisterRequest.setPort(port);
        instanceRegisterRequest.setTtl(ttl);
        instanceRegisterRequest.setMetadata(parameters);
        instanceRegisterRequest.setProtocol(url.getProtocol());
        try {
            InstanceRegisterResponse instanceRegisterResponse = PROVIDER_API.register(instanceRegisterRequest);
            LOGGER.info("response after register is {}", instanceRegisterResponse);
            // 注册完成后执行心跳上报
            LOGGER.info("heartbeat task start, ttl is {}", ttl);
            HEARTBEAT_EXECUTOR.scheduleWithFixedDelay(new HeartbeatTask(namespace, service, host, port), ttl, ttl, TimeUnit.SECONDS);
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * 根据注册的key列表过滤掉不需要的meta-data
     *
     * @param parameters 待过滤的map
     */
    private static void paramFilter(Map<String, String> parameters) {
        for (String key : FILTERED_PARAMS) {
            parameters.remove(key);
        }
    }

    /**
     * 心跳上报线程
     */
    private static class HeartbeatTask implements Runnable {

        private final String namespace;

        private final String service;

        private final String host;

        private final int port;

        private HeartbeatTask(String namespace, String service, String host, int port) {
            this.namespace = namespace;
            this.service = service;
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            PolarisUtil.heartbeat(namespace, service, host, port);
        }
    }

    /**
     * 调用PROVIDER_API进行心跳上报
     *
     * @param namespace 服务的namespace
     * @param service   服务的service
     * @param host      服务的ip
     * @param port      服务的端口
     */
    private static void heartbeat(String namespace, String service, String host, int port) {
        InstanceHeartbeatRequest heartbeatRequest = new InstanceHeartbeatRequest();
        heartbeatRequest.setNamespace(namespace);
        heartbeatRequest.setService(service);
        heartbeatRequest.setHost(host);
        heartbeatRequest.setPort(port);
        try {
            PROVIDER_API.heartbeat(heartbeatRequest);
            LOGGER.info("heartbeat instance, address is {}:{}", host, port);
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * 服务下线，包括移除map信息、停止心跳上报、服务反注册、关闭PROVIDER_API
     *
     * @param url Dubbo的URL对象，存有服务反注册需要的相关信息
     */
    public static void shutdown(URL url) {
        InvokerMap.removeAll();
        HEARTBEAT_EXECUTOR.shutdown();
        String namespace = url.getParameter("polaris.namespace", DEFAULT_NAMESPACE);
        InstanceDeregisterRequest deregisterRequest = new InstanceDeregisterRequest();
        deregisterRequest.setNamespace(namespace);
        deregisterRequest.setService(url.getServiceInterface());
        deregisterRequest.setHost(url.getHost());
        deregisterRequest.setPort(url.getPort());
        try {
            PROVIDER_API.deRegister(deregisterRequest);
            LOGGER.info("deregister instance, address is {}:{}", url.getHost(), url.getPort());
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
        }
        PROVIDER_API.close();
    }

    /**
     * 调用CONSUMER_API获取实例信息
     *
     * @param namespace 服务的namespace
     * @param service   服务的service
     * @return Polaris选择的Instance对象
     */
    public static ServiceInstances getTargetInstances(String namespace, String service) {
        LOGGER.info("namespace {}, service {}", namespace, service);
        try {
            // init ServiceInfo
            ServiceInfo serviceInfo = initServiceInfo(namespace, service);
            // get available instances
            GetInstancesRequest getInstancesRequest = new GetInstancesRequest();
            getInstancesRequest.setNamespace(namespace);
            getInstancesRequest.setService(service);
            getInstancesRequest.setServiceInfo(serviceInfo);
            InstancesResponse instancesResp = CONSUMER_API.getInstances(getInstancesRequest);
            ServiceInstances dstInstances = instancesResp.toServiceInstances();
            if (dstInstances == null || dstInstances.getInstances().isEmpty()) {
                LOGGER.error("instances is null or empty");
                return null;
            }
            LOGGER.info("instances count after routing is {}", dstInstances.getInstances().size());
            return dstInstances;
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static ServiceInfo initServiceInfo(String namespace, String service) {
        ServiceInfo serviceInfo = new ServiceInfo();
        String tag = RpcContext.getContext().getAttachment(CommonConstants.TAG_KEY);
        // 只有当用户设置了tag时才进行绑定，否则路由时会报错
        if (null != tag) {
            LOGGER.info("tag: {}={}", CommonConstants.TAG_KEY, tag);
            Map<String, String> metadata = new HashMap<>();
            metadata.put(CommonConstants.TAG_KEY, tag);
            serviceInfo.setMetadata(metadata);
        }
        serviceInfo.setNamespace(namespace);
        serviceInfo.setService(service);
        return serviceInfo;
    }

    /**
     * 获取全量Instances，用于PolarisRegistry更新Instances信息
     */
    public static ServiceInstances getAllInstances(String namespace, String service) {
        GetAllInstancesRequest getAllInstancesRequest = new GetAllInstancesRequest();
        getAllInstancesRequest.setNamespace(namespace);
        getAllInstancesRequest.setService(service);
        InstancesResponse allInstancesResp = CONSUMER_API.getAllInstance(getAllInstancesRequest);
        return allInstancesResp.toServiceInstances();
    }

    /**
     * 调用CONSUMER_API上报服务请求结果
     *
     * @param url       Dubbo的URL对象，存有host，port等相关信息
     * @param delay     本次服务调用延迟，单位ms
     * @param result    本次服务调用结果
     * @param throwable 本次服务调用的异常，无异常则为null
     */
    public static void reportInvokeResult(URL url, long delay, Result result, Throwable throwable) {
        ServiceCallResult serviceCallResult = new ServiceCallResult();
        serviceCallResult.setNamespace(properties.getNamespace());
        serviceCallResult.setService(url.getServiceInterface());
        serviceCallResult.setHost(url.getHost());
        serviceCallResult.setPort(url.getPort());
        serviceCallResult.setDelay(delay);
        serviceCallResult.setRetStatus((null == throwable && null != result && !result.hasException()) ? RetStatus.RetSuccess : RetStatus.RetFail);
        serviceCallResult.setRetCode((null == throwable && null != result && !result.hasException()) ? 200 : -1);
        try {
            CONSUMER_API.updateServiceCallResult(serviceCallResult);
            LOGGER.info("success to call updateServiceCallResult, status:{}", serviceCallResult.getRetStatus());
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
