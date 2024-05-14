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

package cn.polarismesh.agent.plugin.spring.cloud.common;

import org.springframework.core.env.Environment;

public class Utils {

	public static boolean checkKeyExists(Environment environment, String key) {
		if (null == environment) {
			return false;
		}
		String property = environment.getProperty(key);
		return null != property && !property.isEmpty();
	}

	public static boolean checkKeyEnabled(Environment environment, String key) {
		if (null == environment) {
			return false;
		}
		String property = environment.getProperty(key);
		return Boolean.parseBoolean(property);
	}

	/**
	 * check properties has spring.application.name
	 * @param environment spring cloud context environment
	 * @return application name
	 */
	public static boolean checkSpringApplicationNameExists(Environment environment) {
		return checkKeyExists(environment, "spring.application.name");
	}



	/**
	 * check properties has spring.cloud.polaris.enabled=true
	 * @param environment spring cloud context environment
	 * @return application name
	 */
	public static boolean checkPolarisEnabled(Environment environment) {
		return checkKeyEnabled(environment, "spring.cloud.polaris.enabled");
	}

}
