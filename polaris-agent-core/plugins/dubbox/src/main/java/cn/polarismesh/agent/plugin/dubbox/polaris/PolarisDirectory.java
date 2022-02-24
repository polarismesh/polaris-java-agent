package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.agent.plugin.dubbox.entity.InvokerMap;
import cn.polarismesh.agent.plugin.dubbox.entity.Properties;
import cn.polarismesh.agent.plugin.dubbox.utils.PolarisUtil;
import cn.polarismesh.agent.plugin.dubbox.utils.StringUtil;
import com.tencent.polaris.api.pojo.Instance;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
        String namespace = Properties.getInstance().getNamespace();
        String service = this.getUrl().getServiceInterface();
        Instance[] instances = PolarisUtil.getTargetInstances(namespace, service);
        if (instances == null) {
            LOGGER.error("get polaris instances fail");
            return super.list(invocation);
        }

        List<Invoker<T>> invokers = new ArrayList<>();
        for (Instance instance : instances) {
            String address = StringUtil.buildAdress(instance.getHost(), instance.getPort());
            Invoker invoker = InvokerMap.get(address);
            if (invoker != null) {
                invokers.add(invoker);
            } else {
                LOGGER.error("can not find invoker in InvokerMap, address is: {}", address);
            }
        }

        if (invokers.isEmpty()) {
            LOGGER.error("invokers build fail, invokers is empty");
            return super.list(invocation);
        }
        return invokers;
    }
}
