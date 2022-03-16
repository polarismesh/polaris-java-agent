package cn.polarismesh.agent.plugin.dubbox.constants;

/**
 * Polaris相关常量
 */
public class PolarisConstants {

    public static final String ADDRESS_KEY = "polaris.server.address";
    public static final String NAMESPACE_KEY = "polaris.namespace";
    public static final String TTL_KEY = "polaris.ttl";

    public static final String DEFAULT_ADDRESS = "127.0.0.1:8091";
    public static final String DEFAULT_NAMESPACE = "default";
    public static final int DEFAULT_TTL = 5;

    public static final String TAG_KEY = "dubbo.tag";

    public static final String[] FILTERED_PARAMS = {"bind.ip", "bind.port"};


    public static final String KEY_HEALTHY = "_internal_healthy";

    public static final String KEY_ISOLATED = "_internal_isolated";

    public static final String KEY_CIRCUIT_BREAKER = "_internal_circuit_breaker";

    public static final String KEY_ID = "_internal_id";
}
