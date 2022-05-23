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

package cn.polarismesh.agent.plugin.dubbox.interceptor;

import static com.alibaba.dubbo.common.Constants.CONSUMER_PROTOCOL;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.common.tools.ReflectionUtils.FieldCallback;
import cn.polarismesh.agent.common.tools.ReflectionUtils.FieldFilter;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * interceptor for com.alibaba.dubbo.common.URL#URL(java.lang.String, java.lang.String, java.lang.String,
 * java.lang.String, int, java.lang.String, java.util.Map)
 */
public class DubboCreateURLInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    private void setAnyValue(Map<String, String> parameters, String key) {
        if (!parameters.containsKey(key)) {
            parameters.put(key, Constants.ANY_VALUE);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        ReflectionUtils.doWithFields(URL.class, field -> {
            String protocol = (String) args[0];
            if (!protocol.equals(CONSUMER_PROTOCOL)) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            Map<String, String> parameters = (Map<String, String>) ReflectionUtils.getField(field, target);
            if (parameters.containsKey(Constants.VERSION_KEY) && parameters.containsKey(Constants.GROUP_KEY)
                    && parameters.containsKey(Constants.CLASSIFIER_KEY)) {
                return;
            }
            Map<String, String> nParameters = new HashMap<>(parameters);
            setAnyValue(nParameters, Constants.VERSION_KEY);
            setAnyValue(nParameters, Constants.GROUP_KEY);
            setAnyValue(nParameters, Constants.CLASSIFIER_KEY);
            ReflectionUtils.setField(field, target, nParameters);
        }, field -> field.getName().equals("parameters"));
    }
}
