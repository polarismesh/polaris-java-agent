package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import cn.polarismesh.agent.plugin.spring.cloud.config.ConfigHandler;
import cn.polarismesh.agent.plugin.spring.cloud.metadata.MetadataHandler;
import cn.polarismesh.agent.plugin.spring.cloud.router.RouterHandler;
import cn.polarismesh.agent.plugin.spring.cloud.rpc.RpcEnhancementHandler;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;

public class EnvironmentChangeEventListener implements SmartApplicationListener {

	private final RouterHandler routerHandler = new RouterHandler();

	private final MetadataHandler metadataHandler = new MetadataHandler();

	private final RpcEnhancementHandler rpcEnhancementHandler = new RpcEnhancementHandler();

	private final ConfigHandler configHandler = new ConfigHandler();

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == EnvironmentChangeEvent.class || eventType == ContextRefreshedEvent.class;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		Object source = event.getSource();
		if (!(source instanceof ApplicationContext)) {
			return;
		}
		ApplicationContext applicationContext = (ApplicationContext) source;
		ConfigurableApplicationContext cfgCtx1 = (ConfigurableApplicationContext) applicationContext;
		String enable = cfgCtx1.getEnvironment().getProperty("spring.cloud.polaris.enabled");
		System.out.println("enable is " + enable);
		if (null == enable || Boolean.parseBoolean(enable)) {
			configHandler.setApplicationContext(applicationContext);
			routerHandler.setApplicationContext(applicationContext);
			metadataHandler.setApplicationContext(applicationContext);
			rpcEnhancementHandler.setApplicationContext(applicationContext);
		}
	}

}
