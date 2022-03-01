package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.agent.plugin.dubbox.entity.InvokerMap;
import cn.polarismesh.agent.plugin.dubbox.utils.StringUtil;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 继承Dubbo的RegistryDirectory类，重写list方法
 *
 * @param <T>
 */
public class PolarisDirectory<T> extends RegistryDirectory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);

    public PolarisDirectory(Class<T> serviceType, URL url) {
        super(serviceType, url);
    }

    /**
     * 重写list()方法，根据polaris提供的instances信息提取对应的invokers作为返回值
     * 若polaris返回结果为空，则执行父类list()逻辑
     *
     * @param invocation 本次服务调用元信息
     * @return Invoker对象数组
     * @throws RpcException 异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        String service = this.getUrl().getServiceInterface();
        Map<String, String> srcLabels = new HashMap<>();
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if (null != attachments) {
            srcLabels.putAll(attachments);
        }
        srcLabels.put("method", invocation.getMethodName());
        List<?> instances = PolarisSingleton.getPolarisOperation()
                .getAvailableInstances(service, srcLabels);
        List<Invoker<T>> invokers = new ArrayList<>();
        if (null != instances) {
            for (Object instance : instances) {
                String host = PolarisSingleton.getPolarisOperation().getHost(instance);
                int port = PolarisSingleton.getPolarisOperation().getPort(instance);
                String address = StringUtil.buildAdress(host, port);
                Invoker invoker = InvokerMap.get(address);
                if (invoker != null) {
                    invokers.add(invoker);
                } else {
                    LOGGER.error("can not find invoker in InvokerMap, address is: {}", address);
                }
            }
        }
        if (invokers.isEmpty()) {
            LOGGER.error("invokers build fail, invokers is empty");
            return super.list(invocation);
        }
        return invokers;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
