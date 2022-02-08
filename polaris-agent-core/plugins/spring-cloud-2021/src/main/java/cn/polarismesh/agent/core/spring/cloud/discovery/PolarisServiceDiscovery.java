package cn.polarismesh.agent.core.spring.cloud.discovery;

import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.rpc.InstancesResponse;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Polaris服务发现实现类
 */
public class PolarisServiceDiscovery {

    private final PolarisDiscoveryHandler polarisDiscoveryHandler = new PolarisDiscoveryHandler();

    /**
     * Return all instances for the given service.
     *
     * @param serviceId id of service
     * @return list of instances
     * @throws PolarisException polarisException
     */
    public List<ServiceInstance> getInstances(String serviceId) throws PolarisException {
        LogUtils.logInvoke(this, "getInstances");
        List<ServiceInstance> instances = new ArrayList<>();
        InstancesResponse filteredInstances = polarisDiscoveryHandler.getFilteredInstances(serviceId);
        ServiceInstances serviceInstances = filteredInstances.toServiceInstances();
        for (Instance instance : serviceInstances.getInstances()) {
            instances.add(new PolarisServiceInstance(instance));
        }
        return instances;
    }

    /**
     * @return list of service names
     * @throws PolarisException polarisException
     */
    public List<String> getServices() throws PolarisException {
        LogUtils.logInvoke(this, "getServices");
        return Collections.emptyList();
    }

}
