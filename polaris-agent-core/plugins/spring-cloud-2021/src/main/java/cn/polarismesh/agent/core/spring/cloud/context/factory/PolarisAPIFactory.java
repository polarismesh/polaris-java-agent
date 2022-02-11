package cn.polarismesh.agent.core.spring.cloud.context.factory;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisContext;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.api.RouterAPIFactory;
import com.tencent.polaris.router.api.core.RouterAPI;

/**
 * Polaris API工厂
 *
 * @author zhuyuhan
 */
public class PolarisAPIFactory {

    private static ConsumerAPI CONSUMER_API;

    private static ProviderAPI PROVIDER_API;

    private static RouterAPI ROUTER_API;

    public static void init(PolarisContext polarisContext) {
        CONSUMER_API = DiscoveryAPIFactory.createConsumerAPIByContext(polarisContext.getSdkContext());
        PROVIDER_API = DiscoveryAPIFactory.createProviderAPIByContext(polarisContext.getSdkContext());
        ROUTER_API = RouterAPIFactory.createRouterAPIByContext(polarisContext.getSdkContext());
    }

    public static ConsumerAPI getConsumerApi() {
        return CONSUMER_API;
    }

    public static ProviderAPI getProviderApi() {
        return PROVIDER_API;
    }

    public static RouterAPI getRouterApi() {
        return ROUTER_API;
    }
}
