package cn.polarismesh.agent.plugin.nacos.route;

import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import java.util.Objects;

/**
 * 就近路由配置.
 *
 */
public class NearbyRouter {

    static NearbyRouter nearbyRouter = new NearbyRouter();

    public static NearbyRouter getRouter() {
        return nearbyRouter;
    }

    /**
     * 是否开启就近路由
     */
    boolean enable;

    /**
     * 就近路由的匹配级别
     */
    String nearbyLevel;

    /**
     * 是否主nacos集群路由优先
     */
    boolean isNearbyNacosCluster;

    public boolean isEnable() {
        return enable;
    }

    public boolean isNearbyNacosCluster() {
        return isNearbyNacosCluster;
    }

    public void init() {

        String routerNearbyLevel = System.getProperty(NacosConstants.ROUTER_NEARBY_LEVEL);

        if (Objects.isNull(routerNearbyLevel)) {
            return;
        }

        if (NearbyLevel.NACOS_CLUSTER.name().equals(routerNearbyLevel.toUpperCase())) {
            this.nearbyLevel = routerNearbyLevel;
            this.enable = true;
            this.isNearbyNacosCluster = true;
        }
    }
}


