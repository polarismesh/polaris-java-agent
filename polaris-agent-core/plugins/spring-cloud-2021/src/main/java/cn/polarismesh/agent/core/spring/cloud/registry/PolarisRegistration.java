package cn.polarismesh.agent.core.spring.cloud.registry;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Polaris Spring Cloud Registration 实现
 */
public class PolarisRegistration implements Registration, ServiceInstance {

    private final PolarisAgentProperties polarisProperties;

    public PolarisRegistration(PolarisAgentProperties polarisAgentProperties) {
        this.polarisProperties = polarisAgentProperties;
    }

    @Override
    public String getServiceId() {
        return polarisProperties.getService();
    }

    @Override
    public String getHost() {
        return polarisProperties.getHost();
    }

    @Override
    public int getPort() {
        return polarisProperties.getPort();
    }

    public void setPort(int port) {
        this.polarisProperties.setPort(port);
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(polarisProperties.getProtocol()) ||
                "grpc".equalsIgnoreCase(polarisProperties.getProtocol());
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return new HashMap<>();
    }

    public PolarisAgentProperties getPolarisProperties() {
        return polarisProperties;
    }

    @Override
    public String toString() {
        return "PolarisRegistration{" +
                ", polarisProperties=" + polarisProperties +
                '}';
    }
}
