package cn.polarismesh.agent.core.nacos.v1.delegate;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.client.naming.net.NamingProxy;
import com.alibaba.nacos.client.naming.utils.NetUtils;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.common.utils.HttpMethod;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 自定义NacosV1NamingProxy
 *
 * @author bruceppeng
 */
public class NacosV1NamingProxy extends NamingProxy {

    public NacosV1NamingProxy(String namespaceId, String endpoint, String serverList, Properties properties) {
        super(namespaceId, endpoint, serverList, properties);
    }

    /**
     * Query instance list.
     *
     * @param serviceName service name
     * @param clusters    clusters
     * @param udpPort     udp port
     * @param healthyOnly healthy only
     * @return instance list
     * @throws NacosException nacos exception
     */
    public String queryList(String serviceName, String clusters, int udpPort, boolean healthyOnly)
            throws NacosException {

        final Map<String, String> params = new HashMap<String, String>(8);
        params.put(CommonParams.NAMESPACE_ID, getNamespaceId());
        params.put(CommonParams.SERVICE_NAME, serviceName);
        params.put("clusters", clusters);
        params.put("udpPort", String.valueOf(udpPort));
        params.put("clientIP", NetUtils.localIP());
        params.put("healthyOnly", String.valueOf(healthyOnly));

        return reqApi(UtilAndComs.nacosUrlBase + "/instance/list", params, HttpMethod.GET);
    }
}