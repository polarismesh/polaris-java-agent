package cn.polarismesh.agent.plugin.nacos.route;

/**
 * 就近路由匹配级别.
 */

public enum NearbyLevel {

    ZONE,//按可用区就近路由
    REGION,//按区域就近路由
    NACOS_CLUSTER,//按主nacos集群就近路由(暂时只支持此模式)
}