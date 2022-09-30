package cn.polarismesh.agent.core.nacos.delegate;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

import cn.polarismesh.agent.core.nacos.constants.NacosConstants;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.api.utils.NetUtils;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.core.ServerListManager;
import com.alibaba.nacos.client.naming.remote.gprc.NamingGrpcClientProxy;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientProxy;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.common.remote.client.ServerListFactory;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

//import java.util.Objects;

/**
 * 自定义 NacosNamingGrpcClientProxy
 *
 * @author bruceppeng
 */
public class NacosNamingGrpcClientProxy extends NamingGrpcClientProxy {

    private int maxRetry;

    private String targetNacosDomain;


    private static final Map<String, Boolean> nacosCallCache = new ConcurrentHashMap<>(256);

    public NacosNamingGrpcClientProxy(String namespaceId, SecurityProxy securityProxy, ServerListFactory serverListFactory,
            Properties properties, ServiceInfoHolder serviceInfoHolder) throws NacosException {
        super((namespaceId, securityProxy, serverListFactory, properties,serviceInfoHolder);

        this.maxRetry = ConvertUtils.toInt(properties.getProperty(PropertyKeyConst.NAMING_REQUEST_DOMAIN_RETRY_COUNT,
                String.valueOf(UtilAndComs.REQUEST_DOMAIN_RETRY_COUNT)));

        targetNacosDomain = System.getProperty(NacosConstants.TARGET_NACOS_SERVER_ADDR);
        init();
    }

    /**
     * 初始化nacosCallCache
     */
    private void init(){
        nacosCallCache.put(NacosConstants.REGISTER_SERVICE, true);
        nacosCallCache.put(NacosConstants.DEREGISTER_SERVICE, true);
        nacosCallCache.put(NacosConstants.SEND_BEAT, true);
        nacosCallCache.put(NacosConstants.QUERY_LIST, true);
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
    @Override
    public ServiceInfo queryInstancesOfService(String serviceName, String groupName, String clusters, int udpPort, boolean healthyOnly)
            throws NacosException {

        final Map<String, String> params = new HashMap<>(8);
        params.put(CommonParams.NAMESPACE_ID, getNamespaceId());
        params.put(CommonParams.SERVICE_NAME, serviceName);
        params.put("clusters", clusters);
        params.put("udpPort", String.valueOf(udpPort));
        params.put("clientIP", NetUtils.localIP());
        params.put("healthyOnly", String.valueOf(healthyOnly));

        String api = UtilAndComs.nacosUrlBase + "/instance/list";

        String result = super.reqApi(api, params, HttpMethod.GET);
        if (Strings.isNullOrEmpty(targetNacosDomain)){
            if (StringUtils.isNotEmpty(result)) {
                return JacksonUtils.toObj(result, ServiceInfo.class);
            }
            return new ServiceInfo(NamingUtils.getGroupedName(serviceName, groupName), clusters);
        }

        String secondResult = callServerForTarget(api, params, Collections.EMPTY_MAP, HttpMethod.GET);

        String finalResult = mergeResult(result, secondResult);
        return JacksonUtils.toObj(finalResult, ServiceInfo.class);

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
            NAMING_LOGGER.error("NacosNamingProxy mergeResult request {} failed.", targetNacosDomain, exp);
        }
        return result;

    }

    /**
     * Request api.
     *
     * @param api     api
     * @param params  parameters
     * @param body    body
     * @param servers servers
     * @param method  http method
     * @return result
     * @throws NacosException nacos exception
     */
    @Override
    public String reqApi(String api, Map<String, String> params, Map<String, String> body, List<String> servers,
            String method) throws NacosException {
        String sourceResult = super.reqApi(api, params, body, servers, method);
        //处理对目的地址的请求,即使报错也不能影响原有的server调用
        callServerForTarget(api, params, body, method);
        return sourceResult;

    }

    /**
     * 调用目标nacos server的指定接口
     * @param api
     * @param params
     * @param body
     * @param method
     * @return
     */
    private String callServerForTarget(String api, Map<String, String> params, Map<String, String> body, String method){
        if (Strings.isNullOrEmpty(targetNacosDomain)){
            return StringUtils.EMPTY;
        }
        String callName = api + NacosConstants.LINK_FLAG + method;
        Boolean bool = nacosCallCache.get(callName);
        if (bool == null || !bool ){
            return StringUtils.EMPTY;
        }
        for (int i = 0; i < maxRetry; i++) {
            try {
                //1.请求目标nacos server
                return callServer(api, params, body, targetNacosDomain, method);
            } catch (NacosException e) {
                if (NAMING_LOGGER.isDebugEnabled()) {
                    NAMING_LOGGER.debug("NacosNamingProxy callServerForTarget request {} failed.", targetNacosDomain, e);
                }
            }
        }
        return StringUtils.EMPTY;
    }

}