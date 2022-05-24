package cn.polarismesh.agent.core.spring.cloud.discovery;

import cn.polarismesh.agent.core.spring.cloud.polaris.PolarisSingleton;
import cn.polarismesh.common.polaris.PolarisOperator;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;

/**
 * Polaris服务实例
 */
public class PolarisServiceInstance implements ServiceInstance {

    private final Object instance;

    private final String serviceId;

    private final String host;

    private final Integer port;

    private final Map<String, String> metadata;

    private final boolean isSecure;

    private final String scheme;

    public PolarisServiceInstance(Object instance, String serviceId) {
        PolarisOperator polarisOperation = PolarisSingleton.getPolarisOperation();
        this.serviceId = serviceId;
        this.host = polarisOperation.getHost(instance);
        this.port = polarisOperation.getPort(instance);
        this.metadata = polarisOperation.getMetadata(instance);
        this.instance = instance;
        this.isSecure = "https".equalsIgnoreCase(polarisOperation.getProtocol(instance));
        if (isSecure) {
            scheme = "https";
        } else {
            scheme = "http";
        }
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }
}
