package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.agent.plugin.dubbox.utils.StringUtil;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 继承Dubbo的RegistryDirectory类，重写list方法
 *
 * @param <T>
 */
public class PolarisDirectory<T> extends RegistryDirectory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);
    private RegistryDirectory<T> originalRegistryDirectory;

    public PolarisDirectory(RegistryDirectory<T> originalRegistryDirectory, Class<T> serviceType, URL url) {
        super(serviceType, url);
        this.originalRegistryDirectory = originalRegistryDirectory;
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
        List<Invoker<T>> newInvokers = new ArrayList<>();
        //原invoker
        List<Invoker<T>> originalInvokers = originalRegistryDirectory.doList(invocation);
        LOGGER.info("[POLARIS] originalInvokers count:{}", originalInvokers.size());
        if (null != instances) {
            LOGGER.info("[POLARIS] getAvailableInstances count:{}",instances.size());
            for (Object instance : instances) {
                String host = PolarisSingleton.getPolarisOperation().getHost(instance);
                int port = PolarisSingleton.getPolarisOperation().getPort(instance);
                String address = StringUtil.buildAdress(host, port);
                Invoker<T> newInvoker = null;
                for(Invoker<T> invoker : originalInvokers) {
                    if(invoker.getUrl().getAddress().equals(address)) {
                        newInvoker = invoker;
                    }
                }
//                Invoker<T> invoker = InvokerMap.get(address);
                if (newInvoker != null) {
                    newInvokers.add(newInvoker);
                } else {
                    LOGGER.error("can not find invoker in InvokerMap, address is: {}", address);
                }
            }
        }
        if (newInvokers.isEmpty()) {
            LOGGER.error("invokers build fail, invokers is empty");
            return originalRegistryDirectory.list(invocation);
        }
        return newInvokers;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
