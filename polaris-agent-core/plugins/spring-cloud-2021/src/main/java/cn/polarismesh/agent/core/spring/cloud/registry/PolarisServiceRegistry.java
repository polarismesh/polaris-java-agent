package cn.polarismesh.agent.core.spring.cloud.registry;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import cn.polarismesh.agent.core.spring.cloud.discovery.PolarisDiscoveryHandler;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceHeartbeatRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.client.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * Polaris 服务注册实现类
 */
public class PolarisServiceRegistry implements ServiceRegistry<Registration>, Registry {

    private static final Logger log = LoggerFactory.getLogger(PolarisServiceRegistry.class);

    private static final int ttl = 5;

    private final ScheduledExecutorService heartbeatExecutor;

    private PolarisAgentProperties polarisProperties;

    private PolarisRegistration polarisRegistration;

    private PolarisDiscoveryHandler polarisDiscoveryHandler;

    public PolarisServiceRegistry(PolarisRegistration polarisRegistration) {
        ScheduledThreadPoolExecutor heartbeatExecutor = new ScheduledThreadPoolExecutor(0,
                new NamedThreadFactory("spring-cloud-heartbeat"));
        heartbeatExecutor.setMaximumPoolSize(1);
        this.heartbeatExecutor = heartbeatExecutor;
        this.polarisProperties = polarisRegistration.getPolarisProperties();
        this.polarisRegistration = polarisRegistration;
        this.polarisDiscoveryHandler = new PolarisDiscoveryHandler();
    }

    /**
     * 服务注册
     *
     * @param registration
     */
    @Override
    public void register(Registration registration) {
        if (StringUtils.isEmpty(polarisRegistration.getServiceId())) {
            log.warn("No service to register for polaris client...");
            return;
        }
        // 注册实例
        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace(polarisProperties.getNamespace());
        instanceRegisterRequest.setService(polarisRegistration.getServiceId());
        instanceRegisterRequest.setHost(polarisRegistration.getHost());
        instanceRegisterRequest.setPort(polarisRegistration.getPort());
        instanceRegisterRequest.setProtocol(polarisProperties.getProtocol());
        if (null != heartbeatExecutor) {
            instanceRegisterRequest.setTtl(ttl);
        }
        try {
            ProviderAPI providerClient = PolarisAPIFactory.getProviderApi();
            providerClient.register(instanceRegisterRequest);
            log.info("Polaris register success with address host:{}, port:{}", polarisRegistration.getHost(), polarisRegistration.getPort());
            if (null != heartbeatExecutor) {
                InstanceHeartbeatRequest heartbeatRequest = new InstanceHeartbeatRequest();
                BeanUtils.copyProperties(instanceRegisterRequest, heartbeatRequest);
                // 添加反注册Hook
                //注册成功后开始启动心跳线程
                heartbeat();
            }
        } catch (Exception e) {
            log.error("polaris registry, {} register failed...{},", polarisRegistration.getServiceId(), polarisRegistration, e);
            rethrowRuntimeException(e);
        }
    }

    /**
     * 服务反注册
     *
     * @param registration
     */
    @Override
    public void deregister(Registration registration) {
        log.info("De-registering from Polaris Server now...");

        if (StringUtils.isEmpty(polarisRegistration.getServiceId())) {
            log.warn("No dom to de-register for polaris client...");
            return;
        }

        InstanceDeregisterRequest deRegisterRequest = new InstanceDeregisterRequest();
        deRegisterRequest.setNamespace(polarisProperties.getNamespace());
        deRegisterRequest.setService(polarisRegistration.getServiceId());
        deRegisterRequest.setHost(polarisRegistration.getHost());
        deRegisterRequest.setPort(polarisRegistration.getPort());

        try {
            ProviderAPI providerClient = PolarisAPIFactory.getProviderApi();
            providerClient.deRegister(deRegisterRequest);
        } catch (Exception e) {
            log.error("ERR_POLARIS_DEREGISTER, de-register failed...{},", polarisRegistration, e);
        } finally {
            if (null != heartbeatExecutor) {
                heartbeatExecutor.shutdown();
            }
        }
        log.info("De-registration finished.");
    }

    @Override
    public void register() {
        this.register(null);
    }

    @Override
    public void setStatus(Registration registration, String status) {

    }

    @Override
    public Object getStatus(Registration registration) {
        String serviceName = registration.getServiceId();
        InstancesResponse instancesResponse = polarisDiscoveryHandler.getInstances(serviceName);
        Instance[] instances = instancesResponse.getInstances();
        if (null == instances || instances.length == 0) {
            return null;
        }
        for (Instance instance : instances) {
            if (instance.getHost().equalsIgnoreCase(registration.getHost())
                    && instance.getPort() == polarisProperties.getPort()) {
                return instance.isHealthy() ? "UP" : "DOWN";
            }
        }
        return null;
    }

    @Override
    public void deregister() {
        this.deregister(null);
    }

    @Override
    public void close() {

    }

    private void heartbeat() {
        String host = polarisProperties.getHost();
        Integer port = polarisProperties.getPort();
        InstanceHeartbeatRequest heartbeatRequest = new InstanceHeartbeatRequest();
        heartbeatRequest.setNamespace(polarisProperties.getNamespace());
        heartbeatRequest.setService(polarisProperties.getService());
        heartbeatRequest.setHost(host);
        heartbeatRequest.setPort(port);
        try {
            heartbeatExecutor.scheduleWithFixedDelay(
                    () -> {
                        PolarisAPIFactory.getProviderApi().heartbeat(heartbeatRequest);
                        log.info("heartbeat instance, address is {}:{}", host, port);
                    },
                    ttl, ttl, TimeUnit.SECONDS);
        } catch (PolarisException e) {
            log.error(e.getMessage());
        }
    }
}
