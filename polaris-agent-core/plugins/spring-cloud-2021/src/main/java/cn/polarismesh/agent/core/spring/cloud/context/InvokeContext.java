package cn.polarismesh.agent.core.spring.cloud.context;

import org.springframework.cloud.client.ServiceInstance;

/**
 * 调用上下文
 *
 * @author zhuyuhan
 */
public class InvokeContext {

    private ServiceInstance serviceInstance;

    private Integer status;

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
