package cn.polarismesh.agent.core.spring.cloud.router;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import com.tencent.polaris.api.config.consumer.LoadBalanceConfig;
import com.tencent.polaris.api.config.consumer.ServiceRouterConfig;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInfo;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.rpc.GetInstancesRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceRequest;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceResponse;
import com.tencent.polaris.router.api.rpc.ProcessRoutersRequest;
import com.tencent.polaris.router.api.rpc.ProcessRoutersResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Polaris 服务路由API类
 *
 * @author zhuyuhan
 */
public class PolarisServiceRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisServiceRouter.class);

    /**
     * 路由服务实例（不具有熔断功能）
     *
     * @param dstInstances
     * @return
     */
    public static ServiceInstances getRoutedServiceInstance(ServiceInstances dstInstances) {
        LogUtils.logInvoke(PolarisServiceRouter.class, "getRoutedServiceInstance");
        PolarisAgentProperties agentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
        // 执行服务路由
        ProcessRoutersRequest processRoutersRequest = new ProcessRoutersRequest();
        // 主调方信息
        ServiceInfo srcSourceInfo = new ServiceInfo();
        String srcService = agentProperties.getService();
        String srcNamespace = agentProperties.getNamespace();
        if (StringUtils.isNotBlank(srcNamespace) && StringUtils.isNotBlank(srcService)) {
            srcSourceInfo.setNamespace(srcNamespace);
            srcSourceInfo.setService(srcService);
            processRoutersRequest.setSourceService(srcSourceInfo);
        }
        ProcessRoutersRequest.RouterNamesGroup routerNamesGroup = new ProcessRoutersRequest.RouterNamesGroup();
        List<String> coreRouters = new ArrayList<>();
        coreRouters.add(ServiceRouterConfig.DEFAULT_ROUTER_RULE);
        coreRouters.add(ServiceRouterConfig.DEFAULT_ROUTER_METADATA);
        coreRouters.add(ServiceRouterConfig.DEFAULT_ROUTER_NEARBY);
        // 设置走规则路由
        routerNamesGroup.setCoreRouters(coreRouters);
        processRoutersRequest.setDstInstances(dstInstances);
        processRoutersRequest.setSourceService(srcSourceInfo);
        processRoutersRequest.setRouters(routerNamesGroup);
        ProcessRoutersResponse processRoutersResponse = PolarisAPIFactory.getRouterApi().processRouters(processRoutersRequest);
        LOGGER.info("success to route by Polaris with instance size:{}", processRoutersResponse.getServiceInstances().getInstances().size());
        return processRoutersResponse.getServiceInstances();
    }

    /**
     * 对服务实例进行负载均衡
     *
     * @param dstInstances
     * @return
     */
    public static Instance getLoadBalancedServiceInstance(ServiceInstances dstInstances) {
        LogUtils.logInvoke(PolarisServiceRouter.class, "getLoadBalancedServiceInstance");
        // 执行负载均衡
        ProcessLoadBalanceRequest processLoadBalanceRequest = new ProcessLoadBalanceRequest();
        processLoadBalanceRequest.setDstInstances(dstInstances);
        processLoadBalanceRequest.setLbPolicy(LoadBalanceConfig.LOAD_BALANCE_WEIGHTED_RANDOM);
        ProcessLoadBalanceResponse processLoadBalanceResponse = PolarisAPIFactory.getRouterApi()
                .processLoadBalance(processLoadBalanceRequest);
        LOGGER.info("success to loadBalanced by Polaris");
        return processLoadBalanceResponse.getTargetInstance();
    }

    /**
     * 利用ConsumerAPI进行路由
     *
     * @param service
     * @return
     */
    public static ServiceInstances getInstances(String service) {
        LogUtils.logInvoke(PolarisServiceRouter.class, "getInstances");
        PolarisAgentProperties polarisAgentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
        String namespace = polarisAgentProperties.getNamespace();
        GetInstancesRequest getInstancesRequest = new GetInstancesRequest();
        getInstancesRequest.setNamespace(namespace);
        getInstancesRequest.setService(service);

        String srcNamespace = polarisAgentProperties.getNamespace();
        String srcService = polarisAgentProperties.getService();

        if (StringUtils.isNotBlank(srcNamespace) || StringUtils.isNotBlank(srcService)) {
            ServiceInfo sourceService = new ServiceInfo();
            sourceService.setNamespace(srcNamespace);
            sourceService.setService(srcService);
            getInstancesRequest.setServiceInfo(sourceService);
        }
        InstancesResponse response = PolarisAPIFactory.getConsumerApi().getInstances(getInstancesRequest);
        LOGGER.info("success to route by Polaris with instance size:{}", response.getInstances().length);
        return response.toServiceInstances();
    }
}
