package cn.polarismesh.agent.plugin.dubbox.polaris;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;

public class PolarisRegistryFactory implements RegistryFactory {
    @Override
    public Registry getRegistry(URL url) {
        return new PolarisRegistry(url);
    }
}
