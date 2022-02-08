package cn.polarismesh.agent.core.spring.cloud.loadbalance.ribbon;

import com.google.common.base.Objects;
import com.netflix.loadbalancer.Server;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Polaris Ribbon Server 实现类
 */
public class PolarisServer extends Server {

    private final ServiceInstances serviceInstances;

    private final Instance instance;

    private final MetaInfo metaInfo;

    public PolarisServer(ServiceInstances serviceInstances, Instance instance) {
        super(instance.getHost(), instance.getPort());
        if (StringUtils.equalsIgnoreCase(instance.getProtocol(), "https")) {
            setSchemea("https");
        } else {
            setSchemea("http");
        }
        this.serviceInstances = serviceInstances;
        this.instance = instance;
        this.metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return instance.getService();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return instance.getService();
            }

            @Override
            public String getInstanceId() {
                return instance.getId();
            }
        };
    }

    public Instance getInstance() {
        return instance;
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public Map<String, String> getMetadata() {
        return instance.getMetadata();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PolarisServer that = (PolarisServer) o;
        return Objects.equal(instance, that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), instance);
    }

    public ServiceInstances getServiceInstances() {
        return serviceInstances;
    }
}
