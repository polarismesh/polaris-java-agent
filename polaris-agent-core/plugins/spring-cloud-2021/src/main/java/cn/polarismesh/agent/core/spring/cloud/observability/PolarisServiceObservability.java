package cn.polarismesh.agent.core.spring.cloud.observability;

import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.rpc.ServiceCallResult;
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
        ServiceCallResult serviceCallResult = new ServiceCallResult();
        serviceCallResult.setNamespace(polarisAgentProperties.getNamespace());
        serviceCallResult.setService(instance.getServiceId());
        serviceCallResult.setHost(instance.getHost());
        serviceCallResult.setPort(instance.getPort());
        serviceCallResult.setDelay(delay);
        serviceCallResult.setRetStatus(null == throwable ? RetStatus.RetSuccess : RetStatus.RetFail);
        serviceCallResult.setRetCode(null != status ? status : -1);
        try {
            PolarisAPIFactory.getConsumerApi().updateServiceCallResult(serviceCallResult);
            LOGGER.info("success to call updateServiceCallResult for polaris with status:{}, code:{}", serviceCallResult.getRetStatus(), serviceCallResult.getRetCode());
        } catch (PolarisException e) {
            LOGGER.error(e.getMessage());
        } finally {
            InvokeContextHolder.remove();
        }
    }
}
