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
import cn.polarismesh.agent.plugin.spring.cloud.common.ClassNames;
import cn.polarismesh.agent.plugin.spring.cloud.common.Constant;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.ApplicationContextAwareInterceptor;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.ConfigurationInjectInterceptor;

import org.springframework.context.ApplicationContext;

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

		// 在 agent 中注入 Spring 的 ApplicationContext
		operations.transform(ClassNames.APPLICATION_CONTEXT_AWARE, ApplicationContextAwareTransform.class);

		// EnvironmentPostProcessor 处理
		// operations.transform(ClassNames.ENVIRONMENT_POST_PROCESSOR, ConfigurationInjectTransform.class);

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

	public static class ConfigurationInjectTransform implements TransformCallback {

		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
			InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
			InstrumentMethod constructMethod = target.getDeclaredMethod("onApplicationEnvironmentPreparedEvent", "org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent");
			if (constructMethod != null) {
				constructMethod.addInterceptor(ConfigurationInjectInterceptor.class);
			}

			return target.toBytecode();
		}
	}

}