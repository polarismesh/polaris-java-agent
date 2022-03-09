package cn.polarismesh.agent.plugin.dubbo2.polaris;

import cn.polarismesh.agent.plugin.dubbo2.utils.StringUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.MapUtils;
import org.apache.dubbo.registry.integration.RegistryDirectory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.remoting.Constants.ACCEPTS_KEY;
import static org.apache.dubbo.remoting.Constants.SERIALIZATION_KEY;
import static org.apache.dubbo.rpc.cluster.Constants.WEIGHT_KEY;
import static org.apache.dubbo.rpc.protocol.dubbo.Constants.OPTIMIZER_KEY;



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
        ignoreRouteKeys.add(ANYHOST_KEY);
        ignoreRouteKeys.add(TIMESTAMP_KEY);
        ignoreRouteKeys.add(CHECK_KEY);
        ignoreRouteKeys.add(METHODS_KEY);
        ignoreRouteKeys.add(KEEP_ALIVE_KEY);
        ignoreRouteKeys.add(ACCEPTS_KEY);
        ignoreRouteKeys.add(ALIVE_KEY);
        ignoreRouteKeys.add(GENERIC_KEY);
        ignoreRouteKeys.add(OPTIMIZER_KEY);
        ignoreRouteKeys.add(SIDE_KEY);
        ignoreRouteKeys.add(DUBBO_VERSION_KEY);
        ignoreRouteKeys.add(WEIGHT_KEY);
        ignoreRouteKeys.add(SERIALIZATION_KEY);
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
            if (ANY_VALUE.equals(entry.getValue())) {
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
    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        String service = invocation.getServiceName();
        Map<String, String> srcLabels = new HashMap<>(routeParameters);
        Map<String, String> attachments = MapUtils.objectToStringMap(RpcContext.getContext().getObjectAttachments());
        if (null != attachments) {
            srcLabels.putAll(attachments);
        }
        srcLabels.put(METHOD_KEY, invocation.getMethodName());
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
