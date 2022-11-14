package cn.polarismesh.agent.plugin.spring.cloud.common;


import java.util.concurrent.atomic.AtomicBoolean;

import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.client.api.SDKContext;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.api.RouterAPIFactory;
import com.tencent.polaris.ratelimit.api.core.LimitAPI;
import com.tencent.polaris.ratelimit.factory.LimitAPIFactory;
import com.tencent.polaris.router.api.core.RouterAPI;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class PolarisOperator {


	private static PolarisOperator INSTANCE;

	public static PolarisOperator getInstance() {
		return INSTANCE;
	}

	public static void init(Configuration configuration) {
		INSTANCE = new PolarisOperator(configuration);
	}

	private final Object lock = new Object();

	private final AtomicBoolean inited = new AtomicBoolean(false);

	private SDKContext sdkContext;

	private ConsumerAPI consumerAPI;

	private ProviderAPI providerAPI;

	private LimitAPI limitAPI;

	private RouterAPI routerAPI;

	private Configuration configuration;

	public PolarisOperator(Configuration configuration) {
		this.configuration = configuration;
	}

	private void init() {
		if (inited.get()) {
			return;
		}
		synchronized (lock) {
			if (inited.get()) {
				return;
			}
			sdkContext = SDKContext.initContextByConfig(configuration);
			consumerAPI = DiscoveryAPIFactory.createConsumerAPIByContext(sdkContext);
			providerAPI = DiscoveryAPIFactory.createProviderAPIByContext(sdkContext);
			limitAPI = LimitAPIFactory.createLimitAPIByContext(sdkContext);
			routerAPI = RouterAPIFactory.createRouterAPIByContext(sdkContext);
			inited.set(true);
		}
	}

	public SDKContext getSdkContext() {
		init();
		return sdkContext;
	}

	public ConsumerAPI getConsumerAPI() {
		init();
		return consumerAPI;
	}

	public ProviderAPI getProviderAPI() {
		init();
		return providerAPI;
	}

	public LimitAPI getLimitAPI() {
		init();
		return limitAPI;
	}

	public RouterAPI getRouterAPI() {
		init();
		return routerAPI;
	}

	public void destroy() {
		synchronized (lock) {
			if (!inited.get()) {
				return;
			}
			sdkContext.close();
			inited.set(false);
		}
	}

}
