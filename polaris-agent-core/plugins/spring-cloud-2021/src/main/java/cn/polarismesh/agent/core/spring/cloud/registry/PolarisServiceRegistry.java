package cn.polarismesh.agent.core.spring.cloud.registry;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.polaris.PolarisSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * Polaris 服务注册实现类
 */
public class PolarisServiceRegistry implements ServiceRegistry<Registration>, Registry {

    private static final Logger log = LoggerFactory.getLogger(PolarisServiceRegistry.class);

    private PolarisAgentProperties polarisProperties;

    private PolarisRegistration polarisRegistration;

    public PolarisServiceRegistry(PolarisRegistration polarisRegistration) {
        this.polarisProperties = polarisRegistration.getPolarisProperties();
        this.polarisRegistration = polarisRegistration;
    }

    /**
     * 服务注册
     *
     * @param registration
     */
    @Override
    public void register(Registration registration) {
        String serviceId = polarisRegistration.getServiceId();
        if (serviceId == null || "".equals(serviceId)) {
            log.warn("No service to register for polaris client...");
            return;
        }
        try {
            // 注册实例
            // TODO: 2022/3/1 注册到Polaris上的服务协议
            PolarisSingleton.getPolarisOperation().register(
                    serviceId, polarisRegistration.getHost(), polarisRegistration.getPort(), polarisProperties.getProtocol(), null, 1, null);
        } catch (Exception e) {
            log.error("polaris registry, {} register failed...{},", serviceId, polarisRegistration, e);
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

        String serviceId = polarisRegistration.getServiceId();
        if (serviceId == null || "".equals(serviceId)) {
            log.warn("No dom to de-register for polaris client...");
            return;
        }

        try {
            PolarisSingleton.getPolarisOperation().deregister(serviceId, polarisRegistration.getHost(), polarisRegistration.getPort());
        } catch (Exception e) {
            log.error("ERR_POLARIS_DEREGISTER, de-register failed...{},", polarisRegistration, e);
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
//        String serviceName = registration.getServiceId();
//        InstancesResponse instancesResponse = polarisDiscoveryHandler.getInstances(serviceName);
//        Instance[] instances = instancesResponse.getInstances();
//        if (null == instances || instances.length == 0) {
//            return null;
//        }
//        for (Instance instance : instances) {
//            if (instance.getHost().equalsIgnoreCase(registration.getHost())
//                    && instance.getPort() == polarisProperties.getPort()) {
//                return instance.isHealthy() ? "UP" : "DOWN";
//            }
//        }
        return null;
    }

    @Override
    public void deregister() {
        this.deregister(null);
    }

    @Override
    public void close() {

    }

}
