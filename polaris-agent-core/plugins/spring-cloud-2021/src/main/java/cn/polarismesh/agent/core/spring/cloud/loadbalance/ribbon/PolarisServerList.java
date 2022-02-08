package cn.polarismesh.agent.core.spring.cloud.loadbalance.ribbon;

import cn.polarismesh.agent.core.spring.cloud.discovery.PolarisDiscoveryHandler;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.rpc.InstancesResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Polaris Ribbon Server 实现类
 */
public class PolarisServerList extends AbstractServerList<Server> {

    private String serviceId;

    private PolarisDiscoveryHandler polarisDiscoveryHandler;

    public PolarisServerList(PolarisDiscoveryHandler polarisDiscoveryHandler) {
        this.polarisDiscoveryHandler = polarisDiscoveryHandler;
    }

    @Override
    public List<Server> getInitialListOfServers() {
        LogUtils.logInvoke(this, "getInitialListOfServers");
        return getServers();
    }

    @Override
    public List<Server> getUpdatedListOfServers() {
        LogUtils.logInvoke(this, "getUpdatedListOfServers");
        return getServers();
    }

    private List<Server> getServers() {
        InstancesResponse filteredInstances = polarisDiscoveryHandler.getFilteredInstances(serviceId);
        ServiceInstances serviceInstances = filteredInstances.toServiceInstances();
        List<Server> polarisServers = new ArrayList<>();
        for (Instance instance : serviceInstances.getInstances()) {
            polarisServers.add(new PolarisServer(serviceInstances, instance));
        }
        return polarisServers;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.serviceId = iClientConfig.getClientName();
    }

}
