package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.common.polaris.PolarisBlockException;
import com.alibaba.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String service = invoker.getInterface().getName();
        String method = invocation.getMethodName();
        boolean result = true;
        try {
            result = PolarisSingleton.getPolarisOperation().getQuota(service, method, invocation.getAttachments(), 1);
        } catch (RuntimeException e) {
            LOGGER.error("[POLARIS] get quota fail, {}", e.getMessage());
        }
        if (!result) {
            // 请求被限流，则抛出异常
            String namespace = PolarisSingleton.getPolarisOperation().getPolarisConfig().getNamespace();
            throw new PolarisBlockException("rate limit", namespace, service, method, invocation.getAttachments());
        }
        return invoker.invoke(invocation);
    }
}
