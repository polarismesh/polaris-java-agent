package cn.polarismesh.agent.plugin.dubbo2.polaris.loadbalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalanceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceFactory.class);

    public static PolarisAbstractLoadBalance getLoadBalance(String loadbalance) {
        if (loadbalance == null || loadbalance.isEmpty()) {
            LOGGER.info("loadbalance is not defined");
            return null;
        }
        return new RandomLoadBalance();
        // TODO 其他负载均衡策略
    }
}
