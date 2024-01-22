package cn.polarismesh.agent.plugin.nacos.delegate;

import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import cn.polarismesh.agent.plugin.nacos.route.NearbyRouter;
import cn.polarismesh.agent.plugin.nacos.utils.JacksonUtils;
import cn.polarismesh.agent.plugin.nacos.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NamingProxyAssist {


    private final NearbyRouter nearbyRouter;
    private final String nacosClusterName;

    public NamingProxyAssist() {
        this.nearbyRouter = NearbyRouter.getRouter();
        this.nearbyRouter.init();
        this.nacosClusterName = System.getProperty(NacosConstants.NACOS_CLUSTER_NAME);

    }

    /**
     * 合并两个nacos server的实例列表
     *
     * @param result
     * @param secondResult
     */
    public String mergeResult(String result, String secondResult) {

        if (StringUtils.isBlank(result) && StringUtils.isBlank(result)) {
            return result;
        }
        Map primaryServiceInfo = parseServiceInfo(result);
        Map secondaryServiceInfo = parseServiceInfo(secondResult);
        Map mergedServiceInfo = primaryServiceInfo == null ? secondaryServiceInfo : primaryServiceInfo;
        if (mergedServiceInfo == null) {
            return "";
        }
        List<Map> primary = parseHost(result);
        List<Map> secondary = parseHost(secondResult);
        List<Map> mergedHost = mergeInstance(primary, secondary);
        mergedServiceInfo.put("hosts", mergedHost);
        return JacksonUtils.toJson(mergedServiceInfo);
    }


    public Map parseServiceInfo(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        Map serviceInfo = JacksonUtils.toObj(json, Map.class);
        return serviceInfo;
    }

    public List<Map> parseHost(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        Map serviceInfo = JacksonUtils.toObj(json, Map.class);
        Object hosts = serviceInfo.get("hosts");
        if (!(hosts instanceof List)
                || ((List<?>) hosts).isEmpty()
                || (!((((List<?>) hosts).get(0)) instanceof Map))
        ) {
            return Collections.emptyList();
        }

        return (List<Map>) hosts;
    }

    public List<Map> mergeInstance(List<Map> primary, List<Map> secondary) {
        List<Map> list = new ArrayList<>();
        list.addAll(primary);
        list.addAll(secondary);
        list = filterInstances(list);
        Map<String, Map> map = list.stream().collect(Collectors.toMap(item -> {
                    Object ip = item.get("ip");
                    Object port = item.get("port");
                    return ip + ":" + port;
                }, Function.identity(),
                (existing, replacement) -> existing));
        list= new ArrayList<>(map.values());
        return list;
    }


    /**
     * filterInstances 对实例列表进行过滤筛选.
     *
     * @param hosts
     * @return
     */
    public List<Map> filterInstances(List<Map> hosts) {

        // 针对服务实例做特殊处理，如果开启同nacos集群优先，则优先返回同nacos集群的实例
        if (!nearbyRouter.isEnable()) {
            return hosts;
        }

        List<Map> finalHosts = Lists.newArrayList();

        if (nearbyRouter.isNearbyNacosCluster()) {
            filterByNearbyNacosCluster(hosts, finalHosts);
        }

        if (finalHosts.isEmpty()) {
            return hosts;
        }
        return finalHosts;
    }

    /**
     * filterByNearbyNacosCluster NearbyNacosCluster方式对实例列表进行过滤筛选.
     */
    public void filterByNearbyNacosCluster(List<Map> hosts, List<Map> finalHosts) {
        for (Map instance : hosts) {
            Object metadata = instance.get("metadata");
            if (!(metadata instanceof Map)) {
                continue;
            }
            Object nacosClusterName = ((Map<?, ?>) metadata).get(NacosConstants.NACOS_CLUSTER_NAME);

            if (Objects.equals(nacosClusterName, this.nacosClusterName)) {
                finalHosts.add(instance);
            }
        }
    }

    public void fillMetadata(Map<String, String> params) {

        String s = params.get(NacosConstants.METADATA);
        Map<Object, Object> metadata;
        if (s == null) {
            metadata = new HashMap<>();
        } else {
            metadata = JacksonUtils.toObj(s, Map.class);
        }
        metadata.put(NacosConstants.NACOS_CLUSTER_NAME, this.nacosClusterName);
        params.put(NacosConstants.METADATA, JacksonUtils.toJson(metadata));

    }

    public String getNacosClusterName(){
        return this.nacosClusterName;
    }
}
