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

package cn.polarismesh.agent.common.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class SystemPropertyUtils {

	public static Integer getInteger(String key) {
		return getInteger(key, 0);
	}

	public static Integer getInteger(String key, Integer defaultVal) {
		String val = System.getProperty(key);
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public static Boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public static Boolean getBoolean(String key, Boolean defaultVal) {
		String val = System.getProperty(key);
		if ("".equals(val) || val == null) {
			return defaultVal;
		}
		try {
			return Boolean.parseBoolean(val);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public static List<String> getStringList(String key) {
		return getStringList(key, new ArrayList<>());
	}

	public static List<String> getStringList(String key, List<String> defaultVal) {
		String val = System.getProperty(key);
		if (val == null || Objects.equals("", val)) {
			return defaultVal;
		}

		List<String> ret = Arrays.asList(key.split(","));
		return ret;
	}

}
