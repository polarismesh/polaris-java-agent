package cn.polarismesh.agent.plugin.dubbo2.constants;

import cn.polarismesh.agent.plugin.dubbo2.polaris.loadbalance.RandomLoadBalance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Polaris相关常量
 */
public class PolarisConstants {
    public static final String DEFAULT_NAMESPACE = "default";
    public static final int TTL = 5;
    public static final String[] FILTERED_PARAMS = {"bind.ip", "bind.port"};
    public static final String DEFAULT_LOADBALANCE = "random";

    public static final ConcurrentMap<String, Class<?>> LOADBALANCE_MAP = new ConcurrentHashMap<>();

    static {
        LOADBALANCE_MAP.put("random", RandomLoadBalance.class);
        // TODO 其他负载均衡策略
    }
}
