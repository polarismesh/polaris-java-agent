package cn.polarismesh.agent.core.nacos.v1.delegate;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.net.NamingProxy;
import com.alibaba.nacos.client.naming.utils.NetUtils;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * 自定义NacosV1NamingProxy
 *
 * @author bruceppeng
 */
public class NacosV1NamingProxy extends NamingProxy {

    private int maxRetry;

    private String targetNacosDomain;

    public NacosV1NamingProxy(String namespaceId, String endpoint, String serverList, Properties properties) {
        super(namespaceId, endpoint, serverList, properties);

        this.maxRetry = ConvertUtils.toInt(properties.getProperty(PropertyKeyConst.NAMING_REQUEST_DOMAIN_RETRY_COUNT,
                String.valueOf(UtilAndComs.REQUEST_DOMAIN_RETRY_COUNT)));

        targetNacosDomain = System.getProperty(NacosConstants.TARGET_NACOS_SERVER_ADDR);
        System.out.println("NacosV1NamingProxy targetNacosDomain:"+targetNacosDomain);
        Objects.requireNonNull(targetNacosDomain);
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

        String api = UtilAndComs.nacosUrlBase + "/instance/list";
        String result = reqApi(api, params, HttpMethod.GET);
        System.out.println("NacosV1NamingProxy result:"+result);
        for (int i = 0; i < maxRetry; i++) {
            try {
                String secondResult = callServer(api, params, Collections.EMPTY_MAP, targetNacosDomain, HttpMethod.GET);
                System.out.println("NacosV1NamingProxy secondResult:"+secondResult);
                return mergeResult(result, secondResult);
            } catch (NacosException e) {
                System.out.println("NacosV1NamingProxy NacosException:"+e.getMessage());
                if (NAMING_LOGGER.isDebugEnabled()) {
                    NAMING_LOGGER.debug("NacosV1NamingProxy queryList request {} failed.", targetNacosDomain, e);
                }
            }
        }

        return result;
    }


    /**
     * 合并两个nacos server的实例列表
     * @param result
     * @param secondResult
     */
    private String mergeResult(String result, String secondResult) {

        if (StringUtils.isEmpty(secondResult)) {
            return result;
        }

        if (StringUtils.isEmpty(result)) {
            return secondResult;
        }

        try {
            ServiceInfo serviceInfo = JacksonUtils.toObj(result, ServiceInfo.class);
            ServiceInfo secondServiceInfo = JacksonUtils.toObj(secondResult, ServiceInfo.class);

            List<Instance> hosts =  serviceInfo.getHosts();
            List<Instance> secondHosts =  secondServiceInfo.getHosts();

            Map<String, Instance> hostMap = new HashMap<String, Instance>(hosts.size());
            for (Instance host : hosts) {
                hostMap.put(host.toInetAddr(), host);
            }

            for (Instance host : secondHosts) {
                String inetAddr = host.toInetAddr();
                if (hostMap.get(inetAddr) == null) {
                    hosts.add(host);
                }
            }
            serviceInfo.setHosts(hosts);
            return JacksonUtils.toJson(serviceInfo);
        }catch(Exception exp){
            System.out.println("NacosV1NamingProxy mergeResult NacosException:"+exp.getMessage());
            NAMING_LOGGER.error("NacosV1NamingProxy mergeResult request {} failed.", targetNacosDomain, exp);
        }
        return result;

    }

}