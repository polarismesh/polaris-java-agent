package cn.polarismesh.agent.plugin.dubbox.constants;

/**
 * Polaris相关常量
 */
public class PolarisConstants {
    public static final String ADDRESS_KEY = "polaris.server.address";
    public static final String NAMESPACE_KEY = "polaris.namespace";
    public static final String TTL_KEY = "polaris.ttl";
    public static final String LOADBALANCE_KEY = "polaris.loadbalance";

    public static final String DEFAULT_ADDRESS = "127.0.0.1:8091";
    public static final String DEFAULT_NAMESPACE = "default";
    public static final int DEFAULT_TTL = 5;
    public static final String DEFAULT_LOADBALANCE = "random";

    public static final String[] FILTERED_PARAMS = {"bind.ip", "bind.port"};
}
