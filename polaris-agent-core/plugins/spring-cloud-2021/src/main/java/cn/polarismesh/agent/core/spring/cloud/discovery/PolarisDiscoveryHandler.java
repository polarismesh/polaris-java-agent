package cn.polarismesh.agent.core.spring.cloud.discovery;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.pojo.ServiceInfo;
import com.tencent.polaris.api.rpc.GetAllInstancesRequest;
import com.tencent.polaris.api.rpc.GetInstancesRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.utils.StringUtils;

/**
 * Polaris服务发现Handler
 */
public class PolarisDiscoveryHandler {

    private final ConsumerAPI consumerAPI = PolarisAPIFactory.getConsumerApi();

    /**
     * 获取服务路由后的实例列表
     *
     * @param service 服务名
     * @return 服务实例列表
     */
    public InstancesResponse getFilteredInstances(String service) {
        PolarisAgentProperties polarisAgentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
        String namespace = polarisAgentProperties.getNamespace();
        GetInstancesRequest getInstancesRequest = new GetInstancesRequest();
        getInstancesRequest.setNamespace(namespace);
        getInstancesRequest.setService(service);

        String localNamespace = polarisAgentProperties.getNamespace();
        String localService = polarisAgentProperties.getService();

        if (StringUtils.isNotBlank(localNamespace) || StringUtils.isNotBlank(localService)) {
            ServiceInfo sourceService = new ServiceInfo();
            sourceService.setNamespace(localNamespace);
            sourceService.setService(localService);
            getInstancesRequest.setServiceInfo(sourceService);
        }
        return consumerAPI.getInstances(getInstancesRequest);
    }

    /**
     * Return all instances for the given service.
     *
     * @param service serviceName
     * @return 服务实例列表
     */
    public InstancesResponse getInstances(String service) {
        PolarisAgentProperties polarisAgentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
        String namespace = polarisAgentProperties.getNamespace();
        GetAllInstancesRequest request = new GetAllInstancesRequest();
        request.setNamespace(namespace);
        request.setService(service);
        return consumerAPI.getAllInstance(request);
    }

}
