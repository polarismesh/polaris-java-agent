package cn.polarismesh.agent.plugin.nacos.delegate;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import cn.polarismesh.agent.plugin.nacos.exception.UnsupportedNacosClientException;
import cn.polarismesh.agent.plugin.nacos.utils.JacksonUtils;


import java.util.Map;


public class NamingClientProxyAssist extends NamingProxyAssist {
    private final Class serviceInfoClz;

    public NamingClientProxyAssist() {
        try {
            serviceInfoClz = Class.forName("com.alibaba.nacos.api.naming.pojo.ServiceInfo");
        } catch (ClassNotFoundException e) {
            throw new UnsupportedNacosClientException();
        }
    }

    public void fillMetadata(Object instance) {
        Map<String, String> metadata = (Map<String, String>) ReflectionUtils.getObjectByFieldName(instance, "metadata");
        metadata.put(NacosConstants.NACOS_CLUSTER_NAME, getNacosClusterName());
    }


    public Object mergeInstances(Object serviceInfo, Object otherServiceInfo) {
        if (otherServiceInfo == null) {
            return serviceInfo;
        }

        if (serviceInfo == null) {
            return otherServiceInfo;
        }
        String primary = JacksonUtils.toJson(serviceInfo);
        String secondary = JacksonUtils.toJson(otherServiceInfo);
        String mergerd = super.mergeResult(primary, secondary);
        Object o = JacksonUtils.toObj(mergerd, serviceInfoClz);
        return o;

    }

}
