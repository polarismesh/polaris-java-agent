package cn.polarismesh.agent.core.spring.cloud.context;

import org.springframework.cloud.client.ServiceInstance;

import java.util.Map;

/**
 * 调用上下文
 *
 * @author zhuyuhan
 */
public class InvokeContext {

    private Map<String, String> metadata;

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

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
