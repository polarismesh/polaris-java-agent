package cn.polarismesh.agent.plugin.dubbox.polaris;

import cn.polarismesh.common.polaris.PolarisConfig;
import cn.polarismesh.common.polaris.PolarisOperator;

public class PolarisSingleton {

    private static final PolarisConfig polarisConfig = new PolarisConfig();

    private static final PolarisOperator polarisWatcher = new PolarisOperator(getPolarisConfig());

    public static PolarisOperator getPolarisWatcher() {
        return polarisWatcher;
    }

    public static PolarisConfig getPolarisConfig() {
        return polarisConfig;
    }


}
