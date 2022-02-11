package cn.polarismesh.agent.core.spring.cloud.discovery;

import com.tencent.polaris.api.pojo.Instance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;

/**
 * Polaris服务实例
 */
public class PolarisServiceInstance implements ServiceInstance {

    private final Instance instance;

    private final boolean isSecure;

    private final String scheme;

    public PolarisServiceInstance(Instance instance) {
        this.instance = instance;
        this.isSecure = StringUtils.equalsIgnoreCase(instance.getProtocol(), "https");
        if (isSecure) {
            scheme = "https";
        } else {
            scheme = "http";
        }
    }

    public Instance getPolarisInstance() {
        return instance;
    }

    @Override
    public String getServiceId() {
        return instance.getService();
    }

    @Override
    public String getHost() {
        return instance.getHost();
    }

    @Override
    public int getPort() {
        return instance.getPort();
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
        return instance.getMetadata();
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }
}
