package cn.polarismesh.agent.core.spring.cloud.ratelimit;

import cn.polarismesh.agent.core.spring.cloud.context.InvokeContext;
import cn.polarismesh.agent.core.spring.cloud.polaris.PolarisSingleton;
import cn.polarismesh.common.polaris.PolarisBlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Polaris 限流工具类
 *
 * @author zhuyuhan
 */
public class PolarisRateLimit {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisRateLimit.class);

    public static void judge(InvokeContext invokeContext) {
        // judge limit
        boolean quota = false;
        String serviceId = invokeContext.getServiceInstance().getServiceId();
        String method = invokeContext.getMethod();
        Map<String, String> labels = new HashMap<>();
        labels.put("path", invokeContext.getPath());
        try {
            quota = PolarisSingleton.getPolarisOperation().getQuota(serviceId, method, labels, 1);
        } catch (RuntimeException e) {
            LOGGER.error("[POLARIS] get quota fail, {}", e.getMessage());
        }
        if (!quota) {
            // 请求被限流，则抛出异常
            throw new PolarisBlockException("rate limit", invokeContext.getSrcNamespace(), serviceId, method, labels);
        }
        LOGGER.info("rate limit success with service:{}", serviceId);
    }
}
