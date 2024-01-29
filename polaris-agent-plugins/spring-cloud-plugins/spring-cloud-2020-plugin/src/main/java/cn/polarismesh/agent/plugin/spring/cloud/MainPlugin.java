/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.spring.cloud;

import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.spring.cloud.common.ClassNames;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware.ApplicationContextAwareInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware.report.BlockingLoadBalancerClientInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.disable.alibaba.DisableSpringCloudAlibabaInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.discovery.DiscoveryInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.discovery.reactive.ReactiveDiscoveryInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.filter.reactive.ReactiveWebFilterInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.filter.servlet.ServletWebFilterInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.invoker.FeignInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.invoker.RestTemplateInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.router.ServiceInstanceListSupplierBuilderInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.serviceregistry.RegistryInterceptor;
import org.springframework.context.ApplicationContext;

import java.security.ProtectionDomain;

/**
 * Polaris Spring Cloud 2021 Plugin
 *
 * @author zhuyuhan
 */
public class MainPlugin implements AgentPlugin {

	public void init(PluginContext context) {
		System.setProperty(Constant.AGENT_CONF_PATH, context.getAgentDirPath());
		TransformOperations operations = context.getTransformOperations();
		addPolarisTransformers(operations);
	}

	/**
	 * add polaris transformers
	 */
	private void addPolarisTransformers(TransformOperations operations) {

		operations.transform(ClassNames.SERVICE_REGISTRATION, SpringCloudRegistryTransform.class);
		operations.transform(ClassNames.DISCOVERY_CLIENT, SpringCloudDiscoveryTransform.class);
		operations.transform(ClassNames.REACTIVE_DISCOVERY_CLIENT, SpringCloudReactiveDiscoveryTransform.class);

		// 北极星路由执行
		operations.transform(ClassNames.ROUTER, SpringCloudTrafficRouterTransform.class);

		// 流量标签信息收集
		operations.transform(ClassNames.REACTIVE_WEB_FILTER, ReactiveWebFilterTransform.class);
		operations.transform(ClassNames.SERVLET_WEB_FILTER, ServletWebFilterTransform.class);

		// 请求发起时需要收集流量标签信息
		operations.transform(ClassNames.REST_TEMPLATE, RestTemplateTransform.class);
		operations.transform(ClassNames.FEIGN_TEMPLATE, FeignTransform.class);

		// 在 agent 中注入 Spring 的 ApplicationContext
		operations.transform(ClassNames.APPLICATION_CONTEXT_AWARE, ApplicationContextAwareTransform.class);

		// EnvironmentPostProcessor 处理
		operations.transform(ClassNames.ENVIRONMENT_POST_PROCESSOR, DisableSpringCloudAlibabaTransform.class);

		operations.transform(ClassNames.BLOCKING_LOADBALANCER_CLIENT, BlockingLoadbalancerClientTransform.class);
	}


	/**
	 * SpringCloud 注册拦截
	 */
	public static class SpringCloudRegistryTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod registerMethod = target.getDeclaredMethod("start"); if (registerMethod != null) {
				registerMethod.addInterceptor(RegistryInterceptor.class);
			} return target.toBytecode();
		}
	}

	/**
	 * Spring Cloud 服务发现拦截
	 */
	public static class SpringCloudDiscoveryTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getConstructor("java.util.List"); if (constructMethod != null) {
				constructMethod.addInterceptor(DiscoveryInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class SpringCloudReactiveDiscoveryTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getConstructor("java.util.List"); if (constructMethod != null) {
				constructMethod.addInterceptor(ReactiveDiscoveryInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class SpringCloudTrafficRouterTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);

			InstrumentMethod withBlockingDiscoveryClient = target.getDeclaredMethod("withBlockingDiscoveryClient");
			if (withBlockingDiscoveryClient != null) {
				withBlockingDiscoveryClient.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderBlockingInterceptor.class);
			}

			InstrumentMethod withDiscoveryClient = target.getDeclaredMethod("withDiscoveryClient");
			if (withDiscoveryClient != null) {
				withDiscoveryClient.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderReactiveInterceptor.class);
			}

			InstrumentMethod withCaching = target.getDeclaredMethod("withCaching"); if (withCaching != null) {
				withCaching.addInterceptor(ServiceInstanceListSupplierBuilderInterceptor.ServiceInstanceListSupplierBuilderDisableCachingInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class ReactiveWebFilterTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getConstructor("org.springframework.web.server.WebHandler", "java.util.List");
			if (constructMethod != null) {
				constructMethod.addInterceptor(ReactiveWebFilterInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class ServletWebFilterTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod(/* "doDispatch" */ "initStrategies", "org.springframework.context.ApplicationContext");
			if (constructMethod != null) {
				constructMethod.addInterceptor(ServletWebFilterInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class RestTemplateTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("getInterceptors");
			if (constructMethod != null) {
				constructMethod.addInterceptor(RestTemplateInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class FeignTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("getInheritedAwareInstances", "org.springframework.cloud.openfeign.FeignContext", "java.lang.Class");
			if (constructMethod != null) {
				constructMethod.addInterceptor(FeignInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	/**
	 * 注入 spring 的 {@link ApplicationContext}
	 */
	public static class ApplicationContextAwareTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {

			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getConstructor("org.springframework.context.ConfigurableApplicationContext");
			if (constructMethod != null) {
				constructMethod.addInterceptor(ApplicationContextAwareInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class DisableSpringCloudAlibabaTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("onApplicationEnvironmentPreparedEvent", "org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent");
			if (constructMethod != null) {
				constructMethod.addInterceptor(DisableSpringCloudAlibabaInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class BlockingLoadbalancerClientTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("reconstructURI", "org.springframework.cloud"
					+ ".client.ServiceInstance", "java.net.URI");
			if (constructMethod != null) {
				constructMethod.addInterceptor(BlockingLoadBalancerClientInterceptor.class);
			}

			return target.toBytecode();
		}
	}

}