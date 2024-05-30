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

import java.security.ProtectionDomain;

import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.ConfigurationParserInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.ConfigurationPostProcessorInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.RegisterBeanInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.SpringFactoriesLoaderInterceptor;

/**
 * Polaris Spring Cloud hoxton Plugin
 *
 * @author shuhanliu
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

		// 注入默认配置
		operations.transform(Constant.CONFIGURATION_CLAZZ_POST_PROCESSOR, ConfigurationPostProcessorTransform.class);

		// 注入bootstrap的bean定义
		operations.transform(Constant.CONFIGURATION_CLAZZ_PARSER, ConfigurationParserTransform.class);

		// 注入bean定义的调整设置
		operations.transform(Constant.BEAN_DEFINITION_REGISTRY, RegisterBeanDefinitionTransform.class);

		// 注入JNI定义
		operations.transform(Constant.SPRING_FACTORIES_LOADER, SpringFactoriesLoaderTransform.class);
	}

	public static class ConfigurationParserTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("parse", "java.util.Set");
			if (constructMethod != null) {
				constructMethod.addInterceptor(ConfigurationParserInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class ConfigurationPostProcessorTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("processConfigBeanDefinitions", "org.springframework.beans.factory.support.BeanDefinitionRegistry");
			if (constructMethod != null) {
				constructMethod.addInterceptor(ConfigurationPostProcessorInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class RegisterBeanDefinitionTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("registerBeanDefinition", "java.lang.String", "org.springframework.beans.factory.config.BeanDefinition");
			if (constructMethod != null) {
				constructMethod.addInterceptor(RegisterBeanInterceptor.class);
			}

			return target.toBytecode();
		}
	}

	public static class SpringFactoriesLoaderTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("loadSpringFactories", "java.lang.ClassLoader");
			if (constructMethod != null) {
				constructMethod.addInterceptor(SpringFactoriesLoaderInterceptor.class);
			}

			return target.toBytecode();
		}
	}

}