package cn.polarismesh.agent.plugin.dubbo2.polaris;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;

public class PolarisRegistryFactory implements RegistryFactory {
    @Override
    public Registry getRegistry(URL url) {
        return new PolarisRegistry(url);
    }
}
