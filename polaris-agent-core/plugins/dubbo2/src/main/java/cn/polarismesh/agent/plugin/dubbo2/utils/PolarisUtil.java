package cn.polarismesh.agent.plugin.dubbo2.utils;

import cn.polarismesh.agent.plugin.dubbo2.entity.InvokerMap;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.rpc.*;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.polarismesh.agent.plugin.dubbo2.constants.PolarisConstants.*;

/**
 * 实现Polaris相关逻辑的工具类
 */
public class PolarisUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisUtil.class);

    private static final ConsumerAPI CONSUMER_API = DiscoveryAPIFactory.createConsumerAPI();
    private static final ProviderAPI PROVIDER_API = DiscoveryAPIFactory.createProviderAPI();

    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    /**
     * 服务注册
     *
     * @param url Dubbo的URL对象，存有服务注册需要的相关信息
     */
    public static void register(URL url) {
        String namespace = System.getProperty("namespace", DEFAULT_NAMESPACE);
        String service = url.getServiceInterface();
        String host = url.getHost();
        int port = url.getPort();
        String ttlStr = System.getProperty("ttl");
        int ttl = StringUtil.isNumeric(ttlStr) ? Integer.parseInt(ttlStr) : TTL;
        Map<String, String> parameters = new HashMap<>(url.getParameters());
        paramFilter(parameters);

        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace(namespace);
        instanceRegisterRequest.setService(service);
        instanceRegisterRequest.setHost(host);
        instanceRegisterRequest.setPort(port);
        instanceRegisterRequest.setTtl(ttl);
        instanceRegisterRequest.setMetadata(parameters);
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
        String address = url.getAddress();
        InvokerMap.remove(address);
        HEARTBEAT_EXECUTOR.shutdown();
        deregister(url);
        PROVIDER_API.close();
    }

    /**
     * 调用PROVIDER_API实现服务反注册
     *
     * @param url Dubbo的URL对象，存有服务反注册需要的相关信息
     */
    private static void deregister(URL url) {
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
    }

    /**
     * 调用CONSUMER_API获取实例信息
     *
     * @param namespace 服务的namespace
     * @param service   服务的service
     * @return Polaris选择的Instance对象
     */
    public static Instance[] getTargetInstances(String namespace, String service) {
        LOGGER.info("namespace {}, service {}", namespace, service);
        GetAllInstancesRequest getAllInstancesRequest = new GetAllInstancesRequest();
        getAllInstancesRequest.setNamespace(namespace);
        getAllInstancesRequest.setService(service);
        LOGGER.info("request set complete");
        try {
            InstancesResponse oneInstance = CONSUMER_API.getAllInstance(getAllInstancesRequest);
            Instance[] instances = oneInstance.getInstances();
            if (instances == null || instances.length == 0) {
                LOGGER.error("instances is null or empty");
                return null;
            }
            LOGGER.info("instances count is {}", instances.length);
            return instances;
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
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
        serviceCallResult.setNamespace(System.getProperty("namespace", DEFAULT_NAMESPACE));
        serviceCallResult.setService(url.getServiceInterface());
        serviceCallResult.setHost(url.getHost());
        serviceCallResult.setPort(url.getPort());
        serviceCallResult.setDelay(delay);
        serviceCallResult.setRetStatus(null == throwable ? RetStatus.RetSuccess : RetStatus.RetFail);
        serviceCallResult.setRetCode(null != result ? 200 : -1);
        try {
            CONSUMER_API.updateServiceCallResult(serviceCallResult);
            LOGGER.info("success to call updateServiceCallResult");
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
