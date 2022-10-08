package cn.polarismesh.agent.core.nacos.delegate;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.event.InstancesChangeNotifier;
import com.alibaba.nacos.client.naming.remote.NamingClientProxy;
import com.alibaba.nacos.client.naming.remote.NamingClientProxyDelegate;
import java.util.Properties;

/**
 * 自定义 NacosNamingClientProxyDelegate.
 *
 * @author bruceppeng
 */
public class NacosNamingClientProxyDelegate extends NamingClientProxyDelegate {

    public NacosNamingClientProxyDelegate(String namespace,
            ServiceInfoHolder serviceInfoHolder, Properties properties,
            InstancesChangeNotifier changeNotifier) throws NacosException {
        super(namespace, serviceInfoHolder, properties, changeNotifier);
    }

    private NamingClientProxy getExecuteClientProxy(Instance instance) {
        return instance.isEphemeral() ? grpcClientProxy : httpClientProxy;
    }
}