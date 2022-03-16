package cn.polarismesh.agent.plugin.dubbox.polaris;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.tencent.polaris.api.pojo.Instance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisRouter implements Router {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisRouter.class);


    private final URL url;

    private final int priority;

    public PolarisRouter(URL url) {
        this.url = url;
        LOGGER.info("[POLARIS] init service router, url is {}, parameters are {}", url,
                url.getParameters());
        this.priority = url.getParameter(Constants.PRIORITY_KEY, 0);
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public int compareTo(Router o) {
        return (this.getPriority() < o.getPriority()) ? -1 : ((this.getPriority() == o.getPriority()) ? 0 : 1);
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (null == invokers || invokers.size() == 0) {
            return invokers;
        }
        List<Instance> instances = (List<Instance>) ((List<?>) invokers);
        Map<String, String> srcLabels = url.getParameters();
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if (null != attachments && !attachments.isEmpty()) {
            srcLabels = new HashMap<>(srcLabels);
            srcLabels.putAll(attachments);
        }
        String service = url.getServiceInterface();
        LOGGER.debug("[POLARIS] list service {}, method {}, attachment {}, labels {}, url {}", service,
                invocation.getMethodName(),
                attachments, srcLabels, url);
        List<Instance> resultInstances = PolarisSingleton.getPolarisWatcher()
                .route(service, invocation.getMethodName(), srcLabels, instances);
        return (List<Invoker<T>>) ((List<?>) resultInstances);
    }
}
