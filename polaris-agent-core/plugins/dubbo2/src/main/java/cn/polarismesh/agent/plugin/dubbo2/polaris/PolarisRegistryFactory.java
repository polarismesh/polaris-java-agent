package cn.polarismesh.agent.plugin.dubbo2.polaris;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.registry.ListenerRegistryWrapper;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.RegistryServiceListener;

import java.util.Collections;

public class PolarisRegistryFactory implements RegistryFactory {
    private RegistryFactory registryFactory;

    public PolarisRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }
    @Override
    public Registry getRegistry(URL url) {
        String protocol = url.getProtocol();
        if (null != protocol && protocol.equals("zookeeper")) {
            return new PolarisRegistry(url);
        }
        return new ListenerRegistryWrapper(this.registryFactory.getRegistry(url),
                Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(RegistryServiceListener.class)
                        .getActivateExtension(url, "registry.listeners")));
    }
}
