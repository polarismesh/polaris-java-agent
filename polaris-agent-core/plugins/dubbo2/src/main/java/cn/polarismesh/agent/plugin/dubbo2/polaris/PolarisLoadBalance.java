package cn.polarismesh.agent.plugin.dubbo2.polaris;

import cn.polarismesh.agent.plugin.dubbo2.entity.InstanceInvoker;
import com.tencent.polaris.api.pojo.Instance;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PolarisLoadBalance extends AbstractLoadBalance {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisLoadBalance.class);

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        if (null == invokers || invokers.size() == 0) {
            return null;
        }
        String service = url.getServiceInterface();
        LOGGER.info("[POLARIS] select instance for service {} by PolarisLoadBalance", service);
        String key = invokers.get(0).getUrl().getServiceKey() + "." + invocation.getMethodName();
        List<Instance> instances = (List<Instance>) ((List<?>) invokers);
        Instance instance = PolarisSingleton.getPolarisWatcher().loadBalance(service, key, instances);
        return (InstanceInvoker<T>) instance;
    }
}
