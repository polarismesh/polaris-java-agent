package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.agent.plugin.dubbox.entity.InstanceInvoker;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import com.tencent.polaris.api.pojo.Instance;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
