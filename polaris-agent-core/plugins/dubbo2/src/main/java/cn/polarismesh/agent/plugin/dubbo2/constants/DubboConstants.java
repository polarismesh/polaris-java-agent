package cn.polarismesh.agent.plugin.dubbo2.constants;

import java.util.HashSet;
import java.util.Set;

/**
 * Dubbo相关常量
 */
public class DubboConstants {
    public static final Set<String> DUBBO_LOADBALANCES = new HashSet<>();

    static {
        DUBBO_LOADBALANCES.add("random");
        DUBBO_LOADBALANCES.add("roundrobin");
        DUBBO_LOADBALANCES.add("leastactive");
    }
}
