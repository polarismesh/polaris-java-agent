package cn.polarismesh.agent.core.spring.cloud.discovery;

import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

/**
 * Polaris服务发现Client
 */
public class PolarisDiscoveryClient implements DiscoveryClient {

    /**
     * Polaris Discovery Agent Client Description.
     */
    public final String description = "Spring Cloud Polaris Agent Discovery Client";

    private final PolarisServiceDiscovery polarisServiceDiscovery;

    public PolarisDiscoveryClient(PolarisServiceDiscovery polarisServiceDiscovery) {
        this.polarisServiceDiscovery = polarisServiceDiscovery;
    }

    @Override
    public String description() {
        return description;
    }

    /**
     * 获取服务实例列表
     *
     * @param service
     * @return
     */
    @Override
    public List<ServiceInstance> getInstances(String service) {
        LogUtils.logInvoke(this, "getInstances");
        return polarisServiceDiscovery.getInstances(service);
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    @Override
    public List<String> getServices() {
        LogUtils.logInvoke(this, "getServices");
        return polarisServiceDiscovery.getServices();
    }

}
