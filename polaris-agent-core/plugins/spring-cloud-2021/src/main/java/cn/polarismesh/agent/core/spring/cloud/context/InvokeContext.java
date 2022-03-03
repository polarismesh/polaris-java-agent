package cn.polarismesh.agent.core.spring.cloud.context;

import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
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

    private String srcNamespace = PolarisAgentPropertiesFactory.getPolarisAgentProperties().getNamespace();

    private String srcService = PolarisAgentPropertiesFactory.getPolarisAgentProperties().getService();

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

    public String getSrcNamespace() {
        return srcNamespace;
    }

    public void setSrcNamespace(String srcNamespace) {
        this.srcNamespace = srcNamespace;
    }

    public String getSrcService() {
        return srcService;
    }

    public void setSrcService(String srcService) {
        this.srcService = srcService;
    }
}
