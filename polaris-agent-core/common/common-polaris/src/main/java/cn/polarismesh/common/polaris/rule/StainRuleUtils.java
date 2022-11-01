/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.common.polaris.rule;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.client.pb.ModelProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class StainRuleUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StainRuleUtils.class);

	private static final String EXPRESSION_PREFIX = "$.param";

	public static boolean matchHeaders(ModelProto.MatchArgument matcher, Map<String, String> headers) {
		String key = matcher.getKey();
		if (StringUtils.isBlank(key)) {
			return false;
		}
		if (!headers.containsKey(key)) {
			return false;
		}
		String value = headers.get(key);
		return matchString(matcher, value);
	}

	public static boolean matchParameters(ModelProto.MatchArgument matcher, Object[] parameters) {
		String key = matcher.getKey();
		if (StringUtils.isBlank(key)) {
			return false;
		}
		if (parameters.length == 0) {
			return false;
		}
		if (!key.startsWith(EXPRESSION_PREFIX)) {
			return false;
		}
		String restKey = key.substring(EXPRESSION_PREFIX.length());
		int index = 0;
		if (restKey.startsWith("[")) {
			int endInx = restKey.indexOf(']');
			String indexValue = restKey.substring(1, endInx);
			index = Integer.parseInt(indexValue);
			restKey = restKey.substring(endInx + 1);
		}
		if (parameters.length < index) {
			return false;
		}
		// omit the starting dot
		restKey = restKey.substring(1);
		Object targetValue = parameters[index];
		if (!StringUtils.isBlank(restKey)) {
			String[] tokens = restKey.split("\\.");
			for (String token : tokens) {
				if (null == targetValue) {
					break;
				}
				targetValue = ObjectParser.resolveValue(token, targetValue);
			}
		}
		if (null == targetValue) {
			return false;
		}
		return matchString(matcher, String.valueOf(targetValue));
	}

	public static boolean matchString(ModelProto.MatchArgument matcher, String value) {
		ModelProto.Operation operation = matcher.getOperation();
		List<String> values = matcher.getValuesList();
		if (values.isEmpty()) {
			return true;
		}
		switch (operation) {
		case EXACT: {
			return StringUtils.equals(values.get(0), value);
		}
		case NOT_EQUALS: {
			return !StringUtils.equals(values.get(0), value);
		}
		case IN: {
			for (String matchValue : values) {
				if (StringUtils.equals(matchValue, value)) {
					return true;
				}
			}
			return false;
		}
		case NOT_IN: {
			for (String matchValue : values) {
				if (StringUtils.equals(matchValue, value)) {
					return false;
				}
			}
			return true;
		}
		case REGEX: {
			return matchRegex(values.get(0), value);
		}
		default: {
			return false;
		}
		}
	}

	private static boolean matchRegex(String regex, String value) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(value);
			return matcher.matches();
		} catch (Exception e) {
			LOGGER.error("[POLARIS] fail to verify regex {}", regex, e);
			return false;
		}
	}
}