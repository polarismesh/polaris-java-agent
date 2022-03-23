package cn.polarismesh.agent.plugin.dubbo2.polaris;

import cn.polarismesh.common.polaris.PolarisConfig;
import cn.polarismesh.common.polaris.PolarisWatcher;

public class PolarisSingleton {

    private static final PolarisConfig polarisConfig = new PolarisConfig();

    private static final PolarisWatcher polarisWatcher = new PolarisWatcher(getPolarisConfig());

    public static PolarisWatcher getPolarisWatcher() {
        return polarisWatcher;
    }

    public static PolarisConfig getPolarisConfig() {
        return polarisConfig;
    }

}
