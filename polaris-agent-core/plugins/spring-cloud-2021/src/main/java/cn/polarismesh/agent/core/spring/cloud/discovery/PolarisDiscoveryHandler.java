package cn.polarismesh.agent.core.spring.cloud.discovery;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import cn.polarismesh.agent.core.spring.cloud.router.PolarisServiceRouter;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.rpc.GetAllInstancesRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;

/**
 * Polaris服务发现Handler
 */
public class PolarisDiscoveryHandler {

    private final ConsumerAPI consumerAPI = PolarisAPIFactory.getConsumerApi();

    /**
     * 获取服务路由&负载均衡后的实例列表
     *
     * @param service 服务名
     * @return 服务实例列表
     */
    public ServiceInstances getFilteredInstances(String service) {
        LogUtils.logInvoke(this, "getFilteredInstances");
        return PolarisServiceRouter.getInstances(service);
    }

    /**
     * Return all instances for the given service.
     *
     * @param service serviceName
     * @return 服务实例列表
     */
    public InstancesResponse getInstances(String service) {
        LogUtils.logInvoke(this, "getInstances");
        PolarisAgentProperties polarisAgentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
        String namespace = polarisAgentProperties.getNamespace();
        GetAllInstancesRequest request = new GetAllInstancesRequest();
        request.setNamespace(namespace);
        request.setService(service);
        return consumerAPI.getAllInstance(request);
    }

}
