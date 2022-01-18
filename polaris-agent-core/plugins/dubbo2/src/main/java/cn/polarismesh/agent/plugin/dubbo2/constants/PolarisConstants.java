package cn.polarismesh.agent.plugin.dubbo2.constants;

/**
 * Polaris相关常量
 */
public class PolarisConstants {
    public static final String DEFAULT_NAMESPACE = "default";
    public static final int TTL = 5;
    public static final String[] FILTERED_PARAMS = {"bind.ip", "bind.port"};
    public static final String DEFAULT_LOADBALANCE = "random";
}
