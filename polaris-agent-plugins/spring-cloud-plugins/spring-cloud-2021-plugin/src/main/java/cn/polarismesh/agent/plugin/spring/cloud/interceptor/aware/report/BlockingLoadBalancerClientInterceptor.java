package cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware.report;

import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.common.metadata.MetadataContextHolder;

import org.springframework.cloud.client.ServiceInstance;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class BlockingLoadBalancerClientInterceptor extends BaseInterceptor {

	@Override
	public void onBefore(Object target, Object[] args) {
		Object server = args[0];
		if (server instanceof ServiceInstance) {
			ServiceInstance instance = (ServiceInstance) server;
			MetadataContextHolder.get().setLoadbalancer("host", instance.getHost());
			MetadataContextHolder.get().setLoadbalancer("port", String.valueOf(instance.getPort()));
		}
	}

}
