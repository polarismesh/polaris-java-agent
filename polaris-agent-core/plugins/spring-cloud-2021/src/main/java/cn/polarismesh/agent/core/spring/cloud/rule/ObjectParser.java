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

package cn.polarismesh.agent.core.spring.cloud.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ObjectParser {

	private static final Pattern ARRAY_PATTERN = Pattern.compile("^.+\\[[0-9]+\\]");

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectParser.class);

	public static Object resolveValue(String path, Object value) {
		String fieldName = path;
		int index = -1;
		Matcher matcher = ARRAY_PATTERN.matcher(path);
		if (matcher.matches()) {
			//array
			fieldName = path.substring(0, path.indexOf('['));
			String indexStr = path.substring(path.indexOf('[') + 1, path.lastIndexOf(']'));
			index = Integer.parseInt(indexStr);
		}
		Object objectByFieldName = null;
		if (value.getClass().isAssignableFrom(Map.class)) {
			Map<?, ?> mapValues = (Map<?, ?>) value;
			objectByFieldName = mapValues.get(fieldName);
		} else {
			try {
				objectByFieldName = ReflectionUtils.getObjectByFieldName(value, fieldName);
			} catch (Exception e) {
				LOGGER.error("[POLARIS] fail to resolve field {} by class {}", fieldName,
						value.getClass().getCanonicalName(), e);
			}
		}
		if (index < 0 || objectByFieldName == null) {
			return objectByFieldName;
		}
		Class<?> targetClazz = objectByFieldName.getClass();
		if (targetClazz.isArray()) {
			Class<?> componentType = targetClazz.getComponentType();
			if (!componentType.isPrimitive()) {
				Object[] values = (Object[]) objectByFieldName;
				if (values.length > index) {
					return values[index];
				}
				return null;
			} else {
				return processPrimitiveArray(componentType, objectByFieldName, index);
			}
		} else if (targetClazz.isAssignableFrom(List.class)) {
			List<?> listValues = (List<?>) objectByFieldName;
			if (listValues.size() > index) {
				return listValues.get(index);
			}
			return null;
		} else if (targetClazz.isAssignableFrom(Collection.class)) {
			Collection<?> collectionValues = (Collection<?>) objectByFieldName;
			if (collectionValues.size() > index) {
				Iterator<?> iterator = collectionValues.iterator();
				Object nextValue = null;
				for (int i = 0; i < index; i++) {
					nextValue = iterator.next();
				}
				return nextValue;
			}
		}
		return null;
	}

	private static Object processPrimitiveArray(Class<?> componentType, Object object, int index) {
		if (componentType == int.class) {
			int[] values = (int[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == long.class) {
			long[] values = (long[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == double.class) {
			double[] values = (double[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == short.class) {
			short[] values = (short[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == float.class) {
			float[] values = (float[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == boolean.class) {
			boolean[] values = (boolean[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == byte.class) {
			byte[] values = (byte[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		} else if (componentType == char.class) {
			char[] values = (char[]) object;
			if (values.length > index) {
				return values[index];
			}
			return null;
		}
		return null;
	}

}
