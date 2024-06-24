package cn.polarismesh.agent.core.bootstrap;

public class PolarisInitProperties {

    public void initialize() {

        System.setProperty("spring.cloud.nacos.config.enabled", "false");
        System.setProperty("spring.cloud.nacos.discovery.enabled", "false");

    }
}
