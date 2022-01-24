package cn.polarismesh.agent.core.spring.cloud.context;

import cn.polarismesh.agent.core.spring.cloud.constant.PolarisServiceConstants;
import com.tencent.polaris.api.config.ConfigProvider;
import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.client.api.SDKContext;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;

import java.util.Collections;

/**
 * Polaris Agent上下文
 *
 * @author zhuyuhan
 */
public class PolarisContext {

    private final SDKContext sdkContext;

    private final PolarisAgentProperties polarisAgentProperties;

    public PolarisContext(PolarisAgentProperties polarisAgentProperties) {
        this.polarisAgentProperties = polarisAgentProperties;
        this.sdkContext = SDKContext.initContextByConfig(configuration(polarisAgentProperties));
    }

    /**
     * 初始化配置
     *
     * @param polarisAgentProperties
     * @return
     */
    private static Configuration configuration(PolarisAgentProperties polarisAgentProperties) {
        ConfigurationImpl configuration = (ConfigurationImpl) ConfigAPIFactory
                .defaultConfig(ConfigProvider.DEFAULT_CONFIG);
        configuration.setDefault();
        configuration.getGlobal().getAPI().setBindIP(PolarisServiceConstants.HOST);
        configuration.getGlobal().getServerConnector().setAddresses(Collections.singletonList(polarisAgentProperties.getServerAddress()));
        return configuration;
    }

    public SDKContext getSdkContext() {
        return sdkContext;
    }

    public PolarisAgentProperties getPolarisContextAgentProperties() {
        return polarisAgentProperties;
    }
}
