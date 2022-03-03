package cn.polarismesh.agent.core.spring.cloud.discovery;

import cn.polarismesh.agent.core.spring.cloud.polaris.PolarisSingleton;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Polaris服务发现实现类
 */
public class PolarisServiceDiscovery {

    /**
     * Return all instances for the given service.
     *
     * @param serviceId id of service
     * @return list of instances
     */
    public List<ServiceInstance> getInstances(String serviceId) {
        LogUtils.logInvoke(this, "getInstances");
        List<ServiceInstance> instances = new ArrayList<>();
        List<?> availableInstances = PolarisSingleton.getPolarisOperation().getAvailableInstances(serviceId, null);

        for (Object availableInstance : availableInstances) {
            instances.add(new PolarisServiceInstance(availableInstance, serviceId));
        }
        return instances;
    }

    /**
     * @return list of service names
     */
    public List<String> getServices() {
        LogUtils.logInvoke(this, "getServices");
        return Collections.emptyList();
    }

}
