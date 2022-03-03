package cn.polarismesh.agent.core.spring.cloud.observability;

import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.polaris.PolarisSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

/**
 * Polaris 可观测性工具类
 *
 * @author zhuyuhan
 */
public class PolarisServiceObservability {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisServiceObservability.class);

    public static void updateServiceCallResult(Long delay, Throwable throwable, Integer status, PolarisAgentProperties polarisAgentProperties) {
        ServiceInstance instance = InvokeContextHolder.get().getServiceInstance();
        if (instance == null) {
            LOGGER.warn("fail to call updateServiceCallResult for polaris with instance empty");
            return;
        }
        try {
            PolarisSingleton.getPolarisOperation().reportInvokeResult(
                    instance.getServiceId(), null, instance.getHost(), instance.getPort()
                    , delay, null == throwable, null != status ? status : -1);
            LOGGER.info("success to call updateServiceCallResult for polaris with status code:{}", status);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            InvokeContextHolder.remove();
        }
    }
}
