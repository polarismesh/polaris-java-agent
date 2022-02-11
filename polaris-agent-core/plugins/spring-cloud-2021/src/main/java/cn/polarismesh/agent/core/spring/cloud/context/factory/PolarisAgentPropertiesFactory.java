package cn.polarismesh.agent.core.spring.cloud.context.factory;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;

/**
 * Polaris服务属性工厂
 *
 * @author zhuyuhan
 */
public class PolarisAgentPropertiesFactory {

    private static PolarisAgentProperties polarisAgentProperties;

    public static PolarisAgentProperties getPolarisAgentProperties() {
        return polarisAgentProperties;
    }

    public static void setPolarisAgentProperties(PolarisAgentProperties polarisAgentProperties) {
        PolarisAgentPropertiesFactory.polarisAgentProperties = polarisAgentProperties;
    }
}
