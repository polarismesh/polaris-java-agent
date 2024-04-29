package cn.polarismesh.agent.plugin.spring.cloud.discovery;

import cn.polarismesh.agent.plugin.spring.cloud.base.AbstractContextHandler;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.google.common.collect.Maps;
import com.tencent.cloud.plugin.lossless.config.LosslessAutoConfiguration;
import com.tencent.cloud.polaris.PolarisDiscoveryProperties;
import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.discovery.PolarisDiscoveryAutoConfiguration;
import com.tencent.cloud.polaris.discovery.PolarisDiscoveryHandler;
import com.tencent.cloud.polaris.registry.PolarisAutoServiceRegistration;
import com.tencent.cloud.polaris.registry.PolarisRegistration;
import com.tencent.cloud.polaris.registry.PolarisServiceRegistry;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatProperties;
import com.tencent.polaris.assembly.api.AssemblyAPI;
import com.tencent.polaris.client.api.SDKContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

public class DiscoveryHandler extends AbstractContextHandler {


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		String discoveryEnable = applicationContext.getEnvironment().getProperty("spring.cloud.discovery.enabled");
		if (null == discoveryEnable || Boolean.parseBoolean(discoveryEnable)) {
			registerBean(applicationContext, "polarisDiscoveryProperties", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(PolarisDiscoveryProperties.class, new Supplier<PolarisDiscoveryProperties>() {
							@Override
							public PolarisDiscoveryProperties get() {
								return Holder.getDiscoveryProperties();
							}
						}).getBeanDefinition());
			});
			registerBean(applicationContext, "polarisDiscoveryHandler", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(PolarisDiscoveryHandler.class, new Supplier<PolarisDiscoveryHandler>() {
							@Override
							public PolarisDiscoveryHandler get() {
								PolarisDiscoveryProperties polarisDiscoveryProperties = (PolarisDiscoveryProperties) ctx.getBean("polarisDiscoveryProperties");
								PolarisSDKContextManager polarisSDKContextManager = (PolarisSDKContextManager) ctx.getBean("polarisSDKContextManager");
								return new PolarisDiscoveryHandler(polarisDiscoveryProperties, polarisSDKContextManager);
							}
						}).getBeanDefinition());
			});
			registerBean(applicationContext, "polarisStatProperties", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(PolarisStatProperties.class, new Supplier<PolarisStatProperties>() {
							@Override
							public PolarisStatProperties get() {
								return Holder.getPolarisStatProperties();
							}
						}).getBeanDefinition());
			});
			registerBean(applicationContext, "polarisDiscoveryAutoConfiguration", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
						PolarisDiscoveryAutoConfiguration.class).getBeanDefinition());
			});
			registerBean(applicationContext, "sdkContext", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(SDKContext.class, new Supplier<SDKContext>() {
							@Override
							public SDKContext get() {
								return Holder.getContextManager().getSDKContext();
							}
						}).getBeanDefinition());
			});
			registerBean(applicationContext, "assemblyAPI", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(AssemblyAPI.class, new Supplier<AssemblyAPI>() {
							@Override
							public AssemblyAPI get() {
								return Holder.getContextManager().getAssemblyAPI();
							}
						}).getBeanDefinition());
			});
			registerBean(applicationContext, "polarisRegistration", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
						PolarisRegistration.class).setPrimary(true).getBeanDefinition());
			});
			registerBean(applicationContext, "polarisServiceRegistry", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
						PolarisServiceRegistry.class).setPrimary(true).getBeanDefinition());
			});
			registerBean(applicationContext, "autoServiceRegistrationProperties", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(AutoServiceRegistrationProperties.class, new Supplier<AutoServiceRegistrationProperties>() {
							@Override
							public AutoServiceRegistrationProperties get() {
								return new AutoServiceRegistrationProperties();
							}
						}).setPrimary(true).getBeanDefinition());
			});
			System.out.println("11111112321312341231231231231231231231312312312312313121312312");
			registerBean(applicationContext, "polarisAutoServiceRegistration", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
						PolarisAutoServiceRegistration.class).setFactoryMethodOnBean("org.springframework.beans.factory.support.DefaultListableBeanFactory","").setPrimary(true).getBeanDefinition());
			});
//			@Component
//			class TimeCostBeanPostProcessor implements BeanPostProcessor {
//
//				Map<String, Long> costMap = Maps.newConcurrentMap();
//
//				@Autowired
//				private ApplicationContext applicationContext;
//
//				@Override
//				public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//					costMap.put(beanName, System.currentTimeMillis());
//					return bean;
//				}
//
//				@Override
//				public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//					if ("autoServiceRegistrationProperties".equals(beanName)) {
//						// 在polarisAutoServiceRegistration初始化之后注册autoServiceRegistrationProperties
//						ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) applicationContext;
//						DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
//						beanFactory.registerBeanDefinition("polarisAutoServiceRegistration",
//								BeanDefinitionBuilder.genericBeanDefinition(PolarisAutoServiceRegistration.class)
//										.getBeanDefinition());
//					}
//					Long start = costMap.get(beanName);
//					long cost = System.currentTimeMillis() - start;
//					costMap.put(beanName, cost);
////				System.out.println("class: " + bean.getClass().getName()
////						+ "\tbean: "+ beanName
////						+ "\ttime: "+ cost);
//
//					return bean;
//				}
//			}
//			registerBean(applicationContext, "polarisSDKContextManager", (ctx, name) -> {
//				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
//				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
//				beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
//						PolarisSDKContextManager.class).setPrimary(true).getBeanDefinition());
//			});
			registerBean(applicationContext, "losslessAutoConfiguration", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
						LosslessAutoConfiguration.class).setPrimary(true).getBeanDefinition());
			});

		}
	}
}
