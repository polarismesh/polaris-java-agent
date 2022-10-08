package cn.polarismesh.agent.core.nacos.adapter;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

import cn.polarismesh.agent.core.nacos.constants.NacosConstants;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.event.InstancesChangeNotifier;
import com.alibaba.nacos.client.naming.remote.NamingClientProxy;
import com.alibaba.nacos.client.naming.remote.NamingClientProxyDelegate;
import com.alibaba.nacos.common.utils.ThreadUtils;
import java.util.Properties;
import java.util.Set;

/**
 * 自定义 NamingClientProxyAdapter.
 *
 * @author bruceppeng
 */
public class NamingClientProxyAdapter implements NamingClientProxy {

    private NamingClientProxy clientProxy;

    private NamingClientProxy targetClientProxy;

    private String targetNacosDomain;

    public NamingClientProxyAdapter(String namespace, ServiceInfoHolder serviceInfoHolder, Properties properties,
            InstancesChangeNotifier changeNotifier) throws NacosException {
        this.clientProxy = new NamingClientProxyDelegate(namespace, serviceInfoHolder, properties, changeNotifier);

        targetNacosDomain = System.getProperty(NacosConstants.TARGET_NACOS_SERVER_ADDR);
        //组装target nacos的properties配置信息
        Properties targetProperties = new Properties();
        targetProperties.putAll(properties);
        targetProperties.setProperty(PropertyKeyConst.SERVER_ADDR, targetNacosDomain);
        this.targetClientProxy = new NamingClientProxyDelegate(namespace, serviceInfoHolder, targetProperties, changeNotifier);
    }

    @Override
    public void registerService(String serviceName, String groupName, Instance instance) throws NacosException {
        clientProxy.registerService(serviceName, groupName, instance);
        targetClientProxy.registerService(serviceName, groupName, instance);
    }

    @Override
    public void deregisterService(String serviceName, String groupName, Instance instance) throws NacosException {
        clientProxy.deregisterService(serviceName, groupName, instance);
        targetClientProxy.deregisterService(serviceName, groupName, instance);
    }

    @Override
    public void updateInstance(String serviceName, String groupName, Instance instance) throws NacosException {

    }

    @Override
    public ServiceInfo queryInstancesOfService(String serviceName, String groupName, String clusters, int udpPort,
            boolean healthyOnly) throws NacosException {
        clientProxy.queryInstancesOfService(serviceName, groupName, clusters, udpPort, healthyOnly);
        targetClientProxy.queryInstancesOfService(serviceName, groupName, clusters, udpPort, healthyOnly);
        return ;
    }

    @Override
    public Service queryService(String serviceName, String groupName) throws NacosException {
        return null;
    }

    @Override
    public void createService(Service service, AbstractSelector selector) throws NacosException {

    }

    @Override
    public boolean deleteService(String serviceName, String groupName) throws NacosException {
        return false;
    }

    @Override
    public void updateService(Service service, AbstractSelector selector) throws NacosException {

    }

    @Override
    public ListView<String> getServiceList(int pageNo, int pageSize, String groupName, AbstractSelector selector)
            throws NacosException {
        ListView<String> result = clientProxy.getServiceList(pageNo, pageSize, groupName, selector);
        ListView<String> targetResult = targetClientProxy.getServiceList(pageNo, pageSize, groupName, selector);
        return ;
    }

    @Override
    public ServiceInfo subscribe(String serviceName, String groupName, String clusters) throws NacosException {
        clientProxy.subscribe(serviceName, groupName, clusters);
        targetClientProxy.subscribe(serviceName, groupName, clusters);
        return ;
    }

    @Override
    public void unsubscribe(String serviceName, String groupName, String clusters) throws NacosException {
        clientProxy.unsubscribe(serviceName, groupName, clusters);
        targetClientProxy.unsubscribe(serviceName, groupName, clusters);
    }

    @Override
    public boolean isSubscribed(String serviceName, String groupName, String clusters) throws NacosException {
        return clientProxy.isSubscribed(serviceName, groupName, clusters) || targetClientProxy.isSubscribed(serviceName, groupName, clusters) ;
    }

    @Override
    public void updateBeatInfo(Set<Instance> modifiedInstances) {
        clientProxy.updateBeatInfo(modifiedInstances);
        targetClientProxy.updateBeatInfo(modifiedInstances);
    }

    @Override
    public boolean serverHealthy() {
        return clientProxy.serverHealthy();
    }

    @Override
    public void shutdown() throws NacosException {
        clientProxy.shutdown();
        targetClientProxy.shutdown();
    }
}