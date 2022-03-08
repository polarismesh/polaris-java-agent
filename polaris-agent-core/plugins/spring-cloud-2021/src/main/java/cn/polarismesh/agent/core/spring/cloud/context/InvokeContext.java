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

    /**
     * 调用元信息
     */
    private Map<String, String> metadata;

    /**
     * Spring Cloud 服务实例对象
     */
    private ServiceInstance serviceInstance;

    /**
     * 来源服务Namespace
     */
    private String srcNamespace = PolarisAgentPropertiesFactory.getPolarisAgentProperties().getNamespace();

    /**
     * 来源服务名称
     */
    private String srcService = PolarisAgentPropertiesFactory.getPolarisAgentProperties().getService();

    /**
     * 调用状态
     */
    private Integer status;

    /**
     * 调用路径
     */
    private String path;

    /**
     * 调用Http方法
     */
    private String method;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
