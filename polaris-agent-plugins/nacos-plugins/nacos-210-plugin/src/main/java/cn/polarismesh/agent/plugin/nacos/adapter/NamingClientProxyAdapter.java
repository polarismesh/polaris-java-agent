package cn.polarismesh.agent.plugin.nacos.adapter;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import cn.polarismesh.agent.plugin.nacos.route.NearbyRouter;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.event.InstancesChangeNotifier;
import com.alibaba.nacos.client.naming.remote.NamingClientProxy;
import com.alibaba.nacos.client.naming.remote.NamingClientProxyDelegate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * 自定义 NamingClientProxyAdapter.
 *
 * @author bruceppeng
 */
public class NamingClientProxyAdapter implements NamingClientProxy {

    private NamingClientProxy clientProxy;

    private NamingClientProxy otherClientProxy;

    private String otherNacosDomain;

    private String nacosClusterName;

    private NearbyRouter nearbyRouter;

    public NamingClientProxyAdapter(String namespace, ServiceInfoHolder serviceInfoHolder, Properties properties,
            InstancesChangeNotifier changeNotifier) throws NacosException {
        this.clientProxy = new NamingClientProxyDelegate(namespace, serviceInfoHolder, properties, changeNotifier);
        this.otherNacosDomain = System.getProperty(NacosConstants.OTHER_NACOS_SERVER_ADDR);
        this.nacosClusterName = System.getProperty(NacosConstants.NACOS_CLUSTER_NAME);

        Objects.requireNonNull(this.otherNacosDomain, "other nacos server addr can not be empty");
        Objects.requireNonNull(this.nacosClusterName, "nacos cluster name can not be empty");
        this.nearbyRouter = NearbyRouter.getRouter();
        this.nearbyRouter.init();

        //组装other nacos的properties配置信息
        Properties otherProperties = new Properties();
        otherProperties.putAll(properties);
        otherProperties.setProperty(PropertyKeyConst.SERVER_ADDR, otherNacosDomain);
        this.otherClientProxy = new NamingClientProxyDelegate(namespace, serviceInfoHolder, otherProperties,
                changeNotifier);

    }

    @Override
    public void registerService(String serviceName, String groupName, Instance instance) throws NacosException {
        fillMetadata(instance);
        try {
            clientProxy.registerService(serviceName, groupName, instance);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy registerService err.", exp);
        }
        try {
            otherClientProxy.registerService(serviceName, groupName, instance);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy registerService err.", exp);
        }
    }

    /**
     * fillMetadata 补充元数据信息.
     *
     * @param instance
     */
    private void fillMetadata(Instance instance) {
        instance.addMetadata(NacosConstants.NACOS_CLUSTER_NAME, this.nacosClusterName);
    }

    @Override
    public void deregisterService(String serviceName, String groupName, Instance instance) throws NacosException {
        try {
            clientProxy.deregisterService(serviceName, groupName, instance);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy deregisterService err.", exp);
        }
        try {
            otherClientProxy.deregisterService(serviceName, groupName, instance);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy deregisterService err.", exp);
        }
    }

    @Override
    public void updateInstance(String serviceName, String groupName, Instance instance) throws NacosException {

    }

    @Override
    public ServiceInfo queryInstancesOfService(String serviceName, String groupName, String clusters, int udpPort,
            boolean healthyOnly) throws NacosException {
        ServiceInfo serviceInfo = null;
        try {
            serviceInfo = clientProxy.queryInstancesOfService(serviceName, groupName, clusters, udpPort, healthyOnly);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy queryInstancesOfService err.", exp);
        }
        ServiceInfo otherServiceInfo = null;
        try {
            otherServiceInfo = otherClientProxy
                    .queryInstancesOfService(serviceName, groupName, clusters, udpPort, healthyOnly);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy queryInstancesOfService err.", exp);
        }

        return mergeInstances(serviceInfo, otherServiceInfo);
    }

    /**
     * 合并两个nacos server的实例列表
     *
     * @param serviceInfo
     * @param otherServiceInfo
     */
    private ServiceInfo mergeInstances(ServiceInfo serviceInfo, ServiceInfo otherServiceInfo) {

        if (otherServiceInfo == null) {
            return serviceInfo;
        }

        if (serviceInfo == null) {
            return otherServiceInfo;
        }

        try {
            List<Instance> hosts = serviceInfo.getHosts();
            List<Instance> otherHosts = otherServiceInfo.getHosts();

            Map<String, Instance> hostMap = new HashMap<String, Instance>(hosts.size());
            for (Instance host : hosts) {
                hostMap.put(host.toInetAddr(), host);
            }

            for (Instance host : otherHosts) {
                String inetAddr = host.toInetAddr();
                if (hostMap.get(inetAddr) == null) {
                    hosts.add(host);
                }
            }
            // 对hosts进行过滤
            List<Instance> finalHosts = filterInstances(hosts);
            serviceInfo.setHosts(finalHosts);
        } catch (Exception exp) {
            NAMING_LOGGER.error("NamingClientProxyAdapter mergeInstances request {} failed.", otherNacosDomain, exp);
        }
        return serviceInfo;

    }

    /**
     * filterInstances 对实例列表进行过滤筛选.
     *
     * @param hosts
     * @return
     */
    private List<Instance> filterInstances(List<Instance> hosts) {

        // 针对服务实例做特殊处理，如果开启同nacos集群优先，则优先返回同nacos集群的实例
        if (!nearbyRouter.isEnable()) {
            return hosts;
        }

        List<Instance> finalHosts = Lists.newArrayList();

        if (nearbyRouter.isNearbyNacosCluster()) {
            filterByNearbyNacosCluster(hosts, finalHosts);
        }

        if (finalHosts.isEmpty()) {
            return hosts;
        }
        return finalHosts;
    }

    /**
     * filterByNearbyNacosCluster NearbyNacosCluster方式对实例列表进行过滤筛选.
     *
     * @param hosts
     * @param finalHosts
     * @return
     */
    private void filterByNearbyNacosCluster(List<Instance> hosts, List<Instance> finalHosts) {
        for (Instance instance : hosts) {
            String nacosClusterName = Optional.ofNullable(instance.getMetadata()).orElse(Maps.newHashMap())
                    .get(NacosConstants.NACOS_CLUSTER_NAME);
            if (this.nacosClusterName.equals(nacosClusterName)) {
                finalHosts.add(instance);
            }
        }
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
        return clientProxy.getServiceList(pageNo, pageSize, groupName, selector);
    }


    @Override
    public ServiceInfo subscribe(String serviceName, String groupName, String clusters) throws NacosException {
        ServiceInfo serviceInfo = null;
        try {
            serviceInfo = clientProxy.subscribe(serviceName, groupName, clusters);
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy subscribe err.", exp);
        }

        ServiceInfo otherServiceInfo = null;
        try {
            otherServiceInfo = otherClientProxy.subscribe(serviceName, groupName, clusters);
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy subscribe err.", exp);
        }
        return mergeInstances(serviceInfo, otherServiceInfo);
    }

    @Override
    public void unsubscribe(String serviceName, String groupName, String clusters) throws NacosException {
        try {
            clientProxy.unsubscribe(serviceName, groupName, clusters);
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy unsubscribe err.", exp);
        }

        try {
            otherClientProxy.unsubscribe(serviceName, groupName, clusters);
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy unsubscribe err.", exp);
        }
    }

    @Override
    public boolean isSubscribed(String serviceName, String groupName, String clusters) throws NacosException {
        return clientProxy.isSubscribed(serviceName, groupName, clusters) || otherClientProxy
                .isSubscribed(serviceName, groupName, clusters);
    }

    @Override
    public void updateBeatInfo(Set<Instance> modifiedInstances) {
        try {
            clientProxy.updateBeatInfo(modifiedInstances);
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy updateBeatInfo err.", exp);
        }

        try {
            otherClientProxy.updateBeatInfo(modifiedInstances);
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy updateBeatInfo err.", exp);
        }
    }

    @Override
    public boolean serverHealthy() {
        return clientProxy.serverHealthy();
    }

    @Override
    public void shutdown() throws NacosException {
        try {
            clientProxy.shutdown();
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter clientProxy shutdown err.", exp);
        }

        try {
            otherClientProxy.shutdown();
        }catch (Exception exp){
            NAMING_LOGGER.error("NamingClientProxyAdapter otherClientProxy shutdown err.", exp);
        }
    }
}