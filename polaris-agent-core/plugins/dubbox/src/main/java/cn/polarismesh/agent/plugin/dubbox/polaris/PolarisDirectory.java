package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.agent.plugin.dubbox.utils.StringUtil;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 继承Dubbo的RegistryDirectory类，重写list方法
 *
 * @param <T>
 */
public class PolarisDirectory<T> extends RegistryDirectory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);
    private final RegistryDirectory<T> originalRegistryDirectory;

    private static final Set<String> ignoreRouteKeys = new HashSet<>();

    private final Map<String, String> routeParameters = new HashMap<>();

    static {
        ignoreRouteKeys.add(Constants.ANYHOST_KEY);
        ignoreRouteKeys.add(Constants.TIMESTAMP_KEY);
        ignoreRouteKeys.add(Constants.CHECK_KEY);
        ignoreRouteKeys.add(Constants.METHODS_KEY);
        ignoreRouteKeys.add(Constants.KEEP_ALIVE_KEY);
        ignoreRouteKeys.add(Constants.ACCEPTS_KEY);
        ignoreRouteKeys.add(Constants.ALIVE_KEY);
        ignoreRouteKeys.add(Constants.GENERIC_KEY);
        ignoreRouteKeys.add(Constants.OPTIMIZER_KEY);
        ignoreRouteKeys.add(Constants.SIDE_KEY);
        ignoreRouteKeys.add(Constants.DUBBO_VERSION_KEY);
        ignoreRouteKeys.add(Constants.WEIGHT_KEY);
        ignoreRouteKeys.add(Constants.SERIALIZATION_KEY);
        ignoreRouteKeys.add("organization");
        ignoreRouteKeys.add("owner");
    }

    public PolarisDirectory(RegistryDirectory<T> originalRegistryDirectory, Class<T> serviceType, URL url) {
        super(serviceType, url);
        this.originalRegistryDirectory = originalRegistryDirectory;
        for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
            if (ignoreRouteKeys.contains(entry.getKey())) {
                continue;
            }
            if (Constants.ANY_VALUE.equals(entry.getValue())) {
                continue;
            }
            routeParameters.put(entry.getKey(), entry.getValue());
        }
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
        Map<String, String> srcLabels = new HashMap<>(routeParameters);
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if (null != attachments) {
            srcLabels.putAll(attachments);
        }
        srcLabels.put(Constants.METHOD_KEY, invocation.getMethodName());
        LOGGER.debug("list service {}, attachment {}, labels {}", invocation.getMethodName(), attachments, srcLabels);
        List<?> instances = PolarisSingleton.getPolarisOperation()
                .getAvailableInstances(service, srcLabels);
        List<Invoker<T>> newInvokers = new ArrayList<>();
        //原invoker
        List<Invoker<T>> originalInvokers = originalRegistryDirectory.doList(invocation);
        LOGGER.debug("[POLARIS] originalInvokers count:{}", originalInvokers.size());
        if (null != instances) {
            LOGGER.info("[POLARIS] getAvailableInstances count:{}, service {}, labels {}", instances.size(), service,
                    srcLabels);
            for (Object instance : instances) {
                String host = PolarisSingleton.getPolarisOperation().getHost(instance);
                int port = PolarisSingleton.getPolarisOperation().getPort(instance);
                String address = StringUtil.buildAdress(host, port);
                Invoker<T> newInvoker = null;
                for (Invoker<T> invoker : originalInvokers) {
                    if (invoker.getUrl().getAddress().equals(address)) {
                        newInvoker = invoker;
                    }
                }
//                Invoker<T> invoker = InvokerMap.get(address);
                if (newInvoker != null) {
                    newInvokers.add(newInvoker);
                } else {
                    LOGGER.error("[POLARIS] can not find invoker in InvokerMap, address is: {}", address);
                }
            }
        }
        if (newInvokers.isEmpty()) {
            LOGGER.error("[POLARIS] invokers build fail, invokers is empty");
            return originalRegistryDirectory.list(invocation);
        }
        return newInvokers;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
