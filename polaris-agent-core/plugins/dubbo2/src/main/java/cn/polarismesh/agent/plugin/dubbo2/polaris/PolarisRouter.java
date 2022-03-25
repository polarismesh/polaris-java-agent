package cn.polarismesh.agent.plugin.dubbo2.polaris;

import com.tencent.polaris.api.pojo.Instance;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Constants;
import org.apache.dubbo.rpc.cluster.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public boolean isRuntime() {
        return true;
    }

    @Override
    public boolean isForce() {
        return true;
    }
}
