package cn.polarismesh.agent.core.spring.cloud.router;

import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.polaris.PolarisSingleton;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Polaris 服务路由API类
 *
 * @author zhuyuhan
 */
public class PolarisServiceRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisServiceRouter.class);

    /**
     * 利用ConsumerAPI进行路由
     *
     * @param service
     * @return
     */
    public static List<?> getInstances(String service) {
        LogUtils.logInvoke(PolarisServiceRouter.class, "getInstances");
        List<?> availableInstances = PolarisSingleton.getPolarisOperation().getAvailableInstances(service, InvokeContextHolder.get().getMetadata());

        LOGGER.info("success to route by Polaris with instance size:{}", availableInstances.size());
        return availableInstances;
    }

    /**
     * 路由服务实例（不具有熔断功能）
     *
     * @param dstInstances
     * @return
     */
//    public static ServiceInstances getRoutedServiceInstance(ServiceInstances dstInstances) {
//        LogUtils.logInvoke(PolarisServiceRouter.class, "getRoutedServiceInstance");
//        PolarisAgentProperties agentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
//        // 执行服务路由
//        ProcessRoutersRequest processRoutersRequest = new ProcessRoutersRequest();
//        // 主调方信息
//        ServiceInfo srcSourceInfo = new ServiceInfo();
//        String srcService = agentProperties.getService();
//        String srcNamespace = agentProperties.getNamespace();
//        if (StringUtils.isNotBlank(srcNamespace) && StringUtils.isNotBlank(srcService)) {
//            srcSourceInfo.setNamespace(srcNamespace);
//            srcSourceInfo.setService(srcService);
//            processRoutersRequest.setSourceService(srcSourceInfo);
//        }
//        ProcessRoutersRequest.RouterNamesGroup routerNamesGroup = new ProcessRoutersRequest.RouterNamesGroup();
//        List<String> coreRouters = new ArrayList<>();
//        coreRouters.add(ServiceRouterConfig.DEFAULT_ROUTER_RULE);
//        coreRouters.add(ServiceRouterConfig.DEFAULT_ROUTER_METADATA);
//        coreRouters.add(ServiceRouterConfig.DEFAULT_ROUTER_NEARBY);
//        // 设置走规则路由
//        routerNamesGroup.setCoreRouters(coreRouters);
//        processRoutersRequest.setDstInstances(dstInstances);
//        processRoutersRequest.setSourceService(srcSourceInfo);
//        processRoutersRequest.setRouters(routerNamesGroup);
//        ProcessRoutersResponse processRoutersResponse = PolarisAPIFactory.getRouterApi().processRouters(processRoutersRequest);
//        LOGGER.info("success to route by Polaris with instance size:{}", processRoutersResponse.getServiceInstances().getInstances().size());
//        return processRoutersResponse.getServiceInstances();

//    }
    /**
     * 对服务实例进行负载均衡
     *
     * @param dstInstances
     * @return
     */
//    public static Instance getLoadBalancedServiceInstance(ServiceInstances dstInstances) {
//        LogUtils.logInvoke(PolarisServiceRouter.class, "getLoadBalancedServiceInstance");
//        // 执行负载均衡
//        ProcessLoadBalanceRequest processLoadBalanceRequest = new ProcessLoadBalanceRequest();
//        processLoadBalanceRequest.setDstInstances(dstInstances);
//        processLoadBalanceRequest.setLbPolicy(LoadBalanceConfig.LOAD_BALANCE_WEIGHTED_RANDOM);
//        ProcessLoadBalanceResponse processLoadBalanceResponse = PolarisAPIFactory.getRouterApi()
//                .processLoadBalance(processLoadBalanceRequest);
//        LOGGER.info("success to loadBalanced by Polaris");
//        return processLoadBalanceResponse.getTargetInstance();

//    }

    /**
     * 利用ConsumerAPI进行路由、负载均衡
     *
     * @param service
     * @return
     */
//    public static ServiceInstance getOneInstance(String service) {
//        LogUtils.logInvoke(PolarisServiceRouter.class, "getOneInstance");
//        PolarisAgentProperties polarisAgentProperties = PolarisAgentPropertiesFactory.getPolarisAgentProperties();
//        String namespace = polarisAgentProperties.getNamespace();
//
//        GetOneInstanceRequest getOneInstanceRequest = new GetOneInstanceRequest();
//        getOneInstanceRequest.setService(service);
//        getOneInstanceRequest.setNamespace(namespace);
//        String srcNamespace = polarisAgentProperties.getNamespace();
//        String srcService = polarisAgentProperties.getService();
//
//        if (StringUtils.isNotBlank(srcNamespace) || StringUtils.isNotBlank(srcService)) {
//            ServiceInfo sourceService = new ServiceInfo();
//            sourceService.setNamespace(srcNamespace);
//            sourceService.setService(srcService);
//            getOneInstanceRequest.setServiceInfo(sourceService);
//        }
//
//        Map<String, String> metadata = InvokeContextHolder.get().getMetadata();
//        if (metadata != null) {
//            getOneInstanceRequest.setMetadata(metadata);
//        }
//
//        InstancesResponse response = PolarisAPIFactory.getConsumerApi().getOneInstance(getOneInstanceRequest);
//        List<Instance> instances = response.toServiceInstances().getInstances();
//        if (CollectionUtils.isNotEmpty(instances)) {
//            LOGGER.info("success to route and loadBalance by Polaris with instance:{}", instances.get(0));
//        }
//        return new PolarisServiceInstance(instances.get(0));
//    }
}
