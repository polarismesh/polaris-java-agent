package cn.polarismesh.agent.plugin.dubbo2.utils;

import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceHeartbeatRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.apache.dubbo.common.URL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PolarisUtil {
    private static final String DEFAULT_NAMESPACE = "default";
    private static final int TTL = 5;

    private static final ProviderAPI PROVIDER_API = DiscoveryAPIFactory.createProviderAPI();

    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    // 服务注册
    public static void register(URL url) {
        String namespace = url.getParameter("polaris.namespace", DEFAULT_NAMESPACE);
        String service = url.getServiceInterface();
        String host = url.getHost();
        int port = url.getPort();
        String ttlStr = url.getParameter("polaris.ttl");
        int ttl = StringUtil.isNumeric(ttlStr) ? Integer.parseInt(ttlStr) : TTL;

        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace(namespace);
        instanceRegisterRequest.setService(service);
        instanceRegisterRequest.setHost(host);
        instanceRegisterRequest.setPort(port);
        instanceRegisterRequest.setTtl(ttl);
        instanceRegisterRequest.setMetadata(url.getParameters());
        InstanceRegisterResponse instanceRegisterResponse = PROVIDER_API.register(instanceRegisterRequest);
        System.out.println("response after register is " + instanceRegisterResponse);
        // 注册完成后执行心跳上报
        System.out.println("heartbeat task start, ttl is " + ttl);
        HEARTBEAT_EXECUTOR.scheduleWithFixedDelay(new HeartbeatTask(namespace, service, host, port), ttl, ttl, TimeUnit.SECONDS);
    }

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

    // 心跳上报
    private static void heartbeat(String namespace, String service, String host, int port) {
        InstanceHeartbeatRequest heartbeatRequest = new InstanceHeartbeatRequest();
        heartbeatRequest.setNamespace(namespace);
        heartbeatRequest.setService(service);
        heartbeatRequest.setHost(host);
        heartbeatRequest.setPort(port);
        PROVIDER_API.heartbeat(heartbeatRequest);
        System.out.printf("heartbeat instance, address is %s:%d%n", host, port);
    }

    // 服务下线逻辑
    public static void shutdown(URL url) {
        HEARTBEAT_EXECUTOR.shutdown();
        deregister(url);
        PROVIDER_API.close();
    }

    // 服务反注册
    private static void deregister(URL url) {
        String namespace = url.getParameter("polaris.namespace", DEFAULT_NAMESPACE);
        InstanceDeregisterRequest deregisterRequest = new InstanceDeregisterRequest();
        deregisterRequest.setNamespace(namespace);
        deregisterRequest.setService(url.getServiceInterface());
        deregisterRequest.setHost(url.getHost());
        deregisterRequest.setPort(url.getPort());
        PROVIDER_API.deRegister(deregisterRequest);
        System.out.printf("deregister instance, address is %s:%d%n", url.getHost(), url.getPort());
    }
}
