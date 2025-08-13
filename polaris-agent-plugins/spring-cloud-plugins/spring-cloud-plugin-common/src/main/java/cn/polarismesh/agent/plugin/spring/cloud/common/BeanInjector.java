/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.plugin.spring.cloud.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public interface BeanInjector {

	/**
	 * Spring Cloud Tencent的模块信息
	 * @return 模块信息
	 */
	String getModule();

	/**
	 * 获取需要通过JNI加载的类名称
	 * @return 类型列表
	 */
	default Map<String, List<String>> getClassNameForType() {
		return Collections.emptyMap();
	}

	/**
	 * 在Bootstrap启动过程中进行Bean装载
	 */
	default void onBootstrapStartup(Object configurationParser,
			Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {

	}

	/**
	 * 在应用启动过程中进行Bean装载
	 */
	default void onApplicationStartup(Object configurationParser,
			Constructor<?> configClassCreator, Method processConfigurationClass, BeanDefinitionRegistry registry, Environment environment) {

	}
}
