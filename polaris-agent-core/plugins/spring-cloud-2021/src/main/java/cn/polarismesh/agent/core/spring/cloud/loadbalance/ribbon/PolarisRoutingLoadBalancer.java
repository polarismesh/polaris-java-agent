package cn.polarismesh.agent.core.spring.cloud.loadbalance.ribbon;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.router.PolarisServiceRouter;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import com.tencent.polaris.api.pojo.*;
import com.tencent.polaris.router.api.core.RouterAPI;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Polaris Ribbon路由负载均衡器
 */
public class PolarisRoutingLoadBalancer extends DynamicServerListLoadBalancer<Server> {

    private final RouterAPI routerAPI;

    private final PolarisAgentProperties polarisAgentProperties;

    public PolarisRoutingLoadBalancer(IClientConfig config, IRule rule, IPing ping,
                                      ServerList<Server> serverList, RouterAPI routerAPI,
                                      PolarisAgentProperties polarisAgentProperties) {
        super(config, rule, ping, serverList, null, new PollingServerListUpdater());
        this.routerAPI = routerAPI;
        this.polarisAgentProperties = polarisAgentProperties;
    }

    /**
     * 获取可达的实例列表
     *
     * @return
     */
    @Override
    public List<Server> getReachableServers() {
        LogUtils.logInvoke(this, "getReachableServers");
        List<Server> allServers = super.getAllServers();
        ServiceInstances serviceInstances = null;
        if (allServers.get(0) instanceof PolarisServer) {
            serviceInstances = ((PolarisServer) allServers.get(0)).getServiceInstances();
        } else {
            String serviceName;
            // notice the difference between different service registries
            if (StringUtils.isNotBlank(allServers.get(0).getMetaInfo().getServiceIdForDiscovery())) {
                serviceName = allServers.get(0).getMetaInfo().getServiceIdForDiscovery();
            } else {
                serviceName = allServers.get(0).getMetaInfo().getAppName();
            }
            if (StringUtils.isBlank(serviceName)) {
                throw new IllegalStateException(
                        "PolarisRoutingLoadBalancer only Server with AppName or ServiceIdForDiscovery attribute");
            }
            ServiceKey serviceKey = new ServiceKey(polarisAgentProperties.getNamespace(), serviceName);
            List<Instance> instances = new ArrayList<>(8);
            for (Server server : allServers) {
                DefaultInstance instance = new DefaultInstance();
                instance.setNamespace(polarisAgentProperties.getNamespace());
                instance.setService(serviceName);
                instance.setHealthy(server.isAlive());
                instance.setProtocol(server.getScheme());
                instance.setId(server.getId());
                instance.setHost(server.getHost());
                instance.setPort(server.getPort());
                instance.setZone(server.getZone());
                instance.setWeight(100);
                instances.add(instance);
            }
            serviceInstances = new DefaultServiceInstances(serviceKey, instances);
        }

        ServiceInstances filteredServiceInstances = PolarisServiceRouter.getRoutedServiceInstance(serviceInstances);
        List<Server> filteredInstances = new ArrayList<>();
        for (Instance instance : filteredServiceInstances.getInstances()) {
            filteredInstances.add(new PolarisServer(serviceInstances, instance));
        }
        return filteredInstances;
    }

    /**
     * 获取所有实例列表
     *
     * @return
     */
    @Override
    public List<Server> getAllServers() {
        LogUtils.logInvoke(this, "getAllServers");
        return getReachableServers();
    }

    @Override
    public String toString() {
        return "PolarisRoutingLoadBalancer{" +
                "routerAPI=" + routerAPI +
                ", rule=" + rule +
                ", ping=" + ping +
                '}';
    }
}
